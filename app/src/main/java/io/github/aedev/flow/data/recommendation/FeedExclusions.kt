package io.github.aedev.flow.data.recommendation

import io.github.aedev.flow.data.contentfilter.aislist.AiListMatcher
import io.github.aedev.flow.data.contentfilter.family.FamilyChannelMatcher
import io.github.aedev.flow.data.contentfilter.family.FamilySearchRule
import io.github.aedev.flow.data.contentfilter.family.FamilyTextMatcher
import io.github.aedev.flow.data.model.Channel
import io.github.aedev.flow.data.model.Video

/**
 * What the viewer asked not to see, read from the brain in one snapshot for the surfaces that never
 * pass through [FlowNeuroEngine.rank]: Subscriptions, the subscription Shorts queue, search, the
 * player's related list and autoplay, and the Home feed restored from cache.
 *
 * Two strengths, because not every surface should hide the same things (owner decision D5):
 * [hides] is what the viewer said outright, a video marked not interested or a blocked channel, and
 * applies everywhere, Subscriptions included. [hidesFromRecommendations] adds what the engine
 * inferred, the channels a not-interested mark suppresses for a while and blocked topics, and applies
 * only where the app chose the video rather than the viewer.
 *
 * The AI channel list ([ai]) joins [hidesFromRecommendations], so it never reaches Subscriptions.
 * Search, which uses [hides], asks [hidesAsAi] on its own.
 *
 * The family whitelist ([family]) joins [hides]: a video whose channel is not approved is hidden
 * everywhere, Subscriptions included. The family text filter ([familyText]) joins [hidesText] at
 * the hard tier and [hidesFromRecommendations] at the entertainment tier for non-approved
 * channels — approved channels are exempt from the entertainment tier, because their own titles
 * say "Song" and "Music".
 *
 * Open search uses a different rule ([familySearch]): the whitelist still admits its own
 * channels, but a non-whitelisted video is kept when it is about an approved topic and passes
 * the blocklist ([hidesFromSearch]). [hides] itself stays whitelist-only, so ranked surfaces
 * are unchanged.
 */
class FeedExclusions(
    val suppressedVideoIds: Set<String> = emptySet(),
    val blockedChannelIds: Set<String> = emptySet(),
    val suppressedChannelIds: Set<String> = emptySet(),
    private val blockedText: (title: String, channelName: String) -> Boolean = { _, _ -> false },
    val ai: AiListMatcher = AiListMatcher.NONE,
    val family: FamilyChannelMatcher = FamilyChannelMatcher.ALLOW_ALL,
    val familyText: FamilyTextMatcher = FamilyTextMatcher.EMPTY,
    val familySearch: FamilySearchRule? = null,
) {
    val isEmpty: Boolean
        get() =
            suppressedVideoIds.isEmpty() && blockedChannelIds.isEmpty() && suppressedChannelIds.isEmpty() &&
                !ai.isActive && !family.isRestrictive && familyText.isEmpty && familySearch == null

    fun hides(video: Video): Boolean =
        video.id in suppressedVideoIds || hidesChannel(video.channelId) ||
            !family.allows(video.channelId, video.channelHandle)

    fun hidesChannel(channelId: String): Boolean = channelId.isNotBlank() && channelId in blockedChannelIds

    fun hidesAsAi(video: Video): Boolean = ai.blocks(video)

    /** The blocked-words check alone, for cards that carry a name and a byline but no channel. */
    fun hidesText(
        title: String,
        byline: String,
    ): Boolean = blockedText(title, byline) || familyText.blocksHard("$title\n$byline")

    /**
     * What the viewer hid outright, without the family whitelist — the user-hide part of
     * [hides], for search, where the family rule is [hidesFromSearch] instead.
     */
    fun hidesUserBlocked(video: Video): Boolean = video.id in suppressedVideoIds || hidesChannel(video.channelId)

    /**
     * Whether open search hides this video: the viewer's own hides, then the family search
     * rule when one is set. Without a family search rule this is just [hidesUserBlocked].
     */
    fun hidesFromSearch(
        video: Video,
        queryMatchesTopic: Boolean = false,
    ): Boolean {
        if (hidesUserBlocked(video)) return true
        val rule = familySearch ?: return false
        return !rule.keepsVideo(video, queryMatchesTopic)
    }

    /** Whether open search hides this channel card. */
    fun hidesChannelFromSearch(channel: Channel): Boolean {
        if (hidesChannel(channel.id)) return true
        val rule = familySearch ?: return false
        return !rule.keepsChannel(channel)
    }

    fun hidesFromRecommendations(video: Video): Boolean =
        hides(video) ||
            (video.channelId.isNotBlank() && video.channelId in suppressedChannelIds) ||
            blockedText(video.title, video.channelName) ||
            familyText.blocks(
                video.title,
                video.description,
                video.channelId,
                video.channelHandle,
                family,
            ) ||
            ai.blocks(video)

    /** The same exclusions, also hiding every video from [channelIds]. */
    fun hidingChannels(channelIds: Set<String>): FeedExclusions =
        if (channelIds.isEmpty()) {
            this
        } else {
            FeedExclusions(
                suppressedVideoIds,
                blockedChannelIds + channelIds,
                suppressedChannelIds,
                blockedText,
                ai,
                family,
                familyText,
                familySearch,
            )
        }

    companion object {
        val NONE = FeedExclusions()
    }
}

/** Records a "not interested" mark. Marks never expire; past [max] the oldest are dropped. */
internal fun suppressVideo(
    suppressed: Map<String, Long>,
    videoId: String,
    now: Long,
    max: Int = FlowNeuroEngine.MAX_SUPPRESSED_VIDEOS,
): Map<String, Long> {
    val updated = suppressed + (videoId to now)
    if (updated.size <= max) return updated
    return updated.entries
        .sortedByDescending { it.value }
        .take(max)
        .associate { it.key to it.value }
}

/** A fresh brain that still carries every block and "not interested" mark of this one. */
internal fun UserBrain.keepingHiddenContent(): UserBrain =
    UserBrain(
        blockedChannels = blockedChannels,
        blockedTopics = blockedTopics,
        suppressedVideoIds = suppressedVideoIds,
        suppressedChannels = suppressedChannels,
    )

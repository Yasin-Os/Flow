package io.github.aedev.flow.data.contentfilter.family

import io.github.aedev.flow.data.model.Channel
import io.github.aedev.flow.data.model.Video

/**
 * The open-search rule: which search results survive in family mode. Home stays
 * whitelist-only; search is open to every channel whose videos are about an approved topic
 * (see [FamilyTopicMatcher]) and pass the blocklist.
 *
 * A whitelisted channel's videos pass the topic gate outright, but the blocklist still applies
 * to them — [FamilyTextMatcher.blocks] checks the hard and vice tiers before the whitelist
 * early-return, and the entertainment tier for whitelisted news channels. A non-whitelisted
 * video needs a matching topic (or a query that matches a topic, which relaxes the result-side
 * topic gate to blocklist-only) and must pass the blocklist with the drama carve-out when its
 * topic is halal drama, plus the category gate. Whitelisted channels are exempt from the
 * category gate: their uploads are routinely categorized Music.
 */
data class FamilySearchRule(
    val channels: FamilyChannelMatcher,
    val text: FamilyTextMatcher,
    val topics: FamilyTopicMatcher = FamilyTopicMatcher.DEFAULT,
) {
    /** True when the query itself matches an approved topic. */
    fun matchesTopic(query: String): Boolean = topics.matchesTopic(query)

    fun keepsVideo(
        video: Video,
        queryMatchesTopic: Boolean = false,
    ): Boolean {
        if (channels.allows(video.channelId, video.channelHandle)) {
            return !text.blocks(
                video.title,
                video.description,
                video.channelId,
                video.channelHandle,
                channels,
                video.channelName,
            )
        }
        val topic =
            topics.matchTopic(
                "${video.title}\n${video.description}\n${video.channelName}\n${video.snippet}",
            )
        if (topic == null && !queryMatchesTopic) return false
        if (
            text.blocks(
                video.title,
                video.description,
                video.channelId,
                video.channelHandle,
                channels,
                video.channelName,
                exemptDrama = topic == FamilyTopic.HALAL_DRAMA,
            )
        ) {
            return false
        }
        return !FamilyCategoryBlock.isBlocked(video.category)
    }

    fun keepsChannel(channel: Channel): Boolean {
        if (channels.allows(channel.id, channel.handle)) return true
        if (text.isBlockedChannelName(channel.name)) return false
        return topics.matchesTopic("${channel.name}\n${channel.handle}\n${channel.description}")
    }
}

package io.github.aedev.flow.data.contentfilter.family

import io.github.aedev.flow.data.model.Video

/**
 * The safe suggestions system: every suggestion comes from the approved channels' uploads
 * already cached in the subscription store ([FamilyWhitelistRepository] mirrors the whitelist
 * into subscriptions, so the store IS the whitelist's uploads). No new network calls, no
 * YouTube recommendation pool, no neighbour or discovery input — the only input is the cached
 * feed the caller passes in.
 *
 * Two layers, in order:
 * 1. **Whitelist sourcing** — a video is kept only when its channel is approved
 *    ([FamilyChannelMatcher.allows]). A collaboration uploaded by a channel that is not
 *    approved is not an approved channel's upload, so it is dropped even when it features
 *    an approved channel.
 * 2. **The two-sided filter** — [FamilySearchRule.keepsVideo]: the blocklist
 *    ([FamilyTextMatcher.blocks], hard and vice tiers for every channel, entertainment tier
 *    for whitelisted news channels) and the topic allowlist ([FamilyTopicMatcher], which a
 *    whitelisted channel passes outright). Reusing the rule keeps suggestions identical to
 *    what search and the player already admit.
 *
 * Ranking is category-weighted, then Bangladeshi-first, then recency: waz, tilawat and
 * ghazal/nasheed first, then the other approved categories (cartoons, comedy, drama, tour,
 * ummah, and any channel whose category has not been stored yet), then news last — and inside each
 * tier, Bangladeshi videos before foreign ones, newest first within each step. So a
 * Bangladeshi news video still ranks after a foreign waz video: Bangladeshi-first applies
 * inside a tier, it never lifts a lower tier above a higher one. [suggestRelated]
 * additionally puts uploads from the playing video's own category first, so a waz suggests
 * more waz.
 *
 * Cost: one pass over the already-loaded cached rows, on the caller's background dispatcher,
 * capped outputs ([HOME_SUGGESTIONS_MAX], [RELATED_SUGGESTIONS_MAX]). Nothing here holds
 * the feed in memory beyond the returned list, and nothing touches the main thread.
 *
 * The related/up-next surface is NOT wired to this yet: the player's autoplay queue is fed
 * independently by `EnhancedPlayerManager.loadAutoplayCandidates` from YouTube's own related
 * list, so publishing a clean lane alone cannot keep autoplay clean. Until that path is
 * gated to the same source, related stays off and [suggestRelated] waits, tested, for it.
 */
object FamilySuggestionRanker {
    /** The most videos the home suggestions lane ever holds. */
    const val HOME_SUGGESTIONS_MAX = 20

    /** The most videos a related / up-next lane ever holds. */
    const val RELATED_SUGGESTIONS_MAX = 10

    /**
     * Ranks an already-built home pool (watched- and exclusions-filtered by the caller, as
     * [io.github.aedev.flow.ui.screens.home.buildHomeFeedLanes] does): keeps only what the
     * two layers admit, then orders category-first, Bangladeshi-first inside each tier,
     * newest-second. [limit] bounds the returned list; the lane caps downstream still apply.
     *
     * Windowed scan: the pool is sorted first — the sort keys are cheap matcher lookups
     * plus the timestamp — and the expensive layer-2 check ([FamilySearchRule.keepsVideo],
     * giant-regex scans) then runs lazily in ranked order, stopping as soon as [limit]
     * videos pass. The output is identical to filtering the whole pool first: the keep
     * predicate is a pure per-video function and the sort is stable, so the first [limit]
     * keepers of a sorted pool are exactly the filter-then-sort-then-take list, ties
     * included. In the common case only ~[limit] plus a few skipped videos are
     * regex-scanned instead of the whole ≤1,500-row pool — and [rank] runs twice per
     * load (the main lane and the relevance lane), so the saving lands twice.
     *
     * When the matcher has not loaded yet ([FamilyChannelMatcher.channelIds] empty) the
     * layers cannot tell an approved channel from any other, so nothing is filtered — the
     * pool is already the subscription mirror, and the order alone still applies. Filtering
     * against an empty matcher would empty the feed on a startup race.
     */
    fun rank(
        pool: List<Video>,
        channels: FamilyChannelMatcher,
        text: FamilyTextMatcher,
        limit: Int = HOME_SUGGESTIONS_MAX,
    ): List<Video> {
        if (pool.isEmpty()) return emptyList()
        val sorted =
            pool.sortedWith(
                compareBy(
                    { tier(channels.categoryOf(it.channelId, it.channelHandle)) },
                    { bangladeshiRank(channels, it) },
                    { -it.timestamp },
                ),
            )
        if (channels.channelIds.isEmpty()) return sorted.take(limit)
        val rule = FamilySearchRule(channels, text)
        return sorted
            .asSequence()
            .filter { video ->
                video.id.isNotBlank() &&
                    channels.allows(video.channelId, video.channelHandle) &&
                    rule.keepsVideo(video)
            }
            .take(limit)
            .toList()
    }

    /**
     * The home suggestions lane from the cached subscription feed: the same two layers as
     * [rank], plus the watched / Shorts / upcoming / exclusions filtering the raw cache
     * still needs. Shorts stay on their own shelf; upcoming rows are not suggestions.
     */
    fun suggestHome(
        cachedFeed: List<Video>,
        channels: FamilyChannelMatcher,
        text: FamilyTextMatcher,
        watchedVideoIds: Set<String>,
        hidesFromRecommendations: (Video) -> Boolean,
        limit: Int = HOME_SUGGESTIONS_MAX,
    ): List<Video> {
        if (cachedFeed.isEmpty() || channels.channelIds.isEmpty()) return emptyList()
        val rule = FamilySearchRule(channels, text)
        return cachedFeed
            .filter { video ->
                video.id.isNotBlank() && video.title.isNotBlank() &&
                    video.id !in watchedVideoIds &&
                    !video.isShort && !video.isUpcoming &&
                    !hidesFromRecommendations(video) &&
                    channels.allows(video.channelId, video.channelHandle) &&
                    rule.keepsVideo(video)
            }.distinctBy { it.id }
            .sortedWith(
                compareBy(
                    { tier(channels.categoryOf(it.channelId, it.channelHandle)) },
                    { bangladeshiRank(channels, it) },
                    { -it.timestamp },
                ),
            )
            .take(limit)
    }

    /**
     * Related / up-next suggestions for [playing], from the same cached feed and layers as
     * [suggestHome]. The playing video itself never suggests itself; uploads from its own
     * category come first, then the usual category order, Bangladeshi first inside each
     * tier, newest first within each step. Not wired to the player yet — see the file
     * header for why.
     */
    fun suggestRelated(
        playing: Video,
        cachedFeed: List<Video>,
        channels: FamilyChannelMatcher,
        text: FamilyTextMatcher,
        watchedVideoIds: Set<String>,
        hidesFromRecommendations: (Video) -> Boolean,
        limit: Int = RELATED_SUGGESTIONS_MAX,
    ): List<Video> {
        if (cachedFeed.isEmpty() || channels.channelIds.isEmpty()) return emptyList()
        val rule = FamilySearchRule(channels, text)
        val playingCategory = channels.categoryOf(playing.channelId, playing.channelHandle)
        return cachedFeed
            .filter { video ->
                video.id.isNotBlank() && video.title.isNotBlank() &&
                    video.id != playing.id &&
                    video.id !in watchedVideoIds &&
                    !video.isShort && !video.isUpcoming &&
                    !hidesFromRecommendations(video) &&
                    channels.allows(video.channelId, video.channelHandle) &&
                    rule.keepsVideo(video)
            }.distinctBy { it.id }
            .sortedWith(
                compareBy(
                    {
                        if (playingCategory.isNotBlank() &&
                            channels.categoryOf(it.channelId, it.channelHandle) == playingCategory
                        ) {
                            0
                        } else {
                            1
                        }
                    },
                    { tier(channels.categoryOf(it.channelId, it.channelHandle)) },
                    { bangladeshiRank(channels, it) },
                    { -it.timestamp },
                ),
            ).take(limit)
    }

    /**
     * 0 — a Bangladeshi video; 1 — a foreign video. A channel without the marker yet
     * (stored before Round 6, or a channel the PIN holder added themselves) ranks as
     * foreign, so the order is unchanged until the seed carries the marker.
     */
    private fun bangladeshiRank(
        channels: FamilyChannelMatcher,
        video: Video,
    ): Int = if (channels.isBangladeshi(video.channelId, video.channelHandle)) 0 else 1

    /**
     * 0 — waz, tilawat, ghazal: the categories the user asked to see more and more of.
     * 1 — every other approved category, and channels whose category has not been stored
     * yet (an uncategorized approved channel is still approved, never demoted to news).
     * Tour sits here: light general-interest travel, below the religious core and above
     * news, alongside cartoons, comedy and drama.
     * 2 — news, last: approved for news, but it must not crowd out waz, tilawat and ghazal.
     */
    private fun tier(category: String): Int =
        when (category) {
            FamilyChannelMatcher.CATEGORY_WAZ,
            FamilyChannelMatcher.CATEGORY_TILAWAT,
            FamilyChannelMatcher.CATEGORY_GHAZAL,
            -> 0

            FamilyChannelMatcher.CATEGORY_TOUR -> 1

            FamilyChannelMatcher.CATEGORY_NEWS -> 2

            else -> 1
        }
}

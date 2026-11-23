package io.github.aedev.flow.data.contentfilter.family

import io.github.aedev.flow.data.recommendation.NeuroCategories

/**
 * YouTube categories that never play in the family app — Music, Entertainment, Film & Animation —
 * matched by [NeuroCategories.idOf] on the category NAME (`Video.category` is a name like
 * "Music"; no numeric category id exists anywhere in the app).
 *
 * Coverage: the watch/player path only, where the category already arrives with the player
 * response or the secondary metadata fetch ([io.github.aedev.flow.ui.screens.player.SecondaryMetadataApplier]).
 * Feeds never call [io.github.aedev.flow.data.repository.YouTubeRepository.videoCategory] per
 * video — one extra request per card would break the one-fetch-per-cause rule — so feeds are
 * covered by the whitelist plus the title filter instead. A whitelisted channel is EXEMPT:
 * its uploads are routinely categorized Music, and blocking them would break the app's core
 * use case.
 */
object FamilyCategoryBlock {
    private val BLOCKED_IDS = setOf("music", "entertainment", "film")

    /** True when [category] names a blocked category. Null or unknown names never block. */
    fun isBlocked(category: String?): Boolean = NeuroCategories.idOf(category) in BLOCKED_IDS
}

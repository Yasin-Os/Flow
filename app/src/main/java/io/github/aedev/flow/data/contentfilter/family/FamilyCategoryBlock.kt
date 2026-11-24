package io.github.aedev.flow.data.contentfilter.family

import io.github.aedev.flow.data.recommendation.NeuroCategories
import java.util.Locale

/**
 * YouTube categories that never play in the family app, from Gemini's blocked-category sets
 * (BlocklistMeta.kt): STRICT (Music, Film & Animation, Movies, Short Movies, Drama, Shows,
 * Trailers, Anime/Animation, Action/Adventure, Horror, Sci-Fi/Fantasy, Thriller, Classics)
 * plus MODERATE (Entertainment, Gaming) — matched by lowercased display name, because
 * [NeuroCategories.idOf] only maps 15 names and lacks Movies/Drama/Shows/Trailers, so the
 * id check alone cannot cover them. The existing id check is kept too.
 *
 * "Comedy" is deliberately NOT blocked even though Gemini lists it under MODERATE: halal
 * comedy is an approved topic and YouTube categorizes it Comedy. The text/topic filter gates
 * comedy instead; a non-whitelisted haram comedy can only be reached by direct link, and the
 * player text filter still applies there.
 *
 * Coverage: the watch/player path, where the category already arrives with the player response
 * or the secondary metadata fetch
 * ([io.github.aedev.flow.ui.screens.player.SecondaryMetadataApplier]), and open search, where
 * [FamilySearchRule.keepsVideo] applies it when the list response carries a category — search
 * list responses usually do not, so title/channel filtering is the main gate there and the
 * category check only fires when a category is present. Feeds never call
 * [io.github.aedev.flow.data.repository.YouTubeRepository.videoCategory] per video — one extra
 * request per card would break the one-fetch-per-cause rule — so feeds are covered by the
 * whitelist plus the title filter instead. A whitelisted channel is EXEMPT: its uploads are
 * routinely categorized Music, and blocking them would break the app's core use case.
 */
object FamilyCategoryBlock {
    private val BLOCKED_IDS = setOf("music", "entertainment", "film")

    /** Gemini's blocked category names, lowercased; "Comedy" deliberately excluded. */
    private val BLOCKED_NAMES: Set<String> =
        (BLOCKED_CATEGORIES_STRICT + BLOCKED_CATEGORIES_MODERATE - "Comedy")
            .map { it.lowercase(Locale.ROOT) }
            .toSet()

    /** True when [category] names a blocked category. Null or unknown names never block. */
    fun isBlocked(category: String?): Boolean {
        if (category.isNullOrBlank()) return false
        if (NeuroCategories.idOf(category) in BLOCKED_IDS) return true
        return category.trim().lowercase(Locale.ROOT) in BLOCKED_NAMES
    }
}

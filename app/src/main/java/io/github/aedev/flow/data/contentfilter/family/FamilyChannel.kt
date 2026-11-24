package io.github.aedev.flow.data.contentfilter.family

import io.github.aedev.flow.data.contentfilter.aislist.AiHandle
import kotlinx.serialization.Serializable

/**
 * One approved channel. The `UC…` id is the match key wherever YouTube supplies one; the handle
 * is the fallback for channels whose id has not been resolved yet. A blank handle means no
 * handle could be confirmed for the channel — the id alone is the match key. The shipped list
 * lives in [FAMILY_SEED_CHANNELS] (FamilySeedChannels.kt); the full list with categories, and
 * the channels deliberately left out and why, is in `docs/family-whitelist.md`.
 */
@Serializable
data class FamilyChannel(
    val channelId: String = "",
    val handle: String,
    val name: String,
)

/**
 * A lookup over the whitelist: an id match, or a normalized handle match. Videos that carry
 * neither an id nor a handle cannot be attributed to a channel — local media and legacy entries
 * — so they pass; every YouTube video carries an id, and the named-item gate
 * ([io.github.aedev.flow.data.shorts.ShortsFeedRepository.isBlocked]) catches a Short once its
 * `/player` response names it.
 */
class FamilyChannelMatcher(
    /** The resolved `UC…` ids. */
    val channelIds: Set<String>,
    /** Normalized handles, without `@`, lowercased. */
    val handles: Set<String>,
    /** When true, every channel is allowed: the "no family filtering" matcher. */
    private val allowAll: Boolean = false,
) {
    fun allows(
        channelId: String,
        handle: String?,
    ): Boolean {
        if (allowAll) return true
        if (channelId.isBlank() && handle.isNullOrBlank()) return true
        if (channelId.isNotBlank() && channelId in channelIds) return true
        val normalized = handle?.let(AiHandle::normalize)
        return normalized != null && normalized in handles
    }

    /** True when this matcher can hide anything. */
    val isRestrictive: Boolean get() = !allowAll

    companion object {
        /** Allows nothing: the matcher before the repository has loaded, so a feed cannot leak. */
        val EMPTY = FamilyChannelMatcher(emptySet(), emptySet())

        /** Allows everything: the matcher for surfaces that carry no family filtering. */
        val ALLOW_ALL = FamilyChannelMatcher(emptySet(), emptySet(), allowAll = true)

        fun of(channels: List<FamilyChannel>): FamilyChannelMatcher =
            FamilyChannelMatcher(
                channelIds = channels.mapNotNull { it.channelId.takeIf(String::isNotBlank) }.toSet(),
                handles = channels.mapNotNull { AiHandle.normalize(it.handle) }.toSet(),
            )
    }
}

package io.github.aedev.flow.data.contentfilter.family

import io.github.aedev.flow.data.contentfilter.aislist.AiHandle
import kotlinx.serialization.Serializable

/**
 * One approved channel. The `UC…` id is the match key wherever YouTube supplies one; the handle
 * is the fallback for channels whose id has not been resolved yet (see [FAMILY_SEED_CHANNELS]).
 * A handle-only entry still matches its videos once the id is resolved at runtime.
 */
@Serializable
data class FamilyChannel(
    val channelId: String = "",
    val handle: String,
    val name: String,
)

/**
 * The seven channels the family app ships with. Three ids were verified against YouTube channel
 * pages; the other four ship handle-only and are resolved to `UC…` ids at startup
 * ([FamilyWhitelistRepository.resolveMissingIds]) — never invent an id for them.
 *
 * Rajiya Risha and MercifulServant are deliberately NOT here; they were held back.
 */
val FAMILY_SEED_CHANNELS: List<FamilyChannel> =
    listOf(
        FamilyChannel(channelId = "UCUNkaUjhRArzU4Tm_TPSDlg", handle = "@holytunebdofficial", name = "Holy Tune"),
        FamilyChannel(handle = "@arafahrecord", name = "Arafah Records"),
        FamilyChannel(channelId = "UCxStLx7yb96MGBfIMo20x7Q", handle = "@drmizanurrahmanazharichannel", name = "Mizanur Rahman Azhari"),
        FamilyChannel(handle = "@sheikhahmadullahofficial", name = "Ahmadullah"),
        FamilyChannel(handle = "@alafasy", name = "Alafasy"),
        FamilyChannel(handle = "@islam_sobhi", name = "Islam Sobhi"),
        FamilyChannel(channelId = "UCL8fnPgqDPPBbUphwdv99og", handle = "@sheikhsaadalghamdi", name = "Saad Al Ghamdi"),
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

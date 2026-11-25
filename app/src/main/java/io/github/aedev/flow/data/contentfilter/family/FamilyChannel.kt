package io.github.aedev.flow.data.contentfilter.family

import io.github.aedev.flow.data.contentfilter.aislist.AiHandle
import kotlinx.serialization.Serializable

/**
 * One approved channel. The `UC…` id is the match key wherever YouTube supplies one; the handle
 * is the fallback for channels whose id has not been resolved yet. A blank handle means no
 * handle could be confirmed for the channel — the id alone is the match key. The shipped list
 * lives in [FAMILY_SEED_CHANNELS] (FamilySeedChannels.kt); the full list with categories, and
 * the channels deliberately left out and why, is in `docs/family-whitelist.md`.
 *
 * [category] is one of the [FamilyChannelMatcher] `CATEGORY_*` values — waz, ghazal, tilawat,
 * cartoons, comedy, drama, news, tour or ummah — and the suggestions ranker reads it to surface
 * waz, tilawat and ghazal first ([FamilySuggestionRanker]). A news channel is whitelisted
 * for news only, and a tour channel for tour only: the entertainment-tier title filter still
 * applies to their videos (see [FamilyTextMatcher.blocks]), so a music/drama upload from a
 * news or tour channel is blocked even though the channel itself is approved. The field
 * defaults to "" so stored entries written before it existed still decode; an uncategorized
 * channel ranks as an ordinary approved channel, never as news.
 *
 * [isBangladeshi] marks a channel as Bangladeshi; the ranker surfaces Bangladeshi videos
 * before foreign ones inside each category tier ([FamilySuggestionRanker]). It defaults to
 * `false`, so stored entries written before it existed still decode — as foreign — and the
 * order is unchanged until the seed carries the marker.
 */
@Serializable
data class FamilyChannel(
    val channelId: String = "",
    val handle: String,
    val name: String,
    val category: String = "",
    val isBangladeshi: Boolean = false,
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
    /** The resolved `UC…` ids of whitelisted news channels. */
    val newsChannelIds: Set<String> = emptySet(),
    /** Normalized handles of whitelisted news channels. */
    val newsHandles: Set<String> = emptySet(),
    /** The resolved `UC…` ids of whitelisted tour channels. */
    val tourChannelIds: Set<String> = emptySet(),
    /** Normalized handles of whitelisted tour channels. */
    val tourHandles: Set<String> = emptySet(),
    /** The [FamilyChannel.category] of each resolved `UC…` id, for suggestion ranking. */
    val categoryByChannelId: Map<String, String> = emptyMap(),
    /** The [FamilyChannel.category] of each normalized handle, for id-less entries. */
    val categoryByHandle: Map<String, String> = emptyMap(),
    /** The resolved `UC…` ids of Bangladeshi whitelisted channels. */
    val bangladeshiChannelIds: Set<String> = emptySet(),
    /** Normalized handles of Bangladeshi whitelisted channels. */
    val bangladeshiHandles: Set<String> = emptySet(),
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

    /** True when the channel is a whitelisted news channel (entertainment filter still applies). */
    fun isNewsChannel(
        channelId: String,
        handle: String?,
    ): Boolean {
        if (channelId.isNotBlank() && channelId in newsChannelIds) return true
        val normalized = handle?.let(AiHandle::normalize)
        return normalized != null && normalized in newsHandles
    }

    /** True when the channel is a whitelisted tour channel (entertainment filter still applies). */
    fun isTourChannel(
        channelId: String,
        handle: String?,
    ): Boolean {
        if (channelId.isNotBlank() && channelId in tourChannelIds) return true
        val normalized = handle?.let(AiHandle::normalize)
        return normalized != null && normalized in tourHandles
    }

    /** The [FamilyChannel.category] of this channel, or "" when it is not categorized. */
    fun categoryOf(
        channelId: String,
        handle: String?,
    ): String {
        if (channelId.isNotBlank()) {
            categoryByChannelId[channelId]?.let { return it }
        }
        val normalized = handle?.let(AiHandle::normalize)
        return normalized?.let { categoryByHandle[it] }.orEmpty()
    }

    /** True when this channel is marked Bangladeshi ([FamilyChannel.isBangladeshi]). */
    fun isBangladeshi(
        channelId: String,
        handle: String?,
    ): Boolean {
        if (channelId.isNotBlank() && channelId in bangladeshiChannelIds) return true
        val normalized = handle?.let(AiHandle::normalize)
        return normalized != null && normalized in bangladeshiHandles
    }

    /** True when this matcher can hide anything. */
    val isRestrictive: Boolean get() = !allowAll

    companion object {
        /** The [FamilyChannel.category] value for a waz channel. */
        const val CATEGORY_WAZ = "waz"

        /** The [FamilyChannel.category] value for a ghazal / nasheed channel. */
        const val CATEGORY_GHAZAL = "ghazal"

        /** The [FamilyChannel.category] value for a Quran tilawat channel. */
        const val CATEGORY_TILAWAT = "tilawat"

        /** The [FamilyChannel.category] value for an Islamic cartoons channel. */
        const val CATEGORY_CARTOONS = "cartoons"

        /** The [FamilyChannel.category] value for a halal comedy channel. */
        const val CATEGORY_COMEDY = "comedy"

        /** The [FamilyChannel.category] value for a halal drama channel. */
        const val CATEGORY_DRAMA = "drama"

        /** The [FamilyChannel.category] value for a news channel. */
        const val CATEGORY_NEWS = "news"

        /** The [FamilyChannel.category] value for a tour / travel channel. */
        const val CATEGORY_TOUR = "tour"

        /** The [FamilyChannel.category] value for the Ummah Network channel. */
        const val CATEGORY_UMMAH = "ummah"

        /** Allows nothing: the matcher before the repository has loaded, so a feed cannot leak. */
        val EMPTY = FamilyChannelMatcher(emptySet(), emptySet())

        /** Allows everything: the matcher for surfaces that carry no family filtering. */
        val ALLOW_ALL = FamilyChannelMatcher(emptySet(), emptySet(), allowAll = true)

        fun of(channels: List<FamilyChannel>): FamilyChannelMatcher =
            FamilyChannelMatcher(
                channelIds = channels.mapNotNull { it.channelId.takeIf(String::isNotBlank) }.toSet(),
                handles = channels.mapNotNull { AiHandle.normalize(it.handle) }.toSet(),
                newsChannelIds =
                    channels
                        .filter { it.category == CATEGORY_NEWS }
                        .mapNotNull { it.channelId.takeIf(String::isNotBlank) }
                        .toSet(),
                newsHandles =
                    channels
                        .filter { it.category == CATEGORY_NEWS }
                        .mapNotNull { AiHandle.normalize(it.handle) }
                        .toSet(),
                tourChannelIds =
                    channels
                        .filter { it.category == CATEGORY_TOUR }
                        .mapNotNull { it.channelId.takeIf(String::isNotBlank) }
                        .toSet(),
                tourHandles =
                    channels
                        .filter { it.category == CATEGORY_TOUR }
                        .mapNotNull { AiHandle.normalize(it.handle) }
                        .toSet(),
                categoryByChannelId =
                    channels
                        .filter { it.channelId.isNotBlank() && it.category.isNotBlank() }
                        .associate { it.channelId to it.category },
                categoryByHandle =
                    channels
                        .filter { it.category.isNotBlank() }
                        .mapNotNull { channel -> AiHandle.normalize(channel.handle)?.let { it to channel.category } }
                        .toMap(),
                bangladeshiChannelIds =
                    channels
                        .filter { it.isBangladeshi }
                        .mapNotNull { it.channelId.takeIf(String::isNotBlank) }
                        .toSet(),
                bangladeshiHandles =
                    channels
                        .filter { it.isBangladeshi }
                        .mapNotNull { AiHandle.normalize(it.handle) }
                        .toSet(),
            )
    }
}

package io.github.aedev.flow.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.aedev.flow.data.local.ContentType
import io.github.aedev.flow.data.local.SearchFilter
import io.github.aedev.flow.data.local.WatchState
import io.github.aedev.flow.data.model.DistinctKeyTracker
import io.github.aedev.flow.data.recommendation.FeedExclusions
import io.github.aedev.flow.innertube.YouTube
import io.github.aedev.flow.innertube.pages.renderer.FeedItem
import io.github.aedev.flow.innertube.pages.renderer.FeedShelf
import io.github.aedev.flow.innertube.pages.renderer.FeedShelfStyle
import io.github.aedev.flow.innertube.pages.renderer.videoOrNull
import io.github.aedev.flow.innertube.pages.search.SearchHeader
import io.github.aedev.flow.innertube.pages.search.SearchResultsPage
import io.github.aedev.flow.innertube.pages.search.SearchSection
import io.github.aedev.flow.utils.PerformanceDispatcher
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * One InnerTube request per page, and nothing after it.
 *
 * Every filter and sort is a `params` token the server applies, so a page arrives already narrowed
 * and already ordered; the avatars, badges and verification the old path fetched per video are read
 * out of the same response. The watched filter is the exception: it is applied here, so a page it
 * thins out reads on, up to [MAX_REQUESTS_PER_LOAD] requests, rather than reaching the grid nearly empty.
 * Paging asks again while the list is short, so after [MAX_EMPTY_LOADS] loads in a row that kept
 * nothing the search ends there instead of walking every page YouTube has.
 */
class SearchPagingSource(
    private val query: String,
    private val filter: SearchFilter = SearchFilter.DEFAULT,
    private val shortsEnabled: Boolean = true,
    private val onHeader: (SearchHeader) -> Unit = {},
    private val loadPage: SearchPageLoader = DefaultSearchPageLoader,
    private val watchedVideos: WatchedVideoLookup = WatchedVideoLookup { emptySet() },
    private val exactQuery: Boolean = false,
    private val loadContext: CoroutineContext = PerformanceDispatcher.networkIO,
    private val exclusions: suspend () -> FeedExclusions = { FeedExclusions.NONE },
) : PagingSource<String, SearchResultItem>() {
    override fun getRefreshKey(state: PagingState<String, SearchResultItem>): String? = null

    private val loadedItemKeys = DistinctKeyTracker()
    private var emptyLoadsInARow = 0

    private val watchState =
        filter.watchState.takeIf { filter.contentType.supportsVideoFilters() } ?: WatchState.ANY

    override suspend fun load(params: LoadParams<String>): LoadResult<String, SearchResultItem> =
        withContext(loadContext) { loadPages(params) }

    private suspend fun loadPages(params: LoadParams<String>): LoadResult<String, SearchResultItem> =
        try {
            val hidden = exclusions()
            val queryMatchesTopic = hidden.familySearch?.matchesTopic(query) == true
            val results = mutableListOf<SearchResultItem>()
            var continuation = params.key
            var requests = 0
            do {
                val page = loadPage(query, filter.toSearchParams(exactQuery), continuation)
                if (params.key == null && requests == 0) onHeader(page.header)
                requests++
                val items =
                    if (filter.contentType == ContentType.SHORTS) {
                        page.toShortsResults(shortsEnabled)
                    } else {
                        page.toResultItems(shortsEnabled)
                    }.withoutHidden(hidden, queryMatchesTopic)
                results +=
                    loadedItemKeys.filter(items.watchFiltered()) { it.identityKey() }
                continuation = page.continuation
            } while (watchState != WatchState.ANY &&
                results.size < MIN_FILTERED_PAGE &&
                continuation != null &&
                requests < MAX_REQUESTS_PER_LOAD
            )
            emptyLoadsInARow = if (results.isEmpty()) emptyLoadsInARow + 1 else 0
            val exhausted = watchState != WatchState.ANY && emptyLoadsInARow >= MAX_EMPTY_LOADS
            LoadResult.Page(data = results, prevKey = null, nextKey = continuation.takeUnless { exhausted })
        } catch (e: Exception) {
            LoadResult.Error(e)
        }

    private suspend fun List<SearchResultItem>.watchFiltered(): List<SearchResultItem> {
        if (watchState == WatchState.ANY) return this
        return withWatchState(watchState, watchedVideos(videos()))
    }

    private companion object {
        const val MIN_FILTERED_PAGE = 6
        const val MAX_REQUESTS_PER_LOAD = 3
        const val MAX_EMPTY_LOADS = 2
    }
}

fun interface SearchPageLoader {
    suspend operator fun invoke(
        query: String,
        params: String?,
        continuation: String?,
    ): SearchResultsPage
}

private val DefaultSearchPageLoader =
    SearchPageLoader { query, params, continuation ->
        YouTube.videoSearch(query, params, continuation).getOrThrow()
    }

/**
 * YouTube returns the creator's "Latest from" strip as its own shelf directly after the channel
 * card and renders the two as one block, so they are folded together here rather than reaching the
 * grid as two rows that have to find each other again.
 */
internal fun SearchResultsPage.toResultItems(shortsEnabled: Boolean): List<SearchResultItem> {
    val items = mutableListOf<SearchResultItem>()
    var index = 0
    while (index < sections.size) {
        val item =
            when (val section = sections[index]) {
                is SearchSection.Result -> section.item.toResultItem(shortsEnabled)
                is SearchSection.Strip -> section.shelf.toShelfItem(shortsEnabled)
            }
        val latest = (item as? SearchResultItem.ChannelResult)?.let { sections.latestStripAfter(index) }
        if (latest != null) {
            items +=
                (item as SearchResultItem.ChannelResult).copy(
                    latestTitle = latest.title,
                    latestVideos = latest.videos,
                )
            index += 2
            continue
        }
        item?.let(items::add)
        index++
    }
    return items
}

/**
 * The Shorts filter's results arrive as Shorts strips (#1425), but the Shorts grid lays out single
 * Shorts, so every Short becomes a result of its own and anything that is not a Short is left out.
 * Keyed by video, a later page's Shorts also survive de-duplication: its strips reuse the first
 * page's position-based ids.
 */
internal fun SearchResultsPage.toShortsResults(shortsEnabled: Boolean): List<SearchResultItem> {
    if (!shortsEnabled) return emptyList()
    return sections
        .flatMap { section ->
            when (section) {
                is SearchSection.Result -> {
                    listOfNotNull(section.item.videoOrNull())
                }

                is SearchSection.Strip -> {
                    section.shelf
                        .takeIf { it.style == FeedShelfStyle.Grid }
                        ?.items
                        .orEmpty()
                        .mapNotNull { it.videoOrNull() }
                }
            }
        }.map { SearchResultItem.VideoResult(it) }
}

/**
 * Drops everything a blocked creator put in the results, the way the home feed already drops them
 * before ranking: their own card, their videos, and their videos inside a strip. A video marked not
 * interested goes too. A strip left with nothing goes, rather than staying as a heading over a gap.
 *
 * Only what the viewer hid outright applies: a search is an explicit ask, so the engine's inferred
 * suppressions and blocked topics do not filter it. The AI channel list does, when its hide switch is
 * on; a channel result stays, and only teaches the list which handle its id has.
 *
 * Community posts carry no channel id in the response, so a blocked creator's post survives here.
 *
 * In the family app the whitelist joins too: only approved channels' videos and cards survive,
 * playlists (which carry no channel id) go, and a strip keeps only approved videos — a query
 * with no approved match ends on the empty state, never on outside results. With a family
 * search rule set, search is topic-scoped open instead: a non-whitelisted video survives when
 * it is about an approved topic and passes the blocklist (see [FamilySearchRule]), and a
 * channel card survives when its name or handle matches a topic. A kept card's "Latest from"
 * strip is filtered by the same rule, so a blocked upload never rides along with the card.
 * [queryMatchesTopic] is the query itself matching a topic, computed once per load: it relaxes
 * the result-side topic gate to blocklist-only.
 */
internal fun List<SearchResultItem>.withoutHidden(
    exclusions: FeedExclusions,
    queryMatchesTopic: Boolean = false,
): List<SearchResultItem> {
    if (exclusions.isEmpty) return this
    val familySearch = exclusions.familySearch

    return mapNotNull { item ->
        when (item) {
            is SearchResultItem.VideoResult -> {
                if (familySearch != null) {
                    item.takeUnless {
                        exclusions.hidesFromSearch(it.video, queryMatchesTopic) ||
                            exclusions.hidesAsAi(it.video) ||
                            (exclusions.family.isRestrictive && it.video.channelId.isBlank())
                    }
                } else {
                    item.takeUnless {
                        exclusions.hides(it.video) || exclusions.hidesAsAi(it.video) ||
                            (exclusions.family.isRestrictive && it.video.channelId.isBlank())
                    }
                }
            }

            is SearchResultItem.ChannelResult -> {
                exclusions.ai.learn(item.channel.id, item.channel.handle)
                if (familySearch != null) {
                    val keptLatest =
                        item.latestVideos.filter { familySearch.keepsVideo(it, queryMatchesTopic) }
                    item.copy(latestVideos = keptLatest).takeUnless {
                        exclusions.hidesChannelFromSearch(it.channel)
                    }
                } else {
                    item.takeUnless {
                        exclusions.hidesChannel(it.channel.id) ||
                            (exclusions.family.isRestrictive && !exclusions.family.allows(it.channel.id, it.channel.handle))
                    }
                }
            }

            is SearchResultItem.PlaylistResult -> {
                // A playlist carries no channel id, so it cannot be attributed to an approved
                // channel; the family app drops it rather than guessing.
                if (exclusions.family.isRestrictive) null else item
            }

            is SearchResultItem.ShelfResult -> {
                val videos =
                    if (familySearch != null) {
                        item.videos.filterNot {
                            exclusions.hidesFromSearch(it, queryMatchesTopic) || exclusions.hidesAsAi(it)
                        }
                    } else {
                        item.videos.filterNot { exclusions.hides(it) || exclusions.hidesAsAi(it) }
                    }
                when {
                    videos.isNotEmpty() -> item.copy(videos = videos)
                    item.posts.isNotEmpty() && !exclusions.family.isRestrictive -> item
                    else -> null
                }
            }
        }
    }
}

/** The videos strip that immediately follows [index], if that is what the next section holds. */
private fun List<SearchSection>.latestStripAfter(index: Int): SearchResultItem.ShelfResult? =
    (getOrNull(index + 1) as? SearchSection.Strip)
        ?.shelf
        ?.toShelfItem(shortsEnabled = true)
        ?.let { it as? SearchResultItem.ShelfResult }
        ?.takeIf { it.kind == SearchShelfKind.VIDEOS }

private fun FeedItem.toResultItem(shortsEnabled: Boolean): SearchResultItem? =
    when (this) {
        is FeedItem.VideoItem -> SearchResultItem.VideoResult(video)
        is FeedItem.ShortItem -> SearchResultItem.VideoResult(video).takeIf { shortsEnabled }
        is FeedItem.PlaylistItem -> SearchResultItem.PlaylistResult(playlist)
        is FeedItem.RelatedChannelItem -> SearchResultItem.ChannelResult(channel)
        is FeedItem.PostItem -> null
    }

private fun FeedShelf.toShelfItem(shortsEnabled: Boolean): SearchResultItem? {
    val posts = items.filterIsInstance<FeedItem.PostItem>().map { it.post }
    if (posts.isNotEmpty()) {
        return SearchResultItem.ShelfResult(id, title, SearchShelfKind.POSTS, posts = posts)
    }
    val videos = items.mapNotNull { it.videoOrNull() }
    if (videos.isEmpty()) return null
    val kind = if (style == FeedShelfStyle.Grid) SearchShelfKind.SHORTS else SearchShelfKind.VIDEOS
    if (kind == SearchShelfKind.SHORTS && !shortsEnabled) return null
    return SearchResultItem.ShelfResult(
        id = id,
        title = title,
        kind = kind,
        videos = videos,
        collapsedItemCount = collapsedItemCount,
    )
}

private fun SearchResultItem.identityKey(): String =
    when (this) {
        is SearchResultItem.VideoResult -> video.id.prefixed("video")
        is SearchResultItem.ChannelResult -> channel.id.prefixed("channel")
        is SearchResultItem.PlaylistResult -> playlist.id.prefixed("playlist")
        is SearchResultItem.ShelfResult -> "shelf:$id"
    }

private fun String.prefixed(type: String): String = takeIf(String::isNotBlank)?.let { "$type:$it" }.orEmpty()

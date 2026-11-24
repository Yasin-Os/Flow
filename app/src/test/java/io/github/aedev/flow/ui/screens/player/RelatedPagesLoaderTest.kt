package io.github.aedev.flow.ui.screens.player

import com.google.common.truth.Truth.assertThat
import io.github.aedev.flow.data.local.PlayerPreferences
import io.github.aedev.flow.data.model.Video
import io.github.aedev.flow.data.model.WatchRelatedPage
import io.github.aedev.flow.data.recommendation.FeedExclusions
import io.github.aedev.flow.data.repository.WatchRelatedPages
import io.github.aedev.flow.player.EnhancedPlayerManager
import io.github.aedev.flow.ui.screens.player.state.RelatedAppend
import io.github.aedev.flow.ui.screens.player.state.VideoPlayerUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RelatedPagesLoaderTest {
    private val dispatcher = StandardTestDispatcher()
    private val scope = CoroutineScope(dispatcher)
    private val pages: WatchRelatedPages = mockk()
    private val playerManager: EnhancedPlayerManager =
        mockk(relaxed = true) {
            every { watchedSuggestionsSource } returns null
        }
    private val playerPreferences: PlayerPreferences =
        mockk(relaxed = true) {
            every { autoplayEnabled } returns flowOf(true)
        }
    private var shortsEnabled = true
    private val uiState =
        MutableStateFlow(
            VideoPlayerUiState(
                cachedVideo = video("playing"),
                relatedVideos = listOf(video("a"), video("b")),
                relatedContinuation = "t1",
            ),
        )

    private val loader =
        RelatedPagesLoader(
            pages = pages,
            uiState = uiState,
            playerManager = playerManager,
            playerPreferences = playerPreferences,
            scope = scope,
            networkDispatcher = dispatcher,
            shortsEnabled = { shortsEnabled },
            exclusions = { FeedExclusions.NONE },
        )

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun `family mode never pages the related lane however often the end is reached`() =
        runTest(dispatcher) {
            repeat(3) { loader.loadMore("playing") }
            advanceUntilIdle()

            coVerify(exactly = 0) { pages.nextPage(any()) }
            assertThat(uiState.value.relatedVideos.map { it.id }).containsExactly("a", "b").inOrder()
            assertThat(uiState.value.relatedContinuation).isEqualTo("t1")
            assertThat(uiState.value.relatedAppend).isEqualTo(RelatedAppend.IDLE)
        }

    @Test
    fun `family mode leaves the related lane untouched and sets no autoplay candidates`() =
        runTest(dispatcher) {
            loader.loadMore("playing")
            advanceUntilIdle()

            assertThat(uiState.value.relatedVideos.map { it.id }).containsExactly("a", "b").inOrder()
            assertThat(uiState.value.relatedContinuation).isEqualTo("t1")
            assertThat(uiState.value.relatedAppend).isEqualTo(RelatedAppend.IDLE)
            coVerify(exactly = 0) { pages.nextPage(any()) }
            verify(exactly = 0) { playerManager.setAutoplayCandidates(any(), any(), any()) }
        }

    @Test
    fun `the end of the lane asks for nothing more`() =
        runTest(dispatcher) {
            uiState.value = uiState.value.copy(relatedContinuation = null)

            loader.loadMore("playing")
            advanceUntilIdle()

            coVerify(exactly = 0) { pages.nextPage(any()) }
        }

    @Test
    fun `a page that lands after the video changed is dropped`() =
        runTest(dispatcher) {
            val pending = CompletableDeferred<WatchRelatedPage?>()
            coEvery { pages.nextPage("t1") } coAnswers { pending.await() }

            loader.loadMore("playing")
            advanceUntilIdle()
            uiState.value = VideoPlayerUiState(cachedVideo = video("other"), relatedVideos = listOf(video("x")))
            pending.complete(WatchRelatedPage(listOf(video("c")), continuation = "t2"))
            advanceUntilIdle()

            assertThat(uiState.value.relatedVideos.map { it.id }).containsExactly("x")
            verify(exactly = 0) { playerManager.setAutoplayCandidates(any(), any(), any()) }
        }

    @Test
    fun `family mode never asks for a related page, so there is nothing to retry`() =
        runTest(dispatcher) {
            loader.loadMore("playing")
            advanceUntilIdle()

            assertThat(uiState.value.relatedAppend).isEqualTo(RelatedAppend.IDLE)
            assertThat(uiState.value.relatedContinuation).isEqualTo("t1")

            loader.loadMore("playing")
            advanceUntilIdle()

            coVerify(exactly = 0) { pages.nextPage(any()) }
            assertThat(uiState.value.relatedVideos.map { it.id }).containsExactly("a", "b").inOrder()
        }

    private fun video(
        id: String,
        isShort: Boolean = false,
    ) = Video(
        id = id,
        title = id,
        channelName = "Channel",
        channelId = "channel",
        thumbnailUrl = "https://example.invalid/$id.jpg",
        duration = 120,
        viewCount = 1L,
        uploadDate = "2026-01-01",
        isShort = isShort,
    )
}

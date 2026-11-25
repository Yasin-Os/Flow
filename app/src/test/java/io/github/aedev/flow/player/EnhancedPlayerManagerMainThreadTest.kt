package io.github.aedev.flow.player

import android.app.Application
import androidx.media3.common.util.UnstableApi
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.github.aedev.flow.player.resolver.Media3JvmShims
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import kotlin.concurrent.thread

/**
 * The player lives on the main thread: ExoPlayer throws "Player is accessed on the wrong
 * thread" for any access from elsewhere (seen as a crash from a FlowNetwork thread while a
 * video played). Every control that touches the player must run on the main thread itself,
 * or post there when called from another one, and a read must never throw.
 */
@UnstableApi
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], application = Application::class)
class EnhancedPlayerManagerMainThreadTest {
    private val dispatcher = StandardTestDispatcher()

    private lateinit var harness: PlayerManagerHarness

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        Media3JvmShims.install()
        harness = PlayerManagerHarness()
    }

    @After
    fun tearDown() {
        Media3JvmShims.uninstall()
        Dispatchers.resetMain()
    }

    @Test
    fun `pause on the main thread pauses immediately`() {
        harness.manager.pause()

        verify(exactly = 1) { harness.player.pause() }
    }

    @Test
    fun `pause from a network thread pauses on the main thread`() {
        thread(name = "FlowNetwork-8") { harness.manager.pause() }.join()

        verify(exactly = 0) { harness.player.pause() }
        ShadowLooper.idleMainLooper()
        verify(exactly = 1) { harness.player.pause() }
    }

    @Test
    fun `play from a network thread plays on the main thread`() {
        thread(name = "FlowNetwork-8") { harness.manager.play() }.join()

        verify(exactly = 0) { harness.player.play() }
        ShadowLooper.idleMainLooper()
        verify(exactly = 1) { harness.player.play() }
    }

    @Test
    fun `seekTo from a network thread seeks on the main thread`() {
        thread(name = "FlowNetwork-8") { harness.manager.seekTo(10_000L) }.join()

        verify(exactly = 0) { harness.player.seekTo(10_000L) }
        ShadowLooper.idleMainLooper()
        verify(exactly = 1) { harness.player.seekTo(10_000L) }
    }

    @Test
    fun `isPlaying from a network thread reads the mirrored state`() {
        harness.state.value = harness.state.value.copy(isPlaying = true)

        var playing = false
        thread(name = "FlowNetwork-8") { playing = harness.manager.isPlaying() }.join()

        assertThat(playing).isTrue()
        verify(exactly = 0) { harness.player.isPlaying }
    }
}

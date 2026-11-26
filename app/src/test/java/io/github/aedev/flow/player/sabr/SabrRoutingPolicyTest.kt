package io.github.aedev.flow.player.sabr

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SabrRoutingPolicyTest {
    @Test
    fun `direct ladders below the floor are upgrade candidates`() {
        assertThat(SabrRoutingPolicy.shouldAttemptSabrUpgrade(360)).isTrue()
        assertThat(SabrRoutingPolicy.shouldAttemptSabrUpgrade(480)).isTrue()
        assertThat(SabrRoutingPolicy.shouldAttemptSabrUpgrade(0)).isTrue()
    }

    @Test
    fun `direct ladders at or above the floor are accepted as-is`() {
        assertThat(SabrRoutingPolicy.shouldAttemptSabrUpgrade(720)).isFalse()
        assertThat(SabrRoutingPolicy.shouldAttemptSabrUpgrade(1080)).isFalse()
        assertThat(SabrRoutingPolicy.shouldAttemptSabrUpgrade(2160)).isFalse()
    }

    @Test
    fun `the sabr upgrade waits less on mobile data`() {
        assertThat(SabrRoutingPolicy.upgradeBudgetMs(isWifi = true)).isEqualTo(3_000L)
        assertThat(SabrRoutingPolicy.upgradeBudgetMs(isWifi = false)).isEqualTo(1_000L)
    }

    @Test
    fun `a slow mobile link skips the sabr upgrade`() {
        // Below 2 Mbps on mobile data the direct result already plays; the upgrade is not worth its wait.
        assertThat(SabrRoutingPolicy.shouldAttemptSabrUpgrade(360, 1_000_000L, false)).isFalse()
        // At the threshold, and with an unknown estimate (0), the upgrade still runs.
        assertThat(SabrRoutingPolicy.shouldAttemptSabrUpgrade(360, 2_000_000L, false)).isTrue()
        assertThat(SabrRoutingPolicy.shouldAttemptSabrUpgrade(360, 0L, false)).isTrue()
        // Wi-Fi always attempts it, however slow the estimate.
        assertThat(SabrRoutingPolicy.shouldAttemptSabrUpgrade(360, 1_000_000L, true)).isTrue()
        // The floor check still applies first: a tall direct ladder never upgrades.
        assertThat(SabrRoutingPolicy.shouldAttemptSabrUpgrade(720, 10_000_000L, false)).isFalse()
    }

    @Test
    fun `sabr is preferred only when it beats the direct ceiling`() {
        // SABR 1080p vs direct 360p -> upgrade
        assertThat(SabrRoutingPolicy.shouldPreferSabr(false, 1080, 360)).isTrue()
        // SABR equal to direct -> keep direct (no needless session)
        assertThat(SabrRoutingPolicy.shouldPreferSabr(false, 1080, 1080)).isFalse()
        // SABR lower than direct -> keep direct
        assertThat(SabrRoutingPolicy.shouldPreferSabr(false, 720, 1080)).isFalse()
        // No SABR height -> never prefer
        assertThat(SabrRoutingPolicy.shouldPreferSabr(false, 0, 0)).isFalse()
    }

    @Test
    fun `forced escalation always prefers sabr regardless of heights`() {
        // A 403/expiry reload forces SABR even if heights look equal or unknown
        assertThat(SabrRoutingPolicy.shouldPreferSabr(true, 0, 1080)).isTrue()
        assertThat(SabrRoutingPolicy.shouldPreferSabr(true, 1080, 1080)).isTrue()
    }

    @Test
    fun `an upgrade that timed out or resolved nothing keeps the playable direct result`() {
        assertThat(SabrRoutingPolicy.upgradeReplacesDirect(directMaxHeight = 480, sabrHeight = null)).isFalse()
    }

    @Test
    fun `an upgrade replaces the direct ladder only when it is taller`() {
        assertThat(SabrRoutingPolicy.upgradeReplacesDirect(directMaxHeight = 480, sabrHeight = 1080)).isTrue()
        assertThat(SabrRoutingPolicy.upgradeReplacesDirect(directMaxHeight = 480, sabrHeight = 480)).isFalse()
        assertThat(SabrRoutingPolicy.upgradeReplacesDirect(directMaxHeight = 480, sabrHeight = 360)).isFalse()
    }
}

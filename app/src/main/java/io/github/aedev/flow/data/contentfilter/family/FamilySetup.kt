package io.github.aedev.flow.data.contentfilter.family

import android.util.Log
import io.github.aedev.flow.data.local.PlayerPreferences
import io.github.aedev.flow.player.preferences.ThumbnailQuality

/**
 * First-launch family setup, run from [io.github.aedev.flow.FlowApplication] on an IO coroutine.
 * Seeds the approved channels (and mirrors them into subscriptions, so the home feed has
 * uploads to show) and turns off the navigation the family app does not have: the Music tab
 * and the home Shorts shelf. The Search tab stays on: search is whitelist-scoped, so it can
 * only ever return approved channels and their videos. The [FamilyMode] matcher
 * providers are wired separately in `FlowApplication.onCreate`, next to the AiSList wiring.
 */
object FamilySetup {
    private const val TAG = "FamilySetup"

    suspend fun seedIfNeeded(
        whitelist: FamilyWhitelistRepository,
        playerPreferences: PlayerPreferences,
    ) {
        runCatching { whitelist.ensureSeeded() }
            .onFailure { Log.w(TAG, "Family whitelist seeding failed", it) }
        runCatching {
            playerPreferences.setMusicNavigationEnabled(false)
            playerPreferences.setSearchNavigationEnabled(true)
            playerPreferences.setHomeShortsShelfEnabled(false)
            playerPreferences.setThumbnailQualityCellularIfUnset(ThumbnailQuality.MEDIUM)
        }.onFailure { Log.w(TAG, "Family navigation prefs failed", it) }
    }
}

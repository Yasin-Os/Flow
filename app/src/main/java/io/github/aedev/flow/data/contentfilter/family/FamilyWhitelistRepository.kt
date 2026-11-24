package io.github.aedev.flow.data.contentfilter.family

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.aedev.flow.data.local.ChannelSubscription
import io.github.aedev.flow.data.local.SubscriptionRepository
import io.github.aedev.flow.data.local.safePreferencesDataStore
import io.github.aedev.flow.data.profile.StoreScope
import io.github.aedev.flow.data.repository.YouTubeRepository
import io.github.aedev.flow.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.familyWhitelistDataStore: DataStore<Preferences> by safePreferencesDataStore(
    name = "family_whitelist",
    scope = StoreScope.PROFILE_LIBRARY,
)

/**
 * The approved channels, kept as one JSON list in their own DataStore — the same shape as the
 * subscriptions store ([SubscriptionRepository]), so the list survives with the profile and a
 * corrupt file resets to the seed instead of crashing the feed.
 *
 * Why a list store and not one key per channel: the whitelist is read as a whole on every feed
 * build, and the matcher snapshot is rebuilt from it; one JSON string is one read, and add/remove
 * stays a single edit. The subscriptions mirror is what feeds the home feed: every whitelisted
 * channel with a resolved id is also a subscription, so the existing RSS
 * `SubscriptionFeedRepository` pipeline fetches its uploads with no changes there.
 */
@Singleton
class FamilyWhitelistRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val youTubeRepository: YouTubeRepository,
        private val subscriptionRepository: SubscriptionRepository,
        @ApplicationScope private val scope: CoroutineScope,
    ) {
        private val json = Json { ignoreUnknownKeys = true }

        /** The stored list; empty until [ensureSeeded] runs at startup. */
        val channels: Flow<List<FamilyChannel>> =
            context.familyWhitelistDataStore.data
                .map { preferences ->
                    val raw = preferences[CHANNELS_KEY].orEmpty()
                    if (raw.isBlank()) {
                        emptyList()
                    } else {
                        runCatching { json.decodeFromString(ListSerializer(FamilyChannel.serializer()), raw) }
                            .onFailure { Log.w(TAG, "Family whitelist unreadable, starting empty", it) }
                            .getOrDefault(emptyList())
                    }
                }.catch { e ->
                    Log.w(TAG, "Family whitelist read failed, starting empty", e)
                    emit(emptyList())
                }

        private val matcherState = MutableStateFlow(FamilyChannelMatcher.EMPTY)

        /** The matcher every surface reads synchronously; rebuilt whenever the list changes. */
        val matcher: StateFlow<FamilyChannelMatcher> = matcherState.asStateFlow()

        init {
            scope.launch {
                channels.collect { matcherState.value = FamilyChannelMatcher.of(it) }
            }
        }

        /** The matcher as it stands now, for singletons that cannot collect a flow. */
        fun currentMatcher(): FamilyChannelMatcher = matcherState.value

        /** The resolved `UC…` ids, for the home feed's subscription scope. */
        suspend fun whitelistChannelIds(): Set<String> = currentMatcher().channelIds

        /**
         * Seeds the approved channels on first launch and mirrors them into subscriptions,
         * then merges in any seed channels added by a later round (tracked by [SEED_VERSION])
         * so an existing install gains the new channels too. A channel the PIN holder removed
         * is never re-added: the merge runs once per seed version, and it skips channels with
         * an unsubscribe tombstone. A failed resolution (offline, renamed handle) keeps the
         * handle entry, which still matches by handle; the next launch retries.
         */
        suspend fun ensureSeeded() {
            val current = channels.first()
            if (current.isEmpty()) {
                save(FAMILY_SEED_CHANNELS)
                mirrorSubscriptions(FAMILY_SEED_CHANNELS)
                setSeedVersion(SEED_VERSION)
            } else {
                mergeSeedUpdates(current)
            }
            resolveMissingIds()
        }

        /** Adds seed channels missing from the stored list, once per [SEED_VERSION]. */
        private suspend fun mergeSeedUpdates(current: List<FamilyChannel>) {
            if (seedVersion() >= SEED_VERSION) return
            val removedIds = runCatching { subscriptionRepository.unsubscribedTombstones().keys }.getOrDefault(emptySet())
            val missing =
                FAMILY_SEED_CHANNELS.filterNot { seed ->
                    seed.channelId.isNotBlank() && seed.channelId in removedIds ||
                        current.any { existing ->
                            (seed.channelId.isNotBlank() && existing.channelId == seed.channelId) ||
                                (seed.handle.isNotBlank() && existing.handle.equals(seed.handle, ignoreCase = true))
                        }
                }
            if (missing.isNotEmpty()) {
                save(current + missing)
                mirrorSubscriptions(missing)
            }
            setSeedVersion(SEED_VERSION)
        }

        private suspend fun seedVersion(): Int =
            context.familyWhitelistDataStore.data.first()[SEED_VERSION_KEY] ?: 0

        private suspend fun setSeedVersion(version: Int) {
            context.familyWhitelistDataStore.edit { preferences -> preferences[SEED_VERSION_KEY] = version }
        }

        suspend fun add(channel: FamilyChannel) {
            val current = channels.first()
            val duplicate =
                current.any { existing ->
                    (channel.channelId.isNotBlank() && existing.channelId == channel.channelId) ||
                        (channel.handle.isNotBlank() && existing.handle.equals(channel.handle, ignoreCase = true))
                }
            if (duplicate) return
            save(current + channel)
            mirrorSubscriptions(listOf(channel))
        }

        suspend fun remove(channel: FamilyChannel) {
            val current = channels.first()
            val updated =
                current.filterNot {
                    (channel.channelId.isNotBlank() && it.channelId == channel.channelId) ||
                        (channel.channelId.isBlank() && it.handle.equals(channel.handle, ignoreCase = true))
                }
            if (updated.size == current.size) return
            save(updated)
            if (channel.channelId.isNotBlank()) {
                runCatching { subscriptionRepository.unsubscribe(channel.channelId) }
            }
        }

        /** Resolves handle-only entries to `UC…` ids via the app's own channel lookup. */
        suspend fun resolveMissingIds() {
            val current = channels.first()
            val unresolved = current.filter { it.channelId.isBlank() }
            if (unresolved.isEmpty()) return
            var changed = false
            val resolved =
                current.map { channel ->
                    if (channel.channelId.isNotBlank()) return@map channel
                    val info = runCatching { youTubeRepository.getChannelInfo(channel.handle) }.getOrNull()
                    val id = info?.id.orEmpty()
                    if (id.isBlank() || !id.startsWith("UC")) {
                        Log.w(TAG, "Could not resolve ${channel.handle} to a channel id")
                        channel
                    } else {
                        changed = true
                        channel.copy(channelId = id)
                    }
                }
            if (!changed) return
            save(resolved)
            mirrorSubscriptions(resolved.filter { it.channelId.isNotBlank() })
        }

        private suspend fun save(channels: List<FamilyChannel>) {
            val raw = json.encodeToString(ListSerializer(FamilyChannel.serializer()), channels)
            context.familyWhitelistDataStore.edit { preferences -> preferences[CHANNELS_KEY] = raw }
            matcherState.value = FamilyChannelMatcher.of(channels)
        }

        private suspend fun mirrorSubscriptions(channels: List<FamilyChannel>) {
            val subscriptions =
                channels.filter { it.channelId.isNotBlank() }.map { channel ->
                    ChannelSubscription(
                        channelId = channel.channelId,
                        channelName = channel.name,
                        channelThumbnail = "",
                    )
                }
            if (subscriptions.isEmpty()) return
            runCatching { subscriptionRepository.subscribeAll(subscriptions) }
                .onFailure { Log.w(TAG, "Could not mirror ${subscriptions.size} channels into subscriptions", it) }
        }

        private companion object {
            const val TAG = "FamilyWhitelist"
            val CHANNELS_KEY = stringPreferencesKey("channels")
            val SEED_VERSION_KEY = intPreferencesKey("seed_version")
            /**
             * Bump when [FAMILY_SEED_CHANNELS] gains channels: existing installs merge them in
             * once. Round 3 bumped it again because run #24's head already carried version 2,
             * so a merge keyed on 2 would skip the new channels on any install of that head.
             */
            const val SEED_VERSION = 3
        }
    }

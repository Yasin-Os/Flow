package io.github.aedev.flow.ui.screens.settings.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aedev.flow.R
import io.github.aedev.flow.data.contentfilter.family.FamilyBlocklistRepository
import io.github.aedev.flow.data.contentfilter.family.FamilyChannel
import io.github.aedev.flow.data.contentfilter.family.FamilyMode
import io.github.aedev.flow.data.contentfilter.family.FamilyPinRepository
import io.github.aedev.flow.data.contentfilter.family.FamilyWhitelistRepository
import io.github.aedev.flow.data.model.Channel
import io.github.aedev.flow.data.paging.ChannelSearch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SEARCH_DEBOUNCE_MS = 400L
private const val MAX_FAILED_PIN_ATTEMPTS = 5
private const val PIN_LOCKOUT_MS = 60_000L

/** Channels-only search results: null while a search runs. */
internal data class FamilyChannelSearchState(
    val results: List<Channel>? = emptyList(),
    val blocked: Boolean = false,
    val failed: Boolean = false,
)

/**
 * The family Settings area: the PIN gate, the approved channels, channels-only search and the
 * extra blocked words. The PIN unlock lasts only while this view model lives — leaving the
 * page locks it again.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class FamilySettingsViewModel
    @Inject
    constructor(
        private val whitelist: FamilyWhitelistRepository,
        private val blocklist: FamilyBlocklistRepository,
        private val pinRepository: FamilyPinRepository,
        private val channelSearch: ChannelSearch,
    ) : ViewModel() {
        val pinSet: StateFlow<Boolean> =
            pinRepository.hasPin.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

        private val _unlocked = MutableStateFlow(false)
        val unlocked: StateFlow<Boolean> = _unlocked.asStateFlow()

        private val _pinError = MutableStateFlow<Int?>(null)
        val pinError: StateFlow<Int?> = _pinError.asStateFlow()

        private val _pinLocked = MutableStateFlow(false)
        val pinLocked: StateFlow<Boolean> = _pinLocked.asStateFlow()

        val channels: StateFlow<List<FamilyChannel>> =
            whitelist.channels.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        val customWords: StateFlow<Set<String>> =
            blocklist.customWords.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

        private val query = MutableStateFlow("")
        val search: StateFlow<FamilyChannelSearchState> =
            query
                .debounce(SEARCH_DEBOUNCE_MS)
                .flatMapLatest { text -> searchFlow(text) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FamilyChannelSearchState())

        private var failedPinAttempts = 0
        private var lockoutJob: Job? = null

        fun onQueryChange(text: String) {
            query.value = text
        }

        /** First use: sets the PIN (no default ever exists) and unlocks. */
        fun setupPin(
            pin: String,
            confirm: String,
        ) {
            when {
                !FamilyPinRepository.isValidPin(pin) -> _pinError.value = R.string.family_pin_invalid
                pin != confirm -> _pinError.value = R.string.family_pin_mismatch
                else ->
                    viewModelScope.launch {
                        pinRepository.setPin(pin)
                        _pinError.value = null
                        _unlocked.value = true
                    }
            }
        }

        /** Unlocks the family area, or counts a failed attempt towards the lockout. */
        fun unlock(pin: String) {
            if (_pinLocked.value) return
            viewModelScope.launch {
                if (pinRepository.verify(pin)) {
                    failedPinAttempts = 0
                    _pinError.value = null
                    _unlocked.value = true
                } else {
                    failedPinAttempts++
                    if (failedPinAttempts >= MAX_FAILED_PIN_ATTEMPTS) {
                        failedPinAttempts = 0
                        _pinLocked.value = true
                        _pinError.value = R.string.family_pin_locked
                        lockoutJob?.cancel()
                        lockoutJob =
                            viewModelScope.launch {
                                delay(PIN_LOCKOUT_MS)
                                _pinLocked.value = false
                                _pinError.value = null
                            }
                    } else {
                        _pinError.value = R.string.family_pin_wrong
                    }
                }
            }
        }

        fun addChannel(channel: Channel) {
            viewModelScope.launch {
                whitelist.add(
                    FamilyChannel(
                        channelId = channel.id,
                        handle = channel.handle,
                        name = channel.name,
                    ),
                )
            }
        }

        fun removeChannel(channel: FamilyChannel) {
            viewModelScope.launch { whitelist.remove(channel) }
        }

        fun addWord(word: String) {
            viewModelScope.launch { blocklist.addWord(word) }
        }

        fun removeWord(word: String) {
            viewModelScope.launch { blocklist.removeWord(word) }
        }

        private fun searchFlow(text: String): Flow<FamilyChannelSearchState> {
            val trimmed = text.trim()
            if (trimmed.isEmpty()) return flowOf(FamilyChannelSearchState())
            if (FamilyMode.textMatcher().blocksQuery(trimmed)) {
                return flowOf(FamilyChannelSearchState(results = emptyList(), blocked = true))
            }
            return flow {
                emit(FamilyChannelSearchState(results = null))
                val results = runCatching { channelSearch.search(trimmed) }.getOrNull()
                emit(
                    FamilyChannelSearchState(
                        results = results ?: emptyList(),
                        failed = results == null,
                    ),
                )
            }
        }
    }

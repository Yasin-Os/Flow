package io.github.aedev.flow.data.contentfilter.family

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.aedev.flow.data.local.safePreferencesDataStore
import io.github.aedev.flow.data.profile.StoreScope
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
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.familyBlocklistDataStore: DataStore<Preferences> by safePreferencesDataStore(
    name = "family_blocklist",
    scope = StoreScope.PROFILE_LIBRARY,
)

/**
 * Extra blocked words the PIN holder adds from Family Settings. They join the entertainment tier
 * (never the hard tier — a user word is a topic word, not an explicit-content verdict), so they
 * apply to non-whitelisted videos and to Settings search queries, exactly like the built-in
 * entertainment words. Stored as one JSON set in their own DataStore, next to the whitelist.
 */
@Singleton
class FamilyBlocklistRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        @ApplicationScope private val scope: CoroutineScope,
    ) {
        private val json = Json { ignoreUnknownKeys = true }

        val customWords: Flow<Set<String>> =
            context.familyBlocklistDataStore.data
                .map { preferences ->
                    val raw = preferences[CUSTOM_WORDS_KEY].orEmpty()
                    if (raw.isBlank()) {
                        emptySet()
                    } else {
                        runCatching { json.decodeFromString(SetSerializer(String.serializer()), raw) }
                            .onFailure { Log.w(TAG, "Family blocklist unreadable, starting empty", it) }
                            .getOrDefault(emptySet())
                    }
                }.catch { e ->
                    Log.w(TAG, "Family blocklist read failed, starting empty", e)
                    emit(emptySet())
                }

        private val matcherState = MutableStateFlow(FamilyTextMatcher.DEFAULT)

        /** The matcher every surface reads synchronously; rebuilt when the words change. */
        val matcher: StateFlow<FamilyTextMatcher> = matcherState.asStateFlow()

        init {
            scope.launch {
                customWords.collect { words ->
                    matcherState.value =
                        FamilyTextMatcher(
                            FamilyTextMatcher.DEFAULT_HARD_WORDS,
                            FamilyTextMatcher.DEFAULT_ENTERTAINMENT_WORDS,
                            words,
                        )
                }
            }
        }

        /** The matcher as it stands now, for singletons that cannot collect a flow. */
        fun currentMatcher(): FamilyTextMatcher = matcherState.value

        suspend fun addWord(word: String) {
            val clean = word.trim()
            if (clean.isEmpty() || clean.length > MAX_WORD_LENGTH) return
            val current = customWords.first()
            if (clean in current) return
            save(current + clean)
        }

        suspend fun removeWord(word: String) {
            val current = customWords.first()
            if (word !in current) return
            save(current - word)
        }

        private suspend fun save(words: Set<String>) {
            val raw = json.encodeToString(SetSerializer(String.serializer()), words)
            context.familyBlocklistDataStore.edit { preferences -> preferences[CUSTOM_WORDS_KEY] = raw }
            matcherState.value =
                FamilyTextMatcher(
                    FamilyTextMatcher.DEFAULT_HARD_WORDS,
                    FamilyTextMatcher.DEFAULT_ENTERTAINMENT_WORDS,
                    words,
                )
        }

        private companion object {
            const val TAG = "FamilyBlocklist"
            const val MAX_WORD_LENGTH = 64
            val CUSTOM_WORDS_KEY = stringPreferencesKey("custom_words")
        }
    }

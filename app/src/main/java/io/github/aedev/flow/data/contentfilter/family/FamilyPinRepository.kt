package io.github.aedev.flow.data.contentfilter.family

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.aedev.flow.data.local.KeystoreSecretBox
import io.github.aedev.flow.data.local.devicePreferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The family PIN: a 4–6 digit code that guards the family Settings area (whitelist add/remove
 * and blocklist word management). There is no default PIN and none is ever shipped or stored:
 * the first visit to Family Settings asks the holder to set one.
 *
 * Storage follows the app-lock pattern exactly: the SHA-256 of `salt:pin` is sealed with
 * [KeystoreSecretBox] (AES-GCM, key in the Android Keystore) and kept in device-scoped keys
 * (`family_pin_hash`, `family_pin_salt`), declared in PlayerPreferences' DeviceKeys and listed
 * in its `BackupExcludedKeys`, so a backup can never carry the PIN to another device. The salt is
 * stored in plaintext — it is not a secret, it only stops two equal PINs hashing alike. If
 * the Keystore key is wiped (app data cleared), the sealed hash no longer opens and the PIN is
 * treated as unset: the holder sets a new one.
 *
 * Lockout lives in the PIN screen's view model (5 wrong tries → 60 seconds), not here: it is
 * in-memory, and a process death resets it — acceptable for a family gate, and it keeps this
 * repository a pure store.
 */
@Singleton
class FamilyPinRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        /** True once a PIN has been set and its sealed hash still opens. */
        val hasPin: Flow<Boolean> =
            context.devicePreferencesDataStore.data
                .map { preferences ->
                    val sealed = preferences[PIN_HASH_KEY].orEmpty()
                    val salt = preferences[PIN_SALT_KEY].orEmpty()
                    sealed.isNotEmpty() && salt.isNotEmpty() && KeystoreSecretBox.open(sealed).isNotEmpty()
                }
                // Opening the sealed hash touches the Keystore; never on the main thread.
                .flowOn(Dispatchers.IO)

        suspend fun hasPinNow(): Boolean = hasPin.first()

        /** Sets (or replaces) the PIN. Callers must validate with [isValidPin] first. */
        suspend fun setPin(pin: String) {
            val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }.toHex()
            val hash = hashPin(salt, pin)
            context.devicePreferencesDataStore.edit { preferences ->
                preferences[PIN_SALT_KEY] = salt
                preferences[PIN_HASH_KEY] = KeystoreSecretBox.seal(hash)
            }
        }

        /** True when [pin] matches the stored PIN; false when no PIN is set. */
        suspend fun verify(pin: String): Boolean {
            val preferences = context.devicePreferencesDataStore.data.first()
            val sealed = preferences[PIN_HASH_KEY].orEmpty()
            val salt = preferences[PIN_SALT_KEY].orEmpty()
            val storedHash = KeystoreSecretBox.open(sealed)
            if (sealed.isEmpty() || salt.isEmpty() || storedHash.isEmpty()) return false
            val attemptHash = hashPin(salt, pin)
            return MessageDigest.isEqual(
                storedHash.toByteArray(Charsets.UTF_8),
                attemptHash.toByteArray(Charsets.UTF_8),
            )
        }

        suspend fun clearPin() {
            context.devicePreferencesDataStore.edit { preferences ->
                preferences.remove(PIN_HASH_KEY)
                preferences.remove(PIN_SALT_KEY)
            }
        }

        private fun hashPin(
            salt: String,
            pin: String,
        ): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest("$salt:$pin".toByteArray(Charsets.UTF_8))
            return bytes.toHex()
        }

        private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

        companion object {
            const val MIN_PIN_LENGTH = 4
            const val MAX_PIN_LENGTH = 6
            private const val SALT_BYTES = 16

            /** The device-store key names; must match PlayerPreferences' DeviceKeys exactly. */
            const val PIN_HASH_KEY_NAME = "family_pin_hash"
            const val PIN_SALT_KEY_NAME = "family_pin_salt"

            /** A PIN is 4–6 digits, nothing else. */
            fun isValidPin(pin: String): Boolean =
                pin.length in MIN_PIN_LENGTH..MAX_PIN_LENGTH && pin.all { it.isDigit() }

            private val PIN_HASH_KEY = stringPreferencesKey(PIN_HASH_KEY_NAME)
            private val PIN_SALT_KEY = stringPreferencesKey(PIN_SALT_KEY_NAME)
        }
    }

package com.simplan.cuentasclaras.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.repository.LockRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class LockRepositoryImpl(
    private val context: Context
) : LockRepository {

    private val prefs: SharedPreferences by lazy { createPrefs() }

    private fun createPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return try {
            buildEncrypted(masterKey)
        } catch (e: Exception) {
            // La clave maestra cambió (p. ej. restauración sin los datos del Keystore):
            // se reinicia el almacén para no dejar la app inutilizable.
            context.deleteSharedPreferences(PREFS_NAME)
            buildEncrypted(masterKey)
        }
    }

    private fun buildEncrypted(masterKey: MasterKey): SharedPreferences =
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    private fun String.toBytes(): ByteArray = Base64.decode(this, Base64.NO_WRAP)

    private fun ByteArray.toBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP)

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun deriveHash(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
    }

    private fun now(): Long = System.currentTimeMillis()

    private fun isLockoutActive(): Boolean = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L) > now()

    /**
     * Duración del bloqueo según el nivel (bloqueos consecutivos acumulados).
     * Endurecimiento escalonado: 30 s → 5 min → 30 min.
     */
    private fun lockoutMillisFor(level: Int): Long = when {
        level <= 1 -> LOCKOUT_MILLIS_LEVEL_1
        level == 2 -> LOCKOUT_MILLIS_LEVEL_2
        else -> LOCKOUT_MILLIS_LEVEL_3
    }

    private fun generateRecoveryCode(): String {
        val random = SecureRandom()
        val chars = (1..RECOVERY_CODE_LENGTH).map {
            RECOVERY_ALPHABET[random.nextInt(RECOVERY_ALPHABET.length)]
        }.joinToString("")
        return chars.chunked(4).joinToString("-")
    }

    private fun normalizeRecoveryCode(code: String): String =
        code.uppercase().filter { it.isLetterOrDigit() }

    override suspend fun isLocked(): Result<Boolean> = try {
        Result.success(prefs.getBoolean(KEY_IS_LOCKED, false))
    } catch (e: Exception) {
        Result.error(e)
    }

    override fun observeLockState(): Flow<Result<Boolean>> = callbackFlow {
        trySend(Result.success(prefs.getBoolean(KEY_IS_LOCKED, false)))
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_IS_LOCKED) {
                trySend(Result.success(prefs.getBoolean(KEY_IS_LOCKED, false)))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun hasPin(): Result<Boolean> = try {
        Result.success(prefs.getString(KEY_PIN_HASH, "").orEmpty().isNotEmpty())
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun getPinLength(): Result<Int> = try {
        Result.success(prefs.getInt(KEY_PIN_LENGTH, 0))
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun lock(): Result<Unit> = try {
        prefs.edit().putBoolean(KEY_IS_LOCKED, true).apply()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun unlock(pin: String): Result<Boolean> = try {
        if (isLockoutActive()) {
            Result.success(false)
        } else {
            val storedHash = prefs.getString(KEY_PIN_HASH, "").orEmpty()
            val salt = prefs.getString(KEY_PIN_SALT, "").orEmpty()
            if (storedHash.isEmpty() || salt.isEmpty()) {
                Result.success(false)
            } else {
                val expected = storedHash.toBytes()
                val actual = deriveHash(pin, salt.toBytes())
                val isValid = MessageDigest.isEqual(expected, actual)
                if (isValid) {
                    resetFailedAttempts()
                    prefs.edit()
                        .putBoolean(KEY_IS_LOCKED, false)
                        .putLong(KEY_LOCKOUT_UNTIL, 0L)
                        .apply()
                } else {
                    val attempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
                    val editor = prefs.edit()
                        .putInt(KEY_FAILED_ATTEMPTS, attempts)
                        .putLong(KEY_LAST_FAILED_AT, now())
                    if (attempts % MAX_ATTEMPTS == 0) {
                        val level = attempts / MAX_ATTEMPTS
                        editor.putLong(KEY_LOCKOUT_UNTIL, now() + lockoutMillisFor(level))
                    }
                    editor.apply()
                }
                Result.success(isValid)
            }
        }
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun verifyPin(pin: String): Result<Boolean> = unlock(pin)

    override suspend fun verifyRecoveryCode(code: String): Result<Boolean> = try {
        val storedHash = prefs.getString(KEY_RECOVERY_HASH, "").orEmpty()
        val salt = prefs.getString(KEY_RECOVERY_SALT, "").orEmpty()
        if (storedHash.isEmpty() || salt.isEmpty()) {
            Result.success(false)
        } else {
            val expected = storedHash.toBytes()
            val actual = deriveHash(normalizeRecoveryCode(code), salt.toBytes())
            Result.success(MessageDigest.isEqual(expected, actual))
        }
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun setPin(pin: String): Result<String> = try {
        val salt = generateSalt()
        val hash = deriveHash(pin, salt)

        val recoveryCode = generateRecoveryCode()
        val recoverySalt = generateSalt()
        val recoveryHash = deriveHash(normalizeRecoveryCode(recoveryCode), recoverySalt)

        prefs.edit()
            .putString(KEY_PIN_SALT, salt.toBase64())
            .putString(KEY_PIN_HASH, hash.toBase64())
            .putInt(KEY_PIN_LENGTH, pin.length)
            .putString(KEY_RECOVERY_SALT, recoverySalt.toBase64())
            .putString(KEY_RECOVERY_HASH, recoveryHash.toBase64())
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .putBoolean(KEY_IS_LOCKED, false)
            .apply()
        Result.success(recoveryCode)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun clearPin(): Result<Unit> = try {
        prefs.edit()
            .remove(KEY_PIN_SALT)
            .remove(KEY_PIN_HASH)
            .remove(KEY_PIN_LENGTH)
            .remove(KEY_RECOVERY_SALT)
            .remove(KEY_RECOVERY_HASH)
            .putBoolean(KEY_BIOMETRIC, false)
            .putBoolean(KEY_IS_LOCKED, false)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .apply()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun isBiometricAvailable(): Result<Boolean> = try {
        val canAuthenticate = BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
        Result.success(canAuthenticate)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun isBiometricEnabled(): Result<Boolean> = try {
        Result.success(prefs.getBoolean(KEY_BIOMETRIC, false))
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun authenticateWithBiometric(): Result<Boolean> =
        Result.error("Biometric authentication must be called from UI thread")

    override suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit> = try {
        prefs.edit().putBoolean(KEY_BIOMETRIC, enabled).apply()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun getFailedAttempts(): Result<Int> = try {
        Result.success(prefs.getInt(KEY_FAILED_ATTEMPTS, 0))
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun incrementFailedAttempts(): Result<Unit> = try {
        val attempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
        val editor = prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, attempts)
            .putLong(KEY_LAST_FAILED_AT, now())
        if (attempts % MAX_ATTEMPTS == 0) {
            val level = attempts / MAX_ATTEMPTS
            editor.putLong(KEY_LOCKOUT_UNTIL, now() + lockoutMillisFor(level))
        }
        editor.apply()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun resetFailedAttempts(): Result<Unit> = try {
        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .apply()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun getLockoutRemainingMillis(): Result<Long> = try {
        val remaining = (prefs.getLong(KEY_LOCKOUT_UNTIL, 0L) - now()).coerceAtLeast(0L)
        Result.success(remaining)
    } catch (e: Exception) {
        Result.error(e)
    }

    companion object {
        private const val PREFS_NAME = "cuentasclaras_lock"
        private const val KEY_IS_LOCKED = "is_locked"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_LENGTH = "pin_length"
        private const val KEY_RECOVERY_SALT = "recovery_salt"
        private const val KEY_RECOVERY_HASH = "recovery_hash"
        private const val KEY_BIOMETRIC = "biometric_enabled"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LAST_FAILED_AT = "last_failed_at"
        private const val KEY_LOCKOUT_UNTIL = "lockout_until"

        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 120_000
        private const val KEY_LENGTH_BITS = 256
        private const val SALT_BYTES = 16
        private const val MAX_ATTEMPTS = 5
        private const val LOCKOUT_MILLIS_LEVEL_1 = 30_000L
        private const val LOCKOUT_MILLIS_LEVEL_2 = 5 * 60_000L
        private const val LOCKOUT_MILLIS_LEVEL_3 = 30 * 60_000L

        // Alfabeto sin caracteres ambiguos (sin 0/O, 1/I/L) para el código de recuperación.
        private const val RECOVERY_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
        private const val RECOVERY_CODE_LENGTH = 12
    }
}

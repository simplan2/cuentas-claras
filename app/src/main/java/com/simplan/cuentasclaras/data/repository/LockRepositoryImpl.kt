package com.simplan.cuentasclaras.data.repository

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.data.dao.LockDao
import com.simplan.cuentasclaras.data.entity.LockEntity
import com.simplan.cuentasclaras.domain.repository.LockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

class LockRepositoryImpl(
    private val lockDao: LockDao,
    private val context: Context
) : LockRepository {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val keyAlias = "cuentas_claras_pin_key"
    private val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

    init {
        initKey()
    }

    private fun initKey() {
        if (!keyStore.containsAlias(keyAlias)) {
            keyGenerator.init(KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .build())
            keyGenerator.generateKey()
        }
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun encryptPin(pin: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = keyStore.getKey(keyAlias, null) as javax.crypto.SecretKey
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.doFinal(pin.toByteArray())
        val iv = cipher.iv
        return (iv + encrypted).joinToString("") { "%02x".format(it) }
    }

    private fun decryptPin(encryptedPin: String): String? {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val key = keyStore.getKey(keyAlias, null) as javax.crypto.SecretKey
            val bytes = encryptedPin.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            val iv = bytes.copyOfRange(0, 12)
            val encrypted = bytes.copyOfRange(12, bytes.size)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            String(cipher.doFinal(encrypted))
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun isLocked(): Result<Boolean> = try {
        val entity = lockDao.getSuspend()
        Result.success(entity?.isLocked ?: false)
    } catch (e: Exception) {
        Result.error(e)
    }

    override fun observeLockState(): Flow<Result<Boolean>> = lockDao.get()
        .map { Result.success(it?.isLocked ?: false) }
        .catch { emit(Result.error(it)) }

    override suspend fun hasPin(): Result<Boolean> = try {
        val entity = lockDao.getSuspend()
        Result.success(entity?.pinHash?.isNotEmpty() == true)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun getPinLength(): Result<Int> = try {
        val entity = lockDao.getSuspend()
        val encryptedPin = entity?.pinHash.orEmpty()
        if (encryptedPin.isEmpty()) {
            Result.success(0)
        } else {
            val pin = decryptPin(encryptedPin)
            Result.success(pin?.length ?: 0)
        }
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun lock(): Result<Unit> = try {
        val entity = lockDao.getSuspend() ?: LockEntity()
        lockDao.insert(entity.copy(isLocked = true))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun unlock(pin: String): Result<Boolean> = try {
        val entity = lockDao.getSuspend() ?: LockEntity()
        val storedHash = entity.pinHash
        val isValid = if (storedHash.isEmpty()) {
            false
        } else {
            val decryptedPin = decryptPin(storedHash)
            decryptedPin == pin
        }
        if (isValid) {
            lockDao.insert(entity.copy(isLocked = false, failedAttempts = 0))
        } else {
            val newAttempts = entity.failedAttempts + 1
            lockDao.insert(entity.copy(failedAttempts = newAttempts, lastFailedAt = System.currentTimeMillis()))
        }
        Result.success(isValid)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun verifyPin(pin: String): Result<Boolean> = unlock(pin)

    override suspend fun setPin(pin: String): Result<Unit> = try {
        val entity = lockDao.getSuspend() ?: LockEntity()
        val encryptedPin = encryptPin(pin)
        lockDao.insert(entity.copy(pinHash = encryptedPin))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun clearPin(): Result<Unit> = try {
        val entity = lockDao.getSuspend() ?: LockEntity()
        lockDao.update(
            entity.copy(
                pinHash = "",
                biometricEnabled = false,
                isLocked = false,
                failedAttempts = 0
            )
        )
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun isBiometricAvailable(): Result<Boolean> = try {
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
        Result.success(canAuthenticate)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun isBiometricEnabled(): Result<Boolean> = try {
        val entity = lockDao.getSuspend()
        Result.success(entity?.biometricEnabled == true)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun authenticateWithBiometric(): Result<Boolean> = Result.error("Biometric authentication must be called from UI thread")

    override suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit> = try {
        val entity = lockDao.getSuspend() ?: LockEntity()
        lockDao.insert(entity.copy(biometricEnabled = enabled))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun getAutoLockDelay(): Result<Int> = try {
        val entity = lockDao.getSuspend() ?: LockEntity()
        Result.success(entity.autoLockDelayMinutes)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun setAutoLockDelay(minutes: Int): Result<Unit> = try {
        val entity = lockDao.getSuspend() ?: LockEntity()
        lockDao.insert(entity.copy(autoLockDelayMinutes = minutes))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun getFailedAttempts(): Result<Int> = try {
        val entity = lockDao.getSuspend() ?: LockEntity()
        Result.success(entity.failedAttempts)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun incrementFailedAttempts(): Result<Unit> = try {
        val entity = lockDao.getSuspend() ?: LockEntity()
        lockDao.insert(entity.copy(failedAttempts = entity.failedAttempts + 1, lastFailedAt = System.currentTimeMillis()))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun resetFailedAttempts(): Result<Unit> = try {
        val entity = lockDao.getSuspend() ?: LockEntity()
        lockDao.insert(entity.copy(failedAttempts = 0))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }
}
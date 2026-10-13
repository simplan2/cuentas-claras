package com.simplan.cuentasclaras.domain.repository

import com.simplan.cuentasclaras.common.Result
import kotlinx.coroutines.flow.Flow

interface LockRepository {
    suspend fun isLocked(): Result<Boolean>
    fun observeLockState(): Flow<Result<Boolean>>
    suspend fun hasPin(): Result<Boolean>
    suspend fun getPinLength(): Result<Int>
    suspend fun lock(): Result<Unit>
    suspend fun unlock(pin: String): Result<Boolean>
    suspend fun verifyPin(pin: String): Result<Boolean>
    suspend fun setPin(pin: String): Result<String>
    suspend fun clearPin(): Result<Unit>
    suspend fun verifyRecoveryCode(code: String): Result<Boolean>
    suspend fun isBiometricAvailable(): Result<Boolean>
    suspend fun isBiometricEnabled(): Result<Boolean>
    suspend fun authenticateWithBiometric(): Result<Boolean>
    suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit>
    suspend fun getFailedAttempts(): Result<Int>
    suspend fun incrementFailedAttempts(): Result<Unit>
    suspend fun resetFailedAttempts(): Result<Unit>
    suspend fun getLockoutRemainingMillis(): Result<Long>
}

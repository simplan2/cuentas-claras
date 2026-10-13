package com.simplan.cuentasclaras.domain.usecase.lock

import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.repository.LockRepository
import kotlinx.coroutines.flow.Flow

sealed interface IsLockedUseCase {
    suspend operator fun invoke(): Result<Boolean>
    fun observe(): Flow<Result<Boolean>>
}

sealed interface HasPinUseCase {
    suspend operator fun invoke(): Result<Boolean>
}

sealed interface GetPinLengthUseCase {
    suspend operator fun invoke(): Result<Int>
}

sealed interface LockUseCase {
    suspend operator fun invoke(): Result<Unit>
}

sealed interface UnlockWithPinUseCase {
    suspend operator fun invoke(pin: String): Result<Boolean>
}

sealed interface SetPinUseCase {
    suspend operator fun invoke(pin: String): Result<String>
}

sealed interface ClearPinUseCase {
    suspend operator fun invoke(): Result<Unit>
}

sealed interface VerifyPinUseCase {
    suspend operator fun invoke(pin: String): Result<Boolean>
}

sealed interface VerifyRecoveryCodeUseCase {
    suspend operator fun invoke(code: String): Result<Boolean>
}

sealed interface IsBiometricAvailableUseCase {
    suspend operator fun invoke(): Result<Boolean>
}

sealed interface IsBiometricEnabledUseCase {
    suspend operator fun invoke(): Result<Boolean>
}

sealed interface AuthenticateWithBiometricUseCase {
    suspend operator fun invoke(): Result<Boolean>
}

sealed interface SetBiometricEnabledUseCase {
    suspend operator fun invoke(enabled: Boolean): Result<Unit>
}

sealed interface GetFailedAttemptsUseCase {
    suspend operator fun invoke(): Result<Int>
}

sealed interface ResetFailedAttemptsUseCase {
    suspend operator fun invoke(): Result<Unit>
}

sealed interface GetLockoutRemainingUseCase {
    suspend operator fun invoke(): Result<Long>
}

interface LockUseCases {
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
    suspend fun resetFailedAttempts(): Result<Unit>
    suspend fun getLockoutRemainingMillis(): Result<Long>
}

class IsLockedUseCaseImpl(private val repository: LockRepository) : IsLockedUseCase {
    override suspend fun invoke(): Result<Boolean> = repository.isLocked()
    override fun observe(): Flow<Result<Boolean>> = repository.observeLockState()
}

class HasPinUseCaseImpl(private val repository: LockRepository) : HasPinUseCase {
    override suspend fun invoke(): Result<Boolean> = repository.hasPin()
}

class GetPinLengthUseCaseImpl(private val repository: LockRepository) : GetPinLengthUseCase {
    override suspend fun invoke(): Result<Int> = repository.getPinLength()
}

class LockUseCaseImpl(private val repository: LockRepository) : LockUseCase {
    override suspend fun invoke(): Result<Unit> = repository.lock()
}

class UnlockWithPinUseCaseImpl(private val repository: LockRepository) : UnlockWithPinUseCase {
    override suspend fun invoke(pin: String): Result<Boolean> = repository.unlock(pin)
}

class SetPinUseCaseImpl(private val repository: LockRepository) : SetPinUseCase {
    override suspend fun invoke(pin: String): Result<String> = repository.setPin(pin)
}

class ClearPinUseCaseImpl(private val repository: LockRepository) : ClearPinUseCase {
    override suspend fun invoke(): Result<Unit> = repository.clearPin()
}

class VerifyPinUseCaseImpl(private val repository: LockRepository) : VerifyPinUseCase {
    override suspend fun invoke(pin: String): Result<Boolean> = repository.verifyPin(pin)
}

class VerifyRecoveryCodeUseCaseImpl(private val repository: LockRepository) : VerifyRecoveryCodeUseCase {
    override suspend fun invoke(code: String): Result<Boolean> = repository.verifyRecoveryCode(code)
}

class IsBiometricAvailableUseCaseImpl(private val repository: LockRepository) : IsBiometricAvailableUseCase {
    override suspend fun invoke(): Result<Boolean> = repository.isBiometricAvailable()
}

class IsBiometricEnabledUseCaseImpl(private val repository: LockRepository) : IsBiometricEnabledUseCase {
    override suspend fun invoke(): Result<Boolean> = repository.isBiometricEnabled()
}

class AuthenticateWithBiometricUseCaseImpl(private val repository: LockRepository) : AuthenticateWithBiometricUseCase {
    override suspend fun invoke(): Result<Boolean> = repository.authenticateWithBiometric()
}

class SetBiometricEnabledUseCaseImpl(private val repository: LockRepository) : SetBiometricEnabledUseCase {
    override suspend fun invoke(enabled: Boolean): Result<Unit> = repository.setBiometricEnabled(enabled)
}

class GetFailedAttemptsUseCaseImpl(private val repository: LockRepository) : GetFailedAttemptsUseCase {
    override suspend fun invoke(): Result<Int> = repository.getFailedAttempts()
}

class ResetFailedAttemptsUseCaseImpl(private val repository: LockRepository) : ResetFailedAttemptsUseCase {
    override suspend fun invoke(): Result<Unit> = repository.resetFailedAttempts()
}

class GetLockoutRemainingUseCaseImpl(private val repository: LockRepository) : GetLockoutRemainingUseCase {
    override suspend fun invoke(): Result<Long> = repository.getLockoutRemainingMillis()
}

class LockUseCasesImpl(
    private val isLockedUseCase: IsLockedUseCase,
    private val hasPinUseCase: HasPinUseCase,
    private val getPinLengthUseCase: GetPinLengthUseCase,
    private val lockUseCase: LockUseCase,
    private val unlockWithPinUseCase: UnlockWithPinUseCase,
    private val setPinUseCase: SetPinUseCase,
    private val clearPinUseCase: ClearPinUseCase,
    private val verifyPinUseCase: VerifyPinUseCase,
    private val verifyRecoveryCodeUseCase: VerifyRecoveryCodeUseCase,
    private val isBiometricAvailableUseCase: IsBiometricAvailableUseCase,
    private val isBiometricEnabledUseCase: IsBiometricEnabledUseCase,
    private val authenticateWithBiometricUseCase: AuthenticateWithBiometricUseCase,
    private val setBiometricEnabledUseCase: SetBiometricEnabledUseCase,
    private val getFailedAttemptsUseCase: GetFailedAttemptsUseCase,
    private val resetFailedAttemptsUseCase: ResetFailedAttemptsUseCase,
    private val getLockoutRemainingUseCase: GetLockoutRemainingUseCase
) : LockUseCases {

    override suspend fun isLocked(): Result<Boolean> = isLockedUseCase()
    override fun observeLockState(): Flow<Result<Boolean>> = isLockedUseCase.observe()
    override suspend fun hasPin(): Result<Boolean> = hasPinUseCase()
    override suspend fun getPinLength(): Result<Int> = getPinLengthUseCase()
    override suspend fun lock(): Result<Unit> = lockUseCase()
    override suspend fun unlock(pin: String): Result<Boolean> = unlockWithPinUseCase(pin)
    override suspend fun verifyPin(pin: String): Result<Boolean> = verifyPinUseCase(pin)
    override suspend fun setPin(pin: String): Result<String> = setPinUseCase(pin)
    override suspend fun clearPin(): Result<Unit> = clearPinUseCase()
    override suspend fun verifyRecoveryCode(code: String): Result<Boolean> = verifyRecoveryCodeUseCase(code)
    override suspend fun isBiometricAvailable(): Result<Boolean> = isBiometricAvailableUseCase()
    override suspend fun isBiometricEnabled(): Result<Boolean> = isBiometricEnabledUseCase()
    override suspend fun authenticateWithBiometric(): Result<Boolean> = authenticateWithBiometricUseCase()
    override suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit> = setBiometricEnabledUseCase(enabled)
    override suspend fun getFailedAttempts(): Result<Int> = getFailedAttemptsUseCase()
    override suspend fun resetFailedAttempts(): Result<Unit> = resetFailedAttemptsUseCase()
    override suspend fun getLockoutRemainingMillis(): Result<Long> = getLockoutRemainingUseCase()
}

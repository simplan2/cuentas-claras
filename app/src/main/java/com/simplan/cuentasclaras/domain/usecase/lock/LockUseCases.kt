package com.simplan.cuentasclaras.domain.usecase.lock

import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.repository.LockRepository
import kotlinx.coroutines.flow.Flow

sealed interface IsLockedUseCase {
    suspend operator fun invoke(): Result<Boolean>
    fun observe(): Flow<Result<Boolean>>
}

sealed interface LockUseCase {
    suspend operator fun invoke(): Result<Unit>
}

sealed interface UnlockWithPinUseCase {
    suspend operator fun invoke(pin: String): Result<Boolean>
}

sealed interface SetPinUseCase {
    suspend operator fun invoke(pin: String): Result<Unit>
}

sealed interface VerifyPinUseCase {
    suspend operator fun invoke(pin: String): Result<Boolean>
}

sealed interface IsBiometricAvailableUseCase {
    suspend operator fun invoke(): Result<Boolean>
}

sealed interface AuthenticateWithBiometricUseCase {
    suspend operator fun invoke(): Result<Boolean>
}

sealed interface SetBiometricEnabledUseCase {
    suspend operator fun invoke(enabled: Boolean): Result<Unit>
}

sealed interface GetAutoLockDelayUseCase {
    suspend operator fun invoke(): Result<Int>
}

sealed interface SetAutoLockDelayUseCase {
    suspend operator fun invoke(minutes: Int): Result<Unit>
}

interface LockUseCases {
    suspend fun isLocked(): Result<Boolean>
    fun observeLockState(): Flow<Result<Boolean>>
    suspend fun lock(): Result<Unit>
    suspend fun unlock(pin: String): Result<Boolean>
    suspend fun verifyPin(pin: String): Result<Boolean>
    suspend fun setPin(pin: String): Result<Unit>
    suspend fun clearPin(): Result<Unit>
    suspend fun isBiometricAvailable(): Result<Boolean>
    suspend fun authenticateWithBiometric(): Result<Boolean>
    suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit>
    suspend fun getAutoLockDelay(): Result<Int>
    suspend fun setAutoLockDelay(minutes: Int): Result<Unit>
    suspend fun getFailedAttempts(): Result<Int>
    suspend fun incrementFailedAttempts(): Result<Unit>
    suspend fun resetFailedAttempts(): Result<Unit>
}

class IsLockedUseCaseImpl(private val repository: LockRepository) : IsLockedUseCase {
    override suspend fun invoke(): Result<Boolean> = repository.isLocked()
    override fun observe(): Flow<Result<Boolean>> = repository.observeLockState()
}

class LockUseCaseImpl(private val repository: LockRepository) : LockUseCase {
    override suspend fun invoke(): Result<Unit> = repository.lock()
}

class UnlockWithPinUseCaseImpl(private val repository: LockRepository) : UnlockWithPinUseCase {
    override suspend fun invoke(pin: String): Result<Boolean> = repository.unlock(pin)
}

class SetPinUseCaseImpl(private val repository: LockRepository) : SetPinUseCase {
    override suspend fun invoke(pin: String): Result<Unit> = repository.setPin(pin)
}

class VerifyPinUseCaseImpl(private val repository: LockRepository) : VerifyPinUseCase {
    override suspend fun invoke(pin: String): Result<Boolean> = repository.verifyPin(pin)
}

class IsBiometricAvailableUseCaseImpl(private val repository: LockRepository) : IsBiometricAvailableUseCase {
    override suspend fun invoke(): Result<Boolean> = repository.isBiometricAvailable()
}

class AuthenticateWithBiometricUseCaseImpl(private val repository: LockRepository) : AuthenticateWithBiometricUseCase {
    override suspend fun invoke(): Result<Boolean> = repository.authenticateWithBiometric()
}

class SetBiometricEnabledUseCaseImpl(private val repository: LockRepository) : SetBiometricEnabledUseCase {
    override suspend fun invoke(enabled: Boolean): Result<Unit> = repository.setBiometricEnabled(enabled)
}

class GetAutoLockDelayUseCaseImpl(private val repository: LockRepository) : GetAutoLockDelayUseCase {
    override suspend fun invoke(): Result<Int> = repository.getAutoLockDelay()
}

class SetAutoLockDelayUseCaseImpl(private val repository: LockRepository) : SetAutoLockDelayUseCase {
    override suspend fun invoke(minutes: Int): Result<Unit> = repository.setAutoLockDelay(minutes)
}

class LockUseCasesImpl(
    private val isLockedUseCase: IsLockedUseCase,
    private val lockUseCase: LockUseCase,
    private val unlockWithPinUseCase: UnlockWithPinUseCase,
    private val setPinUseCase: SetPinUseCase,
    private val verifyPinUseCase: VerifyPinUseCase,
    private val isBiometricAvailableUseCase: IsBiometricAvailableUseCase,
    private val authenticateWithBiometricUseCase: AuthenticateWithBiometricUseCase,
    private val setBiometricEnabledUseCase: SetBiometricEnabledUseCase,
    private val getAutoLockDelayUseCase: GetAutoLockDelayUseCase,
    private val setAutoLockDelayUseCase: SetAutoLockDelayUseCase
) : LockUseCases {

    override suspend fun isLocked(): Result<Boolean> = isLockedUseCase()
    override fun observeLockState(): Flow<Result<Boolean>> = isLockedUseCase.observe()
    override suspend fun lock(): Result<Unit> = lockUseCase()
    override suspend fun unlock(pin: String): Result<Boolean> = unlockWithPinUseCase(pin)
    override suspend fun verifyPin(pin: String): Result<Boolean> = verifyPinUseCase(pin)
    override suspend fun setPin(pin: String): Result<Unit> = setPinUseCase(pin)
    override suspend fun clearPin(): Result<Unit> = setPinUseCase("")
    override suspend fun isBiometricAvailable(): Result<Boolean> = isBiometricAvailableUseCase()
    override suspend fun authenticateWithBiometric(): Result<Boolean> = authenticateWithBiometricUseCase()
    override suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit> = setBiometricEnabledUseCase(enabled)
    override suspend fun getAutoLockDelay(): Result<Int> = getAutoLockDelayUseCase()
    override suspend fun setAutoLockDelay(minutes: Int): Result<Unit> = setAutoLockDelayUseCase(minutes)
    override suspend fun getFailedAttempts(): Result<Int> = Result.error("Not implemented")
    override suspend fun incrementFailedAttempts(): Result<Unit> = Result.error("Not implemented")
    override suspend fun resetFailedAttempts(): Result<Unit> = Result.error("Not implemented")
}
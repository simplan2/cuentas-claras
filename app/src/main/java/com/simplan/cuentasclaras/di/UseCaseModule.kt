package com.simplan.cuentasclaras.di

import com.simplan.cuentasclaras.domain.repository.AccountRepository
import com.simplan.cuentasclaras.domain.repository.BackupRepository
import com.simplan.cuentasclaras.domain.repository.EntryRepository
import com.simplan.cuentasclaras.domain.repository.LockRepository
import com.simplan.cuentasclaras.domain.repository.QuickAmountRepository
import com.simplan.cuentasclaras.domain.repository.SettingsRepository
import com.simplan.cuentasclaras.domain.usecase.account.AccountUseCases
import com.simplan.cuentasclaras.domain.usecase.account.AccountUseCasesImpl
import com.simplan.cuentasclaras.domain.usecase.entry.EntryUseCases
import com.simplan.cuentasclaras.domain.usecase.entry.EntryUseCasesImpl
import com.simplan.cuentasclaras.domain.usecase.lock.IsLockedUseCase
import com.simplan.cuentasclaras.domain.usecase.lock.IsLockedUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.lock.HasPinUseCase
import com.simplan.cuentasclaras.domain.usecase.lock.HasPinUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.lock.GetPinLengthUseCase
import com.simplan.cuentasclaras.domain.usecase.lock.GetPinLengthUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.lock.ClearPinUseCase
import com.simplan.cuentasclaras.domain.usecase.lock.ClearPinUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.lock.LockUseCase
import com.simplan.cuentasclaras.domain.usecase.lock.LockUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.lock.LockUseCases
import com.simplan.cuentasclaras.domain.usecase.lock.LockUseCasesImpl
import com.simplan.cuentasclaras.domain.usecase.lock.SetBiometricEnabledUseCase
import com.simplan.cuentasclaras.domain.usecase.lock.SetBiometricEnabledUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.lock.SetPinUseCase
import com.simplan.cuentasclaras.domain.usecase.lock.SetPinUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.lock.UnlockWithPinUseCase
import com.simplan.cuentasclaras.domain.usecase.lock.UnlockWithPinUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.lock.VerifyPinUseCase
import com.simplan.cuentasclaras.domain.usecase.lock.VerifyPinUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.lock.VerifyRecoveryCodeUseCase
import com.simplan.cuentasclaras.domain.usecase.lock.VerifyRecoveryCodeUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.lock.GetFailedAttemptsUseCase
import com.simplan.cuentasclaras.domain.usecase.lock.GetFailedAttemptsUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.lock.ResetFailedAttemptsUseCase
import com.simplan.cuentasclaras.domain.usecase.lock.ResetFailedAttemptsUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.lock.GetLockoutRemainingUseCase
import com.simplan.cuentasclaras.domain.usecase.lock.GetLockoutRemainingUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.lock.IsBiometricAvailableUseCase
import com.simplan.cuentasclaras.domain.usecase.lock.IsBiometricAvailableUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.lock.IsBiometricEnabledUseCase
import com.simplan.cuentasclaras.domain.usecase.lock.IsBiometricEnabledUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.lock.AuthenticateWithBiometricUseCase
import com.simplan.cuentasclaras.domain.usecase.lock.AuthenticateWithBiometricUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.quickamount.GetQuickAmountsUseCase
import com.simplan.cuentasclaras.domain.usecase.quickamount.GetQuickAmountsUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.quickamount.GetEnabledQuickAmountsUseCase
import com.simplan.cuentasclaras.domain.usecase.quickamount.GetEnabledQuickAmountsUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.quickamount.GetQuickAmountUseCase
import com.simplan.cuentasclaras.domain.usecase.quickamount.GetQuickAmountUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.quickamount.CreateQuickAmountUseCase
import com.simplan.cuentasclaras.domain.usecase.quickamount.CreateQuickAmountUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.quickamount.UpdateQuickAmountUseCase
import com.simplan.cuentasclaras.domain.usecase.quickamount.UpdateQuickAmountUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.quickamount.DeleteQuickAmountUseCase
import com.simplan.cuentasclaras.domain.usecase.quickamount.DeleteQuickAmountUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.quickamount.ReorderQuickAmountsUseCase
import com.simplan.cuentasclaras.domain.usecase.quickamount.ReorderQuickAmountsUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.quickamount.QuickAmountUseCases
import com.simplan.cuentasclaras.domain.usecase.quickamount.QuickAmountUseCasesImpl
import com.simplan.cuentasclaras.domain.usecase.settings.SettingsUseCases
import com.simplan.cuentasclaras.domain.usecase.settings.SettingsUseCasesImpl
import com.simplan.cuentasclaras.domain.usecase.backup.BackupUseCases
import com.simplan.cuentasclaras.domain.usecase.backup.BackupUseCasesImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideAccountUseCases(
        accountRepository: AccountRepository,
        settingsRepository: SettingsRepository
    ): AccountUseCases = AccountUseCasesImpl(accountRepository, settingsRepository)

    @Provides
    @Singleton
    fun provideEntryUseCases(entryRepository: EntryRepository): EntryUseCases =
        EntryUseCasesImpl(entryRepository)

    @Provides
    @Singleton
    fun provideSettingsUseCases(settingsRepository: SettingsRepository): SettingsUseCases =
        SettingsUseCasesImpl(settingsRepository)

    @Provides
    @Singleton
    fun provideBackupUseCases(backupRepository: BackupRepository): BackupUseCases =
        BackupUseCasesImpl(backupRepository)

    @Provides
    @Singleton
    fun provideGetQuickAmountsUseCase(quickAmountRepository: QuickAmountRepository): GetQuickAmountsUseCase =
        GetQuickAmountsUseCaseImpl(quickAmountRepository)

    @Provides
    @Singleton
    fun provideGetEnabledQuickAmountsUseCase(quickAmountRepository: QuickAmountRepository): GetEnabledQuickAmountsUseCase =
        GetEnabledQuickAmountsUseCaseImpl(quickAmountRepository)

    @Provides
    @Singleton
    fun provideGetQuickAmountUseCase(quickAmountRepository: QuickAmountRepository): GetQuickAmountUseCase =
        GetQuickAmountUseCaseImpl(quickAmountRepository)

    @Provides
    @Singleton
    fun provideCreateQuickAmountUseCase(quickAmountRepository: QuickAmountRepository): CreateQuickAmountUseCase =
        CreateQuickAmountUseCaseImpl(quickAmountRepository)

    @Provides
    @Singleton
    fun provideUpdateQuickAmountUseCase(quickAmountRepository: QuickAmountRepository): UpdateQuickAmountUseCase =
        UpdateQuickAmountUseCaseImpl(quickAmountRepository)

    @Provides
    @Singleton
    fun provideDeleteQuickAmountUseCase(quickAmountRepository: QuickAmountRepository): DeleteQuickAmountUseCase =
        DeleteQuickAmountUseCaseImpl(quickAmountRepository)

    @Provides
    @Singleton
    fun provideReorderQuickAmountsUseCase(quickAmountRepository: QuickAmountRepository): ReorderQuickAmountsUseCase =
        ReorderQuickAmountsUseCaseImpl(quickAmountRepository)

    @Provides
    @Singleton
    fun provideQuickAmountUseCases(
        getQuickAmountsUseCase: GetQuickAmountsUseCase,
        getEnabledQuickAmountsUseCase: GetEnabledQuickAmountsUseCase,
        getQuickAmountUseCase: GetQuickAmountUseCase,
        createQuickAmountUseCase: CreateQuickAmountUseCase,
        updateQuickAmountUseCase: UpdateQuickAmountUseCase,
        deleteQuickAmountUseCase: DeleteQuickAmountUseCase,
        reorderQuickAmountsUseCase: ReorderQuickAmountsUseCase
    ): QuickAmountUseCases = QuickAmountUseCasesImpl(
        getQuickAmountsUseCase,
        getEnabledQuickAmountsUseCase,
        getQuickAmountUseCase,
        createQuickAmountUseCase,
        updateQuickAmountUseCase,
        deleteQuickAmountUseCase,
        reorderQuickAmountsUseCase
    )

    @Provides
    @Singleton
    fun provideIsLockedUseCase(lockRepository: LockRepository): IsLockedUseCase =
        IsLockedUseCaseImpl(lockRepository)

    @Provides
    @Singleton
    fun provideHasPinUseCase(lockRepository: LockRepository): HasPinUseCase =
        HasPinUseCaseImpl(lockRepository)

    @Provides
    @Singleton
    fun provideGetPinLengthUseCase(lockRepository: LockRepository): GetPinLengthUseCase =
        GetPinLengthUseCaseImpl(lockRepository)

    @Provides
    @Singleton
    fun provideClearPinUseCase(lockRepository: LockRepository): ClearPinUseCase =
        ClearPinUseCaseImpl(lockRepository)

    @Provides
    @Singleton
    fun provideLockUseCase(lockRepository: LockRepository): LockUseCase =
        LockUseCaseImpl(lockRepository)

    @Provides
    @Singleton
    fun provideUnlockWithPinUseCase(lockRepository: LockRepository): UnlockWithPinUseCase =
        UnlockWithPinUseCaseImpl(lockRepository)

    @Provides
    @Singleton
    fun provideSetPinUseCase(lockRepository: LockRepository): SetPinUseCase =
        SetPinUseCaseImpl(lockRepository)

    @Provides
    @Singleton
    fun provideVerifyPinUseCase(lockRepository: LockRepository): VerifyPinUseCase =
        VerifyPinUseCaseImpl(lockRepository)

    @Provides
    @Singleton
    fun provideVerifyRecoveryCodeUseCase(lockRepository: LockRepository): VerifyRecoveryCodeUseCase =
        VerifyRecoveryCodeUseCaseImpl(lockRepository)

    @Provides
    @Singleton
    fun provideIsBiometricAvailableUseCase(lockRepository: LockRepository): IsBiometricAvailableUseCase =
        IsBiometricAvailableUseCaseImpl(lockRepository)

    @Provides
    @Singleton
    fun provideIsBiometricEnabledUseCase(lockRepository: LockRepository): IsBiometricEnabledUseCase =
        IsBiometricEnabledUseCaseImpl(lockRepository)

    @Provides
    @Singleton
    fun provideAuthenticateWithBiometricUseCase(lockRepository: LockRepository): AuthenticateWithBiometricUseCase =
        AuthenticateWithBiometricUseCaseImpl(lockRepository)

    @Provides
    @Singleton
    fun provideSetBiometricEnabledUseCase(lockRepository: LockRepository): SetBiometricEnabledUseCase =
        SetBiometricEnabledUseCaseImpl(lockRepository)

    @Provides
    @Singleton
    fun provideGetFailedAttemptsUseCase(lockRepository: LockRepository): GetFailedAttemptsUseCase =
        GetFailedAttemptsUseCaseImpl(lockRepository)

    @Provides
    @Singleton
    fun provideResetFailedAttemptsUseCase(lockRepository: LockRepository): ResetFailedAttemptsUseCase =
        ResetFailedAttemptsUseCaseImpl(lockRepository)

    @Provides
    @Singleton
    fun provideGetLockoutRemainingUseCase(lockRepository: LockRepository): GetLockoutRemainingUseCase =
        GetLockoutRemainingUseCaseImpl(lockRepository)

    @Provides
    @Singleton
    fun provideLockUseCases(
        isLockedUseCase: IsLockedUseCase,
        hasPinUseCase: HasPinUseCase,
        getPinLengthUseCase: GetPinLengthUseCase,
        lockUseCase: LockUseCase,
        unlockWithPinUseCase: UnlockWithPinUseCase,
        setPinUseCase: SetPinUseCase,
        clearPinUseCase: ClearPinUseCase,
        verifyPinUseCase: VerifyPinUseCase,
        verifyRecoveryCodeUseCase: VerifyRecoveryCodeUseCase,
        isBiometricAvailableUseCase: IsBiometricAvailableUseCase,
        isBiometricEnabledUseCase: IsBiometricEnabledUseCase,
        authenticateWithBiometricUseCase: AuthenticateWithBiometricUseCase,
        setBiometricEnabledUseCase: SetBiometricEnabledUseCase,
        getFailedAttemptsUseCase: GetFailedAttemptsUseCase,
        resetFailedAttemptsUseCase: ResetFailedAttemptsUseCase,
        getLockoutRemainingUseCase: GetLockoutRemainingUseCase
    ): LockUseCases = LockUseCasesImpl(
        isLockedUseCase,
        hasPinUseCase,
        getPinLengthUseCase,
        lockUseCase,
        unlockWithPinUseCase,
        setPinUseCase,
        clearPinUseCase,
        verifyPinUseCase,
        verifyRecoveryCodeUseCase,
        isBiometricAvailableUseCase,
        isBiometricEnabledUseCase,
        authenticateWithBiometricUseCase,
        setBiometricEnabledUseCase,
        getFailedAttemptsUseCase,
        resetFailedAttemptsUseCase,
        getLockoutRemainingUseCase
    )
}
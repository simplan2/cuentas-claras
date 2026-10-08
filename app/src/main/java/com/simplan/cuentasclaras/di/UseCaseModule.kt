package com.simplan.cuentasclaras.di

import com.simplan.cuentasclaras.domain.repository.AccountRepository
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
import com.simplan.cuentasclaras.domain.usecase.lock.GetAutoLockDelayUseCase
import com.simplan.cuentasclaras.domain.usecase.lock.GetAutoLockDelayUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.lock.SetAutoLockDelayUseCase
import com.simplan.cuentasclaras.domain.usecase.lock.SetAutoLockDelayUseCaseImpl
import com.simplan.cuentasclaras.domain.usecase.lock.IsBiometricAvailableUseCase
import com.simplan.cuentasclaras.domain.usecase.lock.IsBiometricAvailableUseCaseImpl
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
    fun provideIsBiometricAvailableUseCase(lockRepository: LockRepository): IsBiometricAvailableUseCase =
        IsBiometricAvailableUseCaseImpl(lockRepository)

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
    fun provideGetAutoLockDelayUseCase(lockRepository: LockRepository): GetAutoLockDelayUseCase =
        GetAutoLockDelayUseCaseImpl(lockRepository)

    @Provides
    @Singleton
    fun provideSetAutoLockDelayUseCase(lockRepository: LockRepository): SetAutoLockDelayUseCase =
        SetAutoLockDelayUseCaseImpl(lockRepository)

    @Provides
    @Singleton
    fun provideLockUseCases(
        isLockedUseCase: IsLockedUseCase,
        lockUseCase: LockUseCase,
        unlockWithPinUseCase: UnlockWithPinUseCase,
        setPinUseCase: SetPinUseCase,
        verifyPinUseCase: VerifyPinUseCase,
        isBiometricAvailableUseCase: IsBiometricAvailableUseCase,
        authenticateWithBiometricUseCase: AuthenticateWithBiometricUseCase,
        setBiometricEnabledUseCase: SetBiometricEnabledUseCase,
        getAutoLockDelayUseCase: GetAutoLockDelayUseCase,
        setAutoLockDelayUseCase: SetAutoLockDelayUseCase
    ): LockUseCases = LockUseCasesImpl(
        isLockedUseCase,
        lockUseCase,
        unlockWithPinUseCase,
        setPinUseCase,
        verifyPinUseCase,
        isBiometricAvailableUseCase,
        authenticateWithBiometricUseCase,
        setBiometricEnabledUseCase,
        getAutoLockDelayUseCase,
        setAutoLockDelayUseCase
    )
}
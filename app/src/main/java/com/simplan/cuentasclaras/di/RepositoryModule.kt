package com.simplan.cuentasclaras.di

import com.simplan.cuentasclaras.data.dao.AccountDao
import com.simplan.cuentasclaras.data.dao.EntryDao
import com.simplan.cuentasclaras.data.dao.QuickAmountDao
import com.simplan.cuentasclaras.data.dao.SettingsDao
import com.simplan.cuentasclaras.data.database.AppDatabase
import com.simplan.cuentasclaras.data.repository.AccountRepositoryImpl
import com.simplan.cuentasclaras.data.repository.BackupRepositoryImpl
import com.simplan.cuentasclaras.data.repository.EntryRepositoryImpl
import com.simplan.cuentasclaras.data.repository.LockRepositoryImpl
import com.simplan.cuentasclaras.data.repository.QuickAmountRepositoryImpl
import com.simplan.cuentasclaras.data.repository.SettingsRepositoryImpl
import com.simplan.cuentasclaras.domain.repository.AccountRepository
import com.simplan.cuentasclaras.domain.repository.BackupRepository
import com.simplan.cuentasclaras.domain.repository.EntryRepository
import com.simplan.cuentasclaras.domain.repository.LockRepository
import com.simplan.cuentasclaras.domain.repository.QuickAmountRepository
import com.simplan.cuentasclaras.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAccountDao(database: AppDatabase): AccountDao = database.accountDao()

    @Provides
    @Singleton
    fun provideEntryDao(database: AppDatabase): EntryDao = database.entryDao()

    @Provides
    @Singleton
    fun provideSettingsDao(database: AppDatabase): SettingsDao = database.settingsDao()

    @Provides
    @Singleton
    fun provideQuickAmountDao(database: AppDatabase): QuickAmountDao = database.quickAmountDao()

    @Provides
    @Singleton
    fun provideAccountRepository(dao: AccountDao): AccountRepository =
        AccountRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideEntryRepository(dao: EntryDao): EntryRepository =
        EntryRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideSettingsRepository(dao: SettingsDao): SettingsRepository =
        SettingsRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideQuickAmountRepository(dao: QuickAmountDao): QuickAmountRepository =
        QuickAmountRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideLockRepository(@ApplicationContext context: android.content.Context): LockRepository =
        LockRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideBackupRepository(
        database: AppDatabase,
        accountDao: AccountDao,
        entryDao: EntryDao,
        quickAmountDao: QuickAmountDao,
        settingsDao: SettingsDao
    ): BackupRepository = BackupRepositoryImpl(
        database,
        accountDao,
        entryDao,
        quickAmountDao,
        settingsDao
    )
}
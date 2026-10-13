package com.simplan.cuentasclaras.data.repository

import androidx.room.withTransaction
import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.data.dao.AccountDao
import com.simplan.cuentasclaras.data.dao.EntryDao
import com.simplan.cuentasclaras.data.dao.QuickAmountDao
import com.simplan.cuentasclaras.data.dao.SettingsDao
import com.simplan.cuentasclaras.data.database.AppDatabase
import com.simplan.cuentasclaras.data.entity.SettingsEntity
import com.simplan.cuentasclaras.data.mapper.toDomain
import com.simplan.cuentasclaras.data.mapper.toEntity
import com.simplan.cuentasclaras.domain.entity.BackupPayload
import com.simplan.cuentasclaras.domain.entity.Settings
import com.simplan.cuentasclaras.domain.repository.BackupRepository

class BackupRepositoryImpl(
    private val database: AppDatabase,
    private val accountDao: AccountDao,
    private val entryDao: EntryDao,
    private val quickAmountDao: QuickAmountDao,
    private val settingsDao: SettingsDao
) : BackupRepository {

    override suspend fun export(): Result<BackupPayload> = try {
        val accounts = accountDao.getAllSuspend().toDomain()
        val entries = entryDao.getAllSuspend().toDomain()
        val quickAmounts = quickAmountDao.getAllSuspend().toDomain()
        val settings = settingsDao.getSuspend()?.toDomain() ?: Settings()
        Result.success(
            BackupPayload(
                settings = settings,
                accounts = accounts,
                entries = entries,
                quickAmounts = quickAmounts
            )
        )
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun import(payload: BackupPayload): Result<Unit> = try {
        database.withTransaction {
            entryDao.deleteAll()
            accountDao.deleteAll()
            quickAmountDao.deleteAll()

            accountDao.insertAll(payload.accounts.toEntity())
            entryDao.insertAll(payload.entries.toEntity())
            quickAmountDao.insertAll(payload.quickAmounts.toEntity())
            settingsDao.insert(SettingsEntity.fromDomain(payload.settings))
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun wipe(): Result<Unit> = try {
        database.withTransaction {
            entryDao.deleteAll()
            accountDao.deleteAll()
            quickAmountDao.deleteAll()
            settingsDao.insert(SettingsEntity.fromDomain(Settings()))
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }
}

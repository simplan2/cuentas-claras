package com.simplan.cuentasclaras.data.repository

import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.data.dao.SettingsDao
import com.simplan.cuentasclaras.data.mapper.toDomain
import com.simplan.cuentasclaras.data.mapper.toEntity
import com.simplan.cuentasclaras.domain.entity.Settings
import com.simplan.cuentasclaras.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val settingsDao: SettingsDao
) : SettingsRepository {

    override suspend fun get(): Result<Settings> = try {
        val entity = settingsDao.getSuspend()
        Result.success(entity?.toDomain() ?: Settings())
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun save(settings: Settings): Result<Unit> = try {
        settingsDao.update(settings.toEntity())
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override fun observe(): Flow<Result<Settings>> = settingsDao.get()
        .map { Result.success(it?.toDomain() ?: Settings()) }
        .catch { emit(Result.error(it)) }
}
package com.simplan.cuentasclaras.domain.repository

import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun get(): Result<Settings>
    fun observe(): Flow<Result<Settings>>
    suspend fun save(settings: Settings): Result<Unit>
}
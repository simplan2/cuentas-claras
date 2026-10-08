package com.simplan.cuentasclaras.domain.usecase.settings

import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.Settings
import com.simplan.cuentasclaras.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

sealed interface GetSettingsUseCase {
    suspend operator fun invoke(): Result<Settings>
    fun observe(): Flow<Result<Settings>>
}

sealed interface SaveSettingsUseCase {
    suspend operator fun invoke(settings: Settings): Result<Unit>
}

interface SettingsUseCases {
    suspend fun getSettings(): Result<Settings>
    fun observeSettings(): Flow<Result<Settings>>
    suspend fun saveSettings(settings: Settings): Result<Unit>
}

class SettingsUseCasesImpl(
    private val repository: SettingsRepository
) : SettingsUseCases {

    override suspend fun getSettings(): Result<Settings> = repository.get()
    override fun observeSettings(): Flow<Result<Settings>> = repository.observe()
    override suspend fun saveSettings(settings: Settings): Result<Unit> = repository.save(settings)
}

class GetSettingsUseCaseImpl(private val repository: SettingsRepository) : GetSettingsUseCase {
    override suspend fun invoke(): Result<Settings> = repository.get()
    override fun observe(): Flow<Result<Settings>> = repository.observe()
}

class SaveSettingsUseCaseImpl(private val repository: SettingsRepository) : SaveSettingsUseCase {
    override suspend fun invoke(settings: Settings): Result<Unit> = repository.save(settings)
}
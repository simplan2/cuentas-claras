package com.simplan.cuentasclaras.domain.usecase.backup

import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.BackupPayload
import com.simplan.cuentasclaras.domain.repository.BackupRepository

interface BackupUseCases {
    suspend fun export(): Result<BackupPayload>
    suspend fun import(payload: BackupPayload): Result<Unit>
    suspend fun wipe(): Result<Unit>
}

class BackupUseCasesImpl(
    private val repository: BackupRepository
) : BackupUseCases {
    override suspend fun export(): Result<BackupPayload> = repository.export()
    override suspend fun import(payload: BackupPayload): Result<Unit> = repository.import(payload)
    override suspend fun wipe(): Result<Unit> = repository.wipe()
}

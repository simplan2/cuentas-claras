package com.simplan.cuentasclaras.domain.repository

import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.BackupPayload

interface BackupRepository {
    suspend fun export(): Result<BackupPayload>
    suspend fun import(payload: BackupPayload): Result<Unit>
    suspend fun wipe(): Result<Unit>
}

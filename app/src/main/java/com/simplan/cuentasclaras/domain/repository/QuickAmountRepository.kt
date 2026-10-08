package com.simplan.cuentasclaras.domain.repository

import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.QuickAmount
import kotlinx.coroutines.flow.Flow

interface QuickAmountRepository {
    suspend fun getAll(): Result<List<QuickAmount>>
    fun observeAll(): Flow<Result<List<QuickAmount>>>
    suspend fun getEnabled(): Result<List<QuickAmount>>
    fun observeEnabled(): Flow<Result<List<QuickAmount>>>
    suspend fun getById(id: Long): Result<QuickAmount>
    suspend fun insert(quickAmount: QuickAmount): Result<Long>
    suspend fun update(quickAmount: QuickAmount): Result<Unit>
    suspend fun delete(id: Long): Result<Unit>
    suspend fun reorder(quickAmountIds: List<Long>): Result<Unit>
}
package com.simplan.cuentasclaras.domain.repository

import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.Entry
import kotlinx.coroutines.flow.Flow

interface EntryRepository {
    suspend fun getByAccount(accountId: Long): Result<List<Entry>>
    fun observeByAccount(accountId: Long): Flow<Result<List<Entry>>>
    suspend fun getByAccountAndDateRange(accountId: Long, startDate: Long, endDate: Long): Result<List<Entry>>
    fun observeByAccountAndDateRange(accountId: Long, startDate: Long, endDate: Long): Flow<Result<List<Entry>>>
    suspend fun getByCategory(categoryId: Long): Result<List<Entry>>
    suspend fun getById(id: Long): Result<Entry>
    suspend fun getTotalByAccount(accountId: Long): Result<Long>
    suspend fun getTotalByAccountAndType(accountId: Long, type: Entry.EntryType): Result<Long>
    suspend fun getMonthlyTotals(accountId: Long, year: Int): Result<List<MonthlyTotal>>
    suspend fun insert(entry: Entry): Result<Long>
    suspend fun update(entry: Entry): Result<Unit>
    suspend fun delete(id: Long): Result<Unit>
    suspend fun deleteByAccount(accountId: Long): Result<Unit>
}

data class MonthlyTotal(
    val month: String,
    val year: String,
    val income: Long,
    val expense: Long
)
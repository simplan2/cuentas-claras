package com.simplan.cuentasclaras.data.repository

import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.data.dao.EntryDao
import com.simplan.cuentasclaras.data.entity.MonthlyTotalEntity
import com.simplan.cuentasclaras.data.mapper.toDomain
import com.simplan.cuentasclaras.data.mapper.toEntity
import com.simplan.cuentasclaras.domain.entity.Entry
import com.simplan.cuentasclaras.domain.repository.EntryRepository
import com.simplan.cuentasclaras.domain.repository.MonthlyTotal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class EntryRepositoryImpl(
    private val entryDao: EntryDao
) : EntryRepository {

    override suspend fun getByAccount(accountId: Long): Result<List<Entry>> = try {
        Result.success(entryDao.getByAccount(accountId).first().toDomain())
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun getByAccountAndDateRange(accountId: Long, startDate: Long, endDate: Long): Result<List<Entry>> = try {
        Result.success(entryDao.getByAccountAndDateRange(accountId, startDate, endDate).first().toDomain())
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun getByCategory(categoryId: Long): Result<List<Entry>> = try {
        Result.success(entryDao.getByCategory(categoryId).first().toDomain())
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun getById(id: Long): Result<Entry> = try {
        val entity = entryDao.getById(id).firstOrNull()
        if (entity != null) Result.success(entity.toDomain()) else Result.error("Entry not found: $id")
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun getTotalByAccount(accountId: Long): Result<Long> = try {
        Result.success(entryDao.getTotalByAccount(accountId))
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun getTotalByAccountAndType(accountId: Long, type: Entry.EntryType): Result<Long> = try {
        Result.success(entryDao.getTotalByAccountAndType(accountId, type.value))
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun getMonthlyTotals(accountId: Long, year: Int): Result<List<MonthlyTotal>> = try {
        val entities = entryDao.getMonthlyTotals(accountId, year.toString())
        Result.success(entities.map { it.toDomain() })
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun insert(entry: Entry): Result<Long> = try {
        val id = entryDao.insert(entry.toEntity())
        Result.success(id)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun update(entry: Entry): Result<Unit> = try {
        entryDao.update(entry.toEntity())
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun delete(id: Long): Result<Unit> = try {
        entryDao.delete(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun deleteByAccount(accountId: Long): Result<Unit> = try {
        entryDao.deleteByAccount(accountId)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override fun observeByAccount(accountId: Long): Flow<Result<List<Entry>>> = entryDao.getByAccount(accountId)
        .map { Result.success(it.toDomain()) }
        .catch { emit(Result.error(it)) }

    override fun observeByAccountAndDateRange(accountId: Long, startDate: Long, endDate: Long): Flow<Result<List<Entry>>> = entryDao.getByAccountAndDateRange(accountId, startDate, endDate)
        .map { Result.success(it.toDomain()) }
        .catch { emit(Result.error(it)) }
}

private fun com.simplan.cuentasclaras.data.entity.MonthlyTotalEntity.toDomain(): MonthlyTotal = MonthlyTotal(
    month = month,
    year = year,
    income = income,
    expense = expense
)
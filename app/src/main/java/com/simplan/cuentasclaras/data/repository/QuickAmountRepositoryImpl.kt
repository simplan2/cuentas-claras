package com.simplan.cuentasclaras.data.repository

import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.data.dao.QuickAmountDao
import com.simplan.cuentasclaras.data.mapper.toDomain
import com.simplan.cuentasclaras.data.mapper.toEntity
import com.simplan.cuentasclaras.domain.entity.QuickAmount
import com.simplan.cuentasclaras.domain.repository.QuickAmountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class QuickAmountRepositoryImpl(
    private val quickAmountDao: QuickAmountDao
) : QuickAmountRepository {

    override suspend fun getAll(): Result<List<QuickAmount>> = try {
        Result.success(quickAmountDao.getAll().first().toDomain())
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun getEnabled(): Result<List<QuickAmount>> = try {
        Result.success(quickAmountDao.getEnabled().first().toDomain())
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun getById(id: Long): Result<QuickAmount> = try {
        val entity = quickAmountDao.getById(id).firstOrNull()
        if (entity != null) Result.success(entity.toDomain()) else Result.error("QuickAmount not found: $id")
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun insert(quickAmount: QuickAmount): Result<Long> = try {
        val id = quickAmountDao.insert(quickAmount.toEntity())
        Result.success(id)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun update(quickAmount: QuickAmount): Result<Unit> = try {
        quickAmountDao.update(quickAmount.toEntity())
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun delete(id: Long): Result<Unit> = try {
        quickAmountDao.delete(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun reorder(quickAmountIds: List<Long>): Result<Unit> = try {
        quickAmountDao.reorder(quickAmountIds)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override fun observeAll(): Flow<Result<List<QuickAmount>>> = quickAmountDao.getAll()
        .map { Result.success(it.toDomain()) }
        .catch { emit(Result.error(it)) }

    override fun observeEnabled(): Flow<Result<List<QuickAmount>>> = quickAmountDao.getEnabled()
        .map { Result.success(it.toDomain()) }
        .catch { emit(Result.error(it)) }
}
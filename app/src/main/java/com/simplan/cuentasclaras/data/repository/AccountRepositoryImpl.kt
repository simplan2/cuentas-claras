package com.simplan.cuentasclaras.data.repository

import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.data.dao.AccountDao
import com.simplan.cuentasclaras.data.mapper.toDomain
import com.simplan.cuentasclaras.data.mapper.toEntity
import com.simplan.cuentasclaras.domain.entity.Account
import com.simplan.cuentasclaras.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class AccountRepositoryImpl(
    private val accountDao: AccountDao
) : AccountRepository {

    override suspend fun getAll(): Result<List<Account>> = try {
        Result.success(accountDao.getAll().first().toDomain())
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun getOpenAccounts(): Result<List<Account>> = try {
        Result.success(accountDao.getOpenAccounts().first().toDomain())
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun getById(id: Long): Result<Account> = try {
        val entity = accountDao.getById(id).firstOrNull()
        if (entity != null) Result.success(entity.toDomain()) else Result.error("Account not found: $id")
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun getDefaultAccount(): Result<Account?> = try {
        val entity = accountDao.getDefaultAccount().firstOrNull()
        Result.success(entity?.toDomain())
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun insert(account: Account): Result<Long> = try {
        val id = accountDao.insert(account.toEntity())
        Result.success(id)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun update(account: Account): Result<Unit> = try {
        accountDao.update(account.toEntity())
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun delete(id: Long): Result<Unit> = try {
        accountDao.delete(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun reorder(accountIds: List<Long>): Result<Unit> = try {
        accountDao.reorder(accountIds)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }

    override suspend fun countOpen(): Result<Int> = try {
        Result.success(accountDao.countOpen())
    } catch (e: Exception) {
        Result.error(e)
    }

    override fun observeAll(): Flow<Result<List<Account>>> = accountDao.getAll()
        .map { Result.success(it.toDomain()) }
        .catch { emit(Result.error(it)) }

    override fun observeOpenAccounts(): Flow<Result<List<Account>>> = accountDao.getOpenAccounts()
        .map { Result.success(it.toDomain()) }
        .catch { emit(Result.error(it)) }

    override fun observeById(id: Long): Flow<Result<Account?>> = accountDao.getById(id)
        .map { Result.success(it?.toDomain()) }
        .catch { emit(Result.error(it)) }
}
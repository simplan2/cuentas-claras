package com.simplan.cuentasclaras.domain.repository

import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    suspend fun getAll(): Result<List<Account>>
    fun observeAll(): Flow<Result<List<Account>>>
    suspend fun getOpenAccounts(): Result<List<Account>>
    fun observeOpenAccounts(): Flow<Result<List<Account>>>
    suspend fun getById(id: Long): Result<Account>
    fun observeById(id: Long): Flow<Result<Account?>>
    suspend fun getDefaultAccount(): Result<Account?>
    suspend fun insert(account: Account): Result<Long>
    suspend fun update(account: Account): Result<Unit>
    suspend fun delete(id: Long): Result<Unit>
    suspend fun reorder(accountIds: List<Long>): Result<Unit>
    suspend fun countOpen(): Result<Int>
}
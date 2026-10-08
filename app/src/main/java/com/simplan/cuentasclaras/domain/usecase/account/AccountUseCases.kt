package com.simplan.cuentasclaras.domain.usecase.account

import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.Account
import com.simplan.cuentasclaras.domain.repository.AccountRepository
import com.simplan.cuentasclaras.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

sealed interface GetAccountsUseCase {
    suspend operator fun invoke(): Result<List<Account>>
    fun observe(): Flow<Result<List<Account>>>
}

sealed interface GetOpenAccountsUseCase {
    suspend operator fun invoke(): Result<List<Account>>
    fun observe(): Flow<Result<List<Account>>>
}

sealed interface GetAccountUseCase {
    suspend operator fun invoke(id: Long): Result<Account>
    fun observe(id: Long): Flow<Result<Account?>>
}

sealed interface GetDefaultAccountUseCase {
    suspend operator fun invoke(): Result<Account?>
}

sealed interface CreateAccountUseCase {
    suspend operator fun invoke(account: Account): Result<Long>
}

sealed interface UpdateAccountUseCase {
    suspend operator fun invoke(account: Account): Result<Unit>
}

sealed interface DeleteAccountUseCase {
    suspend operator fun invoke(id: Long): Result<Unit>
}

sealed interface ReorderAccountsUseCase {
    suspend operator fun invoke(accountIds: List<Long>): Result<Unit>
}

sealed interface CloseAccountUseCase {
    suspend operator fun invoke(id: Long): Result<Unit>
}

sealed interface SetDefaultAccountUseCase {
    suspend operator fun invoke(id: Long): Result<Unit>
}

interface AccountUseCases {
    suspend fun getAllAccounts(): Result<List<Account>>
    fun observeAllAccounts(): Flow<Result<List<Account>>>
    suspend fun getOpenAccounts(): Result<List<Account>>
    fun observeOpenAccounts(): Flow<Result<List<Account>>>
    suspend fun getAccount(id: Long): Result<Account>
    fun observeAccount(id: Long): Flow<Result<Account?>>
    suspend fun getDefaultAccount(): Result<Account?>
    suspend fun createAccount(account: Account): Result<Long>
    suspend fun updateAccount(account: Account): Result<Unit>
    suspend fun deleteAccount(id: Long): Result<Unit>
    suspend fun reorderAccounts(accountIds: List<Long>): Result<Unit>
    suspend fun closeAccount(id: Long): Result<Unit>
    suspend fun setDefaultAccount(id: Long): Result<Unit>
}

class AccountUseCasesImpl(
    private val repository: AccountRepository,
    private val settingsRepository: SettingsRepository
) : AccountUseCases {

    override suspend fun getAllAccounts(): Result<List<Account>> = repository.getAll()
    override fun observeAllAccounts(): Flow<Result<List<Account>>> = repository.observeAll()
    override suspend fun getOpenAccounts(): Result<List<Account>> = repository.getOpenAccounts()
    override fun observeOpenAccounts(): Flow<Result<List<Account>>> = repository.observeOpenAccounts()
    override suspend fun getAccount(id: Long): Result<Account> = repository.getById(id)
    override fun observeAccount(id: Long): Flow<Result<Account?>> = repository.observeById(id)
    override suspend fun getDefaultAccount(): Result<Account?> = repository.getDefaultAccount()
    override suspend fun createAccount(account: Account): Result<Long> = repository.insert(account)
    override suspend fun updateAccount(account: Account): Result<Unit> = repository.update(account)
    override suspend fun deleteAccount(id: Long): Result<Unit> = repository.delete(id)
    override suspend fun reorderAccounts(accountIds: List<Long>): Result<Unit> = repository.reorder(accountIds)
    override suspend fun closeAccount(id: Long): Result<Unit> {
        val accountResult = repository.getById(id)
        return if (accountResult.isSuccess()) {
            repository.update(accountResult.getOrThrow().close())
        } else {
            accountResult.map { Unit }
        }
    }
    override suspend fun setDefaultAccount(id: Long): Result<Unit> {
        val allResult = repository.getAll()
        if (allResult.isError()) return allResult.map { Unit }
        allResult.getOrThrow().forEach { account ->
            val shouldBeDefault = account.id == id
            if (account.isDefault != shouldBeDefault) {
                val updateResult = repository.update(
                    if (shouldBeDefault) account.setAsDefault() else account.unsetAsDefault()
                )
                if (updateResult.isError()) return updateResult
            }
        }
        val settingsResult = settingsRepository.get()
        return if (settingsResult.isSuccess()) {
            settingsRepository.save(settingsResult.getOrThrow().copyWith(defaultAccountId = id))
        } else {
            settingsResult.map { Unit }
        }
    }
}

class GetAccountsUseCaseImpl(private val repository: AccountRepository) : GetAccountsUseCase {
    override suspend fun invoke(): Result<List<Account>> = repository.getAll()
    override fun observe(): Flow<Result<List<Account>>> = repository.observeAll()
}

class GetOpenAccountsUseCaseImpl(private val repository: AccountRepository) : GetOpenAccountsUseCase {
    override suspend fun invoke(): Result<List<Account>> = repository.getOpenAccounts()
    override fun observe(): Flow<Result<List<Account>>> = repository.observeOpenAccounts()
}

class GetAccountUseCaseImpl(private val repository: AccountRepository) : GetAccountUseCase {
    override suspend fun invoke(id: Long): Result<Account> = repository.getById(id)
    override fun observe(id: Long): Flow<Result<Account?>> = repository.observeById(id)
}

class GetDefaultAccountUseCaseImpl(private val repository: AccountRepository) : GetDefaultAccountUseCase {
    override suspend fun invoke(): Result<Account?> = repository.getDefaultAccount()
}

class CreateAccountUseCaseImpl(private val repository: AccountRepository) : CreateAccountUseCase {
    override suspend fun invoke(account: Account): Result<Long> = repository.insert(account)
}

class UpdateAccountUseCaseImpl(private val repository: AccountRepository) : UpdateAccountUseCase {
    override suspend fun invoke(account: Account): Result<Unit> = repository.update(account)
}

class DeleteAccountUseCaseImpl(private val repository: AccountRepository) : DeleteAccountUseCase {
    override suspend fun invoke(id: Long): Result<Unit> = repository.delete(id)
}

class ReorderAccountsUseCaseImpl(private val repository: AccountRepository) : ReorderAccountsUseCase {
    override suspend fun invoke(accountIds: List<Long>): Result<Unit> = repository.reorder(accountIds)
}

class CloseAccountUseCaseImpl(
    private val repository: AccountRepository,
    private val getAccountUseCase: GetAccountUseCase
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        val accountResult = getAccountUseCase(id)
        return if (accountResult.isSuccess()) {
            repository.update(accountResult.getOrThrow().close())
        } else {
            accountResult.map { Unit }
        }
    }
}

class SetDefaultAccountUseCaseImpl(
    private val repository: AccountRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        val settingsResult = settingsRepository.get()
        return if (settingsResult.isSuccess()) {
            settingsRepository.save(settingsResult.getOrThrow().copyWith(defaultAccountId = id))
        } else {
            settingsResult.map { Unit }
        }
    }
}

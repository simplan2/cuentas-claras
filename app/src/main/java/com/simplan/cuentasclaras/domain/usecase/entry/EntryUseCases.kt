package com.simplan.cuentasclaras.domain.usecase.entry

import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.Entry
import com.simplan.cuentasclaras.domain.repository.EntryRepository
import com.simplan.cuentasclaras.domain.repository.MonthlyTotal
import kotlinx.coroutines.flow.Flow

sealed interface GetEntriesUseCase {
    suspend operator fun invoke(accountId: Long): Result<List<Entry>>
    fun observe(accountId: Long): Flow<Result<List<Entry>>>
}

sealed interface GetEntriesByDateRangeUseCase {
    suspend operator fun invoke(accountId: Long, startDate: Long, endDate: Long): Result<List<Entry>>
    fun observe(accountId: Long, startDate: Long, endDate: Long): Flow<Result<List<Entry>>>
}

sealed interface GetEntriesByCategoryUseCase {
    suspend operator fun invoke(categoryId: Long): Result<List<Entry>>
}

sealed interface GetEntryUseCase {
    suspend operator fun invoke(id: Long): Result<Entry>
}

sealed interface GetAccountTotalUseCase {
    suspend operator fun invoke(accountId: Long): Result<Long>
}

sealed interface GetAccountTotalByTypeUseCase {
    suspend operator fun invoke(accountId: Long, type: Entry.EntryType): Result<Long>
}

sealed interface GetMonthlyTotalsUseCase {
    suspend operator fun invoke(accountId: Long, year: Int): Result<List<MonthlyTotal>>
}

sealed interface CreateEntryUseCase {
    suspend operator fun invoke(entry: Entry): Result<Long>
}

sealed interface UpdateEntryUseCase {
    suspend operator fun invoke(entry: Entry): Result<Unit>
}

sealed interface DeleteEntryUseCase {
    suspend operator fun invoke(id: Long): Result<Unit>
}

sealed interface DeleteEntriesByAccountUseCase {
    suspend operator fun invoke(accountId: Long): Result<Unit>
}

interface EntryUseCases {
    suspend fun getEntries(accountId: Long): Result<List<Entry>>
    fun observeEntries(accountId: Long): Flow<Result<List<Entry>>>
    suspend fun getEntriesByDateRange(accountId: Long, startDate: Long, endDate: Long): Result<List<Entry>>
    fun observeEntriesByDateRange(accountId: Long, startDate: Long, endDate: Long): Flow<Result<List<Entry>>>
    suspend fun getEntriesByCategory(categoryId: Long): Result<List<Entry>>
    suspend fun getEntry(id: Long): Result<Entry>
    suspend fun getAccountTotal(accountId: Long): Result<Long>
    suspend fun getAccountTotalByType(accountId: Long, type: Entry.EntryType): Result<Long>
    suspend fun getMonthlyTotals(accountId: Long, year: Int): Result<List<MonthlyTotal>>
    suspend fun createEntry(entry: Entry): Result<Long>
    suspend fun updateEntry(entry: Entry): Result<Unit>
    suspend fun deleteEntry(id: Long): Result<Unit>
    suspend fun deleteEntriesByAccount(accountId: Long): Result<Unit>
}

class GetEntriesUseCaseImpl(private val repository: EntryRepository) : GetEntriesUseCase {
    override suspend fun invoke(accountId: Long): Result<List<Entry>> = repository.getByAccount(accountId)
    override fun observe(accountId: Long): Flow<Result<List<Entry>>> = repository.observeByAccount(accountId)
}

class GetEntriesByDateRangeUseCaseImpl(private val repository: EntryRepository) : GetEntriesByDateRangeUseCase {
    override suspend fun invoke(accountId: Long, startDate: Long, endDate: Long): Result<List<Entry>> =
        repository.getByAccountAndDateRange(accountId, startDate, endDate)
    override fun observe(accountId: Long, startDate: Long, endDate: Long): Flow<Result<List<Entry>>> =
        repository.observeByAccountAndDateRange(accountId, startDate, endDate)
}

class GetEntriesByCategoryUseCaseImpl(private val repository: EntryRepository) : GetEntriesByCategoryUseCase {
    override suspend fun invoke(categoryId: Long): Result<List<Entry>> = repository.getByCategory(categoryId)
}

class GetEntryUseCaseImpl(private val repository: EntryRepository) : GetEntryUseCase {
    override suspend fun invoke(id: Long): Result<Entry> = repository.getById(id)
}

class GetAccountTotalUseCaseImpl(private val repository: EntryRepository) : GetAccountTotalUseCase {
    override suspend fun invoke(accountId: Long): Result<Long> = repository.getTotalByAccount(accountId)
}

class GetAccountTotalByTypeUseCaseImpl(private val repository: EntryRepository) : GetAccountTotalByTypeUseCase {
    override suspend fun invoke(accountId: Long, type: Entry.EntryType): Result<Long> =
        repository.getTotalByAccountAndType(accountId, type)
}

class GetMonthlyTotalsUseCaseImpl(private val repository: EntryRepository) : GetMonthlyTotalsUseCase {
    override suspend fun invoke(accountId: Long, year: Int): Result<List<MonthlyTotal>> =
        repository.getMonthlyTotals(accountId, year)
}

class CreateEntryUseCaseImpl(private val repository: EntryRepository) : CreateEntryUseCase {
    override suspend fun invoke(entry: Entry): Result<Long> = repository.insert(entry)
}

class UpdateEntryUseCaseImpl(private val repository: EntryRepository) : UpdateEntryUseCase {
    override suspend fun invoke(entry: Entry): Result<Unit> = repository.update(entry)
}

class DeleteEntryUseCaseImpl(private val repository: EntryRepository) : DeleteEntryUseCase {
    override suspend fun invoke(id: Long): Result<Unit> = repository.delete(id)
}

class DeleteEntriesByAccountUseCaseImpl(private val repository: EntryRepository) : DeleteEntriesByAccountUseCase {
    override suspend fun invoke(accountId: Long): Result<Unit> = repository.deleteByAccount(accountId)
}

class EntryUseCasesImpl(
    private val repository: EntryRepository
) : EntryUseCases {

    override suspend fun getEntries(accountId: Long): Result<List<Entry>> = repository.getByAccount(accountId)
    override fun observeEntries(accountId: Long): Flow<Result<List<Entry>>> = repository.observeByAccount(accountId)
    override suspend fun getEntriesByDateRange(accountId: Long, startDate: Long, endDate: Long): Result<List<Entry>> =
        repository.getByAccountAndDateRange(accountId, startDate, endDate)
    override fun observeEntriesByDateRange(accountId: Long, startDate: Long, endDate: Long): Flow<Result<List<Entry>>> =
        repository.observeByAccountAndDateRange(accountId, startDate, endDate)
    override suspend fun getEntriesByCategory(categoryId: Long): Result<List<Entry>> = repository.getByCategory(categoryId)
    override suspend fun getEntry(id: Long): Result<Entry> = repository.getById(id)
    override suspend fun getAccountTotal(accountId: Long): Result<Long> = repository.getTotalByAccount(accountId)
    override suspend fun getAccountTotalByType(accountId: Long, type: Entry.EntryType): Result<Long> =
        repository.getTotalByAccountAndType(accountId, type)
    override suspend fun getMonthlyTotals(accountId: Long, year: Int): Result<List<MonthlyTotal>> =
        repository.getMonthlyTotals(accountId, year)
    override suspend fun createEntry(entry: Entry): Result<Long> = repository.insert(entry)
    override suspend fun updateEntry(entry: Entry): Result<Unit> = repository.update(entry)
    override suspend fun deleteEntry(id: Long): Result<Unit> = repository.delete(id)
    override suspend fun deleteEntriesByAccount(accountId: Long): Result<Unit> = repository.deleteByAccount(accountId)
}
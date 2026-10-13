package com.simplan.cuentasclaras.domain.usecase.entry

import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.Entry
import com.simplan.cuentasclaras.domain.repository.EntryRepository
import com.simplan.cuentasclaras.domain.repository.MonthlyTotal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeEntryRepository : EntryRepository {
    val entries = mutableListOf<Entry>()
    private var nextId = 1L
    var failNextInsert = false

    override suspend fun getByAccount(accountId: Long): Result<List<Entry>> =
        Result.success(entries.filter { it.accountId == accountId })

    override fun observeByAccount(accountId: Long): Flow<Result<List<Entry>>> =
        flowOf(Result.success(entries.filter { it.accountId == accountId }))

    override suspend fun getByAccountAndDateRange(
        accountId: Long,
        startDate: Long,
        endDate: Long
    ): Result<List<Entry>> = Result.success(
        entries.filter { it.accountId == accountId && it.date in startDate..endDate }
    )

    override fun observeByAccountAndDateRange(
        accountId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<Result<List<Entry>>> = flowOf(
        Result.success(entries.filter { it.accountId == accountId && it.date in startDate..endDate })
    )

    override suspend fun getByCategory(categoryId: Long): Result<List<Entry>> =
        Result.success(entries.filter { it.categoryId == categoryId })

    override suspend fun getById(id: Long): Result<Entry> =
        entries.firstOrNull { it.id == id }
            ?.let { Result.success(it) }
            ?: Result.error("Entry not found: $id")

    override suspend fun getTotalByAccount(accountId: Long): Result<Long> =
        Result.success(entries.filter { it.accountId == accountId }.sumOf { it.signedAmountCents })

    override suspend fun getTotalByAccountAndType(
        accountId: Long,
        type: Entry.EntryType
    ): Result<Long> = Result.success(
        entries.filter { it.accountId == accountId && it.type == type }.sumOf { it.amountCents }
    )

    override suspend fun getMonthlyTotals(accountId: Long, year: Int): Result<List<MonthlyTotal>> =
        Result.success(emptyList())

    override suspend fun insert(entry: Entry): Result<Long> {
        if (failNextInsert) return Result.error("insert failed")
        val id = nextId++
        entries.add(entry.copy(id = id))
        return Result.success(id)
    }

    override suspend fun update(entry: Entry): Result<Unit> {
        val index = entries.indexOfFirst { it.id == entry.id }
        if (index >= 0) entries[index] = entry
        return Result.success(Unit)
    }

    override suspend fun delete(id: Long): Result<Unit> {
        entries.removeAll { it.id == id }
        return Result.success(Unit)
    }

    override suspend fun deleteByAccount(accountId: Long): Result<Unit> {
        entries.removeAll { it.accountId == accountId }
        return Result.success(Unit)
    }
}

class EntryUseCasesTest {

    private val repository = FakeEntryRepository()
    private val useCases = EntryUseCasesImpl(repository)

    private fun entry(
        accountId: Long = 1L,
        categoryId: Long = 0L,
        cents: Long = 1000L,
        type: Entry.EntryType = Entry.EntryType.EXPENSE
    ) = Entry(
        accountId = accountId,
        categoryId = categoryId,
        amountCents = cents,
        type = type
    )

    @Test
    fun createEntry_assigns_id_and_persists() = runTest {
        val result = useCases.createEntry(entry(categoryId = 3L))

        assertTrue(result.isSuccess())
        val listResult = useCases.getEntries(1L)
        assertEquals(1, listResult.getOrThrow().size)
        assertEquals(3L, listResult.getOrThrow().first().categoryId)
    }

    @Test
    fun createEntry_propagates_error() = runTest {
        repository.failNextInsert = true

        val result = useCases.createEntry(entry())

        assertTrue(result.isError())
    }

    @Test
    fun getEntriesByCategory_filters() = runTest {
        useCases.createEntry(entry(categoryId = 1L))
        useCases.createEntry(entry(categoryId = 2L))
        useCases.createEntry(entry(categoryId = 1L))

        val result = useCases.getEntriesByCategory(1L)

        assertEquals(2, result.getOrThrow().size)
    }

    @Test
    fun deleteEntry_removes_item() = runTest {
        val id = useCases.createEntry(entry()).getOrThrow()

        useCases.deleteEntry(id)

        assertEquals(0, useCases.getEntries(1L).getOrThrow().size)
    }

    @Test
    fun getAccountTotal_uses_signed_amounts() = runTest {
        useCases.createEntry(entry(cents = 5000L, type = Entry.EntryType.INCOME))
        useCases.createEntry(entry(cents = 1500L, type = Entry.EntryType.EXPENSE))

        val total = useCases.getAccountTotal(1L)

        assertEquals(3500L, total.getOrThrow())
    }
}

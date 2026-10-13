package com.simplan.cuentasclaras.ui.screen.home

import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.Account
import com.simplan.cuentasclaras.domain.entity.Entry
import com.simplan.cuentasclaras.domain.usecase.account.AccountUseCases
import com.simplan.cuentasclaras.domain.usecase.entry.EntryUseCases
import com.simplan.cuentasclaras.domain.usecase.quickamount.QuickAmountUseCases
import com.simplan.cuentasclaras.domain.usecase.settings.SettingsUseCases
import com.simplan.cuentasclaras.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val accountUseCases: AccountUseCases = mockk()
    private val entryUseCases: EntryUseCases = mockk()
    private val quickAmountUseCases: QuickAmountUseCases = mockk()
    private val settingsUseCases: SettingsUseCases = mockk()

    private val account = Account(id = 7L, name = "Principal", currencyCode = "PEN")

    private fun buildViewModel(): HomeViewModel {
        coEvery { accountUseCases.getDefaultAccount() } returns Result.success(account)
        coEvery { accountUseCases.getAllAccounts() } returns Result.success(listOf(account))
        coEvery { quickAmountUseCases.getEnabled() } returns Result.success(emptyList())
        coEvery { entryUseCases.getAccountTotal(any()) } returns Result.success(0L)
        coEvery { entryUseCases.getAccountTotalByType(any(), any()) } returns Result.success(0L)
        return HomeViewModel(
            accountUseCases,
            entryUseCases,
            quickAmountUseCases,
            settingsUseCases
        )
    }

    @Test
    fun addEntry_persists_selected_category_and_resets_it() = runTest {
        val created = slot<Entry>()
        coEvery { entryUseCases.createEntry(capture(created)) } returns Result.success(1L)

        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.setAmountText("10,00")
        viewModel.setConceptText("Almuerzo")
        viewModel.setCategoryId(1L)

        val result = viewModel.addEntry()
        advanceUntilIdle()

        assertTrue(result.isSuccess())
        assertEquals(1L, created.captured.categoryId)
        assertEquals(1000L, created.captured.amountCents)
        assertEquals(7L, created.captured.accountId)
        assertEquals(Entry.EntryType.INCOME, created.captured.type)
        assertEquals(0L, viewModel.categoryId.value)
    }

    @Test
    fun addEntry_fails_without_amount() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.setConceptText("Sin monto")

        val result = viewModel.addEntry()

        assertTrue(result.isError())
    }
}

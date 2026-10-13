package com.simplan.cuentasclaras.ui.screen.accounthistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.Account
import com.simplan.cuentasclaras.domain.entity.Entry
import com.simplan.cuentasclaras.domain.repository.MonthlyTotal
import com.simplan.cuentasclaras.domain.usecase.account.AccountUseCases
import com.simplan.cuentasclaras.domain.usecase.entry.EntryUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class EntryTypeFilter(val label: String) {
    ALL("Todos"),
    INCOME("Entradas"),
    EXPENSE("Salidas")
}

@HiltViewModel
class AccountHistoryViewModel @Inject constructor(
    private val accountUseCases: AccountUseCases,
    private val entryUseCases: EntryUseCases
) : ViewModel() {

    private val _account = MutableStateFlow<Account?>(null)
    val account = _account.asStateFlow()

    private val _year = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val year = _year.asStateFlow()

    private val _monthlyTotals = MutableStateFlow<List<MonthlyTotal>>(emptyList())
    val monthlyTotals = _monthlyTotals.asStateFlow()

    private val _selectedMonth = MutableStateFlow<Int?>(null)
    val selectedMonth = _selectedMonth.asStateFlow()

    private val _entries = MutableStateFlow<List<Entry>>(emptyList())
    val entries = _entries.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _typeFilter = MutableStateFlow(EntryTypeFilter.ALL)
    val typeFilter = _typeFilter.asStateFlow()

    private val _categoryFilter = MutableStateFlow<Long?>(null)
    val categoryFilter = _categoryFilter.asStateFlow()

    val filteredEntries: StateFlow<List<Entry>> = combine(
        _entries, _searchQuery, _typeFilter, _categoryFilter
    ) { entries, query, type, category ->
        val q = query.trim().lowercase()
        entries.filter { entry ->
            val matchesQuery = q.isEmpty() || entry.note.lowercase().contains(q)
            val matchesType = when (type) {
                EntryTypeFilter.ALL -> true
                EntryTypeFilter.INCOME -> entry.isIncome
                EntryTypeFilter.EXPENSE -> entry.isExpense
            }
            val matchesCategory = category == null || entry.categoryId == category
            matchesQuery && matchesType && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var currentAccountId: Long = 0L
    private var entriesJob: Job? = null

    fun load(accountId: Long) {
        if (accountId == currentAccountId) return
        currentAccountId = accountId
        viewModelScope.launch {
            val accountResult = accountUseCases.getAccount(accountId)
            if (accountResult.isSuccess()) {
                _account.value = accountResult.getOrNull()
            } else {
                _error.value = accountResult.errorMessage ?: "Error al cargar la cuenta"
            }
        }
        loadTotals()
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        _selectedMonth.value = currentMonth
        startObservingEntries(currentMonth)
    }

    fun changeYear(delta: Int) {
        _year.value += delta
        selectMonth(null)
        loadTotals()
    }

    fun selectMonth(month: Int?) {
        if (month == null) {
            _selectedMonth.value = null
            _entries.value = emptyList()
            entriesJob?.cancel()
            entriesJob = null
        } else {
            _selectedMonth.value = month
            startObservingEntries(month)
        }
    }

    private fun loadTotals() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = entryUseCases.getMonthlyTotals(currentAccountId, _year.value)
            if (result.isSuccess()) {
                _monthlyTotals.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.errorMessage ?: "Error al cargar los totales"
            }
            _isLoading.value = false
        }
    }

    private fun startObservingEntries(month: Int?) {
        entriesJob?.cancel()
        if (month == null) return
        val (start, end) = monthRange(_year.value, month)
        entriesJob = viewModelScope.launch {
            entryUseCases.observeEntriesByDateRange(currentAccountId, start, end)
                .collect { result ->
                    if (result.isSuccess()) {
                        _entries.value = result.getOrThrow()
                    } else {
                        _error.value = result.errorMessage ?: "Error al cargar los movimientos"
                    }
                }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(filter: EntryTypeFilter) {
        _typeFilter.value = filter
    }

    fun setCategoryFilter(categoryId: Long?) {
        _categoryFilter.value = categoryId
    }

    fun monthlyTotal(month: Int): MonthlyTotal? =
        _monthlyTotals.value.firstOrNull { it.month.toIntOrNull() == month }

    companion object {
        private fun monthRange(year: Int, month: Int): Pair<Long, Long> {
            val startCal = Calendar.getInstance().apply {
                clear()
                set(year, month - 1, 1, 0, 0, 0)
            }
            val endCal = Calendar.getInstance().apply {
                clear()
                set(year, month - 1, 1, 0, 0, 0)
                add(Calendar.MONTH, 1)
            }
            return startCal.timeInMillis to (endCal.timeInMillis - 1L)
        }
    }
}
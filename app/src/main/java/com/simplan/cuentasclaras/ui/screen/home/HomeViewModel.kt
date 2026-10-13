package com.simplan.cuentasclaras.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplan.cuentasclaras.common.Money
import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.Account
import com.simplan.cuentasclaras.domain.entity.Entry
import com.simplan.cuentasclaras.domain.entity.QuickAmount
import com.simplan.cuentasclaras.domain.usecase.account.AccountUseCases
import com.simplan.cuentasclaras.domain.usecase.entry.EntryUseCases
import com.simplan.cuentasclaras.domain.usecase.quickamount.QuickAmountUseCases
import com.simplan.cuentasclaras.domain.usecase.settings.SettingsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountUseCases: AccountUseCases,
    private val entryUseCases: EntryUseCases,
    private val quickAmountUseCases: QuickAmountUseCases,
    private val settingsUseCases: SettingsUseCases
) : ViewModel() {

    private val _account = MutableStateFlow<Account?>(null)
    val account = _account.asStateFlow()

    private val _total = MutableStateFlow(0L)
    val total = _total.asStateFlow()

    private val _incomeTotal = MutableStateFlow(0L)
    val incomeTotal = _incomeTotal.asStateFlow()

    private val _expenseTotal = MutableStateFlow(0L)
    val expenseTotal = _expenseTotal.asStateFlow()

    private val _quickAmounts = MutableStateFlow<List<QuickAmount>>(emptyList())
    val quickAmounts = _quickAmounts.asStateFlow()

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts = _accounts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _amountText = MutableStateFlow("")
    val amountText = _amountText.asStateFlow()

    private val _conceptText = MutableStateFlow("")
    val conceptText = _conceptText.asStateFlow()

    private val _isIncome = MutableStateFlow(true)
    val isIncome = _isIncome.asStateFlow()

    private val _categoryId = MutableStateFlow(0L)
    val categoryId = _categoryId.asStateFlow()

    private val _justSaved = MutableStateFlow(false)
    val justSaved = _justSaved.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            val accountResult = accountUseCases.getDefaultAccount()
            if (accountResult.isSuccess()) {
                val account = accountResult.getOrNull()
                _account.value = account
                if (account != null) {
                    loadAccountData(account.id)
                } else {
                    _total.value = 0L
                    _incomeTotal.value = 0L
                    _expenseTotal.value = 0L
                }
            }
            val accountsResult = accountUseCases.getAllAccounts()
            if (accountsResult.isSuccess()) {
                _accounts.value = accountsResult.getOrNull() ?: emptyList()
            }
            val quickAmountsResult = quickAmountUseCases.getEnabled()
            if (quickAmountsResult.isSuccess()) {
                _quickAmounts.value = quickAmountsResult.getOrNull() ?: emptyList()
            }
            _isLoading.value = false
        }
    }

    fun switchAccount(accountId: Long) {
        viewModelScope.launch {
            val result = accountUseCases.setDefaultAccount(accountId)
            if (result.isSuccess()) {
                loadData()
            } else {
                _error.value = result.errorMessage ?: "No se pudo cambiar la cuenta"
            }
        }
    }

    private fun loadAccountData(accountId: Long) {
        viewModelScope.launch {
            val totalResult = entryUseCases.getAccountTotal(accountId)
            if (totalResult.isSuccess()) {
                _total.value = totalResult.getOrNull() ?: 0L
            }
            val incomeResult = entryUseCases.getAccountTotalByType(accountId, Entry.EntryType.INCOME)
            if (incomeResult.isSuccess()) {
                _incomeTotal.value = incomeResult.getOrNull() ?: 0L
            }
            val expenseResult = entryUseCases.getAccountTotalByType(accountId, Entry.EntryType.EXPENSE)
            if (expenseResult.isSuccess()) {
                _expenseTotal.value = expenseResult.getOrNull() ?: 0L
            }
        }
    }

    fun setAmountText(text: String) {
        _amountText.value = text
    }

    fun setConceptText(text: String) {
        _conceptText.value = text
    }

    fun setIsIncome(isIncome: Boolean) {
        _isIncome.value = isIncome
    }

    fun setCategoryId(categoryId: Long) {
        _categoryId.value = categoryId
    }

    fun onQuickAmountClick(amountCents: Long) {
        val currency = _account.value?.currency ?: com.simplan.cuentasclaras.common.Currency.PEN
        _amountText.value = Money.format(amountCents, currency)
    }

    suspend fun addEntry(): Result<Unit> {
        val account = _account.value ?: return Result.error("No hay cuenta seleccionada")
        val amountText = _amountText.value.trim()
        val conceptText = _conceptText.value.trim()

        if (amountText.isEmpty()) {
            return Result.error("El monto es requerido")
        }
        if (conceptText.isEmpty()) {
            return Result.error("El concepto es requerido")
        }

        val currency = account.currency
        val amountCents = Money.parse(amountText, currency)
        if (amountCents <= 0) {
            return Result.error("El monto debe ser mayor a 0")
        }

        if (!_isIncome.value) {
            val currentTotal = _total.value
            if (amountCents > currentTotal) {
                return Result.error("El monto a sacar excede el total acumulado ($currentTotal)")
            }
        }

        _isLoading.value = true
        _error.value = null

        val entry = Entry(
            accountId = account.id,
            categoryId = _categoryId.value,
            amountCents = amountCents,
            type = if (_isIncome.value) Entry.EntryType.INCOME else Entry.EntryType.EXPENSE,
            note = conceptText
        )

        val result = entryUseCases.createEntry(entry)
        if (result.isSuccess()) {
            _justSaved.value = true
            viewModelScope.launch {
                delay(2200)
                _justSaved.value = false
            }
            _amountText.value = ""
            _conceptText.value = ""
            _categoryId.value = 0L
            loadAccountData(account.id)
        } else {
            _error.value = result.errorMessage ?: "Error al guardar"
        }

        _isLoading.value = false
        return result.map { Unit }
    }

    fun refresh() {
        loadData()
    }

    fun clearError() {
        _error.value = null
    }
}
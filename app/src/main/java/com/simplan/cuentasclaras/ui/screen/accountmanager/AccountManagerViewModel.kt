package com.simplan.cuentasclaras.ui.screen.accountmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.Account
import com.simplan.cuentasclaras.domain.usecase.account.AccountUseCases
import com.simplan.cuentasclaras.domain.usecase.settings.SettingsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountManagerViewModel @Inject constructor(
    private val accountUseCases: AccountUseCases,
    private val settingsUseCases: SettingsUseCases
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts = _accounts.asStateFlow()

    private val _defaultAccountId = MutableStateFlow(0L)
    val defaultAccountId = _defaultAccountId.asStateFlow()

    private val _defaultCurrencyCode = MutableStateFlow("PEN")
    val defaultCurrencyCode = _defaultCurrencyCode.asStateFlow()

    private val _maxOpenAccounts = MutableStateFlow(10)
    val maxOpenAccounts = _maxOpenAccounts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        loadAccounts()
    }

    fun refresh() {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            _isLoading.value = true
            val accountsResult = accountUseCases.getAllAccounts()
            val accounts = if (accountsResult.isSuccess()) {
                accountsResult.getOrNull() ?: emptyList()
            } else {
                emptyList()
            }
            _accounts.value = accounts
            val settingsResult = settingsUseCases.getSettings()
            val settings = settingsResult.getOrNull()
            _maxOpenAccounts.value = (settings?.maxOpenAccounts ?: 10).coerceAtLeast(1)
            _defaultCurrencyCode.value = settings?.currencyCode ?: "PEN"
            _defaultAccountId.value = accounts
                .firstOrNull { it.isOpen && it.isDefault }
                ?.id
                ?: settings?.defaultAccountId
                ?: 0L
            _isLoading.value = false
        }
    }

    suspend fun createAccount(account: Account): Result<Unit> {
        _isLoading.value = true
        val openCount = _accounts.value.count { it.isOpen }
        if (openCount >= _maxOpenAccounts.value) {
            _isLoading.value = false
            val message = "Has alcanzado el máximo de ${_maxOpenAccounts.value} cuentas abiertas"
            _error.value = message
            return Result.error(message)
        }
        val isNewDefault = openCount == 0
        val idResult = accountUseCases.createAccount(
            if (isNewDefault) account.setAsDefault() else account
        )
        if (idResult.isSuccess()) {
            val newId = idResult.getOrThrow()
            if (isNewDefault) {
                accountUseCases.setDefaultAccount(newId)
            }
            loadAccounts()
            _isLoading.value = false
            return Result.success(Unit)
        }
        _error.value = idResult.errorMessage ?: "Error al crear la cuenta"
        _isLoading.value = false
        return idResult.map { Unit }
    }

    suspend fun updateAccount(account: Account): Result<Unit> {
        _isLoading.value = true
        val result = accountUseCases.updateAccount(account)
        if (result.isSuccess()) {
            loadAccounts()
        } else {
            _error.value = result.errorMessage ?: "Error al actualizar la cuenta"
        }
        _isLoading.value = false
        return result
    }

    suspend fun deleteAccount(id: Long): Result<Unit> {
        _isLoading.value = true
        val wasDefault = id == _defaultAccountId.value
        val result = accountUseCases.deleteAccount(id)
        if (result.isSuccess()) {
            if (wasDefault) {
                val nextDefault = _accounts.value.firstOrNull { it.id != id && it.isOpen }
                if (nextDefault != null) {
                    accountUseCases.setDefaultAccount(nextDefault.id)
                } else {
                    settingsUseCases.getSettings().getOrNull()?.let {
                        settingsUseCases.saveSettings(it.copyWith(defaultAccountId = 0L))
                    }
                }
            }
            loadAccounts()
        } else {
            _error.value = result.errorMessage ?: "Error al eliminar la cuenta"
        }
        _isLoading.value = false
        return result
    }

    suspend fun openAccount(id: Long): Result<Unit> {
        _isLoading.value = true
        val account = _accounts.value.find { it.id == id }
        val result = if (account == null) {
            Result.error("Cuenta no encontrada")
        } else {
            accountUseCases.updateAccount(account.open())
        }
        if (result.isSuccess()) {
            loadAccounts()
        } else {
            _error.value = result.errorMessage ?: "Error al reabrir la cuenta"
        }
        _isLoading.value = false
        return result
    }

    suspend fun setDefaultAccount(id: Long): Result<Unit> {
        _isLoading.value = true
        val result = accountUseCases.setDefaultAccount(id)
        if (result.isSuccess()) {
            loadAccounts()
        } else {
            _error.value = result.errorMessage ?: "Error al establecer como predeterminada"
        }
        _isLoading.value = false
        return result
    }

    suspend fun closeAccount(id: Long): Result<Unit> {
        _isLoading.value = true
        val wasDefault = id == _defaultAccountId.value
        val result = accountUseCases.closeAccount(id)
        if (result.isSuccess()) {
            if (wasDefault) {
                val nextDefault = _accounts.value.firstOrNull { it.id != id && it.isOpen }
                if (nextDefault != null) {
                    accountUseCases.setDefaultAccount(nextDefault.id)
                } else {
                    settingsUseCases.getSettings().getOrNull()?.let {
                        settingsUseCases.saveSettings(it.copyWith(defaultAccountId = 0L))
                    }
                }
            }
            loadAccounts()
        } else {
            _error.value = result.errorMessage ?: "Error al cerrar la cuenta"
        }
        _isLoading.value = false
        return result
    }

    suspend fun reorderAccounts(accountIds: List<Long>): Result<Unit> {
        _isLoading.value = true
        val result = accountUseCases.reorderAccounts(accountIds)
        if (result.isSuccess()) {
            loadAccounts()
        } else {
            _error.value = result.errorMessage ?: "Error al reordenar"
        }
        _isLoading.value = false
        return result
    }

    fun getMaxOpenAccounts(): Int {
        return _maxOpenAccounts.value
    }

    fun canCreateMore(): Boolean {
        val openCount = _accounts.value.count { it.isOpen }
        return openCount < getMaxOpenAccounts()
    }

    fun clearError() {
        _error.value = null
    }
}
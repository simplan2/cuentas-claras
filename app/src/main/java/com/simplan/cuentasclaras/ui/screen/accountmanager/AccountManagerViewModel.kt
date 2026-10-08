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

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            _isLoading.value = true
            val accountsResult = accountUseCases.getAllAccounts()
            if (accountsResult.isSuccess()) {
                _accounts.value = accountsResult.getOrNull() ?: emptyList()
            }
            val settingsResult = settingsUseCases.getSettings()
            if (settingsResult.isSuccess()) {
                _defaultAccountId.value = settingsResult.getOrNull()?.defaultAccountId ?: 0L
            }
            _isLoading.value = false
        }
    }

    suspend fun createAccount(account: Account): Result<Unit> {
        _isLoading.value = true
        val result = accountUseCases.createAccount(account).map { Unit }
        if (result.isSuccess()) {
            loadAccounts()
        } else {
            _error.value = result.errorMessage ?: "Error al crear la cuenta"
        }
        _isLoading.value = false
        return result
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
        val result = accountUseCases.deleteAccount(id)
        if (result.isSuccess()) {
            loadAccounts()
        } else {
            _error.value = result.errorMessage ?: "Error al eliminar la cuenta"
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
        val result = accountUseCases.closeAccount(id)
        if (result.isSuccess()) {
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
        return 10 // TODO: get from settings
    }

    fun canCreateMore(): Boolean {
        val openCount = _accounts.value.count { it.isOpen }
        return openCount < getMaxOpenAccounts()
    }

    fun clearError() {
        _error.value = null
    }
}
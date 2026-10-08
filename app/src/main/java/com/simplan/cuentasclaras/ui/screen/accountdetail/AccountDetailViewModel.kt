package com.simplan.cuentasclaras.ui.screen.accountdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.Account
import com.simplan.cuentasclaras.domain.usecase.account.AccountUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val accountUseCases: AccountUseCases
) : ViewModel() {

    private val _account = MutableStateFlow<Account?>(null)
    val account = _account.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var currentAccountId: Long = 0L

    private fun loadAccount() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = accountUseCases.getAccount(currentAccountId)
            if (result.isSuccess()) {
                _account.value = result.getOrNull()
            } else {
                _error.value = result.errorMessage ?: "Error al cargar la cuenta"
            }
            _isLoading.value = false
        }
    }

    suspend fun updateAccount(account: Account): Result<Unit> {
        _isLoading.value = true
        val result = accountUseCases.updateAccount(account)
        if (result.isSuccess()) {
            _account.value = account
        } else {
            _error.value = result.errorMessage ?: "Error al actualizar"
        }
        _isLoading.value = false
        return result
    }

    suspend fun deleteAccount(): Result<Unit> {
        _isLoading.value = true
        val result = accountUseCases.deleteAccount(currentAccountId)
        _isLoading.value = false
        return result
    }

    suspend fun closeAccount(): Result<Unit> {
        _isLoading.value = true
        val result = accountUseCases.closeAccount(currentAccountId)
        if (result.isSuccess()) {
            _account.value = _account.value?.close()
        } else {
            _error.value = result.errorMessage ?: "Error al cerrar"
        }
        _isLoading.value = false
        return result
    }

    fun loadAccount(accountId: Long) {
        viewModelScope.launch {
            if (accountId == currentAccountId) return@launch
            currentAccountId = accountId
            _isLoading.value = true
            val result = accountUseCases.getAccount(currentAccountId)
            if (result.isSuccess()) {
                _account.value = result.getOrNull()
            } else {
                _error.value = result.errorMessage ?: "Error al cargar la cuenta"
            }
            _isLoading.value = false
        }
    }

    suspend fun exportAccount(): Result<String> {
        // TODO: Implement export functionality
        _isLoading.value = true
        // Simulate export
        _isLoading.value = false
        return Result.success("Exportado correctamente")
    }

    fun clearError() {
        _error.value = null
    }
}
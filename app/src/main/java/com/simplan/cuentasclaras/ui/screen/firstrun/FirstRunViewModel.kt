package com.simplan.cuentasclaras.ui.screen.firstrun

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.Account
import com.simplan.cuentasclaras.domain.usecase.account.AccountUseCases
import com.simplan.cuentasclaras.domain.usecase.settings.SettingsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class FirstRunViewModel @Inject constructor(
    private val accountUseCases: AccountUseCases,
    private val settingsUseCases: SettingsUseCases
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _currencyCode = MutableStateFlow("PEN")
    val currencyCode = _currencyCode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun setName(name: String) {
        _name.value = name
    }

    fun setCurrencyCode(currencyCode: String) {
        _currencyCode.value = currencyCode
    }

    suspend fun createAccount(): Result<Unit> {
        _isLoading.value = true
        _error.value = null

        val name = _name.value.trim()
        if (name.isEmpty()) {
            _isLoading.value = false
            _error.value = "El nombre de la cuenta es requerido"
            return Result.error("El nombre de la cuenta es requerido")
        }

        val account = Account(
            name = name,
            currencyCode = _currencyCode.value,
            isDefault = true
        )

        val result = accountUseCases.createAccount(account)
        if (result.isSuccess()) {
            val accountId = result.getOrThrow()
            val settingsResult = settingsUseCases.getSettings()
            if (settingsResult.isSuccess()) {
                settingsUseCases.saveSettings(settingsResult.getOrThrow().copyWith(defaultAccountId = accountId))
            }
        } else {
            _error.value = result.errorMessage
        }

        _isLoading.value = false
        return result.map { Unit }
    }

    fun clearError() {
        _error.value = null
    }
}
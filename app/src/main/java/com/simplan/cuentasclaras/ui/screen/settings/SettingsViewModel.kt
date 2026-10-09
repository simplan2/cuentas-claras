package com.simplan.cuentasclaras.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.QuickAmount
import com.simplan.cuentasclaras.domain.entity.Settings
import com.simplan.cuentasclaras.domain.usecase.quickamount.QuickAmountUseCases
import com.simplan.cuentasclaras.domain.usecase.lock.LockUseCases
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
class SettingsViewModel @Inject constructor(
    private val settingsUseCases: SettingsUseCases,
    private val quickAmountUseCases: QuickAmountUseCases,
    private val lockUseCases: LockUseCases
) : ViewModel() {

    private val _settings = MutableStateFlow<Settings?>(null)
    val settings = _settings.asStateFlow()

    private val _hasPin = MutableStateFlow(false)
    val hasPin = _hasPin.asStateFlow()

    private val _biometricAvailable = MutableStateFlow(false)
    val biometricAvailable = _biometricAvailable.asStateFlow()

    private val _biometricEnabled = MutableStateFlow(false)
    val biometricEnabled = _biometricEnabled.asStateFlow()

    private val _quickAmounts = MutableStateFlow<List<QuickAmount>>(emptyList())
    val quickAmounts = _quickAmounts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _showQuickAmounts = MutableStateFlow(false)
    val showQuickAmounts = _showQuickAmounts.asStateFlow()

    init {
        loadSettings()
        loadQuickAmounts()
        loadPinState()
    }

    private fun loadPinState() {
        viewModelScope.launch {
            val hasPinResult = lockUseCases.hasPin()
            if (hasPinResult.isSuccess()) {
                _hasPin.value = hasPinResult.getOrNull() == true
            }
            val availableResult = lockUseCases.isBiometricAvailable()
            if (availableResult.isSuccess()) {
                _biometricAvailable.value = availableResult.getOrNull() == true
            }
            val enabledResult = lockUseCases.isBiometricEnabled()
            if (enabledResult.isSuccess()) {
                _biometricEnabled.value = enabledResult.getOrNull() == true
            }
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit> {
        _isLoading.value = true
        val result = lockUseCases.setBiometricEnabled(enabled)
        _isLoading.value = false
        if (result.isError()) {
            _error.value = result.errorMessage ?: "Error al guardar la configuración"
        } else {
            loadPinState()
        }
        return result
    }

    suspend fun setPin(pin: String): Result<Unit> {
        _isLoading.value = true
        val result = lockUseCases.setPin(pin)
        _isLoading.value = false
        if (result.isError()) {
            _error.value = result.errorMessage ?: "Error al guardar el PIN"
        } else {
            loadPinState()
        }
        return result
    }

    suspend fun removePin(): Result<Unit> {
        _isLoading.value = true
        val result = lockUseCases.clearPin()
        _isLoading.value = false
        if (result.isError()) {
            _error.value = result.errorMessage ?: "Error al eliminar el PIN"
        } else {
            loadPinState()
        }
        return result
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = settingsUseCases.getSettings()
            if (result.isSuccess()) {
                _settings.value = result.getOrNull()
            }
            _isLoading.value = false
        }
    }

    private fun loadQuickAmounts() {
        viewModelScope.launch {
            val result = quickAmountUseCases.getAll()
            if (result.isSuccess()) {
                _quickAmounts.value = result.getOrNull() ?: emptyList()
            }
        }
    }

    suspend fun saveSettings(settings: Settings): Result<Unit> {
        _isLoading.value = true
        val result = settingsUseCases.saveSettings(settings)
        if (result.isSuccess()) {
            _settings.value = settings
        } else {
            _error.value = result.errorMessage ?: "Error al guardar ajustes"
        }
        _isLoading.value = false
        return result
    }

    suspend fun updateMaxOpenAccounts(value: Int): Result<Unit> {
        val current = _settings.value
        if (current == null) return Result.success(Unit)
        return saveSettings(current.copyWith(maxOpenAccounts = value.coerceAtLeast(1)))
    }

    suspend fun updateCurrencyCode(code: String): Result<Unit> {
        val current = _settings.value ?: return Result.success(Unit)
        return saveSettings(current.copyWith(currencyCode = code))
    }

    suspend fun updateThemeMode(mode: Settings.ThemeMode): Result<Unit> {
        val current = _settings.value ?: return Result.success(Unit)
        return saveSettings(current.copyWith(themeMode = mode))
    }

    suspend fun addQuickAmount(amount: QuickAmount): Result<Long> {
        _isLoading.value = true
        val result = quickAmountUseCases.insert(amount)
        if (result.isSuccess()) {
            loadQuickAmounts()
        } else {
            _error.value = result.errorMessage ?: "Error al agregar monto rápido"
        }
        _isLoading.value = false
        return result
    }

    suspend fun updateQuickAmount(amount: QuickAmount): Result<Unit> {
        _isLoading.value = true
        val result = quickAmountUseCases.update(amount)
        if (result.isSuccess()) {
            loadQuickAmounts()
        } else {
            _error.value = result.errorMessage ?: "Error al actualizar monto rápido"
        }
        _isLoading.value = false
        return result
    }

    suspend fun deleteQuickAmount(id: Long): Result<Unit> {
        _isLoading.value = true
        val result = quickAmountUseCases.delete(id)
        if (result.isSuccess()) {
            loadQuickAmounts()
        } else {
            _error.value = result.errorMessage ?: "Error al eliminar monto rápido"
        }
        _isLoading.value = false
        return result
    }

    suspend fun reorderQuickAmounts(ids: List<Long>): Result<Unit> {
        _isLoading.value = true
        val result = quickAmountUseCases.reorder(ids)
        if (result.isSuccess()) {
            loadQuickAmounts()
        } else {
            _error.value = result.errorMessage ?: "Error al reordenar"
        }
        _isLoading.value = false
        return result
    }

    fun toggleQuickAmounts() {
        _showQuickAmounts.value = !_showQuickAmounts.value
    }

    fun clearError() {
        _error.value = null
    }
}
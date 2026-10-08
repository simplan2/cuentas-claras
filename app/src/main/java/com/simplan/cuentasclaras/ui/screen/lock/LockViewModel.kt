package com.simplan.cuentasclaras.ui.screen.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.usecase.lock.LockUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LockViewModel @Inject constructor(
    private val lockUseCases: LockUseCases
) : ViewModel() {

    private val _pin = MutableStateFlow("")
    val pin = _pin.asStateFlow()

    private val _pinLength = MutableStateFlow(4)
    val pinLength = _pinLength.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _failedAttempts = MutableStateFlow(0)
    val failedAttempts = _failedAttempts.asStateFlow()

    private val _showBiometric = MutableStateFlow(false)
    val showBiometric = _showBiometric.asStateFlow()

    init {
        checkBiometricAvailability()
        loadPinLength()
    }

    private fun loadPinLength() {
        viewModelScope.launch {
            val result = lockUseCases.getPinLength()
            if (result.isSuccess()) {
                val length = result.getOrNull() ?: 0
                _pinLength.value = if (length in 4..6) length else 4
            }
        }
    }

    private fun checkBiometricAvailability() {
        viewModelScope.launch {
            val availableResult = lockUseCases.isBiometricAvailable()
            val enabledResult = lockUseCases.isBiometricEnabled()
            val available = availableResult.isSuccess() && availableResult.getOrNull() == true
            val enabled = enabledResult.isSuccess() && enabledResult.getOrNull() == true
            _showBiometric.value = available && enabled
        }
    }

    fun addDigit(digit: String) {
        if (_pin.value.length < 6) {
            _pin.value += digit
        }
    }

    fun removeDigit() {
        if (_pin.value.isNotEmpty()) {
            _pin.value = _pin.value.dropLast(1)
        }
    }

    fun clearPin() {
        _pin.value = ""
    }

    suspend fun verifyPin(onSuccess: () -> Unit) {
        if (_pin.value.length < 4) {
            _error.value = "El PIN debe tener al menos 4 dígitos"
            return
        }

        _isLoading.value = true
        _error.value = null

        val result = lockUseCases.verifyPin(_pin.value)
        if (result.isSuccess() && result.getOrNull() == true) {
            _pin.value = ""
            _failedAttempts.value = 0
            onSuccess()
        } else {
            _error.value = "PIN incorrecto"
            _failedAttempts.value += 1
            _pin.value = ""
        }
        _isLoading.value = false
    }

    suspend fun setupPin(newPin: String, onSuccess: () -> Unit) {
        if (newPin.length < 4) {
            _error.value = "El PIN debe tener al menos 4 dígitos"
            return
        }
        if (newPin.length > 6) {
            _error.value = "El PIN no puede tener más de 6 dígitos"
            return
        }

        _isLoading.value = true
        _error.value = null

        val result = lockUseCases.setPin(newPin)
        if (result.isSuccess()) {
            onSuccess()
        } else {
            _error.value = result.errorMessage ?: "Error al configurar PIN"
        }
        _isLoading.value = false
    }

    suspend fun authenticateWithBiometric(onSuccess: () -> Unit) {
        _isLoading.value = true
        val result = lockUseCases.authenticateWithBiometric()
        if (result.isSuccess() && result.getOrNull() == true) {
            onSuccess()
        } else {
            _error.value = "Autenticación biométrica fallida"
        }
        _isLoading.value = false
    }

    fun onBiometricSuccess(onSuccess: () -> Unit) {
        _failedAttempts.value = 0
        _error.value = null
        onSuccess()
    }

    fun onBiometricError(errorCode: Int, errString: String) {
        if (errorCode != androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
            _error.value = errString
        }
    }

    fun clearError() {
        _error.value = null
    }
}
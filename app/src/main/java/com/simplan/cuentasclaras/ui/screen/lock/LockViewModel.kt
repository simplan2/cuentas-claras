package com.simplan.cuentasclaras.ui.screen.lock

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplan.cuentasclaras.data.backup.BackupFileManager
import com.simplan.cuentasclaras.domain.usecase.lock.LockUseCases
import com.simplan.cuentasclaras.domain.usecase.backup.BackupUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LockViewModel @Inject constructor(
    private val lockUseCases: LockUseCases,
    private val backupFileManager: BackupFileManager,
    private val backupUseCases: BackupUseCases
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

    private val _lockoutRemainingMillis = MutableStateFlow(0L)
    val lockoutRemainingMillis = _lockoutRemainingMillis.asStateFlow()

    private val _showBiometric = MutableStateFlow(false)
    val showBiometric = _showBiometric.asStateFlow()

    private var countdownJob: Job? = null

    init {
        checkBiometricAvailability()
        loadPinLength()
        refreshSecurityState()
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

    private fun refreshSecurityState() {
        viewModelScope.launch {
            lockUseCases.getFailedAttempts().getOrNull()?.let { _failedAttempts.value = it }
            lockUseCases.getLockoutRemainingMillis().getOrNull()?.let { remaining ->
                if (remaining > 0L) {
                    startLockoutCountdown(remaining)
                }
            }
        }
    }

    private fun startLockoutCountdown(remaining: Long) {
        countdownJob?.cancel()
        _lockoutRemainingMillis.value = remaining
        _pin.value = ""
        _error.value = "Demasiados intentos. Espera un momento."
        countdownJob = viewModelScope.launch {
            var current = remaining
            while (current > 0L) {
                delay(1000L)
                current = (current - 1000L).coerceAtLeast(0L)
                _lockoutRemainingMillis.value = current
            }
            // No se reinician los intentos: el bloqueo es escalonado y persiste.
            _error.value = null
            val remainingNow = lockUseCases.getLockoutRemainingMillis().getOrNull() ?: 0L
            if (remainingNow > 0L) {
                startLockoutCountdown(remainingNow)
            }
        }
    }

    fun addDigit(digit: String) {
        if (_lockoutRemainingMillis.value == 0L && _pin.value.length < 6) {
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
        if (_lockoutRemainingMillis.value > 0L) return

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
            _lockoutRemainingMillis.value = 0L
            onSuccess()
        } else {
            val attempts = lockUseCases.getFailedAttempts().getOrNull() ?: _failedAttempts.value + 1
            _failedAttempts.value = attempts
            _pin.value = ""
            val remaining = lockUseCases.getLockoutRemainingMillis().getOrNull() ?: 0L
            if (remaining > 0L) {
                startLockoutCountdown(remaining)
            } else {
                val left = MAX_ATTEMPTS - (attempts % MAX_ATTEMPTS)
                _error.value = "PIN incorrecto. Te quedan $left intento(s)"
            }
        }
        _isLoading.value = false
    }

    /**
     * Recupera el acceso con el código de recuperación. Como el usuario no recuerda
     * su PIN, se elimina y la app queda abierta (deberá configurar uno nuevo en Ajustes).
     */
    suspend fun recoverWithCode(code: String, onSuccess: () -> Unit) {
        _isLoading.value = true
        _error.value = null
        val result = lockUseCases.verifyRecoveryCode(code)
        if (result.isSuccess() && result.getOrNull() == true) {
            clearSecurityAndUnlock()
            onSuccess()
        } else {
            _error.value = "Código de recuperación incorrecto"
        }
        _isLoading.value = false
    }

    /**
     * Restaura los datos desde un respaldo JSON. Reemplaza todos los datos actuales.
     */
    suspend fun restoreFromBackup(uri: Uri, onSuccess: () -> Unit) {
        _isLoading.value = true
        _error.value = null
        val result = backupFileManager.importFrom(uri)
        if (result.isSuccess()) {
            clearSecurityAndUnlock()
            onSuccess()
        } else {
            _error.value = result.errorMessage ?: "No se pudo restaurar el respaldo"
        }
        _isLoading.value = false
    }

    /**
     * Borra todos los datos y el PIN. Devuelve la app a su estado inicial.
     */
    suspend fun wipeEverything(onSuccess: () -> Unit) {
        _isLoading.value = true
        _error.value = null
        val result = backupUseCases.wipe()
        if (result.isSuccess()) {
            clearSecurityAndUnlock()
            onSuccess()
        } else {
            _error.value = result.errorMessage ?: "No se pudieron borrar los datos"
        }
        _isLoading.value = false
    }

    private suspend fun clearSecurityAndUnlock() {
        lockUseCases.clearPin()
        _pin.value = ""
        _failedAttempts.value = 0
        _lockoutRemainingMillis.value = 0L
        _error.value = null
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
        _lockoutRemainingMillis.value = 0L
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

    companion object {
        private const val MAX_ATTEMPTS = 5
    }
}

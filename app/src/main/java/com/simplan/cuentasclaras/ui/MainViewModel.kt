package com.simplan.cuentasclaras.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.Settings
import com.simplan.cuentasclaras.domain.usecase.account.AccountUseCases
import com.simplan.cuentasclaras.domain.usecase.lock.LockUseCases
import com.simplan.cuentasclaras.domain.usecase.settings.SettingsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val lockUseCases: LockUseCases,
    private val accountUseCases: AccountUseCases,
    private val settingsUseCases: SettingsUseCases
) : ViewModel() {

    private val _hasAccounts = MutableStateFlow<Boolean?>(null)
    val hasAccounts: StateFlow<Boolean?> = _hasAccounts.asStateFlow()

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _themeMode = MutableStateFlow(Settings.ThemeMode.SYSTEM)
    val themeMode: StateFlow<Settings.ThemeMode> = _themeMode.asStateFlow()

    private val _autoLockEnabled = MutableStateFlow(false)
    val autoLockEnabled: StateFlow<Boolean> = _autoLockEnabled.asStateFlow()

    private val _autoLockDelayMinutes = MutableStateFlow(5)
    val autoLockDelayMinutes: StateFlow<Int> = _autoLockDelayMinutes.asStateFlow()

    var initialLockHandled: Boolean = false

    // Exponer Flow para CollectAsStateWithLifecycle en Activity
    val observeLockState: Flow<Result<Boolean>>
        get() = lockUseCases.observeLockState()

    init {
        viewModelScope.launch {
            accountUseCases.observeAllAccounts().collect { result ->
                if (result.isSuccess()) {
                    _hasAccounts.value = (result.getOrNull() ?: emptyList()).isNotEmpty()
                }
            }
        }
        viewModelScope.launch {
            lockUseCases.observeLockState().collect { result ->
                if (result.isSuccess()) {
                    _isLocked.value = result.getOrNull() ?: false
                }
            }
        }
        viewModelScope.launch {
            settingsUseCases.observeSettings().collect { result ->
                if (result.isSuccess()) {
                    result.getOrNull()?.let {
                        _themeMode.value = it.themeMode
                        _autoLockEnabled.value = it.autoLockEnabled
                        _autoLockDelayMinutes.value = it.autoLockDelayMinutes
                    }
                }
            }
        }
    }

    suspend fun lockIfPinConfigured() {
        val hasPin = lockUseCases.hasPin()
        if (hasPin.isSuccess() && hasPin.getOrNull() == true) {
            lockUseCases.lock()
        }
    }

    suspend fun lock() {
        lockUseCases.lock()
    }
}
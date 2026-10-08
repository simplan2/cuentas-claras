package com.simplan.cuentasclaras.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.usecase.lock.LockUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val lockUseCases: LockUseCases
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isFirstRun = MutableStateFlow(true)
    val isFirstRun: StateFlow<Boolean> = _isFirstRun.asStateFlow()

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    // Exponer Flow para CollectAsStateWithLifecycle en Activity
    val observeLockState: Flow<Result<Boolean>>
        get() = lockUseCases.observeLockState()

    init {
        viewModelScope.launch {
            _isLoading.value = true
            lockUseCases.observeLockState().collect { result ->
                if (result.isSuccess()) {
                    _isLocked.value = result.getOrNull() ?: false
                }
            }
            _isLoading.value = false
        }
    }

    fun markFirstRunCompleted() {
        _isFirstRun.value = false
    }

    suspend fun lockIfPinConfigured() {
        val hasPin = lockUseCases.hasPin()
        if (hasPin.isSuccess() && hasPin.getOrNull() == true) {
            lockUseCases.lock()
        }
    }
}
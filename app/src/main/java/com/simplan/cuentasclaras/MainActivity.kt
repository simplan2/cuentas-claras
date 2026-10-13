package com.simplan.cuentasclaras

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.domain.entity.Settings
import com.simplan.cuentasclaras.ui.MainViewModel
import com.simplan.cuentasclaras.ui.navigation.AppNavHost
import com.simplan.cuentasclaras.ui.screen.firstrun.FirstRunScreen
import com.simplan.cuentasclaras.ui.screen.lock.LockScreen
import com.simplan.cuentasclaras.ui.theme.CuentasClarasTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private var backgroundedAt: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!mainViewModel.initialLockHandled) {
            mainViewModel.initialLockHandled = true
            lifecycleScope.launch {
                mainViewModel.lockIfPinConfigured()
            }
        }
        setContent {
            val themeMode by mainViewModel.themeMode
                .collectAsStateWithLifecycle(initialValue = Settings.ThemeMode.SYSTEM)
            val darkTheme = when (themeMode) {
                Settings.ThemeMode.LIGHT -> false
                Settings.ThemeMode.DARK -> true
                Settings.ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            CuentasClarasTheme(darkTheme = darkTheme) {
                LaunchedEffect(darkTheme) {
                    val controller = WindowCompat.getInsetsController(
                        window,
                        window.decorView
                    )
                    controller.isAppearanceLightStatusBars = !darkTheme
                    controller.isAppearanceLightNavigationBars = !darkTheme
                }
                MainNavHost()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val enabled = mainViewModel.autoLockEnabled.value
        val delay = mainViewModel.autoLockDelayMinutes.value
        if (enabled && backgroundedAt > 0L) {
            val elapsed = System.currentTimeMillis() - backgroundedAt
            if (delay <= 0 || elapsed >= delay * 60_000L) {
                lifecycleScope.launch {
                    mainViewModel.lock()
                }
            }
        }
        backgroundedAt = 0L
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            val enabled = mainViewModel.autoLockEnabled.value
            val delay = mainViewModel.autoLockDelayMinutes.value
            if (enabled) {
                if (delay <= 0) {
                    lifecycleScope.launch {
                        mainViewModel.lock()
                    }
                } else {
                    backgroundedAt = System.currentTimeMillis()
                }
            }
        }
    }
}

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = hiltViewModel()

    var showLock by rememberSaveable { mutableStateOf(false) }

    // ---------- ¿Existen cuentas? ----------
    val hasAccounts by mainViewModel.hasAccounts
        .collectAsStateWithLifecycle(initialValue = null)

    // ---------- Chequear estado de bloqueo ----------
    val lockState by mainViewModel
        .observeLockState
        .collectAsStateWithLifecycle(initialValue = Result.success(false))

    LaunchedEffect(lockState, hasAccounts) {
        val shouldLock = lockState.isSuccess() && lockState.getOrNull() == true
        if (shouldLock && !showLock && hasAccounts == true) {
            showLock = true
        }
    }

    when {
        hasAccounts == null -> {
            // Cargando estado inicial
        }

        hasAccounts == false -> {
            FirstRunScreen(
                onAccountCreated = { showLock = false }
            )
        }

        showLock -> {
            LockScreen(
                onUnlocked = { showLock = false }
            )
        }

        else -> {
            AppNavHost(navController)
        }
    }
}
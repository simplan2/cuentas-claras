package com.simplan.cuentasclaras

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.simplan.cuentasclaras.common.Result
import com.simplan.cuentasclaras.ui.MainViewModel
import com.simplan.cuentasclaras.ui.navigation.AppNavHost
import com.simplan.cuentasclaras.ui.screen.firstrun.FirstRunScreen
import com.simplan.cuentasclaras.ui.screen.lock.LockScreen
import com.simplan.cuentasclaras.ui.theme.CuentasClarasTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CuentasClarasTheme {
                MainNavHost()
            }
        }
    }
}

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = hiltViewModel()
    val context = LocalContext.current

    var isFirstRun by rememberSaveable { mutableStateOf(true) }
    var showLock by rememberSaveable { mutableStateOf(false) }

    // ---------- Chequear first run ----------
    LaunchedEffect(Unit) {
        val sharedPref = context.getSharedPreferences(
            "cuentasclaras_prefs",
            Context.MODE_PRIVATE
        )
        isFirstRun = !sharedPref.getBoolean("first_run_completed", false)
    }

    // ---------- Chequear estado de bloqueo ----------
    val lockState by mainViewModel
        .observeLockState
        .collectAsStateWithLifecycle(initialValue = Result.success(false))

    LaunchedEffect(lockState) {
        val shouldLock = lockState.isSuccess() && lockState.getOrNull() == true
        if (shouldLock && !showLock && !isFirstRun) {
            showLock = true
        }
    }

    when {
        isFirstRun -> {
            FirstRunScreen(
                onAccountCreated = {
                    val sharedPref = context.getSharedPreferences(
                        "cuentasclaras_prefs",
                        Context.MODE_PRIVATE
                    )
                    sharedPref.edit().putBoolean("first_run_completed", true).apply()
                    isFirstRun = false
                }
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
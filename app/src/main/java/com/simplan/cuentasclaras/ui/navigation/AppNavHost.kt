package com.simplan.cuentasclaras.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.simplan.cuentasclaras.ui.screen.accountdetail.AccountDetailScreen
import com.simplan.cuentasclaras.ui.screen.accountmanager.AccountManagerScreen
import com.simplan.cuentasclaras.ui.screen.firstrun.FirstRunScreen
import com.simplan.cuentasclaras.ui.screen.home.HomeScreen
import com.simplan.cuentasclaras.ui.screen.lock.LockScreen
import com.simplan.cuentasclaras.ui.screen.settings.SettingsScreen

@Composable
fun AppNavHost(navController: NavHostController, startDestination: String = Routes.firstRun()) {
    NavHost(navController, startDestination) {
        composable(Routes.firstRun()) {
            FirstRunScreen(onAccountCreated = { navController.navigate(Routes.home()) { popUpTo(Routes.firstRun()) { inclusive = true } } })
        }
        composable(Routes.home()) {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Routes.settings()) },
                onNavigateToAccountManager = { navController.navigate(Routes.accountManager()) }
            )
        }
        composable(
            route = Routes.accountManager(),
            arguments = listOf()
        ) {
            AccountManagerScreen(
                onNavigateToAccountDetail = { accountId ->
                    navController.navigate(Routes.accountDetail(accountId))
                },
                onNavigateToHome = { navController.navigate(Routes.home()) { popUpTo(Routes.accountManager()) { inclusive = true } } }
            )
        }
        composable(
            route = "account_detail/{accountId}"
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId")?.toLongOrNull() ?: 0L
            AccountDetailScreen(
                accountId = accountId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.settings()) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Routes.lock()) {
            LockScreen(onUnlocked = { navController.navigate(Routes.home()) { popUpTo(Routes.lock()) { inclusive = true } } })
        }
        composable(Routes.quickAmounts()) {
            // QuickAmountsScreen will be implemented later
        }
    }
}
package com.simplan.cuentasclaras.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.simplan.cuentasclaras.ui.screen.accountdetail.AccountDetailScreen
import com.simplan.cuentasclaras.ui.screen.accountmanager.AccountManagerScreen
import com.simplan.cuentasclaras.ui.screen.home.HomeScreen
import com.simplan.cuentasclaras.ui.screen.lock.LockScreen
import com.simplan.cuentasclaras.ui.screen.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: Routes = Routes.Home
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<Routes.Home> {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Routes.Settings) },
                onNavigateToAccountManager = { navController.navigate(Routes.AccountManager) },
                onNavigateToAccountDetail = { accountId ->
                    navController.navigate(Routes.AccountDetail(accountId))
                }
            )
        }
        composable<Routes.AccountManager> {
            AccountManagerScreen(
                onNavigateToAccountDetail = { accountId ->
                    navController.navigate(Routes.AccountDetail(accountId))
                },
                onNavigateToHome = {
                    navController.navigate(Routes.Home) {
                        popUpTo<Routes.AccountManager> { inclusive = true }
                    }
                }
            )
        }
        composable<Routes.AccountDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.AccountDetail>()
            AccountDetailScreen(
                accountId = route.accountId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<Routes.Settings> {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable<Routes.Lock> {
            LockScreen(
                onUnlocked = {
                    navController.navigate(Routes.Home) {
                        popUpTo<Routes.Lock> { inclusive = true }
                    }
                }
            )
        }
    }
}

package com.simplan.cuentasclaras.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.simplan.cuentasclaras.ui.Icons
import com.simplan.cuentasclaras.ui.screen.accountdetail.AccountDetailScreen
import com.simplan.cuentasclaras.ui.screen.accounthistory.AccountHistoryScreen
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
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val topLevelRoutes = listOf(Routes.Home, Routes.AccountManager, Routes.Settings)
    val isTopLevel = topLevelRoutes.any { route ->
        currentDestination?.hasRoute(route::class) == true
    }
    val showBar = isTopLevel
    val isHome = currentDestination?.hasRoute(Routes.Home::class) == true

    // Nivel 2: en un tab raíz distinto de Inicio, Atrás del sistema vuelve a Inicio
    // en lugar de salir de la app.
    BackHandler(enabled = isTopLevel && !isHome) {
        navController.navigate(Routes.Home) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToTab(route: Routes) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.weight(1f)
        ) {
            composable<Routes.Home> {
                HomeScreen(
                    onNavigateToSettings = { navigateToTab(Routes.Settings) },
                    onNavigateToAccountManager = { navigateToTab(Routes.AccountManager) },
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
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAccountHistory = { accountId ->
                        navController.navigate(Routes.AccountHistory(accountId))
                    }
                )
            }
            composable<Routes.AccountHistory> { backStackEntry ->
                val route = backStackEntry.toRoute<Routes.AccountHistory>()
                AccountHistoryScreen(
                    accountId = route.accountId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Routes.Settings> {
                SettingsScreen()
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

        if (showBar) {
            CuentasClarasBottomBar(
                navController = navController,
                currentDestination = currentDestination
            )
        }
    }
}

private data class BottomTab(
    val route: Routes,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun CuentasClarasBottomBar(
    navController: NavHostController,
    currentDestination: NavDestination?
) {
    val tabs = listOf(
        BottomTab(Routes.Home, "Inicio", Icons.wallet()),
        BottomTab(Routes.AccountManager, "Cuentas", Icons.accountBalance()),
        BottomTab(Routes.Settings, "Ajustes", Icons.settings())
    )
    NavigationBar {
        tabs.forEach { tab ->
            val selected = currentDestination?.hasRoute(tab.route::class) == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label
                    )
                },
                label = { Text(tab.label) }
            )
        }
    }
}
package com.simplan.cuentasclaras.ui.navigation

sealed interface Routes {
    data object FirstRun : Routes
    data object Home : Routes
    data object AccountManager : Routes
    data class AccountDetail(val accountId: Long) : Routes
    data object Settings : Routes
    data object Lock : Routes
    data object QuickAmounts : Routes

    companion object {
        fun firstRun() = "first_run"
        fun home() = "home"
        fun accountManager() = "account_manager"
        fun accountDetail(accountId: Long) = "account_detail/$accountId"
        fun settings() = "settings"
        fun lock() = "lock"
        fun quickAmounts() = "quick_amounts"
    }
}
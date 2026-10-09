package com.simplan.cuentasclaras.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Routes {

    @Serializable
    data object Home : Routes

    @Serializable
    data object AccountManager : Routes

    @Serializable
    data class AccountDetail(val accountId: Long) : Routes

    @Serializable
    data class AccountHistory(val accountId: Long) : Routes

    @Serializable
    data object Settings : Routes

    @Serializable
    data object Lock : Routes
}

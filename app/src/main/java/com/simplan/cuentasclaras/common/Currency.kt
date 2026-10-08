package com.simplan.cuentasclaras.common

import kotlinx.serialization.Serializable

@Serializable
data class Currency(
    val code: String,
    val symbol: String,
    val name: String,
    val decimalDigits: Int = 2,
    val symbolOnLeft: Boolean = true,
    val spaceBetween: Boolean = false
) {
    companion object {
        val EUR = Currency("EUR", "€", "Euro", symbolOnLeft = false, spaceBetween = true)
        val USD = Currency("USD", "$", "US Dollar")
        val GBP = Currency("GBP", "£", "British Pound")
        val MXN = Currency("MXN", "$", "Mexican Peso")
        val ARS = Currency("ARS", "$", "Argentine Peso")
        val COP = Currency("COP", "$", "Colombian Peso")
        val PEN = Currency("PEN", "S/", "Peruvian Sol", symbolOnLeft = true, spaceBetween = true)
        val CLP = Currency("CLP", "$", "Chilean Peso")
        val BOB = Currency("BOB", "Bs.", "Bolivian Boliviano", symbolOnLeft = true, spaceBetween = true)
        val UYU = Currency("UYU", "\$U", "Uruguayan Peso", symbolOnLeft = true, spaceBetween = true)

        val all = listOf(EUR, USD, GBP, MXN, ARS, COP, PEN, CLP, BOB, UYU)

        fun getByCode(code: String): Currency = all.find { it.code == code } ?: EUR
    }

    fun format(cents: Long): String {
        val amount = cents / 100.0
        val formatted = "%.${decimalDigits}f".format(amount).replace('.', ',')
        val withSymbol = if (symbolOnLeft) "$symbol $formatted" else "$formatted $symbol"
        return if (spaceBetween) withSymbol.replace("$symbol", "$symbol ").replace("$formatted", "$formatted ") else withSymbol
    }
}
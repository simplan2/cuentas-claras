package com.simplan.cuentasclaras.common

object Money {
    fun format(cents: Long, currency: Currency): String {
        return currency.format(cents)
    }

    fun format(cents: Long, currencyCode: String): String {
        return Currency.getByCode(currencyCode).format(cents)
    }

    fun parse(amount: String, currency: Currency): Long {
        val cleanAmount = amount
            .replace(currency.symbol, "")
            .replace(" ", "")
            .replace(",", ".")
            .trim()
        return try {
            (cleanAmount.toDouble() * 100).toLong()
        } catch (e: NumberFormatException) {
            0
        }
    }

    fun parse(amount: String, currencyCode: String): Long {
        return parse(amount, Currency.getByCode(currencyCode))
    }

    fun abs(cents: Long): Long = kotlin.math.abs(cents)

    fun negate(cents: Long): Long = -cents

    fun sum(centsList: List<Long>): Long = centsList.sum()
}
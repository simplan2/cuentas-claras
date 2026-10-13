package com.simplan.cuentasclaras.common

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {

    @Test
    fun format_pen_puts_symbol_left_with_space_and_comma() {
        assertEquals("S/ 12,50", Money.format(1250, Currency.PEN))
    }

    @Test
    fun format_usd_puts_symbol_left_without_space() {
        assertEquals("\$12,50", Money.format(1250, Currency.USD))
    }

    @Test
    fun format_eur_puts_symbol_right_with_space() {
        assertEquals("12,50 €", Money.format(1250, Currency.EUR))
    }

    @Test
    fun format_zero_uses_two_decimals() {
        assertEquals("S/ 0,00", Money.format(0, Currency.PEN))
    }

    @Test
    fun format_negative_keeps_sign() {
        assertEquals("S/ -9,99", Money.format(-999, Currency.PEN))
    }

    @Test
    fun format_by_currency_code() {
        assertEquals("S/ 5,00", Money.format(500, "PEN"))
    }

    @Test
    fun parse_pen_with_symbol_and_comma() {
        assertEquals(1250L, Money.parse("S/ 12,50", Currency.PEN))
    }

    @Test
    fun parse_usd_with_symbol() {
        assertEquals(1250L, Money.parse("\$12,50", Currency.USD))
    }

    @Test
    fun parse_plain_number() {
        assertEquals(1000L, Money.parse("10", Currency.PEN))
    }

    @Test
    fun parse_invalid_returns_zero() {
        assertEquals(0L, Money.parse("abc", Currency.PEN))
    }

    @Test
    fun parse_and_format_round_trip() {
        val original = Money.format(123456, Currency.PEN)
        assertEquals(123456L, Money.parse(original, Currency.PEN))
    }
}

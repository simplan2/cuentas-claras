package com.simplan.cuentasclaras.domain.entity

import com.simplan.cuentasclaras.common.Currency
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Account(
    val id: Long = 0L,
    val name: String,
    val currencyCode: String = "PEN",
    val icon: String = "wallet",
    val color: Long = 0xFF2196F3.toLong(),
    val isOpen: Boolean = true,
    val isDefault: Boolean = false,
    val balanceCents: Long = 0L,
    val sortOrder: Int = 0,
    val createdAt: Long = Instant.now().toEpochMilli(),
    val updatedAt: Long = Instant.now().toEpochMilli()
) {
    val currency: Currency
        get() = Currency.getByCode(currencyCode)

    val currencySymbol: String
        get() = currency.symbol

    fun copyWith(
        name: String = this.name,
        currencyCode: String = this.currencyCode,
        icon: String = this.icon,
        color: Long = this.color,
        isOpen: Boolean = this.isOpen,
        isDefault: Boolean = this.isDefault,
        balanceCents: Long = this.balanceCents,
        sortOrder: Int = this.sortOrder
    ): Account = copy(
        name = name,
        currencyCode = currencyCode,
        icon = icon,
        color = color,
        isDefault = isDefault,
        balanceCents = balanceCents,
        sortOrder = sortOrder,
        updatedAt = Instant.now().toEpochMilli()
    )

    fun close(): Account = copyWith(isOpen = false)
    fun open(): Account = copyWith(isOpen = true)
    fun setAsDefault(): Account = copyWith(isDefault = true)
    fun unsetAsDefault(): Account = copyWith(isDefault = false)
}
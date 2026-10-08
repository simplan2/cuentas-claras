package com.simplan.cuentasclaras.domain.entity

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class QuickAmount(
    val id: Long = 0L,
    val amountCents: Long,
    val label: String = "",
    val icon: String = "flash_on",
    val color: Long = 0xFF4CAF50.toLong(),
    val sortOrder: Int = 0,
    val isEnabled: Boolean = true,
    val createdAt: Long = Instant.now().toEpochMilli()
) {
    fun copyWith(
        amountCents: Long = this.amountCents,
        label: String = this.label,
        icon: String = this.icon,
        color: Long = this.color,
        sortOrder: Int = this.sortOrder,
        isEnabled: Boolean = this.isEnabled
    ): QuickAmount = copy(
        amountCents = amountCents,
        label = label,
        icon = icon,
        color = color,
        sortOrder = sortOrder,
        isEnabled = isEnabled
    )
}
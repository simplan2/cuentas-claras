package com.simplan.cuentasclaras.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.simplan.cuentasclaras.domain.entity.QuickAmount
import kotlinx.serialization.Serializable

@Entity(tableName = "quick_amounts")
@Serializable
data class QuickAmountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountCents: Long,
    val label: String = "",
    val icon: String = "flash_on",
    val color: Long = 0xFF4CAF50.toLong(),
    val sortOrder: Int = 0,
    val isEnabled: Boolean = true,
    val createdAt: Long = 0
) {
    fun toDomain(): QuickAmount = QuickAmount(
        id = id,
        amountCents = amountCents,
        label = label,
        icon = icon,
        color = color,
        sortOrder = sortOrder,
        isEnabled = isEnabled,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(quickAmount: QuickAmount): QuickAmountEntity = QuickAmountEntity(
            id = quickAmount.id,
            amountCents = quickAmount.amountCents,
            label = quickAmount.label,
            icon = quickAmount.icon,
            color = quickAmount.color,
            sortOrder = quickAmount.sortOrder,
            isEnabled = quickAmount.isEnabled,
            createdAt = quickAmount.createdAt
        )
    }
}
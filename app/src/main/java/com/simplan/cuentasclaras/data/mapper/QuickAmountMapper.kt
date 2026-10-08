package com.simplan.cuentasclaras.data.mapper

import com.simplan.cuentasclaras.data.entity.QuickAmountEntity
import com.simplan.cuentasclaras.domain.entity.QuickAmount

fun QuickAmountEntity.toDomain(): QuickAmount = QuickAmount(
    id = id,
    amountCents = amountCents,
    label = label,
    icon = icon,
    color = color,
    sortOrder = sortOrder,
    isEnabled = isEnabled,
    createdAt = createdAt
)

fun QuickAmount.toEntity(): QuickAmountEntity = QuickAmountEntity(
    id = id,
    amountCents = amountCents,
    label = label,
    icon = icon,
    color = color,
    sortOrder = sortOrder,
    isEnabled = isEnabled,
    createdAt = createdAt
)

fun List<QuickAmountEntity>.toDomain(): List<QuickAmount> = map { it.toDomain() }
fun List<QuickAmount>.toEntity(): List<QuickAmountEntity> = map { it.toEntity() }
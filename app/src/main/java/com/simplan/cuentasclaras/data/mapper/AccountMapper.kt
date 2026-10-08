package com.simplan.cuentasclaras.data.mapper

import com.simplan.cuentasclaras.data.entity.AccountEntity
import com.simplan.cuentasclaras.domain.entity.Account

fun AccountEntity.toDomain(): Account = Account(
    id = id,
    name = name,
    currencyCode = currencyCode,
    icon = icon,
    color = color,
    isOpen = isOpen,
    isDefault = isDefault,
    balanceCents = balanceCents,
    sortOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Account.toEntity(): AccountEntity = AccountEntity(
    id = id,
    name = name,
    currencyCode = currencyCode,
    icon = icon,
    color = color,
    isOpen = isOpen,
    isDefault = isDefault,
    balanceCents = balanceCents,
    sortOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun List<AccountEntity>.toDomain(): List<Account> = map { it.toDomain() }
fun List<Account>.toEntity(): List<AccountEntity> = map { it.toEntity() }
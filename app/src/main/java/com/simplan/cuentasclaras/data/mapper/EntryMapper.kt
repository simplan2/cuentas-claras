package com.simplan.cuentasclaras.data.mapper

import com.simplan.cuentasclaras.data.entity.EntryEntity
import com.simplan.cuentasclaras.domain.entity.Entry

fun EntryEntity.toDomain(): Entry = Entry(
    id = id,
    accountId = accountId,
    categoryId = categoryId,
    amountCents = amountCents,
    type = Entry.EntryType.fromInt(type),
    note = note,
    date = date,
    createdAt = createdAt
)

fun Entry.toEntity(): EntryEntity = EntryEntity(
    id = id,
    accountId = accountId,
    categoryId = categoryId,
    amountCents = amountCents,
    type = type.value,
    note = note,
    date = date,
    createdAt = createdAt
)

fun List<EntryEntity>.toDomain(): List<Entry> = map { it.toDomain() }
fun List<Entry>.toEntity(): List<EntryEntity> = map { it.toEntity() }
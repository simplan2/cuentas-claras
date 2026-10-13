package com.simplan.cuentasclaras.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class BackupPayload(
    val schemaVersion: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val settings: Settings = Settings(),
    val accounts: List<Account> = emptyList(),
    val entries: List<Entry> = emptyList(),
    val quickAmounts: List<QuickAmount> = emptyList()
)

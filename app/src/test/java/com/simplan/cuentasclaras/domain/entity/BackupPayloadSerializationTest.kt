package com.simplan.cuentasclaras.domain.entity

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupPayloadSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun round_trip_preserves_data() {
        val payload = BackupPayload(
            exportedAt = 123L,
            accounts = listOf(
                Account(id = 1L, name = "Ahorros", currencyCode = "USD")
            ),
            entries = listOf(
                Entry(
                    id = 5L,
                    accountId = 1L,
                    categoryId = 2L,
                    amountCents = 1500L,
                    type = Entry.EntryType.EXPENSE,
                    note = "Bus",
                    date = 1000L,
                    createdAt = 1000L
                )
            )
        )

        val encoded = json.encodeToString(BackupPayload.serializer(), payload)
        val decoded = json.decodeFromString(BackupPayload.serializer(), encoded)

        assertEquals(payload, decoded)
    }

    @Test
    fun category_id_survives_round_trip() {
        val payload = BackupPayload(
            entries = listOf(
                Entry(id = 1L, accountId = 1L, categoryId = 7L, amountCents = 100L,
                    type = Entry.EntryType.EXPENSE)
            )
        )

        val encoded = json.encodeToString(BackupPayload.serializer(), payload)
        val decoded = json.decodeFromString(BackupPayload.serializer(), encoded)

        assertEquals(7L, decoded.entries.first().categoryId)
    }

    @Test
    fun unknown_keys_are_ignored() {
        val raw = """
            {"schemaVersion":1,"exportedAt":1,"futureField":"x",
             "accounts":[],"entries":[],"quickAmounts":[]}
        """.trimIndent()

        val decoded = json.decodeFromString(BackupPayload.serializer(), raw)

        assertEquals(1, decoded.schemaVersion)
    }
}

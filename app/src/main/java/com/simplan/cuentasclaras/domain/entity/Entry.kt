package com.simplan.cuentasclaras.domain.entity

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Entry(
    val id: Long = 0L,
    val accountId: Long,
    val categoryId: Long = 0L,
    val amountCents: Long,
    val type: EntryType,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
) {
    @Serializable(with = EntryTypeSerializer::class)
    enum class EntryType(val value: Int) {
        INCOME(1),
        EXPENSE(-1);

        companion object {
            fun fromInt(value: Int): EntryType = when (value) {
                1 -> INCOME
                -1 -> EXPENSE
                else -> throw IllegalArgumentException("Invalid entry type: $value")
            }
        }
    }

    val isIncome: Boolean = type == EntryType.INCOME
    val isExpense: Boolean = type == EntryType.EXPENSE

    val signedAmountCents: Long
        get() = amountCents * type.value

    fun copyWith(
        accountId: Long = this.accountId,
        categoryId: Long = this.categoryId,
        amountCents: Long = this.amountCents,
        type: EntryType = this.type,
        note: String = this.note,
        date: Long = this.date
    ): Entry = copy(
        accountId = accountId,
        categoryId = categoryId,
        amountCents = amountCents,
        type = type,
        note = note,
        date = date
    )
}

object EntryTypeSerializer : KSerializer<Entry.EntryType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("EntryType", kotlinx.serialization.descriptors.PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Entry.EntryType) {
        encoder.encodeInt(value.value)
    }

    override fun deserialize(decoder: Decoder): Entry.EntryType {
        return Entry.EntryType.fromInt(decoder.decodeInt())
    }
}
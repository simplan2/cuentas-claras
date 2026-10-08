package com.simplan.cuentasclaras.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.simplan.cuentasclaras.domain.entity.Entry
import kotlinx.serialization.Serializable

@Entity(
    tableName = "entries",
    foreignKeys = [
        ForeignKey(entity = AccountEntity::class, parentColumns = ["id"], childColumns = ["accountId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index("accountId"),
        Index("date")
    ]
)
@Serializable
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val categoryId: Long = 0,
    val amountCents: Long,
    val type: Int,
    val note: String = "",
    val date: Long = 0,
    val createdAt: Long = 0
) {
    fun toDomain(): Entry = Entry(
        id = id,
        accountId = accountId,
        categoryId = categoryId,
        amountCents = amountCents,
        type = Entry.EntryType.fromInt(type),
        note = note,
        date = date,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(entry: Entry): EntryEntity = EntryEntity(
            id = entry.id,
            accountId = entry.accountId,
            categoryId = entry.categoryId,
            amountCents = entry.amountCents,
            type = entry.type.value,
            note = entry.note,
            date = entry.date,
            createdAt = entry.createdAt
        )
    }
}
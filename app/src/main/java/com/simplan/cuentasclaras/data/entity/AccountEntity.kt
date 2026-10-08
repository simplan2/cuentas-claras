package com.simplan.cuentasclaras.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.simplan.cuentasclaras.domain.entity.Account
import kotlinx.serialization.Serializable

@Entity(tableName = "accounts")
@Serializable
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val currencyCode: String = "PEN",
    val icon: String = "wallet",
    val color: Long = 0xFF2196F3.toLong(),
    val isOpen: Boolean = true,
    val isDefault: Boolean = false,
    val balanceCents: Long = 0,
    val sortOrder: Int = 0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    fun toDomain(): Account = Account(
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

    companion object {
        fun fromDomain(account: Account): AccountEntity = AccountEntity(
            id = account.id,
            name = account.name,
            currencyCode = account.currencyCode,
            icon = account.icon,
            color = account.color,
            isOpen = account.isOpen,
            isDefault = account.isDefault,
            balanceCents = account.balanceCents,
            sortOrder = account.sortOrder,
            createdAt = account.createdAt,
            updatedAt = account.updatedAt
        )
    }
}
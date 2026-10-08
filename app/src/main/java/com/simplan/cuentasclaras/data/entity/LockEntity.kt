package com.simplan.cuentasclaras.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "lock_data")
@Serializable
data class LockEntity(
    @PrimaryKey val id: Int = 1,
    val isLocked: Boolean = false,
    val pinHash: String = "",
    val biometricEnabled: Boolean = false,
    val autoLockDelayMinutes: Int = 5,
    val failedAttempts: Int = 0,
    val lastFailedAt: Long = 0
)
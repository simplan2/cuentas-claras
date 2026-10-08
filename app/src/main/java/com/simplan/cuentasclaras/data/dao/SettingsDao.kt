package com.simplan.cuentasclaras.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.simplan.cuentasclaras.data.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun get(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSuspend(): SettingsEntity?

    @Update
    suspend fun update(settings: SettingsEntity): Int
}
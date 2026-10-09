package com.simplan.cuentasclaras.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.simplan.cuentasclaras.data.entity.LockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LockDao {
    @Query("SELECT * FROM lock_data WHERE id = 1")
    fun get(): Flow<LockEntity?>

    @Query("SELECT * FROM lock_data WHERE id = 1")
    suspend fun getSuspend(): LockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lock: LockEntity): Long

    @Update
    suspend fun update(lock: LockEntity): Int
}
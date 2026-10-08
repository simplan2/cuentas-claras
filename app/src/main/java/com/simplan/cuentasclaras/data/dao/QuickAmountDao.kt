package com.simplan.cuentasclaras.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.simplan.cuentasclaras.data.entity.QuickAmountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuickAmountDao {
    @Query("SELECT * FROM quick_amounts ORDER BY sortOrder ASC")
    fun getAll(): Flow<List<QuickAmountEntity>>

    @Query("SELECT * FROM quick_amounts WHERE isEnabled = 1 ORDER BY sortOrder ASC")
    fun getEnabled(): Flow<List<QuickAmountEntity>>

    @Query("SELECT * FROM quick_amounts WHERE id = :id")
    fun getById(id: Long): Flow<QuickAmountEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(quickAmount: QuickAmountEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(quickAmounts: List<QuickAmountEntity>): List<Long>

    @Update
    suspend fun update(quickAmount: QuickAmountEntity): Int

    @Update
    suspend fun updateAll(quickAmounts: List<QuickAmountEntity>): Int

    @Query("DELETE FROM quick_amounts WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Transaction
    suspend fun reorder(ids: List<Long>): Int {
        var count = 0
        for ((index, id) in ids.withIndex()) {
            updateSortOrder(id, index)
            count++
        }
        return count
    }

    @Query("UPDATE quick_amounts SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int): Int
}
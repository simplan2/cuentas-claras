package com.simplan.cuentasclaras.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.simplan.cuentasclaras.data.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY sortOrder ASC")
    fun getAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE isOpen = 1 ORDER BY sortOrder ASC")
    fun getOpenAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getById(id: Long): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE isDefault = 1 AND isOpen = 1 LIMIT 1")
    fun getDefaultAccount(): Flow<AccountEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(accounts: List<AccountEntity>): List<Long>

    @Update
    suspend fun update(account: AccountEntity): Int

    @Update
    suspend fun updateAll(accounts: List<AccountEntity>): Int

    @Query("DELETE FROM accounts WHERE id = :id")
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

    @Query("UPDATE accounts SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int): Int

    @Query("SELECT COUNT(*) FROM accounts WHERE isOpen = 1")
    suspend fun countOpen(): Int

    @Query("SELECT * FROM accounts ORDER BY sortOrder ASC")
    suspend fun getAllSuspend(): List<AccountEntity>

    @Query("DELETE FROM accounts")
    suspend fun deleteAll(): Int
}
package com.simplan.cuentasclaras.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.simplan.cuentasclaras.data.entity.EntryEntity
import com.simplan.cuentasclaras.data.entity.MonthlyTotalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Query("SELECT * FROM entries WHERE accountId = :accountId ORDER BY date DESC, createdAt DESC")
    fun getByAccount(accountId: Long): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE accountId = :accountId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getByAccountAndDateRange(accountId: Long, startDate: Long, endDate: Long): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getByCategory(categoryId: Long): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE id = :id")
    fun getById(id: Long): Flow<EntryEntity?>

    @Query("SELECT COALESCE(SUM(amountCents * type), 0) FROM entries WHERE accountId = :accountId")
    suspend fun getTotalByAccount(accountId: Long): Long

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM entries WHERE accountId = :accountId AND type = :type")
    suspend fun getTotalByAccountAndType(accountId: Long, type: Int): Long

    @Query("SELECT strftime('%m', date/1000, 'unixepoch') as month, " +
           "strftime('%Y', date/1000, 'unixepoch') as year, " +
           "COALESCE(SUM(CASE WHEN type = 1 THEN amountCents ELSE 0 END), 0) as income, " +
           "COALESCE(SUM(CASE WHEN type = -1 THEN amountCents ELSE 0 END), 0) as expense " +
           "FROM entries " +
           "WHERE accountId = :accountId AND strftime('%Y', date/1000, 'unixepoch') = :year " +
           "GROUP BY month, year " +
           "ORDER BY year, month")
    suspend fun getMonthlyTotals(accountId: Long, year: String): List<MonthlyTotalEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: EntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entries: List<EntryEntity>): List<Long>

    @Update
    suspend fun update(entry: EntryEntity): Int

    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Query("DELETE FROM entries WHERE accountId = :accountId")
    suspend fun deleteByAccount(accountId: Long): Int

    @Query("SELECT * FROM entries")
    suspend fun getAllSuspend(): List<EntryEntity>

    @Query("DELETE FROM entries")
    suspend fun deleteAll(): Int
}
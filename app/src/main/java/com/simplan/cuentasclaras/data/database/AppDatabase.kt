package com.simplan.cuentasclaras.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simplan.cuentasclaras.data.dao.AccountDao
import com.simplan.cuentasclaras.data.dao.EntryDao
import com.simplan.cuentasclaras.data.dao.QuickAmountDao
import com.simplan.cuentasclaras.data.dao.SettingsDao
import com.simplan.cuentasclaras.data.entity.AccountEntity
import com.simplan.cuentasclaras.data.entity.EntryEntity
import com.simplan.cuentasclaras.data.entity.QuickAmountEntity
import com.simplan.cuentasclaras.data.entity.SettingsEntity

@Database(
    entities = [
        AccountEntity::class,
        EntryEntity::class,
        QuickAmountEntity::class,
        SettingsEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun entryDao(): EntryDao
    abstract fun quickAmountDao(): QuickAmountDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        // El PIN dejó de vivir en Room (se movió a EncryptedSharedPreferences),
        // por lo que se elimina la tabla lock_data.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS lock_data")
            }
        }
    }
}

class Converters {
    @androidx.room.TypeConverter
    fun fromTimestamp(value: Long?): java.util.Date? = value?.let { java.util.Date(it) }

    @androidx.room.TypeConverter
    fun fromDate(date: java.util.Date?): Long? = date?.time
}

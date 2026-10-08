package com.simplan.cuentasclaras.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simplan.cuentasclaras.data.dao.AccountDao
import com.simplan.cuentasclaras.data.dao.EntryDao
import com.simplan.cuentasclaras.data.dao.LockDao
import com.simplan.cuentasclaras.data.dao.QuickAmountDao
import com.simplan.cuentasclaras.data.dao.SettingsDao
import com.simplan.cuentasclaras.data.entity.*

@Database(
    entities = [
        AccountEntity::class,
        EntryEntity::class,
        QuickAmountEntity::class,
        SettingsEntity::class,
        LockEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun entryDao(): EntryDao
    abstract fun quickAmountDao(): QuickAmountDao
    abstract fun settingsDao(): SettingsDao
    abstract fun lockDao(): LockDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cuentasclaras.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Future migrations
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
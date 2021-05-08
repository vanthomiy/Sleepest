package com.doitstudio.sleepest_master.sleepcalculation.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.doitstudio.sleepest_master.storage.db.*


private const val DATABASE_NAME = "sleep_calculation_database"

/**
 * Stores all sleep segment data.
 */

@Database(
    entities = [SleepApiRawDataEntity::class],
    version = 3,
    exportSchema = true
)

@TypeConverters(Converters::class)
abstract class SleepCalculationDatabase : RoomDatabase() {

    abstract fun sleepApiRawDataDao(): SleepApiRawDataDao

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: SleepCalculationDatabase? = null

        lateinit var instance: SleepCalculationDatabase

        fun getDatabase(context: Context): SleepCalculationDatabase {
            return INSTANCE ?: synchronized(this) {
                instance = Room.databaseBuilder(
                    context,
                    SleepCalculationDatabase::class.java,
                    DATABASE_NAME
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // Migration is not part of this sample.
                    .fallbackToDestructiveMigration()
                        .addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                            }
                        })
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
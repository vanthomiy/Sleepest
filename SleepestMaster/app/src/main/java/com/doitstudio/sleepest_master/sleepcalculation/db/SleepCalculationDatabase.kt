package com.doitstudio.sleepest_master.sleepcalculation.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.doitstudio.sleepest_master.storage.db.*


private const val DATABASE_NAME = "sleep_calculation_database"

/**
 * Stores all sleep segment data.
 */

@Database(
    entities = [SleepStateModelEntity::class, SleepTimeModelEntity::class, SleepStateFactorModelEntity::class, SleepTimeFactorModelEntity::class],
    version = 3,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class SleepCalculationDatabase : RoomDatabase() {

    abstract fun sleepStateModelDao(): SleepStateModelDao
    abstract fun sleepTimeModelDao(): SleepTimeModelDao
    abstract fun sleepStateFactorModelDao(): SleepStateFactorModelDao
    abstract fun sleepTimeFactorModelDao(): SleepTimeFactorModelDao

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: SleepDatabase? = null

        fun getDatabase(context: Context): SleepDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    SleepDatabase::class.java,
                    DATABASE_NAME
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // Migration is not part of this sample.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
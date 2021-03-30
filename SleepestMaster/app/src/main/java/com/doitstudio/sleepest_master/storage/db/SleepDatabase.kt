package com.doitstudio.sleepest_master.storage.db

import android.content.Context
import androidx.room.*
import com.doitstudio.sleepest_master.model.data.sleepcalculation.SleepSegmentEntity
import com.doitstudio.sleepest_master.model.data.sleepcalculation.UserSleepSessionEntity

private const val DATABASE_NAME = "sleepest_database"

/**
 * Stores all sleep segment data.
 */

@Database(
        entities = [SleepApiRawDataEntity::class, SleepSegmentEntity::class, UserSleepSessionEntity::class],
        version = 3,
        exportSchema = false
)

@TypeConverters(Converters::class)
abstract class SleepDatabase : RoomDatabase() {

    abstract fun sleepApiRawDataDao(): SleepApiRawDataDao
    abstract fun sleepDataDao(): SleepSegmentDao
    abstract fun userSleepSegmentDataDao(): UserSleepSessionDao

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
package com.doitstudio.sleepest_master.storage.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase

private const val DATABASE_NAME = "sleepest_database"

/**
 * The database data that we store in a sql database with room
 */

@Database(
        entities = [SleepApiRawDataEntity::class, SleepSegmentEntity::class, UserSleepSessionEntity::class, AlarmEntity::class, ActivityApiRawDataEntity::class],
        version = 6,
        exportSchema = false
)

@TypeConverters(Converters::class)
abstract class SleepDatabase : RoomDatabase() {

    abstract fun sleepApiRawDataDao(): SleepApiRawDataDao
    abstract fun sleepDataDao(): SleepSegmentDao
    abstract fun userSleepSessionDao(): UserSleepSessionDao
    abstract fun alarmDao(): AlarmDao
    abstract fun activityApiRawDataDao(): ActivityApiRawDataDao

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: SleepDatabase? = null
        lateinit var instance:SleepDatabase

        /**
         * This should only once be called by the [MainApplication] to provide a singleton database
         */
        fun getDatabase(context: Context): SleepDatabase {
            return INSTANCE ?: synchronized(this) {
                instance = Room.databaseBuilder(
                        context,
                        SleepDatabase::class.java,
                        DATABASE_NAME
                )
                        .addCallback(object:RoomDatabase.Callback(){
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                //instance.alarmDao().setupAlarmDatabase()
                            }
                        })
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
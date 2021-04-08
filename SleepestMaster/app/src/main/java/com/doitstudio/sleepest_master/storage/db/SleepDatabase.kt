package com.doitstudio.sleepest_master.storage.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.doitstudio.sleepest_master.model.data.sleepcalculation.SleepSegmentEntity
import com.doitstudio.sleepest_master.model.data.sleepcalculation.UserSleepSessionEntity

private const val DATABASE_NAME = "sleepest_database"

/**
 * Stores all sleep segment data.
 */

@Database(
        entities = [SleepApiRawDataEntity::class, SleepSegmentEntity::class, UserSleepSessionEntity::class, AlarmEntity::class],
        version = 3,
        exportSchema = false
)

@TypeConverters(Converters::class)
abstract class SleepDatabase : RoomDatabase() {

    abstract fun sleepApiRawDataDao(): SleepApiRawDataDao
    abstract fun sleepDataDao(): SleepSegmentDao
    abstract fun userSleepSegmentDataDao(): UserSleepSessionDao
    abstract fun alarmDao():AlarmDao

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: SleepDatabase? = null
        lateinit var instance:SleepDatabase

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
                                instance.alarmDao().setupAlarmDatabase()
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
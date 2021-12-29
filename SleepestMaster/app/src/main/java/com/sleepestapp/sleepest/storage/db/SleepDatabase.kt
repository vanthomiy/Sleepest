package com.sleepestapp.sleepest.storage.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

private const val DATABASE_NAME = "sleepest_database"

/**
 * The database data that we store in a sql database with room
 */

@Database(
    entities = [SleepApiRawDataEntity::class, UserSleepSessionEntity::class, AlarmEntity::class, ActivityApiRawDataEntity::class],
    version = 9,
    exportSchema = true
)

@TypeConverters(Converters::class)
abstract class SleepDatabase : RoomDatabase() {

    abstract fun sleepApiRawDataDao(): SleepApiRawDataDao
    abstract fun userSleepSessionDao(): UserSleepSessionDao
    abstract fun alarmDao(): AlarmDao
    abstract fun activityApiRawDataDao(): ActivityApiRawDataDao


    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: SleepDatabase? = null
        lateinit var instance:SleepDatabase

        /**
         * Manual migration for the database when changes are being made
         */
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the neccessary columns / tables
                // database.execSQL("ALTER TABLE Book ADD COLUMN pub_year INTEGER")
            }
        }

        /**
         * Manual migration for the database when changes are being made
         */
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {

                //database.execSQL("ALTER TABLE sleep_api_raw_data_table ADD COLUMN confidence_temp REAL")
                //database.execSQL(   "UPDATE sleep_api_raw_data_table SET confidence_temp = CAST(confidence as REAL)")
                //database.execSQL("ALTER TABLE sleep_api_raw_data_table RENAME COLUMN confidence_temp TO confidence")
            }
        }


        /**
         * This should only once be called by the MainApplication to provide a singleton database
         */
        fun getDatabase(context: Context): SleepDatabase {
            return INSTANCE ?: synchronized(this) {
                instance = Room.databaseBuilder(
                        context,
                        SleepDatabase::class.java,
                        DATABASE_NAME
                )
                        .addCallback(object:RoomDatabase.Callback(){
                        })
                        // .fallbackToDestructiveMigration()
                        // Migrate to new version
                        .addMigrations(MIGRATION_7_8)
                        //.addMigrations(MIGRATION_8_9)
                        .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
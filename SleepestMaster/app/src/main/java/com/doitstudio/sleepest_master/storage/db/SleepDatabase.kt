package com.doitstudio.sleepest_master.storage.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.doitstudio.sleepest_master.storage.DbRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors


private const val DATABASE_NAME = "sleepest_database"

/**
 * Stores all sleep segment data.
 */

@Database(
    entities = [SleepApiRawDataEntity::class, SleepSegmentEntity::class],
    version = 3,
    exportSchema = true
)

@TypeConverters(Converters::class)
abstract class SleepDatabase : RoomDatabase() {

    abstract fun sleepApiRawDataDao(): SleepApiRawDataDao
    abstract fun sleepDataDao(): SleepSegmentDao



    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: SleepDatabase? = null

        lateinit var instance: SleepDatabase


        fun getDatabase(context: Context): SleepDatabase {
            return INSTANCE ?: synchronized(this) {
                instance = Room.databaseBuilder(
                    context,
                    SleepDatabase::class.java,
                    DATABASE_NAME
                )
                    .allowMainThreadQueries()
                        // Wipes and rebuilds instead of migrating if no Migration object.
                        // Migration is not part of this sample.
                        .fallbackToDestructiveMigration()
                        // Prepopulate it with some information
                        .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }


    }
}
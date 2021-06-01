package com.doitstudio.sleepest_master

import android.app.Application
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.storage.db.SleepDatabase

/**
 * Sets up repository for all stored data
 */
class MainApplication : Application() {
    // Both database and repository use lazy so they aren't created when the app starts, but only
    // when repository is first needed.

    private val database by lazy {
        SleepDatabase.getDatabase(applicationContext)
    }

    val dataBaseRepository by lazy {
        DatabaseRepository.getRepo(
            database.sleepApiRawDataDao(),
            database.sleepDataDao(),
            database.userSleepSessionDao(),
            database.alarmDao(),
            database.activityApiRawDataDao()
        )
    }
    val dataStoreRepository by lazy {
        DataStoreRepository.getRepo(applicationContext)
    }

}
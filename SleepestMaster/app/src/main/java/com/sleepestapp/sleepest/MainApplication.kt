package com.sleepestapp.sleepest

import android.app.Application
import android.content.Context
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.storage.db.SleepDatabase

/**
 * Sets up repository for all stored data
 */
class MainApplication : Application() {
    // Both database and repository use lazy so they aren't created when the app starts, but only
    // when repository is first needed.
    init {
        instance = this
    }

    companion object {
        private var instance: MainApplication? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    private val database by lazy {
        SleepDatabase.getDatabase(applicationContext)
    }

    val dataBaseRepository by lazy {
        DatabaseRepository.getRepo(
                database.sleepApiRawDataDao(),
                database.userSleepSessionDao(),
                database.alarmDao(),
                database.activityApiRawDataDao()
        )
    }
    val dataStoreRepository by lazy {
        DataStoreRepository.getRepo(applicationContext)
    }

}
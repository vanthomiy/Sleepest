package com.doitstudio.sleepest_master

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.createDataStore
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DbRepository
import com.doitstudio.sleepest_master.storage.datastorage.*
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

    val dbRepository by lazy {
        DbRepository(
                        database.sleepDataDao(),
                        database.sleepApiRawDataDao(),
                        database.userSleepSegmentDataDao()

        )
    }

    val dataStoreRepository by lazy {
        DataStoreRepository(applicationContext)
    }

}
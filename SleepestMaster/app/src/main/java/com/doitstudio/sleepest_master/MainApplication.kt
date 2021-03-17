package com.doitstudio.sleepest_master

import android.app.Application
import androidx.datastore.preferences.createDataStore
import com.doitstudio.sleepest_master.storage.StorageRepository
import com.doitstudio.sleepest_master.storage.datastorage.ALARM_PREFERENCES_NAME
import com.doitstudio.sleepest_master.storage.datastorage.AlarmPreferencesStatus
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

    val repository by lazy {
        StorageRepository(
                alarmPreferencesStatus = AlarmPreferencesStatus(
                        applicationContext.createDataStore(name = ALARM_PREFERENCES_NAME)),
                        database.sleepDataDao(),
                        database.sleepApiRawDataDao()
        )
    }
}
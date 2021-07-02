package com.doitstudio.sleepest_master

import android.app.Application
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.doitstudio.sleepest_master.background.AlarmReceiver
import android.content.Context
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.storage.db.SleepDatabase
import kotlinx.coroutines.launch
import java.util.*

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
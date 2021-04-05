package com.doitstudio.sleepest_master

import android.app.Application
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationStoreRepository
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DbRepository
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
        DbRepository.getRepo(
                        database.sleepDataDao(),
                        database.sleepApiRawDataDao()

        )
    }

    val dataStoreRepository by lazy {
        DataStoreRepository.getRepo(applicationContext)
    }

    val sleepCalculationRepository by lazy {
        SleepCalculationStoreRepository.getRepo(applicationContext)
    }

}
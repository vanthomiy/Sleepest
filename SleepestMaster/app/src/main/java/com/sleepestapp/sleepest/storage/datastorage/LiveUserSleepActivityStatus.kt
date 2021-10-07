package com.sleepestapp.sleepest.storage.datastorage

import androidx.datastore.core.DataStore
import com.sleepestapp.sleepest.LiveUserSleepActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

const val LIVE_USER_ACTIVITY_DATA_NAME = "live_user_activity_data_repo"

class LiveUserSleepActivityStatus(private val dataStore: DataStore<LiveUserSleepActivity>) {

    suspend fun loadDefault(){
        dataStore.updateData  {
            LiveUserSleepActivity.getDefaultInstance()
        }
    }

    val liveUserSleepActivity: Flow<LiveUserSleepActivity> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(LiveUserSleepActivity.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun updateIsUserSleeping(isUserSleeping: Boolean) {
        dataStore.updateData { preference ->
            preference.toBuilder().setIsUserSleeping(isUserSleeping).build()
        }
    }

    suspend fun updateIsDataAvailable(isDataAvailable: Boolean) {
        dataStore.updateData { preference ->
            preference.toBuilder().setIsDataAvailable(isDataAvailable).build()
        }
    }

    suspend fun updateUserSleepTime(userSleepTime: Int) {
        dataStore.updateData { preference ->
            preference.toBuilder().setUserSleepTime(userSleepTime).build()
        }
    }


}
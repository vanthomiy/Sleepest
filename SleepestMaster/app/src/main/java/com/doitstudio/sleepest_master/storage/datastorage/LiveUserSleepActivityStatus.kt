package com.doitstudio.sleepest_master.storage.datastorage

import android.util.Log
import androidx.datastore.core.DataStore
import com.doitstudio.sleepest_master.LiveUserSleepActivity
import com.doitstudio.sleepest_master.SleepApiData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
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
                Log.d("Error", exception.message.toString())
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

    /*
    suspend fun updateUserSleepFound(userSleepFound: Boolean) {
        dataStore.updateData { preference ->
            preference.toBuilder().setUserSleepFound(userSleepFound).build()
        }
    }*/

    suspend fun updateUserSleepTime(userSleepTime: Int) {
        dataStore.updateData { preference ->
            preference.toBuilder().setUserSleepTime(userSleepTime).build()
        }
    }

    /*
    suspend fun setUserSleepHistory(userSleepHistory: List<Int>) {
        dataStore.updateData { preference ->
            preference.toBuilder().addAllUserSleepHistory(userSleepHistory).build()
        }
    }

    suspend fun addUserSleepHistory(userSleepHistory: Int) {
        dataStore.updateData { preference ->
            preference.toBuilder().addUserSleepHistory(userSleepHistory).build()
        }
    }

    suspend fun clearUserSleepHistory() {
        dataStore.updateData { preference ->
            preference.toBuilder().clearUserSleepHistory().build()
        }
    }*/

}
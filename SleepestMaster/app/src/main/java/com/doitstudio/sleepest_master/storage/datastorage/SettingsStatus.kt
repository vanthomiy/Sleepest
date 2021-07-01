package com.doitstudio.sleepest_master.storage.datastorage

import android.util.Log
import androidx.datastore.core.DataStore
import com.doitstudio.sleepest_master.SettingsData
import com.doitstudio.sleepest_master.SleepApiData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

const val SETTINGS_STATUS_NAME = "settings_status_name"

class SettingsStatus(private val dataStore: DataStore<SettingsData>) {

    suspend fun loadDefault(){
        dataStore.updateData  {
            obj ->
            obj.toBuilder()
                    .build()
        }
    }

    val settingsData: Flow<SettingsData> = dataStore.data
            .catch { exception->
                if(exception is IOException){
                    Log.d("Error", exception.message.toString())
                    emit(SettingsData.getDefaultInstance())
                }else{
                    throw exception
                }
            }

    suspend fun updateAutoDarkMode(isActive:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setDesignAutoDarkMode(isActive).build()
        }
    }

    suspend fun updateDarkMode(isActive:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setDesignDarkMode(isActive).build()
        }
    }

    suspend fun updateLanguage(isActive:Int){
        dataStore.updateData{preference->
            preference.toBuilder().setDesignLanguage(isActive).build()
        }
    }

    suspend fun updateRestartApp(isActive:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setRestartApp(isActive).build()
        }
    }

    suspend fun updateAfterRestartApp(isActive:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setAfterRestartApp(isActive).build()
        }
    }

    suspend fun updatePermissionSleepActivity(isActive:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setPermissionSleepActivity(isActive).build()
        }
    }

    suspend fun updatePermissionDailyActivity(isActive:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setPermissionDailyActivity(isActive).build()
        }
    }

}


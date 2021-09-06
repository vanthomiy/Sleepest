package com.sleepestapp.sleepest.storage.datastorage

import android.util.Log
import androidx.datastore.core.DataStore
import com.sleepestapp.sleepest.SettingsData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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


    suspend fun updateBannerShowAlarmActiv(isActive:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setBannerShowAlarmActiv(isActive).build()
        }
    }
    suspend fun updateBannerShowActualWakeUpPoint(isActive:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setBannerShowActualWakeUpPoint(isActive).build()
        }
    }
    suspend fun updateBannerShowActualSleepTime(isActive:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setBannerShowActualSleepTime(isActive).build()
        }
    }

    suspend fun updateBannerShowSleepState(isActive:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setBannerShowSleepState(isActive).build()
        }
    }

    suspend fun updateAutoDarkMode(isActive:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setDesignAutoDarkMode(isActive).build()
        }
    }

    suspend fun updateAutoDarkModeAckn(isActive:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setDesignDarkModeAckn(isActive).build()
        }
    }

    suspend fun updateDarkMode(isActive:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setDesignDarkMode(isActive).build()
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


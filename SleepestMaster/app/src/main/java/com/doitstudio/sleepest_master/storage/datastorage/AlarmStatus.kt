/*package com.doitstudio.sleepest_master.storage.datastorage

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.doitstudio.sleepest_master.Alarm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

const val ALARM_STATUS_NAME = "alarm_repo"

class AlarmStatus(private val dataStore: DataStore<Alarm>) {

    val alarm: Flow<Alarm> = dataStore.data
        .catch { exception->
            if(exception is IOException){
                Log.d("Error", exception.message.toString())
                emit(Alarm.getDefaultInstance())
            }else{
                throw exception
            }
        }


    suspend fun updateSleepDuration(duration:Int){
        dataStore.updateData{preference->
            preference.toBuilder().setSleepDuration(duration).build()
        }
    }

    suspend fun updateWakeUpEarly(timeOfDay:Int){
        dataStore.updateData{preference->
            preference.toBuilder().setWakeupEarly(timeOfDay).build()
        }
    }

    suspend fun updateWakeUpLate(timeOfDay:Int){
        dataStore.updateData{preference->
            preference.toBuilder().setWakeupLate(timeOfDay).build()
        }
    }


}*/
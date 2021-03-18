package com.doitstudio.sleepest_master.storage.datastorage

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


    suspend fun updateAlarmActive(isActive:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setIsActive(isActive).build()
        }
    }

    suspend fun updateSoundId(soundId:Int){
        dataStore.updateData{preference->
            preference.toBuilder().setSoundId(soundId).build()
        }
    }

    suspend fun updateAlarmName(name:String){
        dataStore.updateData{preference->
            preference.toBuilder().setAlarmName(name).build()
        }
    }

    suspend fun updateAlarmTime(time:Long){
        dataStore.updateData{preference->
            preference.toBuilder().setAlarmTime(time).build()
        }
    }

}
package com.sleepestapp.sleepest.storage.datastorage

import androidx.datastore.core.DataStore
import com.sleepestapp.sleepest.AlarmParameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

const val ALARM_PARAMETER_STATUS = "alarm_parameter_repo"

class AlarmParameterStatus(private val dataStore: DataStore<AlarmParameters>) {

    val alarmParameters: Flow<AlarmParameters> = dataStore.data
        .catch { exception->
            if(exception is IOException){
                emit(AlarmParameters.getDefaultInstance())
            }else{
                throw exception
            }
        }

    suspend fun loadDefault(){
        dataStore.updateData  {
            AlarmParameters.newBuilder()
                    .build()
        }
    }


    suspend fun updateEndAlarmAfterFired(duration:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setEndAlarmAfterFired(duration).build()
        }
    }
    suspend fun updateAlarmType(art:Int){
        dataStore.updateData{preference->
            preference.toBuilder().setAlarmArt(art).build()
        }
    }

    suspend fun updateAlarmTone(tone:String){
        dataStore.updateData{preference->
            preference.toBuilder().setAlarmTone(tone).build()
        }
    }

    suspend fun updateAlarmName(tone:String){
        dataStore.updateData{preference->
            preference.toBuilder().setAlarmName(tone).build()
        }
    }

    suspend fun triggerObserver(){
        dataStore.updateData{preference->
            preference.toBuilder().setTriggerObservable(!preference.triggerObservable).build()
        }
    }




}
package com.doitstudio.sleepest_master.storage.datastorage

import android.util.Log
import androidx.datastore.core.DataStore
import com.doitstudio.sleepest_master.AlarmParameters
import com.doitstudio.sleepest_master.SleepParameters
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.MobileUseFrequency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

const val ALARM_PARAMETER_STATUS = "alarm_parameter_repo"

class AlarmParameterStatus(private val dataStore: DataStore<AlarmParameters>) {

    val alarmParameters: Flow<AlarmParameters> = dataStore.data
        .catch { exception->
            if(exception is IOException){
                Log.d("Error", exception.message.toString())
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




}
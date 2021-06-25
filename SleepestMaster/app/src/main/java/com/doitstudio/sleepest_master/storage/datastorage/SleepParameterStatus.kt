package com.doitstudio.sleepest_master.storage.datastorage

import android.util.Log
import androidx.datastore.core.DataStore
import com.doitstudio.sleepest_master.SleepParameters
import com.doitstudio.sleepest_master.model.data.LightConditions
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.MobileUseFrequency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

const val SLEEP_PARAMETER_STATUS = "sleep_parameter_repo"

class SleepParameterStatus(private val dataStore: DataStore<SleepParameters>) {

    val sleepParameters: Flow<SleepParameters> = dataStore.data
        .catch { exception->
            if(exception is IOException){
                Log.d("Error", exception.message.toString())
                emit(SleepParameters.getDefaultInstance())
            }else{
                throw exception
            }
        }

    suspend fun updateActivityTracking(duration:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setUserActivityTracking(duration).build()
        }
    }
    suspend fun updateActivityInCalculation(duration:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setImplementUserActivityInSleepTime(duration).build()
        }
    }
    suspend fun updateEndAlarmAfterFired(duration:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setEndAlarmAfterFired(duration).build()
        }
    }
    suspend fun updateAlarmArt(art:Int){
        dataStore.updateData{preference->
            preference.toBuilder().setAlarmArt(art).build()
        }
    }

    suspend fun updateAutoSleepTime(duration:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setAutoSleepTime(duration).build()
        }
    }

    suspend fun updateSleepTimeStart(duration:Int){
        dataStore.updateData{preference->
            preference.toBuilder().setSleepTimeStart(duration).build()
        }
    }

    suspend fun updateSleepTimeEnd(duration:Int){
        dataStore.updateData{preference->
            preference.toBuilder().setSleepTimeEnd(duration).build()
        }
    }

    suspend fun updateUserWantedSleepTime(duration:Int){
        dataStore.updateData{preference->
            preference.toBuilder().setNormalSleepTime(duration).build()
        }
    }

    suspend fun updateUserMobileFequency(frequency: Int){
        dataStore.updateData{preference->
            preference.toBuilder().setMobileUseFrequency(frequency).build()
        }
    }

    suspend fun updateStandardMobilePosition(position: Int){
        dataStore.updateData{preference->
            preference.toBuilder().setStandardMobilePosition(position).build()
        }
    }
    suspend fun updateLigthCondition(position: Int){
        dataStore.updateData{preference->
            preference.toBuilder().setStandardLightCondition(position).build()
        }
    }


}
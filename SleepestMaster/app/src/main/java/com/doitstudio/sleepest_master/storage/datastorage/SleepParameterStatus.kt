package com.doitstudio.sleepest_master.storage.datastorage

import android.util.Log
import androidx.datastore.core.DataStore
import com.doitstudio.sleepest_master.Alarm
import com.doitstudio.sleepest_master.SleepParameters
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.MobileUseFrequency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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

    suspend fun updateUserMobileFequency(frequency: MobileUseFrequency){
        dataStore.updateData{preference->
            preference.toBuilder().setMobileUseFrequency(frequency.ordinal).build()
        }
    }

    suspend fun updateStandardMobilePosition(position: MobilePosition){
        dataStore.updateData{preference->
            preference.toBuilder().setStandardMobilePosition(position.ordinal).build()
        }
    }


}
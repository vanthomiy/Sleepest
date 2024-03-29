package com.sleepestapp.sleepest.storage.datastorage

import androidx.datastore.core.DataStore
import com.sleepestapp.sleepest.SleepParameters
import com.sleepestapp.sleepest.model.data.MobilePosition
import com.sleepestapp.sleepest.model.data.MobileUseFrequency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

const val SLEEP_PARAMETER_STATUS = "sleep_parameter_repo"

class SleepParameterStatus(private val dataStore: DataStore<SleepParameters>) {

    val sleepParameters: Flow<SleepParameters> = dataStore.data
        .catch { exception->
            if(exception is IOException){
                emit(SleepParameters.getDefaultInstance())
            }else{
                throw exception
            }
        }

    suspend fun loadDefault(){
        dataStore.updateData  {
            SleepParameters.newBuilder()
                    .setStandardMobilePosition(MobilePosition.UNIDENTIFIED.ordinal)
                    .setMobileUseFrequency(MobileUseFrequency.getValue(MobileUseFrequency.NONE))
                    .setSleepDuration(32400)
                    .setSleepTimeStart(72000)
                    .setSleepTimeEnd(36000)
                    .build()
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
            preference.toBuilder().setSleepDuration(duration).build()
        }
    }

    suspend fun triggerObserver(){
        dataStore.updateData{preference->
            preference.toBuilder().setTriggerObservable(!preference.triggerObservable).build()
        }
    }

    suspend fun updateUserMobileFrequency(frequency: Int){
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

    suspend fun updateStandardMobilePositionOverLastWeek(position: Int){
        dataStore.updateData{preference->
            preference.toBuilder().setStandardMobilePositionOverLastWeek(position).build()
        }
    }

    suspend fun updateLigthConditionOverLastWeek(position: Int){
        dataStore.updateData{preference->
            preference.toBuilder().setStandardLightConditionOverLastWeek(position).build()
        }
    }


}
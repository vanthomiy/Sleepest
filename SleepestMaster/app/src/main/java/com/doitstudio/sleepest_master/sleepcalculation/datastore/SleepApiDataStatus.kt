package com.doitstudio.sleepest_master.sleepcalculation.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import com.doitstudio.sleepest_master.SleepApiData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

const val SLEEP_API_DATA_NAME = "sleep_api_data_repo"

class SleepApiDataStatus(private val dataStore: DataStore<SleepApiData>) {

    val sleepApiData: Flow<SleepApiData> = dataStore.data
            .catch { exception->
                if(exception is IOException){
                    Log.d("Error", exception.message.toString())
                    emit(SleepApiData.getDefaultInstance())
                }else{
                    throw exception
                }
            }

    suspend fun updateIsSubscribed(isActive:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setIsSubscribed(isActive).build()
        }
    }

    suspend fun updatePermissionRemovedError(isActive:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setPermissionRemovedError(isActive).build()
        }
    }

    suspend fun updatePermissionActive(isActive:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setIsPermissionActive(isActive).build()
        }
    }

    suspend fun updateSubscribeFailed(isActive:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setSubscribeFailed(isActive).build()
        }
    }

    suspend fun updateUnsubscribeFailed(isActive:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setUnsubscribeFailed(isActive).build()
        }
    }

    suspend fun updateSleepApiValuesAmount(amount:Int){
        val newAmount = sleepApiData.first().sleepApiValuesAmount + amount
        dataStore.updateData{preference->
            preference.toBuilder().setSleepApiValuesAmount(newAmount).build()
        }
    }

    suspend fun resetSleepApiValuesAmount(){
        dataStore.updateData{preference->
            preference.toBuilder().setSleepApiValuesAmount(0).build()
        }
    }

}

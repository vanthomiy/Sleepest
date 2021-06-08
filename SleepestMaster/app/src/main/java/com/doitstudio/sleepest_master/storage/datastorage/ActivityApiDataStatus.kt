package com.doitstudio.sleepest_master.storage.datastorage

import android.util.Log
import androidx.datastore.core.DataStore
import com.doitstudio.sleepest_master.ActivityApiData
import com.doitstudio.sleepest_master.SleepApiData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

const val ACTIVITY_API_DATA_NAME = "activity_api_data_repo"

class ActivityApiDataStatus(private val dataStore: DataStore<ActivityApiData>) {

    val activityApiData: Flow<ActivityApiData> = dataStore.data
            .catch { exception->
                if(exception is IOException){
                    Log.d("Error", exception.message.toString())
                    emit(ActivityApiData.getDefaultInstance())
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

    suspend fun updateActivityApiValuesAmount(amount:Int){
        val newAmount = activityApiData.first().activityApiValuesAmount + amount
        dataStore.updateData{preference->
            preference.toBuilder().setActivityApiValuesAmount(newAmount).build()
        }
    }

    suspend fun resetActivityApiValuesAmount(){
        dataStore.updateData{preference->
            preference.toBuilder().setActivityApiValuesAmount(0).build()
        }
    }

}

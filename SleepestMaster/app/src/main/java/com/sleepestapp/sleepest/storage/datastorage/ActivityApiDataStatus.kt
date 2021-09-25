package com.sleepestapp.sleepest.storage.datastorage

import androidx.datastore.core.DataStore
import com.sleepestapp.sleepest.ActivityApiData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

const val ACTIVITY_API_DATA_NAME = "activity_api_data_repo"

class ActivityApiDataStatus(private val dataStore: DataStore<ActivityApiData>) {

    suspend fun loadDefault(){
        dataStore.updateData  {
            obj ->
            obj.toBuilder().build()
        }
    }

    val activityApiData: Flow<ActivityApiData> = dataStore.data
            .catch { exception->
                if(exception is IOException) {
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

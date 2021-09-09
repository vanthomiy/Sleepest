package com.sleepestapp.sleepest.storage.datastorage

import android.util.Log
import androidx.datastore.core.DataStore
import com.sleepestapp.sleepest.BackgroundService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

const val BACKGROUND_SERVICE_STATUS = "background_service_status"

class BackgroundServiceStatus(private val dataStore: DataStore<BackgroundService>) {

    suspend fun loadDefault(){
        dataStore.updateData  {
            obj ->
            obj.toBuilder()
                    .build()
        }
    }

    val backgroundService: Flow<BackgroundService> = dataStore.data
        .catch { exception->
            if(exception is IOException){
                Log.d("Error", exception.message.toString())
                emit(BackgroundService.getDefaultInstance())
            }else{
                throw exception
            }
        }

    suspend fun updateShouldBeActive(value:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setIsBackgroundActive(value).build()
        }
    }

    suspend fun updateIsActive(value:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setIsForegroundActive(value).build()
        }
    }

}
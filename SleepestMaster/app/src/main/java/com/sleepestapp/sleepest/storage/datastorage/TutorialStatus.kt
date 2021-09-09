package com.doitstudio.sleepest_master.storage.datastorage

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException
import android.util.Log
import com.sleepestapp.sleepest.Tutorial


const val TUTORIAL_STATUS_NAME = "tutorial_status_repo"

class TutorialStatus(private val dataStore: DataStore<Tutorial>) {

    suspend fun loadDefault(){
        dataStore.updateData  {
                obj ->
            obj.toBuilder().build()
        }
    }

    val tutorialData: Flow<Tutorial> = dataStore.data
        .catch { exception->
            if(exception is IOException){
                Log.d("Error", exception.message.toString())
                emit(Tutorial.getDefaultInstance())
            }else{
                throw exception
            }
        }

    suspend fun updateTutorialCompleted(isCompleted:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setTutorialCompleted(isCompleted).build()
        }
    }

    suspend fun updateEnergyOptionsShown(isShown:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setEnergyOptionsShown(isShown).build()
        }
    }


}
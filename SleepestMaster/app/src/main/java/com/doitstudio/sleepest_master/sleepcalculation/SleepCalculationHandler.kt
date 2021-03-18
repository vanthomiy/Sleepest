package com.doitstudio.sleepest_master.sleepcalculation

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.MainViewModel
import com.doitstudio.sleepest_master.model.data.SleepSegmentEntity
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.storage.StorageRepository
import com.doitstudio.sleepest_master.storage.db.SleepDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This is the actual sleep calculation class.
 * This is singleton and should be created once after we are in sleep time from the backgroundhandler by calling [SleepCalculationHandler.getDatabase] and passing the actual context from the [onrecive]?
 * After the Sleep time it can be destroyed from the Backgroundhandler... (How?).
 * The connection between database and the handler can moved out in a view later...
 *
 */
class SleepCalculationHandler(private val context:Context){

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: SleepCalculationHandler? = null

        var a:Int = 0

        fun getDatabase(context: Context): SleepCalculationHandler {
            return INSTANCE ?: synchronized(this) {
                val instance = SleepCalculationHandler(context)
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

    private val repository: StorageRepository by lazy {
        (context.applicationContext as MainApplication).repository
    }
    private val alarmActiveLiveData = repository.alarmActiveFlow.asLiveData()
    private var alarmActive:Boolean = false

    init{

        alarmActiveLiveData.observe(context as LifecycleOwner) { newAlarmActive ->
            if (alarmActive != newAlarmActive) {
                alarmActive = newAlarmActive
            }
        }
    }


    /**
     * Calculates all neccessary steps with the values
     */
    fun calculateSleepData(){

        CoroutineScope(Dispatchers.Default).launch {
            repository.updateAlarmActive(!alarmActive)
        }
    }

    fun insertSleepValue(){
        val sleepSegment: SleepSegmentEntity = SleepSegmentEntity(a++,2 +a,SleepState.awake)

        CoroutineScope(Dispatchers.Default).launch {
            repository.insertSleepSegment(sleepSegment)
        }

    }


}
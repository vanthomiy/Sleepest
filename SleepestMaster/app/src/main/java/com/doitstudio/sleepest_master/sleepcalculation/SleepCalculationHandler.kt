package com.doitstudio.sleepest_master.sleepcalculation

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.model.data.SleepSegmentEntity
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DbRepository
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

    private val dbRepository: DbRepository by lazy {
        (context.applicationContext as MainApplication).dbRepository
    }

    private val storeRepository: DataStoreRepository by lazy {
        (context.applicationContext as MainApplication).dataStoreRepository
    }

    private val alarmActiveLiveData = storeRepository.alarmFlow.asLiveData()

    private var alarmActive:Boolean = false

    init{

        /*alarmActiveLiveData.observe(context as LifecycleOwner) { alarmData ->
            if (alarmActive != alarmData?.isActive) {
                alarmActive = alarmData?.isActive == true
            }
        }*/
    }


    /**
     * Calculates all neccessary steps with the values
     */
    fun calculateSleepData(){

        updateAlarmTime()
        updateAlarmActive()
    }

    private var counter:Int =0

    private fun updateAlarmActive(){
        CoroutineScope(Dispatchers.Default).launch {
            storeRepository.updateAlarmActive(!alarmActive)
        }
    }

    private fun updateAlarmTime(){
        CoroutineScope(Dispatchers.Default).launch {
            storeRepository.updateAlarmName("Aufruf Nr. " + counter++)
        }
    }

    private fun insertSleepSegmentValue(){
        val sleepSegment: SleepSegmentEntity = SleepSegmentEntity(a++,2 +a,SleepState.awake)

        CoroutineScope(Dispatchers.Default).launch {
            dbRepository.insertSleepSegment(sleepSegment)
        }
    }


}
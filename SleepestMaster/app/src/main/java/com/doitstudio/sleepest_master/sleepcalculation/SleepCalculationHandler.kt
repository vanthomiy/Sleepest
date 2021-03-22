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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * This is the actual sleep calculation class.
 * This is singleton and should be created once after we are in sleep time from the backgroundhandler by calling [SleepCalculationHandler.getDatabase] and passing the actual context from the [onrecive]?
 * After the Sleep time it can be destroyed from the Backgroundhandler... (How?).
 * The connection between database and the handler can moved out in a view later...
 *
 */
class SleepCalculationHandler(private val context:Context){

    // Used to launch coroutines (non-blocking way to insert data).
    private val scope: CoroutineScope = MainScope()

    private val dbRepository: DbRepository by lazy {
        (context.applicationContext as MainApplication).dbRepository
    }

    private val storeRepository: DataStoreRepository by lazy {
        (context.applicationContext as MainApplication).dataStoreRepository
    }

    private val alarmActiveLiveData = storeRepository.alarmFlow.asLiveData()

    private var alarmActive:Boolean = false

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: SleepCalculationHandler? = null

        fun getHandler(context: Context): SleepCalculationHandler {
            return INSTANCE ?: synchronized(this) {
                val instance = SleepCalculationHandler(context)
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

    init{

        alarmActiveLiveData.observe(context as LifecycleOwner) { alarmData ->
            if (alarmActive != alarmData?.isActive) {
                alarmActive = alarmData?.isActive == true
            }
        }
    }


    /**
     * Calculates all necessary steps with the values
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

    /**
     * Create new Alarm time
     */
    private fun updateAlarmTime(){
        scope.launch {
            storeRepository.updateAlarmName("Aufruf Nr. " + counter++)
        }
    }

    /**
     * Update sleep segments
     */
    private fun insertSleepSegmentValue( timestampSecondsStart: Int,
                                         timestampSecondsEnd: Int,
                                         sleepState: SleepState)
    {
        val sleepSegment: SleepSegmentEntity = SleepSegmentEntity(timestampSecondsStart,timestampSecondsEnd,sleepState)

        scope.launch {
            dbRepository.insertSleepSegment(sleepSegment)
        }
    }


}
package com.doitstudio.sleepest_master.sleepcalculation

import android.content.Context
import androidx.lifecycle.asLiveData
import com.doitstudio.sleepest_master.LiveUserSleepActivity
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.sleepcalculation.db.SleepTimeParameterEntity
import com.doitstudio.sleepest_master.storage.DbRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
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

    private val dbRepository: SleepCalculationDbRepository by lazy {
        (context.applicationContext as MainApplication).sleepCalculationDbRepository
    }
    private val storeRepository: SleepCalculationStoreRepository by lazy {
        (context.applicationContext as MainApplication).sleepCalculationRepository
    }

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

    /**
     * Calculates wheter a user is sleeping or not (around 30 mins delay)
     * TESTING: Call this every 30 min while sleeptime
     * It writes in the [LiveUserSleepActivity]
     */
    suspend fun calculateLiveUserSleepActivity()
    {

        //region inital

        // We are using the default values for the user to check whether sleeping or not
        // Get them from the [ActualSleepUserParameterStatus]
        val defaultTimeParameterId = storeRepository.actualSleepUserParameterFlow.first().sleepTimePattern

        // Now retrieve the time parameter for the live user sleep activity
        val defaultParameter = dbRepository.getSleepTimeParameterById(defaultTimeParameterId)?: return

        // Get all available raw sleep api data
        val rawApiData = dbRepository.allSleepApiRawData.first()

        // Check if enough data is available
        if (rawApiData.count() < 5)
            // No/To less data available
            storeRepository.updateIsDataAvailable(false)
            return

        storeRepository.updateIsDataAvailable(true)

        //endregion

        // Now we have everything we need to calculate the first sleep/no sleep segments

        //region calculation

        //Call the sleep analyse function
        val sleep = CalculateSleepTime(defaultParameter, rawApiData)

        //Define the time model out of the data



        //endregion


    }

    fun CalculateSleepTime(parameter:SleepTimeParameterEntity, rawApiData:List<SleepApiRawDataEntity>) : List<Int>
    {
        var sleepList = mutableListOf<Int>(rawApiData.first().timestampSeconds)

        var isSleeping = false

        rawApiData.forEach {
            apiData ->
            if (!isSleeping && apiData.... )
                sleepList.add(apiData.timestampSeconds)
                isSleeping = true
            else if(isSleeping && ....)
                sleepList.add(apiData.timestampSeconds)
                isSleeping = false
        }

        sleepList.add(rawApiData.last().timestampSeconds)

        return sleepList
    }

    fun DefineActualModel(sleep:List<Int>, rawApiData:List<SleepApiRawDataEntity>) : List<Int>
    {

    }

    /**
     * Calculates the alarm time for the user. This should be called before the first wake up time
     * TESTING: Call this before the user alarm time
     */
    fun calculateUserWakup()
    {

    }

    /**
     * Re-Calculates the sleep of the user after the sleep time ( to save the complete sleep). This should be called after user sleep time
     * TESTING: // Call this after the sleep time and it will delete all raw sleep api data and reset the raw sleep api status counter
     */
    fun recalculateUserSleep()
    {

    }



}
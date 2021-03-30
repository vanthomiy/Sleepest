package com.doitstudio.sleepest_master.sleepcalculation

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.model.data.sleepcalculation.*
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DbRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
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

    private val rawSleepApiDataFlow = dbRepository.allSleepApiRawData.asLiveData()

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
    fun calculateLiveuserSleepActivity()
    {
        scope.launch {

            // Some fake values for the testing

            rawSleepApiDataFlow.map {  }
            val a = dbRepository.allSleepApiRawData?.first()

            if (a == null || a.count() == 0)
                return@launch

            val b = a?.last()

            storeRepository.updateIsUserSleeping(b?.confidence!! > 50)
            storeRepository.updateIsDataAvailable(a?.count()!! > 1)
        }
    }


    private var userSleepSessionEntity:UserSleepSessionEntity? = null

    /**
     * Calculates the alarm time for the user. This should be called before the first wake up time
     * TESTING: Call this before the user alarm time
     */
    fun calculateUserWakup()
    {
        if (userSleepSessionEntity == null)
            userSleepSessionEntity = UserSleepSessionEntity(
                    sleepTimes = SleepTimes(0,0,0,0,0,0,0),
                    sleepUserType = SleepUserType(MobilePosition.UNIDENTIFIED),
                    userSleepRating = UserSleepRating(),
                    userCalculationRating = UserCalculationRating()
            )

        //userSleepSessionEntity.sleepTimes = SleepTimes(0,0,0,0)




        userSleepSessionEntity!!.sleepTimes.sleepDuration += 30

        scope.launch {
            dbRepository.deleteUserSleepSession(userSleepSessionEntity!!)

            dbRepository.insertUserSleepSession(userSleepSessionEntity!!)
        }
    }

    /**
     * Re-Calculates the sleep of the user after the sleep time ( to save the complete sleep). This should be called after user sleep time
     * TESTING: // Call this after the sleep time and it will delete all raw sleep api data and reset the raw sleep api status counter
     */
    fun recalculateUserSleep()
    {
        scope.launch {

            if (userSleepSessionEntity != null && userSleepSessionEntity!!.sleepUserType != null)
                userSleepSessionEntity!!.sleepUserType!!.mobilePosition = MobilePosition.INBED
                dbRepository.insertUserSleepSession(userSleepSessionEntity!!)
            dbRepository.deleteSleepApiRawData()
            storeRepository.resetSleepApiValuesAmount()
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
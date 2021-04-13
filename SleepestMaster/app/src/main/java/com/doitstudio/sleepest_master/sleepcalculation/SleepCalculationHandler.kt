package com.doitstudio.sleepest_master.sleepcalculation

import android.content.Context
import com.doitstudio.sleepest_master.LiveUserSleepActivity
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.model.data.SleepTimePattern
import com.doitstudio.sleepest_master.model.data.UserFactorPattern
import com.doitstudio.sleepest_master.sleepcalculation.db.UserSleepSessionEntity
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepModel
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepTimeParameter
import com.doitstudio.sleepest_master.storage.DbRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import java.util.*


/**
 * This is the actual sleep calculation class.
 * This is singleton and should be created once after we are in sleep time from the backgroundhandler by calling [SleepCalculationHandler.getDatabase] and passing the actual context from the [onrecive]?
 * After the Sleep time it can be destroyed from the Backgroundhandler... (How?).
 * The connection between database and the handler can moved out in a view later...
 *
 */
class SleepCalculationHandler(private val context: Context){

    // Used to launch coroutines (non-blocking way to insert data).
    private val scope: CoroutineScope = MainScope()

    private val dbRepository: SleepCalculationDbRepository by lazy {
        (context.applicationContext as MainApplication).sleepCalculationDbRepository
    }

    private val dbNormalRepository: DbRepository by lazy {
        (context.applicationContext as MainApplication).dbRepository
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

    // region Sleep Time Calculation

    /**
     * Calculates wheter a user is sleeping or not (around 30 mins delay)
     * - It first uses standard Parameter and after it tries to find a matching model
     * - When found a model it will use the model and recalculate everything
     * - If no model is found it will use the user-parameter for recalculating everything
     * It writes in the [LiveUserSleepActivity]
     */
    suspend fun calculateLiveUserSleepActivity()
    {
        // Get all available raw sleep api data

        //region inital
        val rawApiData = dbRepository.allSleepApiRawData.first()

        // Check if enough data is available
        if (rawApiData.count() < 5) {
            // No/To less data available
            storeRepository.updateIsDataAvailable(false)
            return
        }

        var userSleepSessionEntity = UserSleepSessionEntity(id = rawApiData.last().timestampSeconds)

        // create a new user sleep session with a id = first sleep time seconds
        // dbNormalRepository.insertUserSleepSession(userSleepSessionEntity)

        storeRepository.updateIsDataAvailable(true)

        //endregion

        // Now we have everything we need to calculate the first sleep/no sleep segments

        //region calculate with default

        var sleep:List<Int> = listOf()

        // Now retrieve the time with stadard parameter for the live user sleep activity
        var parameter = SleepTimeParameter() //dbRepository.getSleepTimeParameterById(SleepTimePattern.STANDARD.toString())

        // Create sleep with parameters
        var userFactorSleep = getSleepAndAdjustFactor(parameter, rawApiData)

        sleep = userFactorSleep.second
        // Now we should have a list of data else we return without more steps
        if (sleep.count() <= 2) {
            storeRepository.updateUserSleepFound(false)
            return
        }
        storeRepository.updateUserSleepFound(true)

        //endregion

        // Now we want to create a model of the actual sleep

        // region model
        // create the actual model
        val actualModel = defineActualModel(sleep, rawApiData)
        userSleepSessionEntity.sleepUserType.sleepLiveModel = actualModel

        // check if model is already defined
        val newParameter = getTimePatternIfAvailable(actualModel)

        // endregion

        //region calulate with new parameter (From model or from user)

        if (newParameter.first.count() == 0)
        {
            // We are using the default values for the user to check whether sleeping or not
            // Get them from the [ActualSleepUserParameterStatus]
            val newTimeParameterId = storeRepository.actualSleepUserParameterFlow.first().sleepTimePatternList
            // check if that are diffrent parameters... else we dont have to calculate again
            if(newTimeParameterId.count() != 0 && !newTimeParameterId.contains("NONE")){
                // Now retrieve the time parameter for the live user sleep activity
                var paramList = mutableListOf<SleepTimeParameter>()
                userSleepSessionEntity.sleepUserType.sleepTimeLiveParams.clear()
                newTimeParameterId.forEach(){
                    paramList.add(dbRepository.getSleepTimeParameterById(it).first().sleepTimeParameter)
                    userSleepSessionEntity.sleepUserType.sleepTimeLiveParams.add(SleepTimePattern.valueOf(it))
                }

                // merge the params together
                parameter = SleepTimeParameter.mergeParameters(paramList)

                // later define for more than one pattern...

                // Create sleep with parameters
                userFactorSleep = getSleepAndAdjustFactor(parameter, rawApiData)
                sleep = userFactorSleep.second

            }
        }
        else{
            // use the found model
            userSleepSessionEntity.sleepUserType.sleepTimeLiveParams = newParameter.first
            userFactorSleep = getSleepAndAdjustFactor(newParameter.second, rawApiData)
            sleep = userFactorSleep.second
        }

        // endregion

        // Now we should have a list of data else we return without more steps
        if (sleep.count()-1 <= 2) {
            storeRepository.updateUserSleepFound(false)
            return
        }


        userSleepSessionEntity.sleepTimes.sleepTimeStart = sleep[1]
        userSleepSessionEntity.sleepTimes.sleepTimeEnd = sleep[sleep.count()-2]
        userSleepSessionEntity.sleepUserType.userFactorPattern = userFactorSleep.first

        storeRepository.updateUserSleepFound(true)
        storeRepository.clearUserSleepHistory()
        storeRepository.setUserSleepHistory(sleep)
        storeRepository.updateIsUserSleeping(sleep.count() % 2 != 0)

        var sleepTime = 0
        for(i in 1 until sleep.count()-1 step 2)
        {
            sleepTime +=  sleep[i+1] - sleep[i]
        }

        storeRepository.updateUserSleepTime(sleepTime)

        // insert or update the user sleep session
        dbNormalRepository.insertUserSleepSession(userSleepSessionEntity)
    }


    /**
     * Calls the sleep calculation and in a second step defines the factor for the user... ( if no sleep is found the factor is adjustet to light or superlight)
     */
    private suspend fun getSleepAndAdjustFactor(parameter:SleepTimeParameter, rawApiData:List<SleepApiRawDataEntity>) : Pair<UserFactorPattern, List<Int>>{

        var sleep:List<Int> = listOf()

        //Check if sleep session was found or not
        var ufp = UserFactorPattern.NORMAL

        // As long as the sleep count is smaller then 2 adjust the sleep user factor
        while (sleep.count() <= 2 && ufp != UserFactorPattern.NONE )
        {

            // Now retrieve the time parameter for the live user sleep activity
            val name = ufp.toString()
            val factorParameter = dbRepository.getSleepTimeParameterById(ufp.toString()).first()
            val actualParameter = SleepTimeParameter.multiplyParameterByParameter(parameter, factorParameter.sleepTimeParameter)

            //Call the sleep analyse function
            sleep = calculateSleepTime(actualParameter, rawApiData)

            ufp = UserFactorPattern.values()[ufp.ordinal-1]
        }

        return Pair(ufp,sleep.reversed())
    }

    /**
     * Takes a parameter and the raw sleep api data and calculates the sleep times and retruns a list with timestamps when user is sleeping or awake
     */
    private fun calculateSleepTime(parameters: SleepTimeParameter, rawApiData: List<SleepApiRawDataEntity>) : List<Int> {

        var sleepList = mutableListOf<Int>(rawApiData.first().timestampSeconds)

        // for each point in the sleep time, we have to calculate some lists and points
        // therefore we loop a few times through the data and store the values local

        val medianAwakeOverLastX = mutableListOf<Int>() // median awake over last x time
        val medianSleepOverLastX = mutableListOf<Int>()  // median sleep over last x time
        val medianSleepOverNextX = mutableListOf<Int>() // median sleep over nect x times
        val medianSleepLastSleepNextDiff = mutableListOf<Int>() // Median sleep diff between sleep and wakeup
        val medianFutureSleepLastSleepNextDiff = mutableListOf<Int>() // Future Median sleep diff between sleep and wakeup
        val medianSleepNextAwakeOverDiff = mutableListOf<Int>()  // Median sleep diff between awake and wakeup

        val sleepPoints = mutableListOf<Int>()  // Fall asleep times
        val wakeUpPoints = mutableListOf<Int>()  // Wakeup times

        // Setup the lists
        for (i in 0 until rawApiData.count()) {

            val actualSeconds = rawApiData[i].timestampSeconds

            // data that was before the time
            var usedData = rawApiData.filter { x -> x.timestampSeconds >= actualSeconds && x.timestampSeconds < (actualSeconds + parameters.awakeTime) }
            medianAwakeOverLastX.add(usedData.sumBy { x -> x.confidence } / usedData.count());

            // data that was before the time
            usedData = rawApiData.filter { x -> x.timestampSeconds >= actualSeconds && x.timestampSeconds < (actualSeconds + parameters.sleepTime) }
            medianSleepOverLastX.add(usedData.sumBy { x -> x.confidence } / usedData.count());

            // data that was after the time
            usedData = rawApiData.filter { x -> x.timestampSeconds <= actualSeconds && x.timestampSeconds > (actualSeconds - parameters.wakeUpTime) }
            medianSleepOverNextX.add(usedData.sumBy { x -> x.confidence } / usedData.count());

            // setup the diffrence between the values
            if (i == 0) {
                medianSleepLastSleepNextDiff.add(0)
                medianSleepNextAwakeOverDiff.add(0)
            } else {
                medianSleepLastSleepNextDiff.add(medianAwakeOverLastX[i] - medianSleepOverNextX[i - 1])
                medianSleepNextAwakeOverDiff.add(medianSleepOverLastX[i] - medianSleepOverNextX[i - 1])
            }


        }

        // Setup future data (for future predictions)
        for (i in 0 until rawApiData.count()) {

            if (rawApiData.count() < i + 6) {
                medianFutureSleepLastSleepNextDiff.add(0)
            } else {
                medianFutureSleepLastSleepNextDiff.add(medianAwakeOverLastX[i + 5] - medianSleepOverNextX[i + 4])
            }
        }

        // Set the sleeping points for each
        for (i in 0 until rawApiData.count()) {
            // Check if fall asleep
            if (sleepPoints.count() <= wakeUpPoints.count() && rawApiData[i].confidence > parameters.sleepSleepBorder && rawApiData[i].motion < parameters.sleepMotionBorder && medianSleepOverLastX[i] > parameters.sleepMedianOverTime && medianSleepLastSleepNextDiff[i] > parameters.diffSleep && medianFutureSleepLastSleepNextDiff[i] > parameters.diffSleepFuture)
            {
                sleepPoints.add(i);
            }
            else if (sleepPoints.count() > wakeUpPoints.count() && rawApiData[i].confidence < parameters.awakeSleepBorder && medianAwakeOverLastX[i] < parameters.awakeMedianOverTime && medianSleepNextAwakeOverDiff[i] < parameters.diffAwake && rawApiData[i].motion > parameters.awakeMotionBorder)
            {
                wakeUpPoints.add(i);
            }
        }

        // set the timestamp list, where user is sleeping or not
        var sleeping = false
        for (i in 0 until rawApiData.count()) {
            if (sleepPoints.contains(i) && !sleeping) {
                sleepList.add(rawApiData[i].timestampSeconds)
                sleeping = true
            } else if (wakeUpPoints.contains(i) && sleeping) {
                sleepList.add(rawApiData[i].timestampSeconds)
                sleeping = false
            }
        }

        sleepList.add(rawApiData.last().timestampSeconds)

        return sleepList
    }

    /**
     * Uses the with default parameter defined sleep and awake lists and creates a model of the sleep
     */
    private fun defineActualModel(sleep: List<Int>, rawApiData: List<SleepApiRawDataEntity>) : SleepModel {
        // create two lists.. one while sleep and one after sleep
        val awakeList = mutableListOf<SleepApiRawDataEntity>()
        val sleepList = mutableListOf<SleepApiRawDataEntity>()

        var isSleep = false

        // Add the date to each list where it belongs
        for(i in 0 until sleep.count()-1)
        {
            if  (!isSleep)
            {
                awakeList.addAll(rawApiData.filter { x-> x.timestampSeconds >= sleep[i] && x.timestampSeconds < sleep[i+1] })
            }
            else
            {
                sleepList.addAll(rawApiData.filter { x-> x.timestampSeconds >= sleep[i] && x.timestampSeconds < sleep[i+1] })
            }

            isSleep = !isSleep
        }

        return SleepModel.calculateModel(awakeList, sleepList)
    }

    /**
     * Finds pre-defined patterns by a model and returns the needed parameter and found patterns for it
     */
    private suspend fun getTimePatternIfAvailable(actualModel:SleepModel) : Pair<ArrayList<SleepTimePattern>, SleepTimeParameter> {

        val patterns = arrayListOf<SleepTimePattern>()
        val parameter = mutableListOf<SleepTimeParameter>()

        val list = dbRepository.allSleepTimeModels.first()

        // add every pattern that matches the model
        list.forEach {
            val a = it.checkIfIsModel(actualModel)
            if (a.ordinal != 0)
            {
                patterns.add(a)
                parameter.add(dbRepository.getSleepTimeParameterById(a.toString()).first().sleepTimeParameter)
            }

        }

        // create the id of the param id of the model

        return Pair(patterns, SleepTimeParameter.mergeParameters(parameter))
    }

    // endregion

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
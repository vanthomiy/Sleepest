package com.doitstudio.sleepest_master.sleepcalculation

import android.content.Context
import com.doitstudio.sleepest_master.LiveUserSleepActivity
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.model.data.SleepStatePattern
import com.doitstudio.sleepest_master.model.data.SleepTimePattern
import com.doitstudio.sleepest_master.model.data.UserFactorPattern
import com.doitstudio.sleepest_master.sleepcalculation.db.SleepStateModelEntity
import com.doitstudio.sleepest_master.sleepcalculation.db.UserSleepSessionEntity
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepModel
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepStateParameter
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepTimeParameter
import com.doitstudio.sleepest_master.storage.DbRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.doitstudio.sleepest_master.storage.db.SleepSegmentEntity
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

    // region live Sleep Detection

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
        val rawApiData = dbRepository.allSleepApiRawData.first()

        // Check if enough data is available
        if (rawApiData.count() < 5) {
            // No/To less data available
            storeRepository.updateIsDataAvailable(false)
            return
        }

        var userSleepSessionEntity = UserSleepSessionEntity(id = rawApiData.last().timestampSeconds)

        // create a new user sleep session with a id = first sleep time seconds
        storeRepository.updateIsDataAvailable(true)

        // Now we have everything we need to calculate the first sleep/no sleep segments

        //region calculate with default or user default

        var sleep:List<Int> = listOf()

        // Now retrieve the time with stadard parameter for the live user sleep activity
        var parameter = SleepTimeParameter() //dbRepository.getSleepTimeParameterById(SleepTimePattern.STANDARD.toString())


        // Use user parameter or just use the default parameters

        // Get user time params
        val userTimeParameterId = storeRepository.actualSleepUserParameterFlow.first().sleepTimePatternList
        if(userTimeParameterId.count() != 0 && !userTimeParameterId.contains("NONE")) {
            // User specific

            // Now retrieve the time parameter for the live user sleep activity
            var paramList = mutableListOf<SleepTimeParameter>()
            userTimeParameterId.forEach(){
                paramList.add(dbRepository.getSleepTimeParameterById(it).first().sleepTimeParameter)
            }

            parameter = SleepTimeParameter.mergeParameters(paramList)

        }

        // Create sleep with parameters
        var userFactorSleep = getSleepAndAdjustFactor(parameter, rawApiData)

        sleep = userFactorSleep.second

        //endregion

        // Now we should have a list of data else we return without more steps
        if (sleep.count() <= 2) {
            storeRepository.updateUserSleepFound(false)
            return
        }


        var sleepTime = 0
        for(i in 1 until sleep.count()-1 step 2)
        {
            sleepTime +=  sleep[i + 1] - sleep[i]
        }


        // Assignments
        storeRepository.updateUserSleepTime(sleepTime)
        storeRepository.updateUserSleepFound(true)
        storeRepository.updateIsUserSleeping(sleep.count() % 2 != 0)
        userSleepSessionEntity.sleepTimes.sleepTimeStart = sleep[1]

        // insert or update the user sleep session
        dbNormalRepository.insertUserSleepSession(userSleepSessionEntity)
    }

    // endregion


    // region Sleep Time Calculation

    /**
     * Calculates the alarm time for the user. This should be called before the first wake up time
     * At the first call it also defines a model that should be used
     */
    suspend fun calculateUserWakup()
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

        // Get the user sleep session, else create a new one
        var userSleepSessionEntity = dbNormalRepository.getSleepSessionById(rawApiData.last().timestampSeconds).first() ?: UserSleepSessionEntity(id = rawApiData.last().timestampSeconds)

        storeRepository.updateIsDataAvailable(true)

        //endregion

        //region define the params

        // Now retrieve the time with stadard parameter for the live user sleep activity
        var parameter = SleepTimeParameter()
        var sleep: List<Int> = listOf()

        if(userSleepSessionEntity.sleepUserType.sleepTimeLiveParams.count() == 0) {
            // Now we have everything we need to calculate the first sleep/no sleep segments
            //region calculate with default

            // Create sleep with parameters
            var userFactorSleep = getSleepAndAdjustFactor(parameter, rawApiData)

            sleep = userFactorSleep.second
            // Now we should have a list of data else we return without more steps
            if (sleep.count() <= 2) {
                storeRepository.updateUserSleepFound(false)
                return
            }

            storeRepository.updateUserSleepFound(true)

            userSleepSessionEntity.sleepUserType.sleepTimeLiveParams.add(SleepTimePattern.STANDARD)

            //endregion

            // Now we want to create a model of the actual sleep
            // region create the actual model
            val actualModel = defineActualModel(sleep, rawApiData)
            userSleepSessionEntity.sleepUserType.sleepLiveModel = actualModel

            // check if model is already defined
            val modelAndParameter = getTimePatternIfAvailable(actualModel)


            // endregion

            // Now we get the actual params of the model
            //region gets new parameter (From model or from user)

            if (modelAndParameter.first.count() == 0) {
                // We are using the default values for the user to check whether sleeping or not
                // Get them from the [ActualSleepUserParameterStatus]
                val newTimeParameterId = storeRepository.actualSleepUserParameterFlow.first().sleepTimePatternList
                // check if that are diffrent parameters... else we dont have to calculate again
                if (newTimeParameterId.count() != 0 && !newTimeParameterId.contains("NONE")) {
                    // Now retrieve the time parameter for the live user sleep activity
                    var paramList = mutableListOf<SleepTimeParameter>()
                    userSleepSessionEntity.sleepUserType.sleepTimeLiveParams.clear()
                    newTimeParameterId.forEach() {
                        paramList.add(dbRepository.getSleepTimeParameterById(it).first().sleepTimeParameter)
                        userSleepSessionEntity.sleepUserType.sleepTimeLiveParams.add(SleepTimePattern.valueOf(it))
                    }

                    // merge the params together
                    parameter = SleepTimeParameter.mergeParameters(paramList)
                }
            } else {
                // use the found model
                userSleepSessionEntity.sleepUserType.sleepTimeLiveParams.clear()
                userSleepSessionEntity.sleepUserType.sleepTimeLiveParams = modelAndParameter.first
                parameter = modelAndParameter.second
            }

            // endregion

        }
        else{
            // get the parameter of the of the usersleepsesssion
            var paramList = mutableListOf<SleepTimeParameter>()
            userSleepSessionEntity.sleepUserType.sleepTimeLiveParams.forEach() {
                paramList.add(dbRepository.getSleepTimeParameterById(it.toString()).first().sleepTimeParameter)
            }

            // merge the params together
            parameter = SleepTimeParameter.mergeParameters(paramList)


        }

        //endregion

        // region Calc the sleep values

        // Calc sleep with parameters
        val userFactorSleep = getSleepAndAdjustFactor(parameter, rawApiData)
        sleep = userFactorSleep.second


        // Now we should have a list of data else we return without more steps
        if (sleep.count() <= 2) {
            storeRepository.updateUserSleepFound(false)
            return
        }

        // endregion

        //region setup values

        userSleepSessionEntity.sleepTimes.sleepTimeStart = sleep[1]
        if (sleep.count() % 2 == 0)
            userSleepSessionEntity.sleepTimes.sleepTimeEnd = sleep[sleep.count()-2]
        else
            userSleepSessionEntity.sleepTimes.sleepTimeEnd = sleep[sleep.count()-1]

        userSleepSessionEntity.sleepUserType.userFactorPattern = userFactorSleep.first


        storeRepository.updateUserSleepFound(true)
        storeRepository.clearUserSleepHistory()
        storeRepository.setUserSleepHistory(sleep)
        storeRepository.updateIsUserSleeping(sleep.count() % 2 != 0)

        var sleepTime = 0
        for(i in 1 until sleep.count()-1 step 2)
        {
            sleepTime +=  sleep[i + 1] - sleep[i]
        }

        storeRepository.updateUserSleepTime(sleepTime)

        // insert or update the user sleep session
        dbNormalRepository.insertUserSleepSession(userSleepSessionEntity)

        //endregion

        calculateUserSleepStates(userSleepSessionEntity, rawApiData.asReversed(), sleep)
    }


    /**
     * Calls the sleep calculation and in a second step defines the factor for the user... ( if no sleep is found the factor is adjustet to light or superlight)
     */
    private suspend fun getSleepAndAdjustFactor(parameter: SleepTimeParameter, rawApiData: List<SleepApiRawDataEntity>) : Pair<UserFactorPattern, List<Int>>{

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
            sleep = calculateSleepTime(actualParameter, rawApiData.asReversed())

            ufp = UserFactorPattern.values()[ufp.ordinal - 1]
        }

        return Pair(ufp, sleep)
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
                awakeList.addAll(rawApiData.filter { x -> x.timestampSeconds >= sleep[i] && x.timestampSeconds < sleep[i + 1] })
            }
            else
            {
                sleepList.addAll(rawApiData.filter { x -> x.timestampSeconds >= sleep[i] && x.timestampSeconds < sleep[i + 1] })
            }

            isSleep = !isSleep
        }

        return SleepModel.calculateModel(awakeList, sleepList)
    }

    /**
     * Finds pre-defined patterns by a model and returns the needed parameter and found patterns for it
     */
    private suspend fun getTimePatternIfAvailable(actualModel: SleepModel) : Pair<ArrayList<SleepTimePattern>, SleepTimeParameter> {

        val patterns = arrayListOf<SleepTimePattern>()
        val parameter = mutableListOf<SleepTimeParameter>()

        val list = dbRepository.allSleepTimeModels.first()


        // add every pattern that matches the model
        list.forEach {
            val a = it.checkIfIsModel(actualModel, 0.005f)
            if (a.ordinal != 0)
            {
                patterns.add(a)
                parameter.add(dbRepository.getSleepTimeParameterById(a.toString()).first().sleepTimeParameter)
            }
        }

        // create the id of the param id of the model

        val aaa = list.count()
        return Pair(patterns, SleepTimeParameter.mergeParameters(parameter))
    }

    // endregion


    //region Sleep State Calculation

    /**
     * This is used to define the sleep states of the user in the before defined user sleep time...
     */
    private suspend fun calculateUserSleepStates(userSleepSessionEntity: UserSleepSessionEntity, rawApiData: List<SleepApiRawDataEntity>, sleep: List<Int>) {

        // delete all segments within that particular range
        dbNormalRepository.deleteSleepSegmentsWithin(rawApiData.first().timestampSeconds, rawApiData.last().timestampSeconds)

        // Now retrieve the time with stadard parameter for the live user sleep activity
        var parameter = SleepStateParameter()

        if(userSleepSessionEntity.sleepUserType.sleepStateLiveParams.count() == 0) {
            // check if model is already defined
            val modelAndParameter = getStatePatternIfAvailable(userSleepSessionEntity.sleepUserType.sleepLiveModel)
            val userParameter = storeRepository.actualSleepUserParameterFlow.first().sleepStatePatternList

            if (modelAndParameter.first.count() != 0) {
                //model parameters
                userSleepSessionEntity.sleepUserType.sleepStateLiveParams = modelAndParameter.first
                parameter = modelAndParameter.second
            } else if (userParameter.count() != 0 && !userParameter.contains("NONE")) {
                // use user parameters
                var paramList = mutableListOf<SleepStateParameter>()

                val userStateParameterId = storeRepository.actualSleepUserParameterFlow.first().sleepStatePatternList
                userStateParameterId.forEach() {
                    paramList.add(dbRepository.getSleepStateParameterById(it).first().sleepStateParameter)
                    userSleepSessionEntity.sleepUserType.sleepStateLiveParams.add(it)
                }

                parameter = SleepStateParameter.mergeParameters(paramList)
            } else {
                // use the default parameters
                userSleepSessionEntity.sleepUserType.sleepStateLiveParams.add(SleepStatePattern.STANDARD.toString() + UserFactorPattern.NORMAL.toString())
            }

        }
        else{
            // get used params
            var paramList = mutableListOf<SleepStateParameter>()
            userSleepSessionEntity.sleepUserType.sleepStateLiveParams.forEach() {
                paramList.add(dbRepository.getSleepStateParameterById(it).first().sleepStateParameter)
            }
            parameter = SleepStateParameter.mergeParameters(paramList)
        }


        val sleepSegments = calculateSleepStates(parameter, rawApiData,userSleepSessionEntity)

        // Setup all data for the sleep session

        // Save awake time
        userSleepSessionEntity.sleepTimes.awakeTime = sleepSegments.filter { x-> x.sleepState == SleepState.AWAKE }?.sumBy { x ->x.timestampSecondsEnd - x.timestampSecondsStart }
        userSleepSessionEntity.sleepTimes.lightSleepDuration = sleepSegments.filter { x-> x.sleepState == SleepState.LIGHT }?.sumBy { x ->x.timestampSecondsEnd - x.timestampSecondsStart }
        userSleepSessionEntity.sleepTimes.deepSleepDuration = sleepSegments.filter { x-> x.sleepState == SleepState.DEEP }?.sumBy { x ->x.timestampSecondsEnd - x.timestampSecondsStart }
        userSleepSessionEntity.sleepTimes.remSleepDuration = sleepSegments.filter { x-> x.sleepState == SleepState.REM }?.sumBy { x ->x.timestampSecondsEnd - x.timestampSecondsStart }
        userSleepSessionEntity.sleepTimes.sleepDuration = userSleepSessionEntity.sleepTimes.lightSleepDuration + userSleepSessionEntity.sleepTimes.deepSleepDuration + userSleepSessionEntity.sleepTimes.remSleepDuration

        // Save usersleepsession
        dbNormalRepository.insertUserSleepSession(userSleepSessionEntity)
        // save user sleep segments
        dbNormalRepository.insertSleepSegments(sleepSegments)
    }

    /**
     * Finds pre-defined patterns by a model and returns the needed parameter and found patterns for it
     */
    private suspend fun getStatePatternIfAvailable(actualModel: SleepModel) : Pair<ArrayList<String>, SleepStateParameter> {

        val patterns = arrayListOf<String>()
        val parameter = mutableListOf<SleepStateParameter>()

        val list = dbRepository.allSleepStateModels.first()

        val a = 1

        // add every pattern that matches the model
        list.forEach {
            val a = it.checkIfIsModel(actualModel, 0.025f)
            if (a != "")
            {
                patterns.add(a)
                parameter.add(dbRepository.getSleepStateParameterById(a.toString()).first().sleepStateParameter)
            }

        }

        // create the id of the param id of the model

        return Pair(patterns, SleepStateParameter.mergeParameters(parameter))
    }

    /**
     * Takes a parameter and the raw sleep api data and calculates the sleep times and retruns a list with timestamps when user is sleeping or awake
     */
    private fun calculateSleepStates(parameters: SleepStateParameter, rawApiData: List<SleepApiRawDataEntity>, userSleepSessionEntity: UserSleepSessionEntity) : List<SleepSegmentEntity> {

        val sleepSegmentList = mutableListOf<SleepSegmentEntity>()

        // Create list with only the sleep segments
        var sleepList = rawApiData.filter{
            it.timestampSeconds >= userSleepSessionEntity.sleepTimes.sleepTimeStart &&
            it.timestampSeconds < userSleepSessionEntity.sleepTimes.sleepTimeEnd
        }

        var sleepStateList = mutableListOf<SleepState>()

        // for each point in the sleep time, we have to calculate some lists and points
        sleepList.forEach{

            if (it.confidence <= parameters.sleepSleepBorder ||
                    it.motion >= parameters.sleepMotionBorder ||
                    it.light >= parameters.sleepLightBorder) {
                if (it.confidence <= parameters.soundClearSleepBorder ||
                        it.motion <= parameters.soundClearMotionBorder) {
                    sleepStateList.add(SleepState.LIGHT)
                }
                else {
                    sleepStateList.add(SleepState.AWAKE)
                }
            }
            else if (it.confidence <= parameters.deepSleepSleepBorder ||
                    it.motion >= parameters.deepSleepMotionBorder ||
                    it.light >= parameters.deepSleepLightBorder) {
                sleepStateList.add(SleepState.LIGHT)
            }
            else if
                    (it.confidence <= parameters.remSleepSleepBorder ||
                    it.motion >= parameters.remSleepMotionBorder ||
                    it.light >= parameters.remSleepLightBorder) {
                sleepStateList.add(SleepState.DEEP)
            }
            else {
                sleepStateList.add(SleepState.REM)
            }




        }

        sleepStateList = sleepStateList.asReversed()

        var startTime:Int = 0
        var endTime:Int
        var actualState = SleepState.NONE

        // Now we put long sleep states together to segments

        for(i in 0 until sleepStateList.count()) {

            if(sleepStateList[i] != actualState) {

                if (actualState != SleepState.NONE){
                    endTime = sleepList[i].timestampSeconds
                    sleepSegmentList.add(SleepSegmentEntity(startTime, endTime, actualState))
                }

                actualState = sleepStateList[i]
                startTime = sleepList[i].timestampSeconds
            }
        }

        endTime = sleepList.last().timestampSeconds
        sleepSegmentList.add(SleepSegmentEntity(startTime, endTime, actualState))

        return sleepSegmentList
    }


    //endregion


}
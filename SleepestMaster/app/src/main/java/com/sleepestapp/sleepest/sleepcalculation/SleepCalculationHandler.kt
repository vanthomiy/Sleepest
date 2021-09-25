package com.sleepestapp.sleepest.sleepcalculation

import android.content.Context
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.googleapi.SleepHandler
import com.sleepestapp.sleepest.model.data.*
import com.sleepestapp.sleepest.storage.db.UserSleepSessionEntity
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.storage.db.SleepApiRawDataEntity
import com.google.android.gms.location.DetectedActivity
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.*
import java.util.stream.IntStream.range

/**
 * This class provides all functionality that is necessary for predicting the users sleep
 * This class is called from the background an analyzes the [SleepApiRawDataEntity] from the database and stores the predicted values in the database
 */
class SleepCalculationHandler(val context: Context) {

    //region Database instantiation

    /**
     * Scope is used to write to the database async without affecting the normal process
     */
    private val scope: CoroutineScope = MainScope()

    /**
     * With [dataBaseRepository] we can access all the data from the database
     */
    private val dataBaseRepository: DatabaseRepository by lazy {
        (context.applicationContext as MainApplication).dataBaseRepository
    }

    /**
     * With [dataStoreRepository] we can access all the data from the proto store
     */
    private val dataStoreRepository: DataStoreRepository by lazy {
        (context.applicationContext as MainApplication).dataStoreRepository
    }

    private val sleepHandler : SleepHandler by lazy {
        SleepHandler.getHandler(context)
    }

    //endregion

    //region sleep calculation helpers

    /**
     * Get the frequency of the list ...its whether 5, 10 or 30 min type of [SleepDataFrequency]
     * It depends on how often there where recordings in the time we need to know the values
     */
    fun getFrequencyFromListByHours(hours:Float, sleepList: List<SleepApiRawDataEntity>) : SleepDataFrequency
    {
        if(sleepList.count() == 0)
        {
            return SleepDataFrequency.NONE
        }

        // actual datetime
        val frequency = (hours * 60.0) / sleepList.count().toFloat()

        return when {
            frequency < 10 -> { SleepDataFrequency.FIVE }
            frequency < 30 -> { SleepDataFrequency.TEN }
            else -> { SleepDataFrequency.THIRTY }
        }
    }

    /**
     * Takes the [SleepApiRawDataEntity] and norms the time with given parameters
     * [hours] is the relative duration where the data should be retrieved from
     * [isBeforeAndAfter] defines whether the data should be only retrieved before or also after the passed time
     * [seconds] the time where we want to specify the data from
     */
    fun createTimeNormedData(hours:Float, isAfter:Boolean, seconds:Int, list: List<SleepApiRawDataEntity>): Pair<List<SleepApiRawDataEntity>, SleepDataFrequency>
    {
        // check the frequency
        val secondsPast = (seconds - (hours * 3600)).toInt()
        val secondsFuture = (seconds + (hours * 3600)).toInt()
        val fullList = list.sortedByDescending { x->x.timestampSeconds }

        var filteredList = if(!isAfter)
            fullList.filter { x -> x.timestampSeconds in secondsPast-1 until seconds+1 }.toList().sortedByDescending { x-> x.timestampSeconds }
        else
            fullList.filter { x -> x.timestampSeconds in seconds-1 until secondsFuture+1 }.toList().sortedBy { x-> x.timestampSeconds }

        val futureItems = fullList.filter { x -> x.timestampSeconds > seconds+1 }.sortedByDescending { x->x.timestampSeconds }

        if((filteredList.count() <= 1 && !isAfter) ||
            (isAfter && futureItems.count() == 0))
        {
            return Pair(listOf<SleepApiRawDataEntity>(),SleepDataFrequency.NONE)
        }

        if (isAfter && fullList.first().timestampSeconds < secondsFuture)
            return Pair(listOf<SleepApiRawDataEntity>(),SleepDataFrequency.NONE)

        val frequencyType = getFrequencyFromListByHours(hours, filteredList)

        val minutes = (hours * 60).toInt()
        val frequency = SleepDataFrequency.getValue(frequencyType)

        var timeNormedData = mutableListOf<SleepApiRawDataEntity>()

        for (i in 0 until minutes step frequency) {
            // get the first element that time is smaller then requested
            // If no item is available anymore take the last usable one
                if(!isAfter){
                    val requestedSecondsPast = seconds - (i * 60)
                    var itemPast: SleepApiRawDataEntity? = filteredList.firstOrNull { x -> x.timestampSeconds <= requestedSecondsPast }
                    timeNormedData.add(itemPast ?: filteredList.last())
                }
                else{
                    val requestedSecondsFuture = seconds + (i * 60)
                    var itemFuture: SleepApiRawDataEntity? = filteredList.firstOrNull { x -> x.timestampSeconds >= requestedSecondsFuture }
                    var last = SleepApiRawDataEntity(
                        timestampSeconds = filteredList.last().timestampSeconds + (i * 60),
                        filteredList.last().confidence,
                        filteredList.last().motion,
                        filteredList.last().light,
                        filteredList.last().sleepState,
                        filteredList.last().oldSleepState,
                        filteredList.last().wakeUpTime
                        )
                    timeNormedData.add(itemFuture ?: last)
                }
        }

        return Pair(timeNormedData.sortedByDescending { x -> x.timestampSeconds }, frequencyType)
    }



    /**
     * Gets the user activity and classifies what type of activity was done
     * TODO(We have to define how much activity is what state)
     */
    private suspend fun getUserActivityOnDay(time:LocalDateTime) : ActivityOnDay{

        val userActivity =
            dataBaseRepository.getActivityApiRawDataFromDate(time.minusDays(1)).first()
                .sortedBy { x -> x.timestampSeconds }

        var activityCount = 0

        if(userActivity.isEmpty())
            return ActivityOnDay.NONE
        for (i in range(0,(userActivity.count()-2))){
            if(userActivity[i].activity == DetectedActivity.WALKING || userActivity[i].activity == DetectedActivity.ON_FOOT){

                activityCount += (userActivity[i+1].timestampSeconds - userActivity[i].timestampSeconds) / 60
            }
            else if(userActivity[i].activity == DetectedActivity.ON_BICYCLE || userActivity[i].activity == DetectedActivity.RUNNING){

                activityCount += 3 * (userActivity[i+1].timestampSeconds - userActivity[i].timestampSeconds) / 60
            }
        }

        return when {
            activityCount > 180 -> ActivityOnDay.EXTREMACTIVITY
            activityCount > 90 -> ActivityOnDay.MUCHACTIVITY
            activityCount > 60 -> ActivityOnDay.NORMALACTIVITY
            activityCount > 30 -> ActivityOnDay.SMALLACTIVITY
            else -> ActivityOnDay.NOACTIVITY
        }
    }

    /**
     * Defines the [SleepState] for a sleep
     */
    fun defineSleepStates(time:Int, sleepApiRawDataEntity:List<SleepApiRawDataEntity>, lightConditions: LightConditions) : SleepState{

        // get the actual sleepApiDataList
        val filteredSleepApiRawDataEntity = sleepApiRawDataEntity.sortedByDescending { x -> x.timestampSeconds }


        // get normed list
        val (normedSleepApiDataPast, frequency1) = createTimeNormedData(0.5f, false, time, filteredSleepApiRawDataEntity.toList())
        val (normedSleepApiDataFuture, frequency2) = createTimeNormedData(0.5f, true, time, filteredSleepApiRawDataEntity.toList())

        if(frequency1 == SleepDataFrequency.NONE || frequency2 == SleepDataFrequency.NONE) {
            return SleepState.SLEEPING
        }

        // create features for ml model
        val sleepClassifier = SleepClassifier.getHandler(context)

        // call the ml model
        return  sleepClassifier.defineUserSleep(normedSleepApiDataPast, normedSleepApiDataFuture, lightConditions)
    }

    // endregion

    //region sleep calculation functions

    /**
     * workaround to call the suspend function from JAVA code
     * calls [checkIsUserSleeping]
     */
    fun checkIsUserSleepingJob(localTime: LocalDateTime? = null){
        scope.launch{
            checkIsUserSleeping(localTime)
        }
    }

    /**
     * Checks if the user is Sleeping or not at the moment.
     * Saves the state in the [SleepApiRawDataEntity] and in the [LiveUserSleepActivityStatus]
     * [time] the actual time in seconds
     */
    suspend fun checkIsUserSleeping(localTime: LocalDateTime? = null, finalCalc: Boolean = false){

            // get the actual sleepApiDataList

        val date = localTime ?: LocalDateTime.now()

        val timestamp = date.atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
        // calculate all sleep states when the user is sleeping
        val id =
            UserSleepSessionEntity.getIdByTimeStamp(timestamp)

        val sessionAvailable = dataBaseRepository.checkIfUserSessionIsDefinedById(id)
        val sleepSessionEntity = dataBaseRepository.getOrCreateSleepSessionById(id)

        val endTime = if(sessionAvailable)
            dataBaseRepository.getOrCreateSleepSessionById(id).sleepTimes.sleepTimeEnd
        else
            dataStoreRepository.getSleepTimeEnd()

        val startTime = if(sessionAvailable)
            dataBaseRepository.getOrCreateSleepSessionById(id).sleepTimes.sleepTimeStart
        else
            dataStoreRepository.getSleepTimeBegin()

        val sleepApiRawDataEntity =
            dataBaseRepository.getSleepApiRawDataFromDate(date, endTime, startTime).first()
                ?.sortedBy { x -> x.timestampSeconds }

        if (sleepApiRawDataEntity == null || sleepApiRawDataEntity.count() == 0) {
            // do something!
            return
        }

        val sleepClassifier = SleepClassifier.getHandler(context)

        val mobilePosition =
            if(MobilePosition.getCount(dataStoreRepository.sleepParameterFlow.first().standardMobilePosition) == MobilePosition.UNIDENTIFIED) // create features for ml model
            // call the model
                sleepClassifier.defineTableBed(sleepApiRawDataEntity)
            else
                MobilePosition.getCount(dataStoreRepository.sleepParameterFlow.first().standardMobilePosition)

        val lightConditions =
            if(LightConditions.getCount(dataStoreRepository.sleepParameterFlow.first().standardLightCondition) == LightConditions.UNIDENTIFIED) // create features for ml model
            // call the model
                sleepClassifier.defineLightConditions(sleepApiRawDataEntity, false)
            else
                LightConditions.getCount(dataStoreRepository.sleepParameterFlow.first().standardLightCondition)

        val mobileUseFrequency = MobileUseFrequency.getCount(dataStoreRepository.sleepParameterFlow.first().mobileUseFrequency)


        // check for each sleepstate
        sleepApiRawDataEntity.forEach { data ->
            // First definition without future data
            if(data.sleepState == SleepState.NONE){

                // get normed list
                val (normedSleepApiDataBefore, frequency) = createTimeNormedData(
                    0.5f,
                    false,
                    data.timestampSeconds,
                    sleepApiRawDataEntity.toList()
                )

                if(frequency == SleepDataFrequency.NONE) {

                    dataBaseRepository.updateSleepApiRawDataSleepState(
                        data.timestampSeconds,
                        SleepState.AWAKE
                    )

                    return@forEach
                }

                // call the ml model
                data.sleepState = sleepClassifier.isUserSleeping(
                    normedSleepApiDataBefore,
                    null,
                    mobilePosition,
                    lightConditions,
                    mobileUseFrequency
                )

                dataBaseRepository.updateSleepApiRawDataSleepState(
                    data.timestampSeconds,
                    data.sleepState
                )

            }

            if (data.sleepState != SleepState.NONE && data.oldSleepState == SleepState.NONE){
                // get normed list
                val (normedSleepApiDataBefore, frequency1) = createTimeNormedData(
                    1f,
                    false,
                    data.timestampSeconds,
                    sleepApiRawDataEntity.toList()
                )

                val (normedSleepApiDataAfter, frequency2) = createTimeNormedData(
                    1f,
                    true,
                    data.timestampSeconds,
                    sleepApiRawDataEntity.toList()
                )


                if(frequency1 == SleepDataFrequency.NONE || frequency2 == SleepDataFrequency.NONE){

                    dataBaseRepository.updateSleepApiRawDataSleepState(
                        data.timestampSeconds,
                        data.sleepState
                    )

                    return@forEach
                }

                // call the ml model
                data.sleepState = sleepClassifier.isUserSleeping(
                    normedSleepApiDataBefore,
                    normedSleepApiDataAfter,
                    mobilePosition,
                    lightConditions,
                    mobileUseFrequency
                )

                dataBaseRepository.updateSleepApiRawDataSleepState(
                    data.timestampSeconds,
                    data.sleepState
                )

                dataBaseRepository.updateOldSleepApiRawDataSleepState(
                    data.timestampSeconds,
                    data.sleepState
                )
            }

            // Workaround to prevent NONE Sleep States
            if(finalCalc && data.sleepState == SleepState.NONE){
                dataBaseRepository.updateSleepApiRawDataSleepState(
                    data.timestampSeconds,
                    SleepState.AWAKE
                )
            }
        }

        // update live user sleep activity
        dataStoreRepository.updateIsUserSleeping(sleepApiRawDataEntity.last().sleepState == SleepState.SLEEPING)
        dataStoreRepository.updateUserSleepTime(
            SleepApiRawDataEntity.getSleepTime(
                sleepApiRawDataEntity
            )
        )

    }

    /**
     * Sets the user sleep states for every sleeping state
     * Saves the state in the [SleepApiRawDataEntity] and in the [LiveUserSleepActivityStatus] and in the [UserSleepSessionEntity] with id
     * Stores alarm data in the main
     * [time] = the actual time in seconds
     */
    suspend fun defineUserWakeup(localTime: LocalDateTime? = null, setAlarm:Boolean = true) {

        val sleepClassifier = SleepClassifier.getHandler(context)

        val date = localTime ?: LocalDateTime.now()

        val timestamp = date.atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
        // calculate all sleep states when the user is sleeping
        val id =
            UserSleepSessionEntity.getIdByTimeStamp(timestamp)

        val sessionAvailable = dataBaseRepository.checkIfUserSessionIsDefinedById(id)
        val sleepSessionEntity = dataBaseRepository.getOrCreateSleepSessionById(id)

        val endTime = if(sessionAvailable)
                sleepSessionEntity.sleepTimes.sleepTimeEnd
            else
                dataStoreRepository.getSleepTimeEnd()

        val startTime = if(sessionAvailable)
            sleepSessionEntity.sleepTimes.sleepTimeStart
        else
            dataStoreRepository.getSleepTimeBegin()

        // for each sleeping time, we have to define the sleep state
        val time = localTime ?: LocalDateTime.now()
        val sleepApiRawDataEntity =
            dataBaseRepository.getSleepApiRawDataFromDate(time, endTime, startTime).first()
                ?.sortedBy { x -> x.timestampSeconds }

        if (sleepApiRawDataEntity == null || sleepApiRawDataEntity.count() == 0) {
            // do something!

            return
        }


        // only when in sleep time and not after it

        if(setAlarm){
            sleepSessionEntity.mobilePosition =
                if(MobilePosition.getCount(dataStoreRepository.sleepParameterFlow.first().standardMobilePosition) == MobilePosition.UNIDENTIFIED) // create features for ml model
                    sleepClassifier.defineTableBed(sleepApiRawDataEntity)
                else if(sleepSessionEntity.mobilePosition == MobilePosition.UNIDENTIFIED)
                    MobilePosition.getCount(dataStoreRepository.sleepParameterFlow.first().standardMobilePosition)
                else
                    sleepSessionEntity.mobilePosition

            // only when unidentified
            if(sleepSessionEntity.lightConditions == LightConditions.UNIDENTIFIED){
                sleepSessionEntity.lightConditions =
                    if(LightConditions.getCount(dataStoreRepository.sleepParameterFlow.first().standardLightCondition) == LightConditions.UNIDENTIFIED) // create features for ml model
                    // call the model
                        sleepClassifier.defineLightConditions(sleepApiRawDataEntity, true)
                    else
                        LightConditions.getCount(dataStoreRepository.sleepParameterFlow.first().standardLightCondition)
            }
        }

        // if in bed then check the single states of the sleep
        if (sleepSessionEntity.mobilePosition == MobilePosition.INBED) {
            sleepApiRawDataEntity.forEach()
            {
                // we take all sleep values that are not already defined as light or deep but sleeping
                if (it.sleepState == SleepState.SLEEPING) {
                    // we need to calculate the sleep state
                    // and then we update it in the sleep api raw data entity
                    dataBaseRepository.updateOldSleepApiRawDataSleepState(
                        it.timestampSeconds,
                        it.sleepState
                    )

                    val result = defineSleepStates(it.timestampSeconds, sleepApiRawDataEntity, sleepSessionEntity.lightConditions)
                    dataBaseRepository.updateSleepApiRawDataSleepState(
                        it.timestampSeconds,
                        result
                    )
                    it.sleepState = result
                }
            }
        }

        // now we need to define "how long" the user slept already
        // define the sleep times and states


        sleepSessionEntity.sleepTimes.sleepTimeStart =
            SleepApiRawDataEntity.getSleepStartTime(sleepApiRawDataEntity)
        sleepSessionEntity.sleepTimes.lightSleepDuration =
            SleepApiRawDataEntity.getSleepTimeByState(sleepApiRawDataEntity, SleepState.LIGHT)
        // also take SLEEPING state into account
        sleepSessionEntity.sleepTimes.lightSleepDuration +=
            SleepApiRawDataEntity.getSleepTimeByState(sleepApiRawDataEntity, SleepState.SLEEPING)
        sleepSessionEntity.sleepTimes.deepSleepDuration =
            SleepApiRawDataEntity.getSleepTimeByState(sleepApiRawDataEntity, SleepState.DEEP)
        sleepSessionEntity.sleepTimes.remSleepDuration =
            SleepApiRawDataEntity.getSleepTimeByState(sleepApiRawDataEntity, SleepState.REM)
        sleepSessionEntity.sleepTimes.awakeTime =
            SleepApiRawDataEntity.getAwakeTime(sleepApiRawDataEntity)


        // how long have the user slept already? REM is not counted at the moment!!!
        // use some custom factors...except its not on table, then use just 1
        // TODO(Add factors to the different sleep states for the calculation)
        sleepSessionEntity.sleepTimes.sleepDuration =
            (sleepSessionEntity.sleepTimes.lightSleepDuration * (if (sleepSessionEntity.mobilePosition != MobilePosition.INBED) 1f else 1f) +
                    sleepSessionEntity.sleepTimes.deepSleepDuration * 1f + sleepSessionEntity.sleepTimes.remSleepDuration * 1f).toInt()


        // now define the new wakeUpPoint for the user...
        // sleep time

        // get user activity
        val activity = getUserActivityOnDay(time)
        sleepSessionEntity.userSleepRating.activityOnDay = activity

        if(setAlarm) {

            // store in the alarm
                val afterSleepTime = dataStoreRepository.isAfterSleepTime(date.toLocalTime())
            val alarm = dataBaseRepository.getNextActiveAlarm(afterSleepTime.first, afterSleepTime.second) ?: return

            var sleepDuration = alarm.sleepDuration

            // If include then add the factors to it
            if (dataStoreRepository.sleepParameterFlow.first().implementUserActivityInSleepTime)
                sleepDuration = (sleepDuration.toFloat() * ActivityOnDay.getFactor(activity)).toInt()

            var restSleepTime = sleepDuration - (sleepSessionEntity.sleepTimes.sleepDuration * 60)

            val actualTimeSeconds = localTime?.toLocalTime()?.toSecondOfDay()
                ?: SleepTimeValidationUtil.getSecondsOfDay()
            var wakeUpTime = actualTimeSeconds + (restSleepTime)

            // if time is greater then 1 day
            if (wakeUpTime > Constants.DAY_IN_SECONDS) {
                wakeUpTime -= Constants.DAY_IN_SECONDS
            }

            // now check if user actually awake, and how long user has been awake in one time

            if (dataStoreRepository.getEndAlarmAfterFired()) {
                val actualAwakeTime =
                    SleepApiRawDataEntity.getActualAwakeTime(sleepApiRawDataEntity) / 60

                // set user can wakeup
                dataBaseRepository.updateAlreadyAwake(alreadyAwake = (actualAwakeTime > Constants.DELAY_USER_ALREADY_AWAKE), alarm.id)
            }

            // store in the alarm...!!!
            dataBaseRepository.updateWakeupTime(wakeupTime = wakeUpTime, alarm.id)


            val last = sleepApiRawDataEntity.last().timestampSeconds
            dataBaseRepository.updateSleepApiRawDataWakeUp(
                last,
                wakeUpTime
            )

            sleepApiRawDataEntity.last().wakeUpTime = wakeUpTime

            sleepSessionEntity.sleepTimes.sleepTimeEnd = last

        }
        else {
            sleepSessionEntity.sleepTimes.sleepTimeEnd =
                SleepApiRawDataEntity.getSleepEndTime(sleepApiRawDataEntity)

            // now we are also recalculating the default light and mobile position over the last week

            val sleepSessions = dataBaseRepository.getUserSleepSessionSinceDays(7).first()

            if(sleepSessions.filter { x -> x.mobilePosition == MobilePosition.INBED }.count() >= sleepSessions.count() / 2){
                dataStoreRepository.updateStandardMobilePositionOverLastWeek(MobilePosition.INBED.ordinal)
            }
            else {
                dataStoreRepository.updateStandardMobilePositionOverLastWeek(MobilePosition.ONTABLE.ordinal)
            }

            if(sleepSessions.filter { x -> x.lightConditions == LightConditions.LIGHT }.count() >= sleepSessions.count() / 2){
                dataStoreRepository.updateLigthConditionOverLastWeek(LightConditions.LIGHT.ordinal)
            }
            else {
                dataStoreRepository.updateLigthConditionOverLastWeek(LightConditions.DARK.ordinal)
            }

            sleepApiRawDataEntity.forEach()
            {
                // we take all sleep values that are not already defined as light or deep but sleeping
                if ((it.sleepState == SleepState.SLEEPING) && (sleepSessionEntity.mobilePosition == MobilePosition.INBED)) {

                    // we need to calculate the sleep state
                    // and then we update it in the sleep api raw data entity

                    dataBaseRepository.updateSleepApiRawDataSleepState(
                        it.timestampSeconds,
                        SleepState.LIGHT
                    )
                }

                if (it.sleepState == SleepState.NONE) {
                    dataBaseRepository.updateSleepApiRawDataSleepState(
                        it.timestampSeconds,
                        SleepState.AWAKE
                    )
                }
            }
        }

        // Add the actual sleep time to the sleep session entity at this moment
        if(sleepSessionEntity.sleepTimes.possibleSleepTimeStart == -1){
            val param = dataStoreRepository.sleepParameterFlow.first()
            sleepSessionEntity.sleepTimes.possibleSleepTimeStart = param.sleepTimeStart
            sleepSessionEntity.sleepTimes.possibleSleepTimeEnd= param.sleepTimeEnd
        }


        dataStoreRepository.updateUserSleepTime(sleepSessionEntity.sleepTimes.sleepDuration)
        dataBaseRepository.insertUserSleepSession(sleepSessionEntity)

    }


    /**
     * workaround to call the suspend function from JAVA code
     * calls [userNotSleeping]
     */
    fun userNotSleepingJob(){

        scope.launch { userNotSleeping(null) }
    }

    /**
     * Defines that the user not fall asleep already and we should change the states of the passed data to [SleepState.AWAKE]
     * Only call this at the first 1 to 2 hours of the sleep... because we overwriting all sleep data before
     * Later use the [userCurrentlyNotSleeping]
     */
    suspend fun userNotSleeping(localTime: LocalDateTime? = null) {

        // for each sleeping time, we have to define the sleep state
        val time = localTime ?: LocalDateTime.now()
        val sleepApiRawDataEntity =
            dataBaseRepository.getSleepApiRawDataFromDateLive(time).first()
                ?.sortedBy { x -> x.timestampSeconds }

        if (sleepApiRawDataEntity == null || sleepApiRawDataEntity.count() == 0) {
            return
        }

        sleepApiRawDataEntity.forEach()
        {
            // we take all sleep values that are not already defined as light or deep but sleeping
            if (it.sleepState != SleepState.AWAKE && it.sleepState != SleepState.NONE) {
                // we need to calculate the sleep state
                // and then we update it in the sleep api raw data entity
                dataBaseRepository.updateOldSleepApiRawDataSleepState(
                    it.timestampSeconds,
                    it.sleepState
                )
                dataBaseRepository.updateSleepApiRawDataSleepState(
                    it.timestampSeconds,
                    SleepState.AWAKE
                )
                it.sleepState = SleepState.AWAKE
            }
        }


        // update live user sleep activity to zero and no sleep
        dataStoreRepository.updateIsUserSleeping(false)
        dataStoreRepository.updateUserSleepTime(0)

        // workaround to prevent the sleep data.
        // TODO(Check if necessary anymore?)
        sleepHandler.stopSleepHandler()
        sleepHandler.startSleepHandler()

    }

    /**
     * workaround to call the suspend function from JAVA code
     * calls [userCurrentlyNotSleeping]
     */
    fun userCurrentlyNotSleepingJob(){
        scope.launch { userCurrentlyNotSleeping(null) }
    }

    /**
     * Defines that the user is currently awake and we should set the actual state to awake
     * Call this in the night. We only change the state until the next Sleep API Data retrieves...
     */
    suspend fun userCurrentlyNotSleeping(localTime: LocalDateTime? = null){         // for each sleeping time, we have to define the sleep state
        val time = localTime ?: LocalDateTime.now()
        val sleepApiRawDataEntity =
            dataBaseRepository.getSleepApiRawDataFromDateLive(time).first()
                ?.sortedBy { x -> x.timestampSeconds }

        if (sleepApiRawDataEntity == null || sleepApiRawDataEntity.count() == 0) {
            return
        }

        // calculate all sleep states when the user is sleeping
        // update this or update the next sleep state ??

        val id = sleepApiRawDataEntity.maxOf { x -> x.timestampSeconds }
        val data = sleepApiRawDataEntity.find { x -> x.timestampSeconds == id }!!

        dataBaseRepository.updateOldSleepApiRawDataSleepState(id, data.sleepState)
        dataBaseRepository.updateSleepApiRawDataSleepState(id, SleepState.AWAKE)

        // update live user sleep activity to zero and no sleep
        dataStoreRepository.updateIsUserSleeping(false)

    }



    /**
     * Updates the sleep start and end times and [SleepState] of a [UserSleepSessionEntity]
     * This can be done by the user in the History Fragment
     */
    suspend fun updateSleepSessionManually(startTimeEpoch: Int, endTimeEpoch: Int, sessionId: Int) {

        val sessionEntity = dataBaseRepository.getSleepSessionById(sessionId).first().firstOrNull() ?: return

        // check what has to be done
        val newStartTime = sessionEntity.sleepTimes.sleepTimeStart != startTimeEpoch
        val newEndTime = sessionEntity.sleepTimes.sleepTimeEnd != endTimeEpoch

        val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli((sessionEntity.id.toLong() * 1000)), ZoneOffset.UTC)

        // remove before
        if (newStartTime){

            // get all api data before the sleep
            val sleepDataBeforeSleep = dataBaseRepository.getSleepApiRawDataBetweenTimestamps(sessionId, startTimeEpoch).first()

            // Make all sleep data to awake before
            sleepDataBeforeSleep?.forEach { sleepData ->

                if(sleepData.sleepState != SleepState.AWAKE && sleepData.sleepState != SleepState.NONE){
                    dataBaseRepository.updateOldSleepApiRawDataSleepState(sleepData.timestampSeconds, sleepData.sleepState)
                    dataBaseRepository.updateSleepApiRawDataSleepState(sleepData.timestampSeconds, SleepState.AWAKE)
                }
            }

        }
        // check in between
        if (newStartTime || newEndTime){

            // get all api data in between the sleep
            val sleepDataWhileSleep = dataBaseRepository.getSleepApiRawDataBetweenTimestamps(startTimeEpoch, endTimeEpoch).first()

            // Check if a old sleep state is now in between these times and retrieve it
            sleepDataWhileSleep?.forEach { sleepData ->

                if((sleepData.sleepState == SleepState.AWAKE || sleepData.sleepState == SleepState.NONE) && (sleepData.oldSleepState != SleepState.AWAKE && sleepData.oldSleepState != SleepState.NONE)){
                    dataBaseRepository.updateSleepApiRawDataSleepState(sleepData.timestampSeconds, sleepData.oldSleepState)
                    dataBaseRepository.updateOldSleepApiRawDataSleepState(sleepData.timestampSeconds, SleepState.NONE)
                }
            }

        }
        // remove after
        if (newEndTime){

            val sleptLonger = (endTimeEpoch > sessionEntity.sleepTimes.sleepTimeEnd)

            if(sleptLonger){
                // get all api data in between the sleep
                val sleepDataAfter = dataBaseRepository.getSleepApiRawDataBetweenTimestamps(sessionEntity.sleepTimes.sleepTimeEnd, endTimeEpoch).first()

                // Check if a old sleep state is now in between these times and retrieve it
                sleepDataAfter?.forEach { sleepData ->
                    if(sleepData.sleepState == SleepState.AWAKE || sleepData.sleepState == SleepState.NONE){
                        dataBaseRepository.updateSleepApiRawDataSleepState(sleepData.timestampSeconds, SleepState.SLEEPING)
                    }
                }
            }
            else{
                // get all api data in between the sleep
                val sleepDataAfter = dataBaseRepository.getSleepApiRawDataBetweenTimestamps(endTimeEpoch, sessionEntity.sleepTimes.sleepTimeEnd).first()

                // Check if a old sleep state is now in between these times and retrieve it
                sleepDataAfter?.forEach { sleepData ->
                    if(sleepData.sleepState != SleepState.AWAKE && sleepData.sleepState != SleepState.NONE){
                        dataBaseRepository.updateOldSleepApiRawDataSleepState(sleepData.timestampSeconds, sleepData.sleepState)
                        dataBaseRepository.updateSleepApiRawDataSleepState(sleepData.timestampSeconds, SleepState.AWAKE)
                    }
                }
            }
        }

        defineUserWakeup(dateTime, false)
    }


    //endregion

    /**
     * Companion object is used for static fields in kotlin
     */
    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: SleepCalculationHandler? = null

        /**
         * This should be used to create or get the actual instance of the [SleepCalculationHandler] class
         */
        fun getHandler(context: Context): SleepCalculationHandler {
            return INSTANCE ?: synchronized(this) {
                val instance = SleepCalculationHandler(context)
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
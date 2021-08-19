package com.doitstudio.sleepest_master.sleepcalculation

import android.content.Context
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.googleapi.SleepHandler
import com.doitstudio.sleepest_master.model.data.*
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * This class provides all functionality that is necessary for predicting the users sleep
 * This class is called from the background an analyzes the [SleepApiRawDataEntity] from the database and stores the predicted values in the database
 * With this we can
 *  -(TODO(define funcitons)
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

    //region private helpers

    /**
     * Gets seconds of day with local time
     */
    fun getSecondsOfDay() : Int{

        return LocalTime.now().toSecondOfDay()

    }

    /**
     * Get the frequency of the list ...its whether 5, 10 or 30 min type of [SleepDataFrequency]
     * It depends on how often there where recordings in the time we need to know the values
     */
    fun getFrequencyFromListByHours(hours:Float, isBeforeAndAfter:Boolean, seconds:Int, sleepList: List<SleepApiRawDataEntity>) : SleepDataFrequency
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

        var filteredList = if(!isAfter)
            list.filter { x -> x.timestampSeconds in secondsPast-1 until seconds+1 }.toList().sortedByDescending { x-> x.timestampSeconds }
        else
            list.filter { x -> x.timestampSeconds in seconds-1 until secondsFuture+1 }.toList().sortedBy { x-> x.timestampSeconds }

        val futureItems = list.filter { x -> x.timestampSeconds > seconds+1 }.sortedByDescending { x->x.timestampSeconds }

        if((filteredList.count() <= 1 && !isAfter) ||
            (isAfter && futureItems.count() == 0) ||
            (isAfter && futureItems.first().timestampSeconds < secondsFuture))
        {
            return Pair(listOf<SleepApiRawDataEntity>(),SleepDataFrequency.NONE)
        }

        val frequencyType = getFrequencyFromListByHours(hours, false, seconds, filteredList)

        val minutes = (hours * 60).toInt()
        val frequency = SleepDataFrequency.getValue(frequencyType)

        var timeNormedData = mutableListOf<SleepApiRawDataEntity>()

        for (i in 0 until minutes step frequency) {
            // get the first element that time is smaller then requested
            // If no item is available anymore take the last usable one
                if(!isAfter){
                    val requestedSecondsPast = seconds - (i * 60)
                    var itemPast: SleepApiRawDataEntity? = filteredList.firstOrNull { x -> x?.timestampSeconds <= requestedSecondsPast }
                    timeNormedData.add(itemPast ?: filteredList.last())
                }
                else{
                    val requestedSecondsFuture = seconds + (i * 60)
                    var itemFuture: SleepApiRawDataEntity? = filteredList.firstOrNull { x -> x?.timestampSeconds >= requestedSecondsFuture }
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

        for (i in userActivity.indices-1){
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
        val sleepApiRawDataEntity = sleepApiRawDataEntity.sortedByDescending { x -> x.timestampSeconds }


        // get normed list
        val (normedSleepApiDataPast, frequency1) = createTimeNormedData(0.5f, false, time, sleepApiRawDataEntity.toList())
        val (normedSleepApiDataFuture, frequency2) = createTimeNormedData(0.5f, true, time, sleepApiRawDataEntity.toList())

        if(frequency1 == SleepDataFrequency.NONE || frequency2 == SleepDataFrequency.NONE) {
            return SleepState.SLEEPING
        }

        // create features for ml model
        val sleepClassifier = SleepClassifier.getHandler(context)

        // call the ml model
        return  sleepClassifier.defineUserSleep(normedSleepApiDataPast, normedSleepApiDataFuture, lightConditions)
    }

    // endregion

    //region Sleep calculation functions

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
            val time = localTime ?: LocalDateTime.now()
            val sleepApiRawDataEntity =
                dataBaseRepository.getSleepApiRawDataFromDateLive(time).first()
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
                if(data.timestampSeconds == 1629225684){
                    val a = 1
                    var b = a
                }

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

                    // create features for ml model
                    val sleepClassifier = SleepClassifier.getHandler(context)


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

                if (data.sleepState != SleepState.NONE){
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

                    // create features for ml model
                    val sleepClassifier = SleepClassifier.getHandler(context)


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
                }


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

            // for each sleeping time, we have to define the sleep state
            val time = localTime ?: LocalDateTime.now()
            val sleepApiRawDataEntity =
                dataBaseRepository.getSleepApiRawDataFromDate(time).first()
                    ?.sortedBy { x -> x.timestampSeconds }

            if (sleepApiRawDataEntity == null || sleepApiRawDataEntity.count() == 0) {
                // do something!

                return
            }

            // calculate all sleep states when the user is sleeping
            val id =
                UserSleepSessionEntity.getIdByTimeStamp(sleepApiRawDataEntity.minOf { x -> x.timestampSeconds })
            val sleepSessionEntity = dataBaseRepository.getOrCreateSleepSessionById(id)

            // only when unidentified
            if(sleepSessionEntity.mobilePosition == MobilePosition.UNIDENTIFIED){
                sleepSessionEntity.mobilePosition =
                    if(MobilePosition.getCount(dataStoreRepository.sleepParameterFlow.first().standardMobilePosition) == MobilePosition.UNIDENTIFIED) // create features for ml model
                    // call the model
                        sleepClassifier.defineTableBed(sleepApiRawDataEntity)
                    else
                        MobilePosition.getCount(dataStoreRepository.sleepParameterFlow.first().standardMobilePosition)
            }

            // only when unidentified
            if(sleepSessionEntity.lightConditions == LightConditions.UNIDENTIFIED){
                sleepSessionEntity.lightConditions =
                    if(LightConditions.getCount(dataStoreRepository.sleepParameterFlow.first().standardLightCondition) == LightConditions.UNIDENTIFIED) // create features for ml model
                    // call the model
                        sleepClassifier.defineLightConditions(sleepApiRawDataEntity, true)
                    else
                        LightConditions.getCount(dataStoreRepository.sleepParameterFlow.first().standardLightCondition)
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
            sleepSessionEntity.sleepTimes.lightSleepDuration += SleepApiRawDataEntity.getSleepTimeByState(
                sleepApiRawDataEntity,
                SleepState.SLEEPING
            )
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

                // store in the alarm...!!!
                val alarm = dataBaseRepository.getNextActiveAlarm() ?: return

                var sleepDuration = alarm.sleepDuration
                // If include then add the factors to it
                if (dataStoreRepository.sleepParameterFlow.first().implementUserActivityInSleepTime)
                    sleepDuration = (sleepDuration.toFloat() * ActivityOnDay.getFactor(activity)).toInt()

                var restSleepTime = sleepDuration - (sleepSessionEntity.sleepTimes.sleepDuration * 60)

                val actualTimeSeconds = localTime?.toLocalTime()?.toSecondOfDay()
                        ?: getSecondsOfDay()
                var wakeUpTime = actualTimeSeconds + (restSleepTime)

                // if time is greater then 1 day
                if (wakeUpTime > 86400) {
                    wakeUpTime -= 86400
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

            dataStoreRepository.updateUserSleepTime(sleepSessionEntity.sleepTimes.sleepDuration)
            dataBaseRepository.insertUserSleepSession(sleepSessionEntity)

    }


    fun userNotSleepingJob(){
        scope.launch { userNotSleeping(null) }
    }

    /**
     * Defines that the user not fall asleep alredy and we should change the states of the passed data to [SleepState.AWAKE]
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

        // calculate all sleep states when the user is sleeping
        val id = sleepApiRawDataEntity.minOf { x -> x.timestampSeconds }

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

        sleepHandler.stopSleepHandler()
        sleepHandler.startSleepHandler()


    }

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
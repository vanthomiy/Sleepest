package com.doitstudio.sleepest_master.sleepcalculation

import android.content.Context
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.model.data.*
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity
import com.doitstudio.sleepest_master.sleepcalculation.ml.SleepClassifier
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
import java.time.ZoneOffset

class SleepCalculationHandler(val context: Context) {

    private val scope: CoroutineScope = MainScope()

    private val dataBaseRepository: DatabaseRepository by lazy {
        (context.applicationContext as MainApplication).dataBaseRepository
    }

    private val dataStoreRepository: DataStoreRepository by lazy {
        (context.applicationContext as MainApplication).dataStoreRepository
    }

    // region private helpers

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
    fun getFrequencyFromListByHours(hours:Int, isBeforeAndAfter:Boolean, seconds:Int, sleepList: List<SleepApiRawDataEntity>) : SleepDataFrequency
    {
        if(sleepList.count() == 0)
        {
            return SleepDataFrequency.NONE
        }

        // actual datetime
        val frequency = (hours * 60.0) / sleepList.count().toFloat()

        return when {
            frequency <= 10 -> { SleepDataFrequency.FIVE }
            frequency <= 30 -> { SleepDataFrequency.TEN }
            else -> { SleepDataFrequency.THIRTY }
        }
    }

    /**
     * Takes the [SleepApiRawDataEntity] and norms the time with given parameters
     * [hours] is the relative duration where the data should be retrieved from
     * [isBeforeAndAfter] defines whether the data should be only retrieved before or also after the passed time
     * [seconds] the time where we want to specify the data from
     */
    fun createTimeNormedData(hours:Int, isBeforeAndAfter:Boolean, seconds:Int, list: List<SleepApiRawDataEntity>): Pair<List<SleepApiRawDataEntity>, SleepDataFrequency>
    {
        // check the frequency
        val secondsPast = seconds - (hours * 3600)
        val secondsFuture = seconds + (hours * 3600)

        val sleepList = if (!isBeforeAndAfter)
            list.filter { x -> x.timestampSeconds in secondsPast-1 until seconds+1 }.toList()
        else
            list.filter { x -> x.timestampSeconds in secondsPast-1 until secondsFuture+1 }.toList()

        if(sleepList.count() == 0)
        {
            return Pair(listOf<SleepApiRawDataEntity>(),SleepDataFrequency.NONE)
        }

        val frequencyType = getFrequencyFromListByHours(hours, isBeforeAndAfter, seconds, list)

        val minutes = hours * 60
        val frequency = SleepDataFrequency.getValue(frequencyType)

        val sleepListPast = sleepList.sortedByDescending { x-> x.timestampSeconds }.toList()
        val sleepListFuture = sleepList.sortedBy { x-> x.timestampSeconds }.toList()

        var timeNormedData = mutableListOf<SleepApiRawDataEntity>()

        for (i in 0 until minutes step frequency) {
            // get the first element that time is smaller then requested
            // If no item is available anymore take the last usable one
            val requestedSecondsPast = seconds - (i * 60)
            var itemPast: SleepApiRawDataEntity? = sleepListPast.firstOrNull { x -> x?.timestampSeconds <= requestedSecondsPast }
            timeNormedData.add(itemPast ?: sleepListPast.last())

            // If no
            if (isBeforeAndAfter) {
                val requestedSecondsFuture = seconds + (i * 60)
                var itemFuture: SleepApiRawDataEntity? = sleepListFuture.firstOrNull { x -> x?.timestampSeconds >= requestedSecondsFuture }
                timeNormedData.add(itemFuture ?: sleepListFuture.last())
            }
        }

        return Pair(timeNormedData.sortedByDescending { x -> x.timestampSeconds }, frequencyType)
    }

    /**
     * Gets the user activity and classifies what type of activity was done
     */
    suspend fun getUserActivityOnDay(time:LocalDateTime) : ActivityOnDay{

        val userActivity =
            dataBaseRepository.getActivityApiRawDataFromDate(time.minusDays(1)).first()
                .sortedBy { x -> x.timestampSeconds }

        var activityCount = 0

        if(userActivity.isEmpty())
            return ActivityOnDay.NONE

        userActivity.forEach{
            if(it.activity == DetectedActivity.WALKING || it.activity == DetectedActivity.ON_FOOT){

                activityCount += 1 * it.duration

            }
            else if(it.activity == DetectedActivity.ON_BICYCLE || it.activity == DetectedActivity.RUNNING){

                activityCount += 3 * it.duration

            }
        }

        // lets say > 1 Stunde schwer und 3 Stunden leicht ist viel
        // 60 * 3 + 60 * 3
        // 360


        return when {
            activityCount > 360 -> ActivityOnDay.EXTREMACTIVITY
            activityCount > 180 -> ActivityOnDay.MUCHACTIVITY
            activityCount > 90 -> ActivityOnDay.NORMALACTIVITY
            activityCount > 45 -> ActivityOnDay.SMALLACTIVITY
            else -> ActivityOnDay.NOACTIVITY
        }

    }

    /**
     * Defines the [SleepState] for a sleep
     */
    fun defineSleepStates(time:Int, sleepApiRawDataEntity:List<SleepApiRawDataEntity>) : SleepState{

        // get the actual sleepApiDataList
        val sleepApiRawDataEntity = sleepApiRawDataEntity.sortedByDescending { x -> x.timestampSeconds }

        // get count of future data
        val futureCount = sleepApiRawDataEntity.filter{x-> x.timestampSeconds > time}.count()

        // return just sleeping if there is a problem with the list or not enough future data
        if(sleepApiRawDataEntity == null || sleepApiRawDataEntity.count() == 0 || futureCount < 3 ){
            // do something!
            return SleepState.SLEEPING
        }

        // get normed list
        val (normedSleepApiData, frequency) = createTimeNormedData(1, true, time, sleepApiRawDataEntity)

        // create features for ml model
        val sleepClassifier = SleepClassifier.getHandler(context)
        val features = sleepClassifier.createFeatures(normedSleepApiData, ModelProcess.SLEEP12, frequency)

        // call the ml model
        return  sleepClassifier.defineUserSleep(features, frequency)
    }


    /**
     * Checks wheter the phone is on bed or on table. Error returns [MobilePosition.UNIDENTIFIED]
     */
    fun checkPhonePosition(sleepApiRawDataEntity:List<SleepApiRawDataEntity>) : MobilePosition {

        // get the actual sleepApiDataList
        val sleepingData = sleepApiRawDataEntity.filter{x->x.sleepState != SleepState.NONE && x.sleepState != SleepState.AWAKE }

        if (sleepingData.count() == 0){
            return MobilePosition.UNIDENTIFIED
        }

        // now we need to calc the values to provide...
        var light = IntArray(4)
        light[0] = sleepingData.maxOf { x->x.light }
        light[1] = sleepingData.minOf { x->x.light }
        light[2] = sleepingData.sumOf { x->x.light } / sleepingData.count()
        light[3] = sleepingData.sortedBy { x-> x.light }[sleepingData.count()/2].light

        var motion = IntArray(4)
        motion[0] = sleepingData.maxOf { x->x.motion }
        motion[1] = sleepingData.minOf { x->x.motion }
        motion[2] = sleepingData.sumOf { x->x.motion } / sleepingData.count()
        motion[3] = sleepingData.sortedBy { x-> x.motion }[sleepingData.count()/2].motion

        var sleep = IntArray(4)
        sleep[0] = sleepingData.maxOf { x->x.confidence }
        sleep[1] = sleepingData.minOf { x->x.confidence }
        sleep[2] = sleepingData.sumOf { x->x.confidence } / sleepingData.count()
        sleep[3] = sleepingData.sortedBy { x-> x.confidence }[sleepingData.count()/2].confidence

        // create features for ml model
        val sleepClassifier = SleepClassifier.getHandler(context)
        val features = sleepClassifier.createTableFeatures(light, motion, sleep)

        // call the model
        return sleepClassifier.defineTableBed(features)
    }

    /**
     * Searches a light user wakeup in the next times...
     * Experimental.. should be first tested and later used...
     * Its not really easy to be defined...

     * Now we are passing the actual [sleepApiRawDataEntity] with the actual [frequency] (FIVE/TEN or THIRTY)
     * Also we are providing a [timeSpan] of time where we are allowed to wakeup and the user around the defined [wakeUpTime]

     * WHAT IT IS DOING:

     * CASE 1: Wakeuppont is to far away
     * We do nothing much and just returning the provided [wakeUpTime]
     *
     * CASE 2: Wakeuppoint is in the past
     * Should not happen but if, we check if the [frequency] * 2 includes the [wakeUpTime]
     * Yes ? -> We calculate the model and check if next time is a [SleepState.LIGHT] and set it to waekup point
     * No ? -> We set the wakeuppont to now
     *
     * CASE 3: Wakeuppoint in near future ( 2 times the [frequency] of the data)
     * So now we are calculating the [SleepState] of the actual data for the next step.
     * If its a [SleepState.LIGHT] we set the wakeuppoint to it.. otherwise we pass back to old wakeup point

     */
    fun findLightUserWakeup(sleepApiRawDataEntity:List<SleepApiRawDataEntity>, wakeUpTime:Int) : Int{


        // get normed list
        val (normedSleepApiData, frequency) = createTimeNormedData(2, false , sleepApiRawDataEntity.maxOf { x->x.timestampSeconds } , sleepApiRawDataEntity)

        // get actual time
        val actualTimeSeconds =  getSecondsOfDay()
        val frequencySeconds = SleepDataFrequency.getValue(frequency) * 60


        // If we are in allowed timeSpace
        if (((actualTimeSeconds < wakeUpTime) && actualTimeSeconds > wakeUpTime - (frequencySeconds * 2)) ||
            ((actualTimeSeconds > wakeUpTime) && actualTimeSeconds < wakeUpTime + (frequencySeconds * 2)))
        {
            // check user light sleep future
            // create features for ml model
            val sleepClassifier = SleepClassifier.getHandler(context)
            val features = sleepClassifier.createFeatures(normedSleepApiData, ModelProcess.LIGHTAWAKE, frequency)

            // call the ml model
            val result = sleepClassifier.defineFutureUserSleep(features, frequency)

            return if(result == SleepState.LIGHT)  (actualTimeSeconds + frequencySeconds) else wakeUpTime
        }
        else{
            return wakeUpTime
        }
    }

    // endregion

    /**
     * Checks if the user is Sleeping or not at the moment.
     * Saves the state in the [SleepApiRawDataEntity] and in the [LiveUserSleepActivityStatus]
     * [time] the actual time in seconds
     */
    fun checkIsUserSleeping(localTime: LocalDateTime? = null){
        scope.launch {
            // get the actual sleepApiDataList
            val time = localTime ?: LocalDateTime.now()
            val sleepApiRawDataEntity =
                dataBaseRepository.getSleepApiRawDataFromDateLive(time).first()
                    .sortedByDescending { x -> x.timestampSeconds }

            if (sleepApiRawDataEntity == null || sleepApiRawDataEntity.count() == 0) {
                // do something!

                return@launch
            }

            // check for each sleepstate
            sleepApiRawDataEntity.forEach { data ->

                if(data.sleepState == SleepState.NONE){

                    // get normed list
                    val (normedSleepApiData, frequency) = createTimeNormedData(
                        2,
                        false,
                        data.timestampSeconds,
                        sleepApiRawDataEntity
                    )

                    // create features for ml model
                    val sleepClassifier = SleepClassifier.getHandler(context)
                    val features =
                        sleepClassifier.createFeatures(normedSleepApiData, ModelProcess.SLEEP04, frequency)

                    // call the ml model
                    val result = sleepClassifier.isUserSleeping(features, frequency)

                    // save the result to the sleep api data
                    dataBaseRepository.updateSleepApiRawDataSleepState(data.timestampSeconds, result)
                }
            }

            // update live user sleep activity
            dataStoreRepository.updateIsUserSleeping(sleepApiRawDataEntity.first().sleepState == SleepState.SLEEPING)
            dataStoreRepository.updateUserSleepTime(
                SleepApiRawDataEntity.getSleepTime(
                    sleepApiRawDataEntity
                )
            )
        }
    }

    /**
     * Sets the user sleep states for every sleeping state
     * Saves the state in the [SleepApiRawDataEntity] and in the [LiveUserSleepActivityStatus] and in the [UserSleepSessionEntity] with id
     * Stores alarm data in the main
     * [time] = the actual time in seconds
     */
    fun defineUserWakeup(localTime: LocalDateTime? = null, setAlarm:Boolean = true) {
        scope.launch {
            // for each sleeping time, we have to define the sleep state
            val time = localTime ?: LocalDateTime.now()
            val sleepApiRawDataEntity =
                dataBaseRepository.getSleepApiRawDataFromDateLive(time).first()
                    .sortedBy { x -> x.timestampSeconds }

            if (sleepApiRawDataEntity == null || sleepApiRawDataEntity.count() == 0) {
                // do something!

                return@launch
            }

            // calculate all sleep states when the user is sleeping
            val id =
                UserSleepSessionEntity.getIdByTimeStamp(sleepApiRawDataEntity.minOf { x -> x.timestampSeconds })
            val sleepSessionEntity = dataBaseRepository.getOrCreateSleepSessionById(id)

            // only when unidentified
            if(sleepSessionEntity.mobilePosition == MobilePosition.UNIDENTIFIED){
                sleepSessionEntity.mobilePosition =
                    if(MobilePosition.getCount(dataStoreRepository.sleepParameterFlow.first().standardMobilePosition) == MobilePosition.UNIDENTIFIED)
                        checkPhonePosition(sleepApiRawDataEntity)
                    else
                        MobilePosition.getCount(dataStoreRepository.sleepParameterFlow.first().standardMobilePosition)
            }


            // if in bed then check the single states of the sleep
            if (sleepSessionEntity.mobilePosition == MobilePosition.INBED) {
                sleepApiRawDataEntity.forEach()
                {
                    // we take all sleep values that are not already defined as light or deep but sleeping
                    if (it.sleepState == SleepState.SLEEPING) {
                        // we need to calculate the sleep state
                        // and then we update it in the sleep api raw data entity
                        val result = defineSleepStates(it.timestampSeconds, sleepApiRawDataEntity)
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
            sleepSessionEntity.sleepTimes.sleepDuration =
                (sleepSessionEntity.sleepTimes.lightSleepDuration * (if (sleepSessionEntity.mobilePosition != MobilePosition.INBED) 1f else 1f) +
                        sleepSessionEntity.sleepTimes.deepSleepDuration * 1f).toInt()


            // now define the new wakeUpPoint for the user...
            // sleep time

            if(setAlarm) {
                // get user activity
                val activity = getUserActivityOnDay(time)
                sleepSessionEntity.userSleepRating.activityOnDay = activity

                // store in the alarm...!!!
                val alarm = dataBaseRepository.getNextActiveAlarm() ?: return@launch

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

                //var wakeUpTimeNew = 0
                // if in bed then check the single states of the sleep
                // TODO check how good light user wakeup is working... for testing it will be deactivated first
                if (sleepSessionEntity.mobilePosition == MobilePosition.INBED) {
                    //wakeUpTime = findLightUserWakeup(sleepApiRawDataEntity, wakeUpTime)
                }

                // store in the alarm...!!!
                dataBaseRepository.updateWakeupTime(wakeupTime = wakeUpTime, alarm.id)


                val last = sleepApiRawDataEntity.last().timestampSeconds
                dataBaseRepository.updateSleepApiRawDataWakeUp(
                        last,
                        wakeUpTime
                )

                sleepApiRawDataEntity.last().wakeUpTime = wakeUpTime

                sleepSessionEntity.sleepTimes.sleepTimeEnd = wakeUpTime

            }
            else {
                sleepSessionEntity.sleepTimes.sleepTimeEnd =
                        SleepApiRawDataEntity.getSleepEndTime(sleepApiRawDataEntity)

            }

            //dataBaseRepository.insertSleepApiRawData(sleepApiRawDataEntity)
            dataStoreRepository.updateUserSleepTime(sleepSessionEntity.sleepTimes.sleepDuration)
            dataBaseRepository.insertUserSleepSession(sleepSessionEntity)
        }
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
                .sortedBy { x -> x.timestampSeconds }

        if (sleepApiRawDataEntity.count() == 0) {
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
                .sortedBy { x -> x.timestampSeconds }

        if (sleepApiRawDataEntity.count() == 0) {
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
}
package com.doitstudio.sleepest_master.sleepcalculation

import android.content.Context
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.ModelProcess
import com.doitstudio.sleepest_master.model.data.SleepDataFrequency
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.sleepcalculation.datastore.LiveUserSleepActivityStatus
import com.doitstudio.sleepest_master.sleepcalculation.db.UserSleepSessionEntity
import com.doitstudio.sleepest_master.sleepcalculation.ml.SleepClassifier
import com.doitstudio.sleepest_master.storage.DbRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.ZoneOffset

class SleepCalculationHandler(val context: Context) {

    private val sleepDbRepository: SleepCalculationDbRepository by lazy {
        (context.applicationContext as MainApplication).sleepCalculationDbRepository
    }

    private val normalDbRepository: DbRepository by lazy {
        (context.applicationContext as MainApplication).dbRepository
    }

    private val sleepCalculationRepository: SleepCalculationStoreRepository by lazy {
        (context.applicationContext as MainApplication).sleepCalculationRepository
    }

    // region private helpers

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
     * Should not happen but if, we check if the [frequency] * 3 includes the [wakeUpTime]
     * Yes ? -> We calculate the model and check if next time is a [SleepState.LIGHT] and set it to waekup point
     * No ? -> We set the wakeuppont to now
     *
     * CASE 3: Wakeuppoint in near future ( 3 times the [frequency] of the data)
     * So now we are calculating the [SleepState] of the actual data for the next step.
     * If its a [SleepState.LIGHT] we set the wakeuppoint to it.. otherwise we pass back to old wakeup point

     */
     fun findLightUserWakeup(sleepApiRawDataEntity:List<SleepApiRawDataEntity>, wakeUpTime:Int) : Int{


        // get normed list
        val (normedSleepApiData, frequency) = createTimeNormedData(2, false , sleepApiRawDataEntity.maxOf { x->x.timestampSeconds } , sleepApiRawDataEntity)

        // get actual time
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val actualTimeSeconds = now.toEpochSecond(ZoneOffset.UTC)
        val frequencySeconds = SleepDataFrequency.getValue(frequency) * 60


        // If we are in allowed timeSpace
        if (((actualTimeSeconds < wakeUpTime) && actualTimeSeconds > wakeUpTime - (frequencySeconds * 3)) ||
                ((actualTimeSeconds > wakeUpTime) && actualTimeSeconds < wakeUpTime + (frequencySeconds * 3)))
                {
                    // check user light sleep future
                    // create features for ml model
                    val sleepClassifier = SleepClassifier.getHandler(context)
                    val features = sleepClassifier.createFeatures(normedSleepApiData, ModelProcess.LIGHTAWAKE, frequency)

                    // call the ml model
                    val result = sleepClassifier.defineFutureUserSleep(features, frequency)

                    return if(result == SleepState.LIGHT)  (actualTimeSeconds.toInt() + frequencySeconds) else wakeUpTime
        }
        else{
            return wakeUpTime
        }
    }

    // endregion

    /**
     * Checks if the user is Sleeping or not at the moment.
     * Saves the state in the [SleepApiRawDataEntity] and in the [LiveUserSleepActivityStatus]
     */
    suspend fun checkIsUserSleeping(){

        // get the actual sleepApiDataList
        val sleepApiRawDataEntity = sleepDbRepository.getSleepApiRawDataSince(86400).first().sortedByDescending { x -> x.timestampSeconds }

        if(sleepApiRawDataEntity == null || sleepApiRawDataEntity.count() == 0){
            // do something!

            return
        }

        val id = sleepApiRawDataEntity.maxByOrNull{ x -> x.timestampSeconds }!!.timestampSeconds

        // get normed list
        val (normedSleepApiData, frequency) = createTimeNormedData(2, false, id, sleepApiRawDataEntity)

        // create features for ml model
        val sleepClassifier = SleepClassifier.getHandler(context)
        val features = sleepClassifier.createFeatures(normedSleepApiData, ModelProcess.SLEEP04, frequency)

        // call the ml model
        val result = sleepClassifier.isUserSleeping(features, frequency)

        // save the result to the sleep api data
        sleepDbRepository.updateSleepApiRawDataSleepState(id, result)

        // update live user sleep activity
        sleepCalculationRepository.updateIsUserSleeping(result == SleepState.SLEEPING)
        sleepCalculationRepository.updateUserSleepTime(SleepApiRawDataEntity.getSleepTime(sleepApiRawDataEntity))

    }

    suspend fun defineUserWakeup(){

        // for each sleeping time, we have to define the sleep state
        val sleepApiRawDataEntity = sleepDbRepository.getSleepApiRawDataSince(43200).first().sortedBy { x -> x.timestampSeconds }

        // calculate all sleep states when the user is sleeping
        val id = sleepApiRawDataEntity.minOf { x->x.timestampSeconds }
        val sleepSessionEntity = normalDbRepository.getOrCreateSleepSessionById(id)

        sleepSessionEntity.mobilePosition = checkPhonePosition(sleepApiRawDataEntity)

        // if in bed then check the single states of the sleep
        if(sleepSessionEntity.mobilePosition == MobilePosition.INBED){
            sleepApiRawDataEntity.forEach()
            {
                // we take all sleep values that are not already defined as light or deep
                if(it.sleepState == SleepState.SLEEPING){
                    // we need to calculate the sleep state
                    // and then we update it in the sleep api raw data entity
                    val result = defineSleepStates(it.timestampSeconds,sleepApiRawDataEntity)
                    sleepDbRepository.updateSleepApiRawDataSleepState(it.timestampSeconds, result)
                    it.sleepState = result
                }
            }
        }

        // now we need to define "how long" the user slept already
        // define the sleep times and states


        sleepSessionEntity.sleepTimes.sleepTimeStart = SleepApiRawDataEntity.getSleepStartTime(sleepApiRawDataEntity)
        sleepSessionEntity.sleepTimes.lightSleepDuration = SleepApiRawDataEntity.getSleepTimeByState(sleepApiRawDataEntity, SleepState.LIGHT)
        sleepSessionEntity.sleepTimes.lightSleepDuration += SleepApiRawDataEntity.getSleepTimeByState(sleepApiRawDataEntity, SleepState.SLEEPING)
        sleepSessionEntity.sleepTimes.deepSleepDuration += SleepApiRawDataEntity.getSleepTimeByState(sleepApiRawDataEntity, SleepState.DEEP)
        sleepSessionEntity.sleepTimes.remSleepDuration += SleepApiRawDataEntity.getSleepTimeByState(sleepApiRawDataEntity, SleepState.REM)
        sleepSessionEntity.sleepTimes.awakeTime += SleepApiRawDataEntity.getAwakeTime(sleepApiRawDataEntity)


        // how long have the user slept already? REM is not counted at the moment!!!
        // use some custom factors...except its not on table, then use just 1
        sleepSessionEntity.sleepTimes.sleepDuration =
                (sleepSessionEntity.sleepTimes.lightSleepDuration * if (sleepSessionEntity.mobilePosition != MobilePosition.INBED ) 1f else 0.9f +
                        sleepSessionEntity.sleepTimes.deepSleepDuration * 1.1f).toInt()

        // now define the new wakeUpPoint for the user...
        // sleep time

        val sleepTargetTime = 420 // as minutes
        var restSleepTime = 420 - sleepSessionEntity.sleepTimes.sleepDuration

        if (restSleepTime < 10)
        {
            restSleepTime = 10
        }


        // user wakuptime is
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val actualTimeSeconds = now.toEpochSecond(ZoneOffset.UTC)

        var wakeUpTime = actualTimeSeconds.toInt() + (restSleepTime * 60)

        // if in bed then check the single states of the sleep
        if(sleepSessionEntity.mobilePosition == MobilePosition.INBED){
            wakeUpTime = findLightUserWakeup(sleepApiRawDataEntity, wakeUpTime.toInt())
        }


        // store in the alarm...!!!
        sleepCalculationRepository.updateUserSleepTime(sleepSessionEntity.sleepTimes.sleepDuration)
    }

    fun checkIsUserSleepingTest(sleepApiRawDataEntity1:List<SleepApiRawDataEntity>): SleepState {

        // get the actual sleepApiDataList
        val sleepApiRawDataEntity = sleepApiRawDataEntity1.sortedByDescending { x -> x.timestampSeconds }

        if(sleepApiRawDataEntity == null || sleepApiRawDataEntity.count() == 0){
            // do something!
            return SleepState.NONE
        }

        // get normed list
        val (normedSleepApiData, frequency) = createTimeNormedData(2, false, sleepApiRawDataEntity.first().timestampSeconds, sleepApiRawDataEntity)

        // create features for ml model
        val sleepClassifier = SleepClassifier.getHandler(context)
        val features = sleepClassifier.createFeatures(normedSleepApiData, ModelProcess.SLEEP04, frequency)

        // call the ml model
        return  sleepClassifier.isUserSleeping(features, frequency)

        // save result and continue

    }

    fun defineUserWakeupTest(sleepApiRawDataEntity:List<SleepApiRawDataEntity>) : Int {

        // calculate all sleep states when the user is sleeping
        val id = sleepApiRawDataEntity.minOf { x->x.timestampSeconds }
        val sleepSessionEntity = UserSleepSessionEntity(id)

        sleepSessionEntity.mobilePosition = checkPhonePosition(sleepApiRawDataEntity)

        // if in bed then check the single states of the sleep
        if(sleepSessionEntity.mobilePosition == MobilePosition.INBED){
            sleepApiRawDataEntity.forEach()
            {
                // we take all sleep values that are not already defined as light or deep
                if(it.sleepState == SleepState.SLEEPING){
                    // we need to calculate the sleep state
                    // and then we update it in the sleep api raw data entity
                    val result = defineSleepStates(it.timestampSeconds,sleepApiRawDataEntity)
                    it.sleepState = result
                }
            }
        }


        // now we need to define "how long" the user slept already
        // define the sleep times and states


        sleepSessionEntity.sleepTimes.sleepTimeStart = SleepApiRawDataEntity.getSleepStartTime(sleepApiRawDataEntity)
        sleepSessionEntity.sleepTimes.lightSleepDuration = SleepApiRawDataEntity.getSleepTimeByState(sleepApiRawDataEntity, SleepState.LIGHT)
        sleepSessionEntity.sleepTimes.lightSleepDuration += SleepApiRawDataEntity.getSleepTimeByState(sleepApiRawDataEntity, SleepState.SLEEPING)
        sleepSessionEntity.sleepTimes.deepSleepDuration += SleepApiRawDataEntity.getSleepTimeByState(sleepApiRawDataEntity, SleepState.DEEP)
        sleepSessionEntity.sleepTimes.remSleepDuration += SleepApiRawDataEntity.getSleepTimeByState(sleepApiRawDataEntity, SleepState.REM)
        sleepSessionEntity.sleepTimes.awakeTime += SleepApiRawDataEntity.getAwakeTime(sleepApiRawDataEntity)


        // how long have the user slept already? REM is not counted at the moment!!!
        sleepSessionEntity.sleepTimes.sleepDuration =
                (sleepSessionEntity.sleepTimes.lightSleepDuration * if (sleepSessionEntity.mobilePosition != MobilePosition.INBED ) 1f else 0.9f +
                        sleepSessionEntity.sleepTimes.deepSleepDuration * 1.1f).toInt()
        // now define the new wakeUpPoint for the user...
        // sleep time

        val sleepTargetTime = 420 // as minutes
        var restSleepTime = 420 - sleepSessionEntity.sleepTimes.sleepDuration

        if (restSleepTime < 10)
        {
            restSleepTime = 10
        }

        // user wakuptime is
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val actualTimeSeconds = now.toEpochSecond(ZoneOffset.UTC)

        var wakeUpTime = actualTimeSeconds.toInt() + (restSleepTime * 60)

        // if in bed then check the single states of the sleep
        if(sleepSessionEntity.mobilePosition == MobilePosition.INBED){
            wakeUpTime = findLightUserWakeup(sleepApiRawDataEntity, wakeUpTime.toInt())
        }

        // store in the alarm...!!!
        return sleepSessionEntity.sleepTimes.sleepDuration
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
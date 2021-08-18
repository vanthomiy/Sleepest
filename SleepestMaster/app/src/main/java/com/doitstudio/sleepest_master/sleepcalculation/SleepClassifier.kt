package com.doitstudio.sleepest_master.sleepcalculation

import android.content.Context
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.model.data.*
import com.doitstudio.sleepest_master.sleepcalculation.model.ThresholdParams
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import kotlinx.coroutines.flow.first

/**
 * This class is used to use the functionality of the imported ml models.
 * In the app are used different types of ml models for specific use-cases.
 * With this we can
 *  - load input assignments [loadInputAssignmentFile]
 *  - create features of an actual sleep api raw data list [createFeatures]
 *  - create features for indicating table/bed of an actual sleep api raw data list [createTableFeatures]
 *  - defining if user is sleeping [isUserSleeping]
 *  - defining if phone is in bed [defineTableBed]
 *  - defining the actual state of sleeping [defineUserSleep]
 */
class SleepClassifier constructor(private val context: Context) {

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

    /**
     * Checks whether the phone is on bed or on table. Error returns [MobilePosition.UNIDENTIFIED]
     * First we are checking if enough data is available to define the [MobilePosition]...
     * Else we are using the most used [MobilePosition] over the last 7 Days
     *

     */
    suspend fun defineTableBed(sleepApiRawDataEntity:List<SleepApiRawDataEntity>) : MobilePosition {

        // get the actual sleepApiDataList
        val sleepingData = sleepApiRawDataEntity.filter{x->x.sleepState != SleepState.NONE && x.sleepState != SleepState.AWAKE }

        if (sleepingData.count() <= 5){
            return MobilePosition.getCount(dataStoreRepository.sleepParameterFlow.first().standardMobilePositionOverLastWeek)
        }

        // under 1.4 is on table and over is in bed
        return if(sleepingData.sumBy { x -> x.motion } / sleepingData.count() > 1.4f){
            MobilePosition.INBED
        } else {
            MobilePosition.ONTABLE
        }
    }

    /**
     * First we are checking if enough data is available to define the [LightConditions]...
     * Then we just check whether the light was over 1 or not.
     * Light conditions don't vary that much.
     * Especially at night we got almost every time value = 1 for light.
     * We are not able to detect the light conditions in the current sleep that well
     * Therefore we are using the avg over the last week if possible
     */
    suspend fun defineLightConditions(sleepApiRawDataEntity:List<SleepApiRawDataEntity>, defineUserWakeup:Boolean) : LightConditions {

        // get the actual sleepApiDataList
        val sleepingData = sleepApiRawDataEntity.filter{x->x.sleepState != SleepState.NONE && x.sleepState != SleepState.AWAKE }

        if (sleepingData.count() == 0){
            return LightConditions.getCount(dataStoreRepository.sleepParameterFlow.first().standardLightConditionOverLastWeek)
        }

        if(sleepingData.filter { x -> x.light > 1 }.count() > 1){
            return LightConditions.LIGHT
        }

        return if (!defineUserWakeup){
            // else we are using the last avg. over the last times
            LightConditions.getCount(dataStoreRepository.sleepParameterFlow.first().standardLightConditionOverLastWeek)
        } else {
            LightConditions.DARK
        }

    }

    fun isUserSleeping(
        normedSleepApiDataBefore: List<SleepApiRawDataEntity>,
        normedSleepApiDataAfter: List<SleepApiRawDataEntity>?,
        mobilePosition: MobilePosition,
        lightConditions: LightConditions,
        mobileUseFrequency: MobileUseFrequency
    ) : SleepState {

        // check for standard mobile position.
        val actualParams =
            ParamsHandler.createDefaultParams(mobilePosition, lightConditions, mobileUseFrequency)

        // Now we are using the algorithm
        val sortedSleepListBefore = normedSleepApiDataBefore.sortedBy { x->x.timestampSeconds }
        var sortedSleepListAfter = normedSleepApiDataAfter?.sortedByDescending { x->x.timestampSeconds }

        // now take the params and check the sleep
        // first check if a sleep start detection is needed

        val countSleeping = sortedSleepListBefore.filter{x->x.sleepState != SleepState.AWAKE && x.sleepState != SleepState.NONE}.count()
        val prePrediction = sortedSleepListAfter.isNullOrEmpty()

        var isSleepStarted = false
        var isSleepingAllowed = false
        var isSleeping = false

        if(!prePrediction && sortedSleepListAfter!!.count() != sortedSleepListBefore.count()){
            sortedSleepListAfter = switchToFrequency(sortedSleepListAfter, sortedSleepListAfter.count(), sortedSleepListBefore.count())
        }


        // Sleep start detection
        if(countSleeping <= 3){// || (startBorderListBefore[1].sleepState != SleepState.SLEEPING || startBorderListBefore[2].sleepState != SleepState.SLEEPING)){
            // check if user is started sleeping

            val nextTimesBorder = 3

            val newListBefore = sortedSleepListBefore.dropLast(1)

            val actualThreshold = ThresholdParams()

            val nextThreeTimes = ThresholdParams()

            if(prePrediction) {
                actualThreshold.confidence = sortedSleepListBefore.last().confidence.toFloat()
                actualThreshold.light = sortedSleepListBefore.last().light.toFloat()
                actualThreshold.motion = sortedSleepListBefore.last().motion.toFloat()
            }
            else{
                actualThreshold.confidence = (sortedSleepListAfter!!.sumOf { x-> x.confidence } / sortedSleepListAfter.count()).toFloat()
                actualThreshold.light = (sortedSleepListAfter!!.sumOf { x-> x.light } / sortedSleepListAfter.count()).toFloat()
                actualThreshold.motion = (sortedSleepListAfter!!.sumOf { x-> x.motion } / sortedSleepListAfter.count()).toFloat()

                nextThreeTimes.confidence = (sortedSleepListAfter!!.take(nextTimesBorder).sumOf { x-> x.confidence } / sortedSleepListAfter.take(nextTimesBorder).count()).toFloat()
                nextThreeTimes.light = (sortedSleepListAfter!!.take(nextTimesBorder).sumOf { x-> x.light } / sortedSleepListAfter.take(nextTimesBorder).count()).toFloat()
                nextThreeTimes.motion = (sortedSleepListAfter!!.take(nextTimesBorder).sumOf { x-> x.motion } / sortedSleepListAfter.take(nextTimesBorder).count()).toFloat()
            }

            val avgStartThreshold = ThresholdParams()

            avgStartThreshold.confidence = (newListBefore.sumOf { x-> x.confidence } / newListBefore.count()).toFloat()
            avgStartThreshold.light = (newListBefore.sumOf { x-> x.light } / newListBefore.count()).toFloat()
            avgStartThreshold.motion = (newListBefore.sumOf { x-> x.motion } / newListBefore.count()).toFloat()

            actualThreshold.absBetweenThresholds(avgStartThreshold)

            isSleepStarted = (actualParams.sleepStartBorder.checkIfDifferenceThreshold(
                true,
                3,
                actualThreshold) &&
                    (!prePrediction || actualParams.sleepStartThreshold.checkIfThreshold(
                        true,
                        3,
                        nextThreeTimes)
                            ))

        }

        // Is Sleeping detection cleanup
        if(isSleepStarted || countSleeping > 3){

            val avgThreshold = ThresholdParams()

            avgThreshold.confidence = 0f
            avgThreshold.light = 0f
            avgThreshold.motion = 0f

            var factor = 0
            val count = sortedSleepListBefore.count()
            for (i in 0 until count) {

                factor += (i * i)

                if(prePrediction){
                    avgThreshold.confidence += ((sortedSleepListBefore[i].confidence)).toFloat() * i * i
                    avgThreshold.light += ((sortedSleepListBefore[i].light)).toFloat() * i * i
                    avgThreshold.motion += ((sortedSleepListBefore[i].motion)).toFloat() * i * i
                }
                else{
                    avgThreshold.confidence += ((sortedSleepListBefore[i].confidence + sortedSleepListAfter!![i].confidence) / 2).toFloat() * i * i
                    avgThreshold.light += ((sortedSleepListBefore[i].light + sortedSleepListAfter!![i].light) / 2).toFloat() * i * i
                    avgThreshold.motion += ((sortedSleepListBefore[i].motion + sortedSleepListAfter!![i].motion) / 2).toFloat() * i * i
                }
            }

            avgThreshold.confidence = avgThreshold.confidence / factor
            avgThreshold.light = avgThreshold.light / factor
            avgThreshold.motion = avgThreshold.motion / factor

            isSleepingAllowed = avgThreshold.checkIfThreshold(false, 2, actualParams.sleepCleanUp)
        }


        // is user sleeping?
        if(isSleepingAllowed){

            val actualThreshold = ThresholdParams()

            actualThreshold.confidence = sortedSleepListBefore.last().confidence.toFloat()
            actualThreshold.light = sortedSleepListBefore.last().light.toFloat()
            actualThreshold.motion = sortedSleepListBefore.last().motion.toFloat()

            isSleeping = actualThreshold.checkIfThreshold(false, 3, actualParams.generalThreshold)
        }



        return if(isSleeping) SleepState.SLEEPING else SleepState.AWAKE
    }

    fun isUserSleepingTest(
        normedSleepApiDataBefore: List<SleepApiRawDataEntity>,
        normedSleepApiDataAfter: List<SleepApiRawDataEntity>?,
        mobilePosition: MobilePosition,
        lightConditions: LightConditions,
        mobileUseFrequency: MobileUseFrequency
    ) : SleepState {

        // check for standard mobile position.
        val actualParams =
            ParamsHandler.createDefaultParams(mobilePosition, lightConditions, mobileUseFrequency)

        // Now we are using the algorithm
        val sortedSleepListBefore = normedSleepApiDataBefore.sortedBy { x->x.timestampSeconds }
        val sortedSleepListAfter = normedSleepApiDataAfter?.sortedByDescending { x->x.timestampSeconds }

        // now take the params and check the sleep
        // first check if a sleep start detection is needed

        val countSleeping = sortedSleepListBefore.filter{x->x.sleepState != SleepState.AWAKE && x.sleepState != SleepState.NONE}.count()
        val prePrediction = sortedSleepListAfter.isNullOrEmpty()

        var isSleepStarted = false
        var isSleepingAllowed = false
        var isSleeping = false

        // Sleep start detection
        if(countSleeping <= 3){// || (startBorderListBefore[1].sleepState != SleepState.SLEEPING || startBorderListBefore[2].sleepState != SleepState.SLEEPING)){
            // check if user is started sleeping

            val newListBefore = sortedSleepListBefore.dropLast(1)

            val actualThreshold = ThresholdParams()

            if(prePrediction) {
                actualThreshold.confidence = sortedSleepListBefore.last().confidence.toFloat()
                actualThreshold.light = sortedSleepListBefore.last().light.toFloat()
                actualThreshold.motion = sortedSleepListBefore.last().motion.toFloat()
            }
            else{
                actualThreshold.confidence = (sortedSleepListAfter!!.sumOf { x-> x.confidence } / sortedSleepListAfter.count()).toFloat()
                actualThreshold.light = (sortedSleepListAfter!!.sumOf { x-> x.light } / sortedSleepListAfter.count()).toFloat()
                actualThreshold.motion = (sortedSleepListAfter!!.sumOf { x-> x.motion } / sortedSleepListAfter.count()).toFloat()
            }

            val avgStartThreshold = ThresholdParams()

            avgStartThreshold.confidence = (newListBefore.sumOf { x-> x.confidence } / newListBefore.count()).toFloat()
            avgStartThreshold.light = (newListBefore.sumOf { x-> x.light } / newListBefore.count()).toFloat()
            avgStartThreshold.motion = (newListBefore.sumOf { x-> x.motion } / newListBefore.count()).toFloat()

            actualThreshold.absBetweenThresholds(avgStartThreshold)

            isSleepStarted = actualParams.sleepStartBorder.checkIfDifferenceThreshold(true, 3, actualThreshold)
        }

        // Is Sleeping detection cleanup
        if(isSleepStarted || countSleeping > 3){

            val avgThreshold = ThresholdParams()

            avgThreshold.confidence = 0f
            avgThreshold.light = 0f
            avgThreshold.motion = 0f

            var factor = 0
            val count = sortedSleepListBefore.count()-1
            for (i in 0..count) {

                factor += (i * i)

                if(prePrediction){
                    avgThreshold.confidence += ((sortedSleepListBefore[i].confidence)).toFloat() * i * i
                    avgThreshold.light += ((sortedSleepListBefore[i].light)).toFloat() * i * i
                    avgThreshold.motion += ((sortedSleepListBefore[i].motion)).toFloat() * i * i
                }
                else{
                    avgThreshold.confidence += ((sortedSleepListBefore[i].confidence + sortedSleepListAfter!![i].confidence) / 2).toFloat() * i * i
                    avgThreshold.light += ((sortedSleepListBefore[i].light + sortedSleepListAfter!![i].light) / 2).toFloat() * i * i
                    avgThreshold.motion += ((sortedSleepListBefore[i].motion + sortedSleepListAfter!![i].motion) / 2).toFloat() * i * i
                }
            }

            avgThreshold.confidence = avgThreshold.confidence / factor
            avgThreshold.light = avgThreshold.light / factor
            avgThreshold.motion = avgThreshold.motion / factor

            isSleepingAllowed = avgThreshold.checkIfThreshold(false, 2, actualParams.sleepCleanUp)
        }


        // is user sleeping?
        if(isSleepingAllowed){

            val actualThreshold = ThresholdParams()

            actualThreshold.confidence = sortedSleepListBefore.last().confidence.toFloat()
            actualThreshold.light = sortedSleepListBefore.last().light.toFloat()
            actualThreshold.motion = sortedSleepListBefore.last().motion.toFloat()

            isSleeping = actualThreshold.checkIfThreshold(false, 3, actualParams.generalThreshold)
        }

        return if(isSleeping) SleepState.SLEEPING else SleepState.AWAKE
    }


    fun switchToFrequency(list: List<SleepApiRawDataEntity>, fromCount: Int, toCount: Int): List<SleepApiRawDataEntity>
    {
        var timeNormedData = mutableListOf<SleepApiRawDataEntity>()

        // 30 / 10 = 3 or 5 / 10 = 0.5
        val frequenceFactor = toCount.toFloat() / fromCount.toFloat()


        var sleepDataBuffer = mutableListOf<SleepApiRawDataEntity>()

        for (i in 0 until list.count()){
            if (frequenceFactor > 1){
                for (j in 0 until frequenceFactor.toInt())
                    timeNormedData.add(list[i])
            }
            else{
                sleepDataBuffer.add(list[i])

                if(sleepDataBuffer.count() >= (1/frequenceFactor).toInt()){
                    timeNormedData.add(SleepApiRawDataEntity(
                        timestampSeconds = sleepDataBuffer[0].timestampSeconds,
                        confidence = sleepDataBuffer.sumBy {x -> x.confidence} / sleepDataBuffer.count(),
                        motion = sleepDataBuffer.sumBy {x -> x.motion} / sleepDataBuffer.count(),
                        light = sleepDataBuffer.sumBy {x -> x.light} / sleepDataBuffer.count(),
                        sleepState = sleepDataBuffer[0].sleepState,
                        oldSleepState = sleepDataBuffer[0].oldSleepState,
                        wakeUpTime = sleepDataBuffer[0].wakeUpTime
                    ))

                    sleepDataBuffer.clear()
                }
            }
        }

        return timeNormedData
    }



    fun defineUserSleep(
        normedSleepApiDataBefore: List<SleepApiRawDataEntity>,
        normedSleepApiDataAfter: List<SleepApiRawDataEntity>,
        lightConditions: LightConditions
    ) : SleepState {

        // check for standard mobile position.

        val sortedSleepListBefore = normedSleepApiDataBefore.sortedBy { x->x.timestampSeconds }
        var sortedSleepListAfter = normedSleepApiDataAfter.sortedByDescending { x->x.timestampSeconds }

        val actualParams = ParamsHandler.createSleepStateParams(lightConditions)

        // Now we are using the algorithm
        val avgThreshold = ThresholdParams()

        avgThreshold.confidence = 0f
        avgThreshold.light = 0f
        avgThreshold.motion = 0f

        var factor = 0
        val count = sortedSleepListBefore.count()

        if(count != sortedSleepListAfter.count()){
            sortedSleepListAfter = switchToFrequency(sortedSleepListAfter, sortedSleepListAfter.count(), count)
        }

        for (i in 0 until count){

            factor += i

            avgThreshold.confidence += ((sortedSleepListBefore[i].confidence + sortedSleepListAfter[i].confidence) / 2).toFloat() * i
            avgThreshold.light += ((sortedSleepListBefore[i].light + sortedSleepListAfter[i].light) / 2).toFloat() * i
            avgThreshold.motion += ((sortedSleepListBefore[i].motion + sortedSleepListAfter[i].motion) / 2).toFloat() * i
        }

        avgThreshold.confidence = avgThreshold.confidence / factor
        avgThreshold.light = avgThreshold.light / factor
        avgThreshold.motion = avgThreshold.motion / factor

        return when {
            actualParams.lightSleepParams.checkIfThreshold(false, 1, avgThreshold) -> SleepState.LIGHT
            actualParams.remSleepParams.checkIfThreshold(true, 2, avgThreshold) -> SleepState.REM
            actualParams.deepSleepParams.checkIfThreshold(true, 2, avgThreshold) -> SleepState.DEEP
            else -> SleepState.LIGHT
        }
    }


    /**
     * Companion object is used for static fields in kotlin
     */
    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: SleepClassifier? = null

        /**
         * This should be used to create or get the actual instance of the [SleepClassifier] class
         */
        fun getHandler(context: Context): SleepClassifier {
            return INSTANCE ?: synchronized(this) {
                val instance = SleepClassifier(context)
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}






package com.sleepestapp.sleepest.sleepcalculation

import android.content.Context
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.model.data.*
import com.sleepestapp.sleepest.sleepcalculation.model.ThresholdParams
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.db.SleepApiRawDataEntity
import kotlinx.coroutines.flow.first

/**
 * This class contains the actual algorithms and functions that classifies the user sleep
 * Gets called from the [SleepCalculationHandler]
 */
class SleepClassifier constructor(private val context: Context) {

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
        val motionAverage = sleepingData.sumOf { x -> x.motion }.toFloat() / sleepingData.count()
        val confidenceAverage = sleepingData.sumOf { x -> x.confidence }.toFloat() / sleepingData.count()

        return if(motionAverage > 1.05f && confidenceAverage < 91f || motionAverage > 1.2f){
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


    /**
     * When future and past sleep data [SleepDataFrequency] don't match. We can match the frequency of one of the data to another by calling this function
     */
    fun switchToFrequency(list: List<SleepApiRawDataEntity>, fromCount: Int, toCount: Int): List<SleepApiRawDataEntity>
    {
        val timeNormedData = mutableListOf<SleepApiRawDataEntity>()

        // 30 / 10 = 3 or 5 / 10 = 0.5
        val frequencyFactor = toCount.toFloat() / fromCount.toFloat()


        val sleepDataBuffer = mutableListOf<SleepApiRawDataEntity>()

        val listReversed = list.reversed();

        for (i in 0 until list.count()){
            if (frequencyFactor > 1){
                for (j in 0 until frequencyFactor.toInt())
                    timeNormedData.add(listReversed[i])
            }
            else{
                sleepDataBuffer.add(listReversed[i])

                if(sleepDataBuffer.count() >= (1/frequencyFactor).toInt()){
                    timeNormedData.add(SleepApiRawDataEntity(
                        timestampSeconds = sleepDataBuffer[0].timestampSeconds,
                        confidence = sleepDataBuffer.sumOf {x -> x.confidence} / sleepDataBuffer.count(),
                        motion = sleepDataBuffer.sumOf {x -> x.motion} / sleepDataBuffer.count(),
                        light = sleepDataBuffer.sumOf {x -> x.light} / sleepDataBuffer.count(),
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


    /**
     * Checks if the user is Sleeping or not at the moment.
     * It creates the actual parameters
     * and then checks whether the user is sleeping or not
     * It handles pre-prediction and after-prediction
     *  1. It checks if the user already slept. Else it checks for the sleep start borders
     *  2. When user already slept or sleep border is given. We check the sleep clean up borders
     *  3. When sleep clean up is okay. We are checking if the actual params are sleeping or not
     *  returns [SleepState.AWAKE] or [SleepState.SLEEPING]
     */
    fun isUserSleeping(
        normedSleepApiDataBefore: List<SleepApiRawDataEntity>,
        normedSleepApiDataAfter: List<SleepApiRawDataEntity>?,
        mobilePosition: MobilePosition,
        lightConditions: LightConditions,
        mobileUseFrequency: MobileUseFrequency,
        fromDefineUserWakeUp : Boolean = false
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
        if(countSleeping <= 2){// || (startBorderListBefore[1].sleepState != SleepState.SLEEPING || startBorderListBefore[2].sleepState != SleepState.SLEEPING)){
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
            else if (sortedSleepListAfter != null){
                actualThreshold.confidence = (sortedSleepListAfter.sumOf { x-> x.confidence } / sortedSleepListAfter.count()).toFloat()
                actualThreshold.light = (sortedSleepListAfter.sumOf { x-> x.light } / sortedSleepListAfter.count()).toFloat()
                actualThreshold.motion = (sortedSleepListAfter.sumOf { x-> x.motion } / sortedSleepListAfter.count()).toFloat()

                nextThreeTimes.confidence = (sortedSleepListAfter.takeLast(nextTimesBorder)
                    .sumOf { x-> x.confidence } / sortedSleepListAfter.takeLast(nextTimesBorder).count()).toFloat()
                nextThreeTimes.light = (sortedSleepListAfter.takeLast(nextTimesBorder).sumOf { x-> x.light } / sortedSleepListAfter.takeLast(nextTimesBorder).count()).toFloat()
                nextThreeTimes.motion = (sortedSleepListAfter.takeLast(nextTimesBorder).sumOf { x-> x.motion } / sortedSleepListAfter.takeLast(nextTimesBorder).count()).toFloat()
            }

            val avgStartThreshold = ThresholdParams()

            avgStartThreshold.confidence = (newListBefore.sumOf { x-> x.confidence } / newListBefore.count()).toFloat()
            avgStartThreshold.light = (newListBefore.sumOf { x-> x.light } / newListBefore.count()).toFloat()
            avgStartThreshold.motion = (newListBefore.sumOf { x-> x.motion } / newListBefore.count()).toFloat()

            avgStartThreshold.absBetweenThresholds(actualThreshold)

            isSleepStarted = (actualParams.sleepStartBorder.checkIfDifferenceThreshold(
                true,
                3,
                avgStartThreshold) &&
                    (prePrediction || actualParams.sleepStartThreshold.checkIfThreshold(
                        true,
                        3,
                        nextThreeTimes)
                            ))

        }

        // Is Sleeping detection cleanup
        if(isSleepStarted || countSleeping > 2){

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
                    avgThreshold.light += ((sortedSleepListBefore[i].light + sortedSleepListAfter[i].light) / 2).toFloat() * i * i
                    avgThreshold.motion += ((sortedSleepListBefore[i].motion + sortedSleepListAfter[i].motion) / 2).toFloat() * i * i
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

            // if its from define user wakup we have to handle it not that hard. Mostly in the morning we have to
            // check it different.
            isSleeping = if(!fromDefineUserWakeUp)
                actualThreshold.checkIfThreshold(false, 3, actualParams.generalThreshold)
            else
                actualThreshold.checkIfThreshold(false, 3, actualParams.generalThresholdFromDefineUserWakeup)
        }



        return if(isSleeping) SleepState.SLEEPING else SleepState.AWAKE
    }

    /**
     * Defines the actual [SleepState] of the users sleep
     * It creates the actual parameters and then checks the [SleepState]
     */
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
            actualParams.remSleepParams.checkIfDifferenceThreshold(true, 3, avgThreshold) -> SleepState.REM
            actualParams.deepSleepParams.checkIfThreshold(true, 3, avgThreshold) -> SleepState.DEEP
            //actualParams.lightSleepParams.checkIfThreshold(false, 1, avgThreshold) -> SleepState.LIGHT
            else -> SleepState.LIGHT
        }
    }
}






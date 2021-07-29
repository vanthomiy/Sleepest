package com.doitstudio.sleepest_master.sleepcalculation.ml

import android.content.Context
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.ml.*
import com.doitstudio.sleepest_master.model.data.*
import com.doitstudio.sleepest_master.sleepcalculation.ml.ParamsHandler
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
     */
    fun defineTableBed(sleepApiRawDataEntity:List<SleepApiRawDataEntity>) : MobilePosition {

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


        //TODO(Implement algorithm)

        return MobilePosition.INBED


    }

    /**
     * Checks whether the phone is on bed or on table. Error returns [MobilePosition.UNIDENTIFIED]
     */
    private fun defineLightConditions(sleepApiRawDataEntity:List<SleepApiRawDataEntity>) : LightConditions {

        // get the actual sleepApiDataList
        val sleepingData = sleepApiRawDataEntity.filter{x->x.sleepState != SleepState.NONE && x.sleepState != SleepState.AWAKE }

        if (sleepingData.count() == 0){
            return LightConditions.UNIDENTIFIED
        }

        // now we need to calc the values to provide...
        var light = IntArray(4)
        light[0] = sleepingData.maxOf { x->x.light }
        light[1] = sleepingData.minOf { x->x.light }
        light[2] = sleepingData.sumOf { x->x.light } / sleepingData.count()
        light[3] = sleepingData.sortedBy { x-> x.light }[sleepingData.count()/2].light

        //TODO(Implement algorithm)

        return LightConditions.UNIDENTIFIED
    }

    suspend fun isUserSleeping(
        normedSleepApiData: List<SleepApiRawDataEntity>,
        frequency: SleepDataFrequency
    ) : SleepState {

        /*
        // check for standard mobile position.
        val sleepParams = dataStoreRepository.sleepParameterFlow.first()
        var mobilePosition = MobilePosition.getCount(sleepParams.standardMobilePosition)
        var lightConditions = LightConditions.getCount(sleepParams.standardLightCondition)
        val mobileUseFrequency = MobileUseFrequency.getCount(sleepParams.mobileUseFrequency)

        mobilePosition = if(mobilePosition != MobilePosition.UNIDENTIFIED || dataBaseRepository.allUserSleepSessions.first().count() == 0) {
            mobilePosition
        } else {
            var sessions = dataBaseRepository.allUserSleepSessions.first()
            val inBedCount = sessions.filter { x -> x.mobilePosition == MobilePosition.INBED }.count()

            if (inBedCount > sessions.count()/2 )
                MobilePosition.INBED
            else
                MobilePosition.ONTABLE
        }


        val actualParams = ParamsHandler.createDefaultParams(mobilePosition, lightConditions, mobileUseFrequency)

        // Now we are using the algorithm
        val sortedSleepList = normedSleepApiData.sortedByDescending { x -> x.timestampSeconds }

        // now take the params and check the sleep
        // first check if a sleep start detection is needed

        var startBorderList = sortedSleepList.take(actualParams.sleepStartBorder.before.toInt())

        val countSleeping = startBorderList.filter{x->x.sleepState == SleepState.SLEEPING}.count()

        var isSleepStarted = false
        var isSleepingAllowed = false
        var isSleeping = false

        // Sleep start detection
        if(countSleeping < startBorderList.count() / 2 || startBorderList[1].sleepState != SleepState.SLEEPING){
            // check if user is started sleeping
                val actualThreshold = ThresholdParams()
            actualThreshold.confidence = startBorderList[0].confidence.toFloat()
            actualThreshold.light = startBorderList[0].light.toFloat()
            actualThreshold.motion = startBorderList[0].motion.toFloat()

            startBorderList = startBorderList.drop(1)

                val avgStartThreshold = ThresholdParams()

                avgStartThreshold.confidence = (startBorderList.sumOf { x-> x.confidence } / startBorderList.count()).toFloat()
                avgStartThreshold.light = (startBorderList.sumOf { x-> x.light } / startBorderList.count()).toFloat()
                avgStartThreshold.motion = (startBorderList.sumOf { x-> x.motion } / startBorderList.count()).toFloat()

                actualThreshold.absBetweenTresholds(avgStartThreshold)

                isSleepStarted = actualParams.sleepStartBorder.threshold.checkIfThreshold(false, 3, actualThreshold)

        }

        // Is Sleeping detection cleanup
        if(isSleepStarted || countSleeping > startBorderList.count() / 2){

            val sleepCleanUpList = sortedSleepList.take(actualParams.sleepCleanUp.before.toInt())

            val avgCleanUpThreshold = ThresholdParams()

            avgCleanUpThreshold.confidence = (sleepCleanUpList.sumOf { x-> x.confidence } / sleepCleanUpList.count()).toFloat()
            avgCleanUpThreshold.light = (sleepCleanUpList.sumOf { x-> x.light } / sleepCleanUpList.count()).toFloat()
            avgCleanUpThreshold.motion = (sleepCleanUpList.sumOf { x-> x.motion } / sleepCleanUpList.count()).toFloat()

            isSleepingAllowed = avgCleanUpThreshold.checkIfThreshold(true, 3, actualParams.sleepStartBorder.threshold)
        }

        // is user sleeping?
        if(isSleepingAllowed){

            val actualThreshold = ThresholdParams()

            actualThreshold.confidence = sortedSleepList[0].confidence.toFloat()
            actualThreshold.light = sortedSleepList[0].light.toFloat()
            actualThreshold.motion = sortedSleepList[0].motion.toFloat()

            isSleeping = actualThreshold.checkIfThreshold(true, 3, actualParams.generalThreshold)
        }

        // when user not sleeping but it is allowed that user is sleeping then we check the wakeup
        if(!isSleeping && (isSleepingAllowed || isSleepStarted)){
            var sleepEndList = sortedSleepList.take(actualParams.sleepEndBorder.before.toInt())

            val actualThreshold = ThresholdParams()
            actualThreshold.confidence = sleepEndList[0].confidence.toFloat()
            actualThreshold.light = sleepEndList[0].light.toFloat()
            actualThreshold.motion = sleepEndList[0].motion.toFloat()

            sleepEndList = sleepEndList.drop(1)

            val avgCleanUpThreshold = ThresholdParams()

            avgCleanUpThreshold.confidence = (sleepEndList.sumOf { x-> x.confidence } / sleepEndList.count()).toFloat()
            avgCleanUpThreshold.light = (sleepEndList.sumOf { x-> x.light } / sleepEndList.count()).toFloat()
            avgCleanUpThreshold.motion = (sleepEndList.sumOf { x-> x.motion } / sleepEndList.count()).toFloat()

            actualThreshold.absBetweenTresholds(avgCleanUpThreshold)
            isSleeping = actualParams.sleepEndBorder.threshold.checkIfThreshold(true, 3, actualThreshold)
        }
*/
        val isSleeping = true
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
        val actualParams = ParamsHandler.createDefaultParams(mobilePosition, lightConditions, mobileUseFrequency)

        // Now we are using the algorithm
        val sortedSleepListBefore = normedSleepApiDataBefore.sortedByDescending { x->x.timestampSeconds }
        val sortedSleepListAfter = normedSleepApiDataAfter?.sortedBy { x->x.timestampSeconds }

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

            val newListBefore = sortedSleepListBefore.drop(1)

            val actualThreshold = ThresholdParams()

            if(prePrediction) {
                actualThreshold.confidence = sortedSleepListBefore[0].confidence.toFloat()
                actualThreshold.light = sortedSleepListBefore[0].light.toFloat()
                actualThreshold.motion = sortedSleepListBefore[0].motion.toFloat()
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

            actualThreshold.absBetweenTresholds(avgStartThreshold)

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
                    avgThreshold.confidence += ((sortedSleepListBefore[(count - i)].confidence)).toFloat() * i * i
                    avgThreshold.light += ((sortedSleepListBefore[(count - i)].light)).toFloat() * i * i
                    avgThreshold.motion += ((sortedSleepListBefore[(count - i)].motion)).toFloat() * i * i
                }
                else{
                    avgThreshold.confidence += ((sortedSleepListBefore[i].confidence + sortedSleepListAfter!![(count - i)].confidence) / 2).toFloat() * i * i
                    avgThreshold.light += ((sortedSleepListBefore[i].light + sortedSleepListAfter!![(count - i)].light) / 2).toFloat() * i * i
                    avgThreshold.motion += ((sortedSleepListBefore[i].motion + sortedSleepListAfter!![(count - i)].motion) / 2).toFloat() * i * i
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

            actualThreshold.confidence = sortedSleepListBefore[0].confidence.toFloat()
            actualThreshold.light = sortedSleepListBefore[0].light.toFloat()
            actualThreshold.motion = sortedSleepListBefore[0].motion.toFloat()

            isSleeping = actualThreshold.checkIfThreshold(false, 3, actualParams.generalThreshold)
        }

        // when user not sleeping but it is allowed that user is sleeping then we check the wakeup
        if(!isSleeping && (isSleepingAllowed || isSleepStarted)){
            /*var sleepEndList = sortedSleepList.take(actualParams.sleepEndBorder.before.toInt())

            val actualThreshold = ThresholdParams()
            actualThreshold.confidence = sleepEndList[0].confidence.toFloat()
            actualThreshold.light = sleepEndList[0].light.toFloat()
            actualThreshold.motion = sleepEndList[0].motion.toFloat()

            sleepEndList = sleepEndList.drop(1)

            val avgCleanUpThreshold = ThresholdParams()

            avgCleanUpThreshold.confidence = (sleepEndList.sumOf { x-> x.confidence } / sleepEndList.count()).toFloat()
            avgCleanUpThreshold.light = (sleepEndList.sumOf { x-> x.light } / sleepEndList.count()).toFloat()
            avgCleanUpThreshold.motion = (sleepEndList.sumOf { x-> x.motion } / sleepEndList.count()).toFloat()

            actualThreshold.absBetweenTresholds(avgCleanUpThreshold)
            isSleeping = actualParams.sleepEndBorder.threshold.checkIfThreshold(true, 3, actualThreshold)*/
        }


        return if(isSleeping) SleepState.SLEEPING else SleepState.AWAKE
    }


    suspend fun defineUserSleep(
        time:Int,
        normedSleepApiData: List<SleepApiRawDataEntity>,
    ) : SleepState {

        // check for standard mobile position.
        val sleepParams = dataStoreRepository.sleepParameterFlow.first()
        var lightConditions = LightConditions.getCount(sleepParams.standardLightCondition)

        if(lightConditions == LightConditions.UNIDENTIFIED){
            lightConditions = defineLightConditions(normedSleepApiData)
        }

        val actualParams = ParamsHandler.createSleepStateParams(lightConditions)

        // Now we are using the algorithm
        val avgThreshold = ThresholdParams()

        avgThreshold.confidence = (normedSleepApiData.sumOf { x-> x.confidence } / normedSleepApiData.count()).toFloat()
        avgThreshold.light = (normedSleepApiData.sumOf { x-> x.light } / normedSleepApiData.count()).toFloat()
        avgThreshold.motion = (normedSleepApiData.sumOf { x-> x.motion } / normedSleepApiData.count()).toFloat()


        return when {
            actualParams.lightSleepParams.checkIfThreshold(false, 2, avgThreshold) -> SleepState.LIGHT
            actualParams.deepSleepParams.checkIfThreshold(false, 2, avgThreshold) -> SleepState.DEEP
            actualParams.remSleepParams.checkIfThreshold(false, 2, avgThreshold) -> SleepState.REM
            else -> SleepState.LIGHT
        }
    }

    fun defineUserSleepTest(
        time:Int,
        normedSleepApiData: List<SleepApiRawDataEntity>,
    ) : SleepState {

        // check for standard mobile position.


        val actualParams = ParamsHandler.createSleepStateParams(LightConditions.UNIDENTIFIED)

        // Now we are using the algorithm
        val avgThreshold = ThresholdParams()

        avgThreshold.confidence = 0f
        avgThreshold.light = 0f
        avgThreshold.motion = 0f

        var factor = 0
        val count = normedSleepApiData.count()
        for (i in 0..count/2){

            factor += i

            avgThreshold.confidence += ((normedSleepApiData[i].confidence + normedSleepApiData[(count - i) - 1].confidence) / 2).toFloat() * i
            avgThreshold.light += ((normedSleepApiData[i].light + normedSleepApiData[(count - i) - 1].light) / 2).toFloat() * i
            avgThreshold.motion += ((normedSleepApiData[i].motion + normedSleepApiData[(count - i) - 1].motion) / 2).toFloat() * i
        }

        avgThreshold.confidence = avgThreshold.confidence / factor
        avgThreshold.light = avgThreshold.light / factor
        avgThreshold.motion = avgThreshold.motion / factor

        return when {
            actualParams.lightSleepParams.checkIfThreshold(false, 1, avgThreshold) -> SleepState.LIGHT
            actualParams.deepSleepParams.checkIfThreshold(false, 2, avgThreshold) -> SleepState.DEEP
            actualParams.remSleepParams.checkIfThreshold(false, 3, avgThreshold) -> SleepState.REM
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






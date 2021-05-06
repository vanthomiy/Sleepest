package com.doitstudio.sleepest_master.sleepcalculation

import android.content.Context
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.model.data.ModelProcess
import com.doitstudio.sleepest_master.model.data.SleepDataFrequency
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.sleepcalculation.ml.SleepClassifier
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.ZoneOffset

class SleepCalculationHandler(val context: Context) {

    private val sleepDbRepository: SleepCalculationDbRepository by lazy {
        (context.applicationContext as MainApplication).sleepCalculationDbRepository
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
        val dataPoints = minutes //frequency

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



    fun checkPhonePosition(){

        // get the actual sleepApiDataList

        //
    }


    suspend fun checkIsUserSleeping(){

        // get the actual sleepApiDataList
        val sleepApiRawDataEntity = sleepDbRepository.getSleepApiRawDataSince(86400).first().sortedByDescending { x -> x.timestampSeconds }

        if(sleepApiRawDataEntity == null || sleepApiRawDataEntity.count() == 0){
            // do something!

            return
        }

        val id = sleepApiRawDataEntity.first().timestampSeconds
        // get normed list
        val (normedSleepApiData, frequency) = createTimeNormedData(2, false, id, sleepApiRawDataEntity)

        // create features for ml model
        val sleepClassifier = SleepClassifier.getHandler(context)
        val features = sleepClassifier.createFeatures(normedSleepApiData, ModelProcess.SLEEP04, frequency)

        // call the ml model
        val result = sleepClassifier.isUserSleeping(features, frequency)

        // save the result to the sleep api data
        sleepDbRepository.updateSleepApiRawDataSleepState(id, result)

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


    fun defineUserWakeup(){

        // for each sleeping time, we have to define the sleep state

    }

    fun defineSleepStates(time:Int, sleepApiRawDataEntity1:List<SleepApiRawDataEntity>) : SleepState{

        // get the actual sleepApiDataList
        val sleepApiRawDataEntity = sleepApiRawDataEntity1.sortedByDescending { x -> x.timestampSeconds }

        if(sleepApiRawDataEntity == null || sleepApiRawDataEntity.count() == 0){
            // do something!
            return SleepState.NONE
        }

        // get normed list
        val (normedSleepApiData, frequency) = createTimeNormedData(1, true, time, sleepApiRawDataEntity)

        // create features for ml model
        val sleepClassifier = SleepClassifier.getHandler(context)
        val features = sleepClassifier.createFeatures(normedSleepApiData, ModelProcess.SLEEP12, frequency)

        // call the ml model
        return  sleepClassifier.defineUserSleep(features, frequency)

        // save result and continue

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
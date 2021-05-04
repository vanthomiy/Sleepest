package com.doitstudio.sleepest_master.sleepcalculation

import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import java.time.LocalDateTime
import java.time.ZoneOffset

class SleepCalculationHandler {

    fun getFrequencyFromListByHours(hours:Int, sleepList:List<SleepApiRawDataEntity>) : Int
    {

        // actual datetime
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val secondsNow = now.atZone(ZoneOffset.UTC).toEpochSecond()
        val secondsPast = secondsNow - (hours * 3600)

        val list =  sleepList.filter { x -> x.timestampSeconds in secondsPast until secondsNow }.toList()

        if(list.count() == 0)
        {
            return 3600
        }

        // actual datetime
        val frequency = (hours * 60.0) / list.count().toFloat()

        return when {
            frequency <= 10 -> { 5 }
            frequency <= 30 -> { 10 }
            else -> { 30 }
        }
    }

    fun createTimeNormedData(hours:Int, frequency:Int, dataEntity: List<SleepApiRawDataEntity>): List<SleepApiRawDataEntity>
    {
        val minutes = hours * 3600
        val dataPoints = minutes/frequency

        val now = LocalDateTime.now(ZoneOffset.UTC)
        val secondsNow = now.atZone(ZoneOffset.UTC).toEpochSecond()

        val sortedDataEntity = dataEntity.sortedByDescending { x-> x.timestampSeconds }.toList()

        var timeNormedData = mutableListOf<SleepApiRawDataEntity>()

        for (i in 0 until dataPoints step frequency)
        {
            // get the first element that time is smaller then requested
            // If no item is available anymore take the last useable one
            val requestedSeconds =  secondsNow - i
            var item = sortedDataEntity.first { x -> x.timestampSeconds < requestedSeconds }
            timeNormedData.add(item ?: timeNormedData.last())
        }

        return timeNormedData
    }



    fun checkPhonePosition(){

        // get the actual sleepApiDataList

        //
    }


    fun checkIsUserSleeping(){

        // get the actual sleepApiDataList

        // get frequency

        // get normed list

        // create features for ml model

        // call the ml model

        // we retrieved our data here

    }

    fun defineUserWakeup(){

        // for each sleeping time, we have to define the sleep state

    }

    fun defineSleepStates(){

        // get the actual sleepApiDataList
    }



}
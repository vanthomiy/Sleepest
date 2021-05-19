package com.doitstudio.sleepest_master.sleepcalculation

import org.junit.Assert.*
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.SleepDataFrequency
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.sleepcalculation.ml.ModelInputAssignment
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataRealEntity
import com.doitstudio.sleepest_master.storage.db.SleepDatabase
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.random.Random

class SleepCalculationHandlerTest
{
    private lateinit var context: Context
    private lateinit var sleepDbRepository: DatabaseRepository

    private val sleepCalcDatabase by lazy {
        SleepDatabase.getDatabase(context)
    }

    private val sleepStoreRepository by lazy {
        DataStoreRepository.getRepo(context)
    }

    @Before
    fun init(){
        context = InstrumentationRegistry.getInstrumentation().targetContext

        sleepDbRepository = DatabaseRepository.getRepo(
            sleepCalcDatabase.sleepApiRawDataDao(),
            sleepCalcDatabase.sleepDataDao(),
            sleepCalcDatabase.userSleepSessionDao(),
            sleepCalcDatabase.alarmDao()
        )




    }

    @Test
    fun getFrequencyFromListByHoursTest(){

        val actualtime = 1000000
        val twohours = 60 * 60  * 2
        val onehour = 60 * 60  * 1
        val sleepCalculationHandler = SleepCalculationHandler.getHandler(context)

        var sleepList = mutableListOf<SleepApiRawDataEntity>()

        // asdhajsdasd


        // no data available
        var result = sleepCalculationHandler.getFrequencyFromListByHours(2, false, actualtime, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.NONE))


        sleepList.clear()

        // add some data that is not in the last two hours ( 45 mins per file)
        for(i in 0..twohours step (45*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        result = sleepCalculationHandler.getFrequencyFromListByHours(2, false, actualtime, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.THIRTY))

        sleepList.clear()

        // add some data that is not in the last two hours ( 25 mins per file)
        for(i in 0..twohours step (25*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        result = sleepCalculationHandler.getFrequencyFromListByHours(2, false, actualtime, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.TEN))

        sleepList.clear()

        // add some data that is not in the last two hours ( 8 mins per file)
        for(i in 0..twohours step (8*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        result = sleepCalculationHandler.getFrequencyFromListByHours(2, false, actualtime, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.FIVE))

        sleepList.clear()

        // add some data that is not in the last two hours ( 8 mins per file) and plus and minus time
        for(i in -twohours/2..twohours/2 step (8*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        result = sleepCalculationHandler.getFrequencyFromListByHours(1, true, actualtime, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.FIVE))

    }

    @Test
    fun createTimeNormedDataTest(){

        val actualtime = 1000000
        val twohours = 60 * 60  * 2
        val onehour = 60 * 60  * 1
        val sleepCalculationHandler = SleepCalculationHandler.getHandler(context)

        var sleepList = mutableListOf<SleepApiRawDataEntity>()


        var (normedSleepApiData, frequency) = sleepCalculationHandler.createTimeNormedData(2, false, actualtime, sleepList)

        assertThat(normedSleepApiData.count(), CoreMatchers.equalTo(0))
        assertThat(frequency, CoreMatchers.equalTo(SleepDataFrequency.NONE))

        sleepList.clear()

        // add some data that is not in the last two hours ( 45 mins per file)
        for(i in 0..twohours step (45*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        var (normedSleepApiData1, frequency1) = sleepCalculationHandler.createTimeNormedData(2, false, actualtime, sleepList)

        assertThat(normedSleepApiData1.count(), CoreMatchers.equalTo(4))
        assertThat(frequency1, CoreMatchers.equalTo(SleepDataFrequency.THIRTY))

        sleepList.clear()

        // add some data that is not in the last two hours ( 25 mins per file)
        for(i in 0..twohours step (25*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        var (normedSleepApiData2, frequency2) = sleepCalculationHandler.createTimeNormedData(2, false, actualtime, sleepList)

        assertThat(normedSleepApiData2.count(), CoreMatchers.equalTo(12))
        assertThat(frequency2, CoreMatchers.equalTo(SleepDataFrequency.TEN))

        sleepList.clear()

        // add some data that is not in the last two hours ( 8 mins per file)
        for(i in 0..twohours step (8*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        var (normedSleepApiData3, frequency3) = sleepCalculationHandler.createTimeNormedData(2, false, actualtime, sleepList)

        assertThat(normedSleepApiData3.count(), CoreMatchers.equalTo(24))
        assertThat(frequency3, CoreMatchers.equalTo(SleepDataFrequency.FIVE))

        sleepList.clear()

        // add some data that is not in the last two hours ( 8 mins per file) and plus and minus time
        for(i in -twohours/2..twohours/2 step (8*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        var (normedSleepApiData4, frequency4) = sleepCalculationHandler.createTimeNormedData(2, false, actualtime, sleepList)

        assertThat(normedSleepApiData4.count(), CoreMatchers.equalTo(24))
        assertThat(frequency4, CoreMatchers.equalTo(SleepDataFrequency.FIVE))


    }

    @Test
    fun userNotSleepingTest() = runBlocking {

        val actualtimeSeconds = 1000000
        val actualTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(actualtimeSeconds.toLong()*1000), ZoneOffset.UTC)

        val sleepCalculationHandler = SleepCalculationHandler.getHandler(context)

        var sleepList = mutableListOf<SleepApiRawDataEntity>()
        sleepDbRepository.deleteSleepApiRawData()

        // if nothing is inside... should not break
        sleepCalculationHandler.userNotSleeping(actualTime)

        for(i in 0..10)
        {
            val data = SleepApiRawDataEntity(actualtimeSeconds-i, 1,2,3,sleepState = SleepState.SLEEPING)
            sleepList.add(data)
            sleepDbRepository.insertSleepApiRawData((sleepList))
        }

        sleepCalculationHandler.userNotSleeping(actualTime)

        val newSleepList = sleepDbRepository.allSleepApiRawData.first().filter{x -> x.sleepState == SleepState.SLEEPING }
        assertThat(newSleepList.count(), CoreMatchers.equalTo(0))
    }

    @Test
    fun userCurrentlyNotSleepingTest() = runBlocking {

        val actualtimeSeconds = 1000000
        val actualTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(actualtimeSeconds.toLong()*1000), ZoneOffset.UTC)

        val sleepCalculationHandler = SleepCalculationHandler.getHandler(context)

        var sleepList = mutableListOf<SleepApiRawDataEntity>()
        sleepDbRepository.deleteSleepApiRawData()

        // if nothing is inside... should not break
        sleepCalculationHandler.userCurrentlyNotSleeping(actualTime)

        for(i in 0..10)
        {
            val data = SleepApiRawDataEntity(actualtimeSeconds-i, 1,2,3,sleepState = SleepState.SLEEPING)
            sleepList.add(data)
            sleepDbRepository.insertSleepApiRawData((sleepList))
        }

        sleepCalculationHandler.userCurrentlyNotSleeping(actualTime)

        val newSleepList = sleepDbRepository.allSleepApiRawData.first().filter{x -> x.sleepState == SleepState.SLEEPING }
        assertThat(newSleepList.count(), CoreMatchers.equalTo(sleepList.count()-1))
    }

    @Test
    fun findLightUserWakeupTest() = runBlocking {

        val sleepCalculationHandler = SleepCalculationHandler.getHandler(context)

        // get actual time
        val actualTimeSeconds =  sleepCalculationHandler.getSecondsOfDay()

        var sleepList5 = mutableListOf<SleepApiRawDataEntity>()
        var sleepList30 = mutableListOf<SleepApiRawDataEntity>()

        for(i in 0..20) // 2 hours / 20  < 10
        {
            val data = SleepApiRawDataEntity(actualTimeSeconds-(i*5*60), 1,2,3,sleepState = SleepState.SLEEPING)
            sleepList5.add(data)
        }

        for(i in 0..2) // 2 hours / 20  < 10
        {
            val data = SleepApiRawDataEntity(actualTimeSeconds-(i*30*60), 1,2,3,sleepState = SleepState.SLEEPING)
            sleepList30.add(data)
        }

        // wakeuptime to far away from now ( 3 times frequency or more)
        var wakeuptime = actualTimeSeconds + (60 * 3 * 5) + 1
        var calcTime = sleepCalculationHandler.findLightUserWakeup(sleepList5, wakeuptime)

        assertThat(wakeuptime, CoreMatchers.equalTo(calcTime))

        // wakeuptime to far away from now ( 3 times frequency or more)
        wakeuptime = actualTimeSeconds + (60 * 3 * 30) + 1
        calcTime = sleepCalculationHandler.findLightUserWakeup(sleepList30, wakeuptime)

        assertThat(wakeuptime, CoreMatchers.equalTo(calcTime))

        // wakeuptime in the past ( 3 times frequency or more)
        wakeuptime = actualTimeSeconds - (60 * 3 * 5 + 1)
        calcTime = sleepCalculationHandler.findLightUserWakeup(sleepList5, wakeuptime)

        assertThat((calcTime) , CoreMatchers.equalTo(wakeuptime))

        // wakeuptime in the past ( 3 times frequency or more)
        wakeuptime = actualTimeSeconds - (60 * 3 * 30 + 1)
        calcTime = sleepCalculationHandler.findLightUserWakeup(sleepList30, wakeuptime)

        assertThat((calcTime), CoreMatchers.equalTo(wakeuptime))

        // wakuptime okay
        wakeuptime = actualTimeSeconds + (60 * 3)
        calcTime = sleepCalculationHandler.findLightUserWakeup(sleepList5, wakeuptime)

        assertThat((calcTime != wakeuptime), CoreMatchers.equalTo(true))

        // wakeuptime okay
        wakeuptime = actualTimeSeconds - (60 * 3)
        calcTime = sleepCalculationHandler.findLightUserWakeup(sleepList5, wakeuptime)

        assertThat((calcTime != wakeuptime), CoreMatchers.equalTo(true))

    }

    @Test
    fun checkPhonePositionTest() = runBlocking{

        val actualTimeSeconds = 100000
        val sleepCalculationHandler = SleepCalculationHandler.getHandler(context)
        var sleepList5 = mutableListOf<SleepApiRawDataEntity>()
        var sleepList30 = mutableListOf<SleepApiRawDataEntity>()

        var calPosition = sleepCalculationHandler.checkPhonePosition(sleepList5)

        assertThat(calPosition, CoreMatchers.equalTo(MobilePosition.UNIDENTIFIED))

        // add 5 freuquency data with table
        for(i in 0..20 step 5) // 2 hours / 20  < 10
        {
            val data = SleepApiRawDataEntity(actualTimeSeconds-(i*5*60), 96,1,1,sleepState = SleepState.SLEEPING)
            sleepList5.add(data)
        }

        // add 5 freuquency data with table
        for(i in 0..5 step 30) // 2 hours / 20  < 10
        {
            val data = SleepApiRawDataEntity(actualTimeSeconds-(i*30*60), 96,1,1,sleepState = SleepState.SLEEPING)
            sleepList30.add(data)
        }

        calPosition = sleepCalculationHandler.checkPhonePosition(sleepList5)
        assertThat(calPosition, CoreMatchers.equalTo(MobilePosition.ONTABLE))

        calPosition = sleepCalculationHandler.checkPhonePosition(sleepList30)
        assertThat(calPosition, CoreMatchers.equalTo(MobilePosition.ONTABLE))

        sleepList5.clear()

        // add 5 freuquency data with bed
        for(i in 0..20 step 5) // 2 hours / 20  < 10
        {
            val data = SleepApiRawDataEntity(actualTimeSeconds-(i*5*60), 85,2,1,sleepState = SleepState.SLEEPING)
            sleepList5.add(data)
        }

        // add 5 freuquency data with bed
        for(i in 0..5 step 30) // 2 hours / 20  < 10
        {
            val data = SleepApiRawDataEntity(actualTimeSeconds-(i*30*60), 85,3,1,sleepState = SleepState.SLEEPING)
            sleepList30.add(data)
        }

        calPosition = sleepCalculationHandler.checkPhonePosition(sleepList5)
        assertThat(calPosition, CoreMatchers.equalTo(MobilePosition.INBED))

        calPosition = sleepCalculationHandler.checkPhonePosition(sleepList30)
        assertThat(calPosition, CoreMatchers.equalTo(MobilePosition.INBED))

    }

    @Test
    fun defineSleepStatesTest() = runBlocking{

        val actualTimeSeconds = 100000
        val sleepCalculationHandler = SleepCalculationHandler.getHandler(context)
        var sleepList5 = mutableListOf<SleepApiRawDataEntity>()
        var sleepList30 = mutableListOf<SleepApiRawDataEntity>()

        // keine daten
        var sleepState = sleepCalculationHandler.defineSleepStates(actualTimeSeconds, sleepList5)
        assertThat(sleepState, CoreMatchers.equalTo(SleepState.SLEEPING))

        // add 5 freuquency data with table but all in past
        for(i in 0..20 step 5) // 2 hours / 20  < 10
        {
            val data = SleepApiRawDataEntity(actualTimeSeconds-(i*5*60), 96,1,1,sleepState = SleepState.SLEEPING)
            sleepList5.add(data)
        }

        sleepState = sleepCalculationHandler.defineSleepStates(actualTimeSeconds, sleepList5)
        assertThat(sleepState, CoreMatchers.equalTo(SleepState.SLEEPING))


        // add 5 freuquency data in future but to less
        for(i in 0..1 step 5) // 2 hours / 20  < 10
        {
            val data = SleepApiRawDataEntity(actualTimeSeconds+(i*5*60), 96,1,1,sleepState = SleepState.SLEEPING)
            sleepList5.add(data)
        }

        sleepState = sleepCalculationHandler.defineSleepStates(actualTimeSeconds, sleepList5)
        assertThat(sleepState, CoreMatchers.equalTo(SleepState.SLEEPING))

        // add 5 freuquency data in future enought
        for(i in 1..20 step 5)
        {
            val data = SleepApiRawDataEntity(actualTimeSeconds+(i*5*60), 96,1,1,sleepState = SleepState.SLEEPING)
            sleepList5.add(data)
        }

        sleepState = sleepCalculationHandler.defineSleepStates(actualTimeSeconds, sleepList5)
        assertThat((sleepState != SleepState.SLEEPING), CoreMatchers.equalTo(true))

    }

    @Test
    fun fullSleepCalculationTest() = runBlocking {

        // load all data
        var path = "database/testdata/SleepValues.json"
        var pathTrue = "database/testdata/SleepValuesTrue.json"

        var gson = Gson()

        val jsonFile = context
            .assets
            .open(path)
            .bufferedReader()
            .use(BufferedReader::readText)

        val jsonFileTrue = context
            .assets
            .open(path)
            .bufferedReader()
            .use(BufferedReader::readText)

        var data =  gson.fromJson(jsonFile, Array<SleepApiRawDataEntity>::class.java).asList()
        var dataTrue =  gson.fromJson(jsonFileTrue, Array<SleepApiRawDataRealEntity>::class.java).asList()


        // now we have all sleep data we want to go through all data and keep the data inside of the storage...
        // we also take the real data and check if the calculated data is far away from the true data
        // we check it for [LifeUserSleepActivity]

        // assign each time new... to check if it is working also
        var sleepCalculationHandler = SleepCalculationHandler.getHandler(context)

        var lastTimestamp = 0
        var holdTime = 15*60
        var lastTimestampWakeup = 0

        // problem: we have same days in here so we have to select just a few ...otherwise we will have buggy things
        val daysCount = 10
        var startDay = LocalDateTime.MIN
        val datasets = mutableMapOf<Int, MutableList<SleepApiRawDataEntity>>()
        val datasetsReal = mutableMapOf<Int, MutableList<SleepApiRawDataRealEntity>>()
        var count = 0

        for(i in 0.. data.lastIndex){
            val actualTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(data[i].timestampSeconds.toLong()*1000), ZoneOffset.UTC)

            if(actualTime > startDay){
                startDay = actualTime.plusDays(daysCount.toLong())
                datasets[count] = mutableListOf()
                datasetsReal[count] = mutableListOf()
                count++
            }

            datasets[count]?.add(data[i])
            datasetsReal[count]?.add(dataTrue[i])
        }

/*
        data.forEach{

            rawdata ->

            // insert the sleep api data
            sleepDbRepository.insertSleepApiRawData(rawdata)

            val actualTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastTimestamp.toLong()*1000), ZoneOffset.UTC)

            // only call it every 15 minutes or like so
            if(lastTimestamp+holdTime < rawdata.timestampSeconds){

                lastTimestamp = rawdata.timestampSeconds
                // call the sleep calc handler...

                sleepCalculationHandler.checkIsUserSleeping(actualTime)
            }

            if(actualTime.hour in 5..7 && lastTimestampWakeup+holdTime < rawdata.timestampSeconds){

                lastTimestampWakeup = rawdata.timestampSeconds
                sleepCalculationHandler.defineUserWakeup(actualTime)
            }
        }
*/

        // at the really end we should have as much sleep user sessions as times....

        val dasas = datasets
        val sdfsdf = datasetsReal


    }
}
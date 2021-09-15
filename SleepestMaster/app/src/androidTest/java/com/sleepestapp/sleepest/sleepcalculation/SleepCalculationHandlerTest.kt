package com.sleepestapp.sleepest.sleepcalculation

import org.junit.Assert.*
import android.content.Context
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.sleepestapp.sleepest.model.data.LightConditions
import com.sleepestapp.sleepest.model.data.MobilePosition
import com.sleepestapp.sleepest.model.data.SleepDataFrequency
import com.sleepestapp.sleepest.model.data.SleepState
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.storage.db.*
import com.google.gson.Gson
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.time.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

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
            sleepCalcDatabase.userSleepSessionDao(),
            sleepCalcDatabase.alarmDao(),
            sleepCalcDatabase.activityApiRawDataDao()

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
        var result = sleepCalculationHandler.getFrequencyFromListByHours(2f, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.NONE))


        sleepList.clear()

        // add some data that is not in the last two hours ( 45 mins per file)
        for(i in 0..twohours step (45*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        result = sleepCalculationHandler.getFrequencyFromListByHours(2f, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.THIRTY))

        sleepList.clear()

        // add some data that is not in the last two hours ( 25 mins per file)
        for(i in 0..twohours step (25*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        result = sleepCalculationHandler.getFrequencyFromListByHours(2f, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.TEN))

        sleepList.clear()

        // add some data that is not in the last two hours ( 8 mins per file)
        for(i in 0..twohours step (8*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        result = sleepCalculationHandler.getFrequencyFromListByHours(2f, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.FIVE))

        sleepList.clear()

        // add some data that is not in the last two hours ( 8 mins per file) and plus and minus time
        for(i in -twohours/2..twohours/2 step (8*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        result = sleepCalculationHandler.getFrequencyFromListByHours(2f, sleepList.toList())
        assertThat(result, CoreMatchers.equalTo(SleepDataFrequency.FIVE))

    }

    @Test
    fun createTimeNormedDataTest(){

        val actualtime = 1000000
        val twohours = 60 * 60  * 2
        val onehour = 60 * 60  * 1
        val sleepCalculationHandler = SleepCalculationHandler.getHandler(context)

        var sleepList = mutableListOf<SleepApiRawDataEntity>()


        var (normedSleepApiData, frequency) = sleepCalculationHandler.createTimeNormedData(1f, false, actualtime, sleepList)

        assertThat(normedSleepApiData.count(), CoreMatchers.equalTo(0))
        assertThat(frequency, CoreMatchers.equalTo(SleepDataFrequency.NONE))

        sleepList.clear()

        // add some data that is not in the last two hours ( 45 mins per file)
        for(i in 0..twohours step (45*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        var (normedSleepApiData1, frequency1) = sleepCalculationHandler.createTimeNormedData(1f, false, actualtime, sleepList)

        assertThat(normedSleepApiData1.count(), CoreMatchers.equalTo(2))
        assertThat(frequency1, CoreMatchers.equalTo(SleepDataFrequency.THIRTY))

        sleepList.clear()

        // add some data that is not in the last two hours ( 25 mins per file)
        for(i in 0..twohours step (25*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        var (normedSleepApiData2, frequency2) = sleepCalculationHandler.createTimeNormedData(1f, false, actualtime, sleepList)

        assertThat(normedSleepApiData2.count(), CoreMatchers.equalTo(6))
        assertThat(frequency2, CoreMatchers.equalTo(SleepDataFrequency.TEN))

        sleepList.clear()

        // add some data that is not in the last two hours ( 8 mins per file)
        for(i in 0..twohours step (8*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        var (normedSleepApiData3, frequency3) = sleepCalculationHandler.createTimeNormedData(1f, false, actualtime, sleepList)

        assertThat(normedSleepApiData3.count(), CoreMatchers.equalTo(12))
        assertThat(frequency3, CoreMatchers.equalTo(SleepDataFrequency.FIVE))

        sleepList.clear()

        // add some data that is not in the last two hours ( 8 mins per file) and plus and minus time
        for(i in -twohours/2..twohours/2 step (8*60))
        {
            val data = SleepApiRawDataEntity(actualtime-i, 1,2,3,sleepState = SleepState.AWAKE)
            sleepList.add(data)
        }

        var (normedSleepApiData4, frequency4) = sleepCalculationHandler.createTimeNormedData(1f, false, actualtime, sleepList)

        assertThat(normedSleepApiData4.count(), CoreMatchers.equalTo(12))
        assertThat(frequency4, CoreMatchers.equalTo(SleepDataFrequency.FIVE))


    }

    @Test
    fun userNotSleepingTest() = runBlocking {

        val actualtimeSeconds = 1000000
        val actualTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(actualtimeSeconds.toLong()*1000), ZoneOffset.systemDefault())

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

        val newSleepList = sleepDbRepository.allSleepApiRawData.first()?.filter{ x -> x.sleepState == SleepState.SLEEPING }
        assertThat(newSleepList?.count(), CoreMatchers.equalTo(0))
    }

    @Test
    fun userCurrentlyNotSleepingTest() = runBlocking {

        val actualtimeSeconds = 1000000
        val actualTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(actualtimeSeconds.toLong()*1000), ZoneOffset.systemDefault())

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

        val newSleepList = sleepDbRepository.allSleepApiRawData.first()?.filter{ x -> x.sleepState == SleepState.SLEEPING }
        assertThat(newSleepList?.count(), CoreMatchers.equalTo(sleepList.count()-1))
    }

    /*
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
     */

    @Test
    fun checkPhonePositionTest() = runBlocking{

        val actualTimeSeconds = 100000
        val sleepCalculationHandler = SleepCalculationHandler.getHandler(context)
        val classifier = SleepClassifier.getHandler(context)

        var sleepList5 = mutableListOf<SleepApiRawDataEntity>()
        var sleepList30 = mutableListOf<SleepApiRawDataEntity>()

        var calPosition = classifier.defineTableBed(sleepList5)

        var pos = MobilePosition.getCount(sleepStoreRepository.sleepParameterFlow.first().standardMobilePositionOverLastWeek)

        assertThat(calPosition, CoreMatchers.equalTo(pos))

        // add 5 freuquency data with table
        for(i in 0..25 step 5) // 2 hours / 20  < 10
        {
            val data = SleepApiRawDataEntity(actualTimeSeconds-(i*5*60), 96,1,1,sleepState = SleepState.SLEEPING)
            sleepList5.add(data)
        }

        // add 5 freuquency data with table
        for(i in 0..180 step 30) // 2 hours / 20  < 10
        {
            val data = SleepApiRawDataEntity(actualTimeSeconds-(i*30*60), 96,1,1,sleepState = SleepState.SLEEPING)
            sleepList30.add(data)
        }

        calPosition = classifier.defineTableBed(sleepList5)
        assertThat(calPosition, CoreMatchers.equalTo(MobilePosition.ONTABLE))

        calPosition = classifier.defineTableBed(sleepList30)
        assertThat(calPosition, CoreMatchers.equalTo(MobilePosition.ONTABLE))

        sleepList5.clear()

        // add 5 freuquency data with bed
        for(i in 0..25 step 5) // 2 hours / 20  < 10
        {
            val data = SleepApiRawDataEntity(actualTimeSeconds-(i*5*60), 85,2,1,sleepState = SleepState.SLEEPING)
            sleepList5.add(data)
        }

        // add 5 freuquency data with bed
        for(i in 0..180 step 30) // 2 hours / 20  < 10
        {
            val data = SleepApiRawDataEntity(actualTimeSeconds-(i*30*60), 85,3,1,sleepState = SleepState.SLEEPING)
            sleepList30.add(data)
        }

        calPosition = classifier.defineTableBed(sleepList5)
        assertThat(calPosition, CoreMatchers.equalTo(MobilePosition.INBED))

        calPosition = classifier.defineTableBed(sleepList30)
        assertThat(calPosition, CoreMatchers.equalTo(MobilePosition.INBED))

    }

    @Test
    fun secondsOfDayTest(){

        val sleepCalculationHandler = SleepCalculationHandler.getHandler(context)

        var time = LocalTime.now()

        var seconds = SleepTimeValidationUtil.getSecondsOfDay()

        assertThat(time.toSecondOfDay(), CoreMatchers.equalTo(seconds))
    }

    @Test
    fun defineSleepStatesTest() = runBlocking{

        val actualTimeSeconds = 100000
        val sleepCalculationHandler = SleepCalculationHandler.getHandler(context)
        var sleepList5 = mutableListOf<SleepApiRawDataEntity>()

        // keine daten
        var sleepState = sleepCalculationHandler.defineSleepStates(actualTimeSeconds, sleepList5, LightConditions.DARK)
        assertThat(sleepState, CoreMatchers.equalTo(SleepState.SLEEPING))

        // add 5 freuquency data with table but all in past
        for(i in 0..20 step 5) // 2 hours / 20  < 10
        {
            val data = SleepApiRawDataEntity(actualTimeSeconds-(i*5*60), 96,1,1,sleepState = SleepState.SLEEPING)
            sleepList5.add(data)
        }

        sleepState = sleepCalculationHandler.defineSleepStates(actualTimeSeconds, sleepList5,LightConditions.DARK)
        assertThat(sleepState, CoreMatchers.equalTo(SleepState.SLEEPING))


        // add 5 freuquency data in future but to less
        for(i in 0..1 step 5) // 2 hours / 20  < 10
        {
            val data = SleepApiRawDataEntity(actualTimeSeconds+(i*5*60), 96,1,1,sleepState = SleepState.SLEEPING)
            sleepList5.add(data)
        }

        sleepState = sleepCalculationHandler.defineSleepStates(actualTimeSeconds, sleepList5,LightConditions.DARK)
        assertThat(sleepState, CoreMatchers.equalTo(SleepState.SLEEPING))

        // add 5 freuquency data in future enought
        for(i in 1..20 step 5)
        {
            val data = SleepApiRawDataEntity(actualTimeSeconds+(i*5*60), 96,1,1,sleepState = SleepState.SLEEPING)
            sleepList5.add(data)
        }

        sleepState = sleepCalculationHandler.defineSleepStates(actualTimeSeconds, sleepList5, LightConditions.DARK)
        assertThat((sleepState != SleepState.SLEEPING), CoreMatchers.equalTo(true))

    }

    /**
     * We test the complete sleep calculation with a few sleeps and check if the sleep amount and the alarm is setup right
     */
    @Test
    fun fullSleepCalculationTest() = runBlocking {

        // region inital
        // load all data
        var path = "databases/testdata/SleepValues.json"
        var pathTrue = "databases/testdata/SleepValuesTrue.json"

        var gson = Gson()

        val jsonFile = context
            .assets
            .open(path)
            .bufferedReader()
            .use(BufferedReader::readText)

        val jsonFileTrue = context
            .assets
            .open(pathTrue)
            .bufferedReader()
            .use(BufferedReader::readText)

        var data =  gson.fromJson(jsonFile, Array<Array<SleepApiRawDataEntity>>::class.java).asList()
        var dataTrue =  gson.fromJson(jsonFileTrue, Array<Array<SleepApiRawDataRealEntity>>::class.java).asList()


        // endregion

        // now we have all sleep data we want to go through all data and keep the data inside of the storage...
        // we also take the real data and check if the calculated data is far away from the true data
        // we check it for [LifeUserSleepActivity]

        // assign each time new... to check if it is working also
        var sleepCalculationHandler = SleepCalculationHandler.getHandler(context)

        sleepDbRepository.deleteAllUserSleepSessions()
        sleepDbRepository.deleteSleepApiRawData()
        sleepDbRepository.deleteAllAlarms()

        // add alarm for each day
        val days = DayOfWeek.values().toCollection(ArrayList())
        val alarm = AlarmEntity(1,
            isActive = true,
            wasFired = false,
            sleepDuration = 25800,
            wakeupEarly = 21600,
            wakeupLate = 32400,
            activeDayOfWeek = days
        )
        sleepDbRepository.insertAlarm(alarm)

        var dayCount = 0
        var offset = 12
        var count = 1

        var sleepSessionsListReal = mutableListOf<UserSleepSessionEntity>()

        // take the real ones and calculate the sleeptimes /wakeuptimes etc.
        for(i in offset..offset+count) {

            val userSleep = UserSleepSessionEntity(UserSleepSessionEntity.getIdByTimeStamp(dataTrue[i][0].timestampSeconds))

            var sleeping = false

            var awakeTime = 0
            var sleepTimes = 0

            for(j in 1 until (dataTrue[i].count() * (0.5)).toInt()){

                if(dataTrue[i][j].real == "awake" && sleeping){
                    awakeTime += dataTrue[i][j].timestampSeconds - dataTrue[i][j-1].timestampSeconds
                }
                else {
                    sleepTimes += dataTrue[i][j].timestampSeconds - dataTrue[i][j-1].timestampSeconds
                    sleeping = true
                }

            }

            userSleep.sleepTimes.awakeTime = awakeTime
            userSleep.sleepTimes.sleepDuration = sleepTimes
            sleepSessionsListReal.add(userSleep)
        }

        // take each time 10 days/nights and calculate!!
        for(i in offset..offset+count) {


            var lastTimestamp = 0
            var holdTime = 15*60
            var lastTimestampWakeup = 0


            var lastCall = 0

            for(j in 1 until (data[i].count() * (0.5)).toInt()){

                val rawdata = data[i][j]
                rawdata.sleepState = SleepState.NONE
                rawdata.oldSleepState = SleepState.NONE
                // insert the sleep api data
                sleepDbRepository.insertSleepApiRawData(rawdata)

                val actualTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(lastTimestamp.toLong() * 1000),
                        ZoneOffset.systemDefault()
                )

                // only call it every 15 minutes or like so
                if (lastTimestamp + holdTime < rawdata.timestampSeconds) {

                    lastTimestamp = rawdata.timestampSeconds
                    // call the sleep calc handler...

                    sleepCalculationHandler.checkIsUserSleeping(actualTime)
                    //lastCall = rawdata.timestampSeconds
                }

                if (j > data[i].count() * 0.25f && lastTimestampWakeup + holdTime < rawdata.timestampSeconds) {

                    // letzter aufruf der verheizten zeit
                    lastTimestampWakeup = rawdata.timestampSeconds
                    sleepCalculationHandler.defineUserWakeup(actualTime)
                    lastCall = rawdata.timestampSeconds

                }

                delay(1000)
            }


            // now check if the alarm was set right
            var sleeptimeseconds = sleepSessionsListReal.find{x-> x.id == UserSleepSessionEntity.getIdByTimeStamp(data[i][0].timestampSeconds)}!!.sleepTimes.sleepDuration
            var restsleep =  alarm.sleepDuration - sleeptimeseconds
            if(restsleep < 3000){
                restsleep = 3000
            }

            // wakeuptime is
            var realWakeup = sleepDbRepository.getNextActiveAlarm()!!.actualWakeup
            val getactiveAlamrs = sleepDbRepository.alarmFlow.first()
            val ok = getactiveAlamrs
            val actualTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastCall.toLong()*1000), ZoneOffset.systemDefault())
            var timeofday = actualTime.toLocalTime().toSecondOfDay()
            timeofday += restsleep
            var diff = abs(timeofday-realWakeup)

            // assert that diff is not greater then 30 min
            //assertThat(diff < (45*60) , CoreMatchers.equalTo(true))
            dayCount +=1

        }

        // at the really end we should have as much sleep user sessions as times....
        val sleepSessions = sleepDbRepository.allUserSleepSessions.first()

        //assertThat(sleepSessions.size , CoreMatchers.equalTo(dayCount))
    }

    /**
     * We test the complete sleep calculation with a few sleeps and check if the sleep amount and the alarm is setup right
     */
    @Test
    fun sleepCalculationTest(): Unit = runBlocking {

        val sleepCalculationHandler = SleepCalculationHandler.getHandler(context)

        sleepDbRepository.deleteAllUserSleepSessions()
        sleepDbRepository.deleteSleepApiRawData()
        sleepDbRepository.deleteAllAlarms()

        // add alarm for each day
        val days = DayOfWeek.values().toCollection(ArrayList())
        val alarm = AlarmEntity(1,
            isActive = true,
            wasFired = false,
            sleepDuration = 25800,
            wakeupEarly = 21600,
            wakeupLate = 32400,
            activeDayOfWeek = days
        )

        sleepDbRepository.insertAlarm(alarm)



        var path = "databases/testdata/SleepValues.json"
        var pathTrue = "databases/testdata/SleepValuesTrue.json"

        var gson = Gson()

        val jsonFile = context
            .assets
            .open(path)
            .bufferedReader()
            .use(BufferedReader::readText)

        val jsonFileTrue = context
            .assets
            .open(pathTrue)
            .bufferedReader()
            .use(BufferedReader::readText)

        var dataUnPred =  gson.fromJson(jsonFile, Array<Array<SleepApiRawDataEntity>>::class.java).asList()
        var dataTrue =  gson.fromJson(jsonFileTrue, Array<Array<SleepApiRawDataRealEntity>>::class.java).asList()


        val awakeState = mutableMapOf<SleepState, Int>(
            SleepState.AWAKE to 0,
            SleepState.NONE to 0,
            SleepState.SLEEPING to 0,
            SleepState.LIGHT to 0,
            SleepState.DEEP to 0,
            SleepState.REM to 0)
        var sleepingState = mutableMapOf<SleepState, Int>(
            SleepState.AWAKE to 0,
            SleepState.NONE to 0,
            SleepState.SLEEPING to 0,
            SleepState.LIGHT to 0,
            SleepState.DEEP to 0,
            SleepState.REM to 0)
        var lightState = mutableMapOf<SleepState, Int>(
            SleepState.AWAKE to 0,
            SleepState.NONE to 0,
            SleepState.SLEEPING to 0,
            SleepState.LIGHT to 0,
            SleepState.DEEP to 0,
            SleepState.REM to 0)
        var deepState = mutableMapOf<SleepState, Int>(
            SleepState.AWAKE to 0,
            SleepState.NONE to 0,
            SleepState.SLEEPING to 0,
            SleepState.LIGHT to 0,
            SleepState.DEEP to 0,
            SleepState.REM to 0)
        var remState = mutableMapOf<SleepState, Int>(
            SleepState.AWAKE to 0,
            SleepState.NONE to 0,
            SleepState.SLEEPING to 0,
            SleepState.LIGHT to 0,
            SleepState.DEEP to 0,
            SleepState.REM to 0)

        var realCounts = mutableMapOf<SleepState, Int>(
            SleepState.AWAKE to 0,
            SleepState.NONE to 0,
            SleepState.SLEEPING to 0,
            SleepState.LIGHT to 0,
            SleepState.DEEP to 0,
            SleepState.REM to 0)

        var predCounts = mutableMapOf<SleepState, Int>(
            SleepState.AWAKE to 0,
            SleepState.NONE to 0,
            SleepState.SLEEPING to 0,
            SleepState.LIGHT to 0,
            SleepState.DEEP to 0,
            SleepState.REM to 0)

        for (i in 1 until 49)
        {
            val data = dataUnPred[i]

            var lastTimestamp = 0
            var holdTime = 15*60
            var lastTimestampWakeup = 0

            var lastCall = 0

            for (j in 1 until (data.count() * (1)).toInt()) {

                val rawdata = data[j]
                rawdata.sleepState = SleepState.NONE
                rawdata.oldSleepState = SleepState.NONE
                // insert the sleep api data
                sleepDbRepository.insertSleepApiRawData(rawdata)

                val actualTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(lastTimestamp.toLong() * 1000),
                    ZoneOffset.systemDefault()
                )

                if(1615094160 < rawdata.timestampSeconds){
                    val a = 1
                    val b = rawdata
                }


                // only call it every 15 minutes or like so
                if (lastTimestamp + holdTime < rawdata.timestampSeconds) {

                    lastTimestamp = rawdata.timestampSeconds
                    // call the sleep calc handler...

                    sleepCalculationHandler.checkIsUserSleeping(actualTime)
                    //lastCall = rawdata.timestampSeconds
                }

                if (j > data.count() * 0.25f && lastTimestampWakeup + holdTime < rawdata.timestampSeconds) {

                    // letzter aufruf der verheizten zeit
                    lastTimestampWakeup = rawdata.timestampSeconds
                    sleepCalculationHandler.defineUserWakeup(actualTime)
                    lastCall = rawdata.timestampSeconds

                }
            }

            val actualTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(lastTimestamp.toLong() * 1000),
                ZoneOffset.systemDefault()
            )

            val newlist = sleepDbRepository.getSleepApiRawDataFromDate(actualTime).first()

            if (newlist != null) {
                if(newlist.count() != dataTrue[i].count()){
                    if (newlist != null) {
                        for (j in 1 until (newlist.count() * (1)).toInt()) {

                            val actData = dataTrue[i].firstOrNull{x-> x.timestampSeconds == newlist[j].timestampSeconds}

                            try {
                                val realSleepState = when (actData?.real) {
                                    "sleeping" -> listOf(SleepState.SLEEPING)
                                    "awake" -> listOf(SleepState.AWAKE)
                                    "light" -> listOf(SleepState.LIGHT)
                                    "deep" -> listOf(SleepState.DEEP)
                                    "rem" -> listOf(SleepState.REM)
                                    else -> listOf(SleepState.NONE)
                                }

                                realSleepState.forEach { state ->

                                    when (state) {
                                        SleepState.AWAKE -> awakeState[newlist[j].sleepState] =
                                            awakeState[newlist[j].sleepState]!! + 1
                                        SleepState.SLEEPING -> sleepingState[newlist[j].sleepState] =
                                            sleepingState[newlist[j].sleepState]!! + 1
                                        SleepState.LIGHT -> {
                                            lightState[newlist[j].sleepState] =
                                                lightState[newlist[j].sleepState]!! + 1
                                            sleepingState[newlist[j].sleepState] =
                                                sleepingState[newlist[j].sleepState]!! + 1
                                        }
                                        SleepState.DEEP -> {
                                            deepState[newlist[j].sleepState] =
                                                deepState[newlist[j].sleepState]!! + 1
                                            sleepingState[newlist[j].sleepState] =
                                                sleepingState[newlist[j].sleepState]!! + 1
                                        }
                                        SleepState.REM -> {
                                            remState[newlist[j].sleepState] =
                                                remState[newlist[j].sleepState]!! + 1
                                            sleepingState[newlist[j].sleepState] =
                                                sleepingState[newlist[j].sleepState]!! + 1
                                        }
                                    }

                                    realCounts[state] = realCounts[state]!! + 1
                                    predCounts[newlist[j].sleepState] =
                                        predCounts[newlist[j].sleepState]!! + 1
                                }
                            } catch (e: Exception) {
                                val d = 2
                                val c = d
                                val ee = e
                                //you can have multiple catch blocks
                                //code to handle if this exception is occurred
                            }
                        }
                    }
                } else {
                    if (newlist != null) {
                        for (j in 1 until (newlist.count() * (1)).toInt()) {
                            try {
                                val realSleepState = when (dataTrue[i][j].real) {
                                    "sleeping" -> listOf(SleepState.SLEEPING)
                                    "awake" -> listOf(SleepState.AWAKE)
                                    "light" -> listOf(SleepState.LIGHT)
                                    "deep" -> listOf(SleepState.DEEP)
                                    "rem" -> listOf(SleepState.REM)
                                    else -> listOf(SleepState.NONE)
                                }

                                realSleepState.forEach { state ->

                                    when (state) {
                                        SleepState.AWAKE -> awakeState[newlist[j].sleepState] =
                                            awakeState[newlist[j].sleepState]!! + 1
                                        SleepState.SLEEPING -> sleepingState[newlist[j].sleepState] =
                                            sleepingState[newlist[j].sleepState]!! + 1
                                        SleepState.LIGHT -> {
                                            lightState[newlist[j].sleepState] =
                                                lightState[newlist[j].sleepState]!! + 1
                                            sleepingState[newlist[j].sleepState] =
                                                sleepingState[newlist[j].sleepState]!! + 1
                                        }
                                        SleepState.DEEP -> {
                                            deepState[newlist[j].sleepState] =
                                                deepState[newlist[j].sleepState]!! + 1
                                            sleepingState[newlist[j].sleepState] =
                                                sleepingState[newlist[j].sleepState]!! + 1
                                        }
                                        SleepState.REM -> {
                                            remState[newlist[j].sleepState] =
                                                remState[newlist[j].sleepState]!! + 1
                                            sleepingState[newlist[j].sleepState] =
                                                sleepingState[newlist[j].sleepState]!! + 1
                                        }
                                    }

                                    realCounts[state] = realCounts[state]!! + 1
                                    predCounts[newlist[j].sleepState] =
                                        predCounts[newlist[j].sleepState]!! + 1
                                }
                            } catch (e: Exception) {
                                val d = 2
                                val c = d
                                val ee = e
                                //you can have multiple catch blocks
                                //code to handle if this exception is occurred
                            }
                        }
                    }
                }
            }
        }

        var a = awakeState
        var b = lightState
        var c = deepState
        var d = remState
        var e = sleepingState
        var f = predCounts
        var g = realCounts

        var TAG = "msg:"

        Log.v(TAG, "Awake, Awake:" + awakeState[SleepState.AWAKE])
        Log.v(TAG, "Awake, Sleep:" + awakeState[SleepState.SLEEPING])
        Log.v(TAG, "Awake, Light:" + awakeState[SleepState.LIGHT])
        Log.v(TAG, "Awake, Deep:" + awakeState[SleepState.DEEP])
        Log.v(TAG, "Awake, REM:" + awakeState[SleepState.REM])
        Log.v(TAG, "Sleep, Awake:" + sleepingState[SleepState.AWAKE])
        Log.v(TAG, "Sleep, Sleep:" + sleepingState[SleepState.SLEEPING])
        Log.v(TAG, "Sleep, Light:" + sleepingState[SleepState.LIGHT])
        Log.v(TAG, "Sleep, Deep:" + sleepingState[SleepState.DEEP])
        Log.v(TAG, "Sleep, REM:" + sleepingState[SleepState.REM])
        Log.v(TAG, "Light, Light:" + lightState[SleepState.LIGHT])
        Log.v(TAG, "Light, Deep:" + lightState[SleepState.DEEP])
        Log.v(TAG, "Light, REM:" + lightState[SleepState.REM])
        Log.v(TAG, "Deep, Light:" + deepState[SleepState.LIGHT])
        Log.v(TAG, "Deep, Deep:" + deepState[SleepState.DEEP])
        Log.v(TAG, "Deep, REM:" + deepState[SleepState.REM])
        Log.v(TAG, "REM, Light:" + remState[SleepState.LIGHT])
        Log.v(TAG, "REM, Deep:" + remState[SleepState.DEEP])
        Log.v(TAG, "REM, REM:" + remState[SleepState.REM])

    }

    /**
     * We recalculate the sleep api data on a specific date
     */
    @Test
    fun sleepCalculationRecaulculateLastSession() = runBlocking {

        var sleepCalculationHandler = SleepCalculationHandler.getHandler(context)


        val sessions  = sleepDbRepository.allUserSleepSessions.first()

        val session = sessions.maxByOrNull { x -> x.id }!!

        val sleepApiRawDataEntityList = sleepDbRepository.getSleepApiRawDataBetweenTimestamps(session.id, session.sleepTimes.sleepTimeEnd).first()

        sleepApiRawDataEntityList?.forEach { data ->
            data.oldSleepState = SleepState.NONE
            data.sleepState = SleepState.NONE

            sleepDbRepository.insertSleepApiRawData(data)
        }

        sleepDbRepository.deleteUserSleepSession(session)

        val newNow = LocalDateTime.ofInstant(Instant.ofEpochMilli((session.id.toLong() * 1000).toLong()), ZoneOffset.UTC)

        sleepCalculationHandler.checkIsUserSleeping(newNow)
        sleepCalculationHandler.checkIsUserSleeping(newNow)

        sleepCalculationHandler.defineUserWakeup(newNow)

    }

    /**
     * We recalculate the sleep api data on a specific date
     */
    @Test
    fun sleepCalculationRecaulculateLastData() = runBlocking {

        var sleepCalculationHandler = SleepCalculationHandler.getHandler(context)

        val day = LocalDateTime.now().minusDays(0)
        val sleepApiRawDataEntityList = sleepDbRepository.getSleepApiRawDataFromDate(day).first()

        sleepApiRawDataEntityList?.forEach { data ->
            data.oldSleepState = SleepState.NONE
            data.sleepState = SleepState.NONE

            sleepDbRepository.insertSleepApiRawData(data)
        }

        sleepCalculationHandler.checkIsUserSleeping(day)
        sleepCalculationHandler.checkIsUserSleeping(day)

        sleepCalculationHandler.defineUserWakeup(day)

    }
}
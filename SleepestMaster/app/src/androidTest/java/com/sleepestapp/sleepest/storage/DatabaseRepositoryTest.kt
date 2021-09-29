package com.sleepestapp.sleepest.storage

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.sleepestapp.sleepest.model.data.SleepState
import com.sleepestapp.sleepest.sleepcalculation.model.SleepTimes
import com.sleepestapp.sleepest.storage.db.*
import com.google.gson.Gson
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.BufferedReader
import java.time.LocalDate

class DatabaseRepositoryTest {

    private lateinit var context: Context

    private lateinit var sleepDatabaseRepository: DatabaseRepository

    private val dbDatabase by lazy {
        SleepDatabase.getDatabase(context)
    }

    private val sleepStoreRepository by lazy {
        DataStoreRepository.getRepo(context)
    }

    @Before
    fun init() {
        context = InstrumentationRegistry.getInstrumentation().targetContext


        sleepDatabaseRepository = DatabaseRepository.getRepo(
            dbDatabase.sleepApiRawDataDao(),
            dbDatabase.userSleepSessionDao(),
            dbDatabase.alarmDao(),
            dbDatabase.activityApiRawDataDao()
        )
    }

    @Test
    fun insertSleepData() = runBlocking {

        sleepDatabaseRepository.deleteAllUserSleepSessions(

        )

        // load all data
        val pathTrue = "databases/testdata/SleepValuesTrue.json"

        val gson = Gson()

        val jsonFileTrue = context
                .assets
                .open(pathTrue)
                .bufferedReader()
                .use(BufferedReader::readText)

        val dataTrue =  gson.fromJson(jsonFileTrue, Array<Array<SleepApiRawDataRealEntity>>::class.java).asList()



        for(i in 45..65) {


            if (dataTrue[i].isNullOrEmpty()) {
                continue
            }

            val startTime = dataTrue[i].filter { x -> x.real != "awake" }.minByOrNull { y -> y.timestampSeconds }!!.timestampSeconds
            val endTimeTime = dataTrue[i].filter { x -> x.real != "awake" }.maxByOrNull { y -> y.timestampSeconds }!!.timestampSeconds


            val rawDataList = mutableListOf<SleepApiRawDataEntity>()

            dataTrue[i].forEach{
                singledata->

                var sleepstate = SleepState.NONE

                when (singledata.real) {
                    "awake" -> {
                        sleepstate = SleepState.AWAKE
                    }
                    "sleeping" -> {
                        sleepstate = SleepState.SLEEPING
                    }
                    "deep" -> {
                        sleepstate = SleepState.DEEP
                    }
                    "light" -> {
                        sleepstate = SleepState.LIGHT
                    }
                    "rem" -> {
                        sleepstate = SleepState.REM
                    }
                }


                val rawData = SleepApiRawDataEntity(singledata.timestampSeconds, singledata.confidence, singledata.motion, singledata.light, sleepstate)

                rawDataList.add(rawData)
                sleepDatabaseRepository.insertSleepApiRawData(rawData)
            }

            val sleepTime = SleepTimes(sleepTimeStart = startTime, sleepTimeEnd = endTimeTime)
            sleepTime.awakeTime = SleepApiRawDataEntity.getAwakeTime(rawDataList)
            sleepTime.lightSleepDuration = SleepApiRawDataEntity.getSleepTimeByState(rawDataList, SleepState.LIGHT)
            sleepTime.deepSleepDuration = SleepApiRawDataEntity.getSleepTimeByState(rawDataList, SleepState.DEEP)
            sleepTime.remSleepDuration = SleepApiRawDataEntity.getSleepTimeByState(rawDataList, SleepState.REM)
            sleepTime.sleepDuration = SleepApiRawDataEntity.getSleepTime(rawDataList)
            val id = UserSleepSessionEntity.getIdByTimeStamp(startTime)
            val session = UserSleepSessionEntity(id, sleepTimes = sleepTime)
            sleepDatabaseRepository.insertUserSleepSession(session)

        }

        var a = 1
    }
}


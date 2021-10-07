package com.sleepestapp.sleepest.storage

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.sleepestapp.sleepest.storage.db.SleepDatabase
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil.getActualAlarmTimeData
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

class DataStoreRepositoryTest{

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
    fun isInSleepTimeTest() = runBlocking {

        var startTime = LocalTime.of(22,0,0)
        var endTime = LocalTime.of(10,0,0)


        sleepStoreRepository.updateSleepTimeStart(startTime.toSecondOfDay())
        sleepStoreRepository.updateSleepTimeEnd(endTime.toSecondOfDay())

        var actualTime = LocalTime.of(5,0,0)
        var result = getActualAlarmTimeData(sleepStoreRepository, actualTime).isInSleepTime
        assertThat(result, CoreMatchers.equalTo(true))

        actualTime = LocalTime.of(11,0,0)
        result = getActualAlarmTimeData(sleepStoreRepository, actualTime).isInSleepTime
        assertThat(result, CoreMatchers.equalTo(false))

        startTime = LocalTime.of(1,0,0)
        endTime = LocalTime.of(13,0,0)

        sleepStoreRepository.updateSleepTimeStart(startTime.toSecondOfDay())
        sleepStoreRepository.updateSleepTimeEnd(endTime.toSecondOfDay())

        actualTime = LocalTime.of(5,0,0)
        result = getActualAlarmTimeData(sleepStoreRepository, actualTime).isInSleepTime
        assertThat(result, CoreMatchers.equalTo(true))

        actualTime = LocalTime.of(15,0,0)
        result = getActualAlarmTimeData(sleepStoreRepository, actualTime).isInSleepTime
        assertThat(result, CoreMatchers.equalTo(false))

    }

}
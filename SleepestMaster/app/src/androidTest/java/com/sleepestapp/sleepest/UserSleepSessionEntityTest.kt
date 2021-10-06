package com.sleepestapp.sleepest

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.storage.db.SleepDatabase
import com.sleepestapp.sleepest.storage.db.UserSleepSessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

class UserSleepSessionEntityTest{

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
    fun idAndTimestampDateCreation(){

        var idDate = UserSleepSessionEntity.getIdByDateTime(LocalDate.of(2021, 6, 3))
        // 7:36
        var idStamp = UserSleepSessionEntity.getIdByTimeStamp(1622612190)
        assertThat(idDate, CoreMatchers.equalTo(idStamp))

        // 20:36
        idStamp = UserSleepSessionEntity.getIdByTimeStamp(1622650832)
        assertThat((idDate != idStamp), CoreMatchers.equalTo(true))

        idDate = UserSleepSessionEntity.getIdByDateTime(LocalDate.of(2021, 6, 4))
        assertThat(idDate, CoreMatchers.equalTo(idStamp))

    }

    /**
     * With this test we are trying to make sure that the created/saved/retrieved id's match witch sleep times and days
     */
    @Test
    fun idTimeStampSleepTimeTest() = runBlocking {

        val secondsOfDayStart = LocalTime.of(20,0).toSecondOfDay() // 20:00
        val secondsOfDayEnd = LocalTime.of(10,0).toSecondOfDay() // 10:00

        // set the sleep times
        sleepStoreRepository.updateSleepTimeEnd(secondsOfDayEnd)
        sleepStoreRepository.updateSleepTimeStart(secondsOfDayStart)

        var actualDate = LocalDate.of(2021, 9, 3)
        var actualTime = LocalTime.of(18,0)

        var actualDateTime = actualDate.atTime(actualTime)
        var actualDateTimeUtc = actualDateTime.minusDays(0).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()

        var idDate1 = UserSleepSessionEntity.getIdByDateTimeWithTimeZone(actualDate)
        var idDate2 = UserSleepSessionEntity.getIdByTimeStampWithTimeZone(actualDateTimeUtc)

        var sessionAvailable = sleepDbRepository.getSleepSessionById(idDate2).first().firstOrNull()

        var a = idDate1
        a = idDate2

        actualDate = LocalDate.of(2021, 9, 3)
        actualTime = LocalTime.of(22,0)

        actualDateTime = actualDate.atTime(actualTime)
        actualDateTimeUtc = actualDateTime.minusDays(0).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()

        idDate1 = UserSleepSessionEntity.getIdByDateTimeWithTimeZone(actualDate)
        idDate2 = UserSleepSessionEntity.getIdByTimeStampWithTimeZone(actualDateTimeUtc)

        sessionAvailable = sleepDbRepository.getSleepSessionById(idDate2).first().firstOrNull()

        a = idDate1
        a = idDate2

        actualDate = LocalDate.of(2021, 9, 4)
        actualTime = LocalTime.of(2,0)

        actualDateTime = actualDate.atTime(actualTime)
        actualDateTimeUtc = actualDateTime.minusDays(0).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()

        idDate1 = UserSleepSessionEntity.getIdByDateTimeWithTimeZone(actualDate)
        idDate2 = UserSleepSessionEntity.getIdByTimeStampWithTimeZone(actualDateTimeUtc)

        sessionAvailable = sleepDbRepository.getSleepSessionById(idDate2).first().firstOrNull()

        a = idDate1
        a = idDate2
        var b = sessionAvailable
    }
}
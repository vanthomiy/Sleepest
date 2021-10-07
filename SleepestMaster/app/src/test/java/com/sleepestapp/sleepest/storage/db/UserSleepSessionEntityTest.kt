package com.sleepestapp.sleepest.storage.db

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.storage.db.UserSleepSessionEntity.Companion.getIdByDateTimeWithTimeZone
import com.sleepestapp.sleepest.storage.db.UserSleepSessionEntity.Companion.getIdByTimeStampWithTimeZone
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
    fun idAndTimestampDateCreation() {

        var idDate = getIdByDateTimeWithTimeZone(LocalDate.of(2021, 6, 3))
        // 7:36
        var idStamp = getIdByTimeStampWithTimeZone(1622612190)
        assertThat(idDate, CoreMatchers.equalTo(idStamp))

        // 20:36
        idStamp = getIdByTimeStampWithTimeZone(1622650832)
        assertThat((idDate != idStamp), CoreMatchers.equalTo(true))

        idDate = getIdByDateTimeWithTimeZone(LocalDate.of(2021, 6, 4))
        assertThat(idDate, CoreMatchers.equalTo(idStamp))

    }

}
package com.sleepestapp.sleepest.storage.db

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
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

}
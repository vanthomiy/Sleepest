package com.doitstudio.sleepest_master.storage

import android.content.Context
import androidx.lifecycle.asLiveData
import androidx.test.platform.app.InstrumentationRegistry
import com.doitstudio.sleepest_master.storage.db.AlarmEntity
import com.doitstudio.sleepest_master.storage.db.SleepDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class DatabaseRepositoryTest {

    private lateinit var context: Context
    private lateinit var sleepDatabaseRepository: DatabaseRepository

    private val alarmLivedata by lazy { sleepDatabaseRepository.activeAlarmsFlow().asLiveData() }

    private val dbDatabase by lazy {
        SleepDatabase.getDatabase(context)
    }


    @Before
    fun init() {
        context = InstrumentationRegistry.getInstrumentation().targetContext


        sleepDatabaseRepository = DatabaseRepository.getRepo(
            dbDatabase.sleepApiRawDataDao(),
            dbDatabase.sleepDataDao(),
            dbDatabase.userSleepSessionDao(),
            dbDatabase.alarmDao(),
            dbDatabase.activityApiRawDataDao()
        )
    }

    /**
     * We test the get alarm request
     * we cannot test the observer ...
     *
     */
    @Test
    fun alarmTimeChanged() = runBlocking {

        // remove all alarms
        sleepDatabaseRepository.deleteAllAlarms()

        // call the get all alarms
        var alarms = sleepDatabaseRepository.alarmFlow.first()
        assertThat(alarms.count(), CoreMatchers.equalTo(0))

        // call the get active alarms ( in time )
        alarms = sleepDatabaseRepository.activeAlarmsFlow().first()
        assertThat(alarms.count(), CoreMatchers.equalTo(0))

        // insert one with id 1 and false
        sleepDatabaseRepository.insertAlarm(AlarmEntity(1, false))

        // call the get all alarms
        alarms = sleepDatabaseRepository.alarmFlow.first()
        assertThat(alarms.count(), CoreMatchers.equalTo(1))

        // call the get active alarms ( in time )
        alarms = sleepDatabaseRepository.activeAlarmsFlow().first()
        assertThat(alarms.count(), CoreMatchers.equalTo(0))

        // insert one with id 2 and active yesterday

        val dayofweekyesterday = LocalDate.now().minusDays(1).dayOfWeek
        val dayofweektoday = LocalDate.now().dayOfWeek
        val dayofweektomorrow = LocalDate.now().plusDays(1).dayOfWeek

        sleepDatabaseRepository.insertAlarm(
            AlarmEntity(
                2,
                true,
                activeDayOfWeek = arrayListOf<DayOfWeek>(dayofweekyesterday)
            )
        )

        // call the get all alarms
        alarms = sleepDatabaseRepository.alarmFlow.first()
        assertThat(alarms.count(), CoreMatchers.equalTo(2))

        // call the get active alarms ( in time )
        alarms = sleepDatabaseRepository.activeAlarmsFlow().first()
        assertThat(alarms.count(), CoreMatchers.equalTo(0))


        sleepDatabaseRepository.insertAlarm(
            AlarmEntity(
                3,
                true,
                activeDayOfWeek = arrayListOf<DayOfWeek>(dayofweektoday, dayofweektomorrow)
            )
        )

        // call the get all alarms
        alarms = sleepDatabaseRepository.alarmFlow.first()
        assertThat(alarms.count(), CoreMatchers.equalTo(3))

        // call the get active alarms ( in time )
        alarms = sleepDatabaseRepository.activeAlarmsFlow().first()
        assertThat(alarms.count(), CoreMatchers.equalTo(1))

    }
}


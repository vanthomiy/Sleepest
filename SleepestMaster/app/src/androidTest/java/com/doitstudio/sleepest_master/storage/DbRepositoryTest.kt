package com.doitstudio.sleepest_master.storage

import android.app.Instrumentation
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
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

class DbRepositoryTest {

    private lateinit var context: Context
    private lateinit var sleepDbRepository: DbRepository

    private val alarmLivedata by lazy { sleepDbRepository.activeAlarmsFlow().asLiveData() }

    private val dbDatabase by lazy {
        SleepDatabase.getDatabase(context)
    }


    @Before
    fun init() {
        context = InstrumentationRegistry.getInstrumentation().targetContext


        sleepDbRepository = DbRepository.getRepo(
            dbDatabase.sleepDataDao(),
            dbDatabase.userSleepSessionDao(),
            dbDatabase.alarmDao()
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
        sleepDbRepository.deleteAllAlarms()

        // call the get all alarms
        var alarms = sleepDbRepository.alarmFlow.first()
        assertThat(alarms.count(), CoreMatchers.equalTo(0))

        // call the get active alarms ( in time )
        alarms = sleepDbRepository.activeAlarmsFlow().first()
        assertThat(alarms.count(), CoreMatchers.equalTo(0))

        // insert one with id 1 and false
        sleepDbRepository.insertAlarm(AlarmEntity(1, false))

        // call the get all alarms
        alarms = sleepDbRepository.alarmFlow.first()
        assertThat(alarms.count(), CoreMatchers.equalTo(1))

        // call the get active alarms ( in time )
        alarms = sleepDbRepository.activeAlarmsFlow().first()
        assertThat(alarms.count(), CoreMatchers.equalTo(0))

        // insert one with id 2 and active yesterday

        val dayofweekyesterday = LocalDate.now().minusDays(1).dayOfWeek
        val dayofweektoday = LocalDate.now().dayOfWeek
        val dayofweektomorrow = LocalDate.now().plusDays(1).dayOfWeek

        sleepDbRepository.insertAlarm(
            AlarmEntity(
                2,
                true,
                activeDayOfWeek = arrayListOf<DayOfWeek>(dayofweekyesterday)
            )
        )

        // call the get all alarms
        alarms = sleepDbRepository.alarmFlow.first()
        assertThat(alarms.count(), CoreMatchers.equalTo(2))

        // call the get active alarms ( in time )
        alarms = sleepDbRepository.activeAlarmsFlow().first()
        assertThat(alarms.count(), CoreMatchers.equalTo(0))


        sleepDbRepository.insertAlarm(
            AlarmEntity(
                3,
                true,
                activeDayOfWeek = arrayListOf<DayOfWeek>(dayofweektoday, dayofweektomorrow)
            )
        )

        // call the get all alarms
        alarms = sleepDbRepository.alarmFlow.first()
        assertThat(alarms.count(), CoreMatchers.equalTo(3))

        // call the get active alarms ( in time )
        alarms = sleepDbRepository.activeAlarmsFlow().first()
        assertThat(alarms.count(), CoreMatchers.equalTo(1))

    }
}


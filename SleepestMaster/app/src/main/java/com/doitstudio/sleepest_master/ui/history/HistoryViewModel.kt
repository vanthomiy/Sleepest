package com.doitstudio.sleepest_master.ui.history

import android.app.Application
import android.app.ApplicationErrorReport
import android.view.View
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.room.Database
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.doitstudio.sleepest_master.storage.db.SleepDatabase
import com.doitstudio.sleepest_master.storage.db.SleepDatabase_Impl
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity
import com.github.mikephil.charting.charts.LineChart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val scope: CoroutineScope = MainScope()
    private val context by lazy { getApplication<Application>().applicationContext }
    private val dataBaseRepository: DatabaseRepository by lazy {
        (context as MainApplication).dataBaseRepository
    }

    val analysisDate: LocalDate = LocalDate.now()

    /** <Int: Sleep session id, Triple<List<[SleepApiRawDataEntity]>, Int: Sleep duration, [UserSleepSessionEntity]>> */
    val sleepSessionData = mutableMapOf<Int, Triple<List<SleepApiRawDataEntity>, Int, UserSleepSessionEntity>>()


    init {
        getSleepData()
    }

    private fun getSleepData() {
        val ids = mutableSetOf<Int>()
        val startDayToGet = analysisDate.minusMonths(1L).withDayOfMonth(1)
        val endDayToGet = analysisDate.withDayOfMonth(analysisDate.lengthOfMonth())
        val dayDifference = (endDayToGet.toEpochDay() - startDayToGet.toEpochDay()).toInt()

        for (day in 0..dayDifference) {
            ids.add(
                UserSleepSessionEntity.getIdByDateTime(
                    LocalDate.of(
                        startDayToGet.plusDays(day.toLong()).year,
                        startDayToGet.plusDays(day.toLong()).month,
                        startDayToGet.plusDays(day.toLong()).dayOfMonth,
                    )
                )
            )
        }

        scope.launch {
            for (id in ids) {
                val session = dataBaseRepository.getSleepSessionById(id).first().firstOrNull()
                session?.let {
                    sleepSessionData[id] = Triple(
                        dataBaseRepository.getSleepApiRawDataBetweenTimestamps(
                            session.sleepTimes.sleepTimeStart,
                            session.sleepTimes.sleepTimeEnd).first().sortedBy { x -> x.timestampSeconds },
                        session.sleepTimes.sleepDuration,
                        session
                    )
                }
            }
        }
    }

    fun checkId(time: LocalDate) : Boolean {
        return sleepSessionData.containsKey(UserSleepSessionEntity.getIdByDateTime(time))
    }
}
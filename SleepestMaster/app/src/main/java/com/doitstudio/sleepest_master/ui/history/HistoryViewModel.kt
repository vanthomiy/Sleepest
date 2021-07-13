package com.doitstudio.sleepest_master.ui.history

import android.app.Application
import android.app.ApplicationErrorReport
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.room.Database
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.storage.db.SleepDatabase
import com.doitstudio.sleepest_master.storage.db.SleepDatabase_Impl
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity
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
    private val analysisDate = LocalDate.of(2021, 7, 13)

    val activityPermissionDescription = ObservableField("View.GONE")

    init {

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
                    
                }
            }
        }

    }



    fun onClick(view: View) {
        activityPermissionDescription.set("Hi")

    }
}
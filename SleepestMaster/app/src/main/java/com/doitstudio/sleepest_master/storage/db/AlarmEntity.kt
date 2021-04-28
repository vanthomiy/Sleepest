package com.doitstudio.sleepest_master.storage.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.doitstudio.sleepest_master.model.data.sleepcalculation.SleepSegmentEntity
import com.google.android.gms.location.SleepClassifyEvent
import java.time.DayOfWeek


@Entity(tableName = "alarm_properties_table")
data class AlarmEntity(
        @PrimaryKey
        @ColumnInfo(name = "id")
        val id: Int = 0,

        @ColumnInfo(name = "isActive")
        val isActive: Boolean = false,

        @ColumnInfo(name = "sleepDuration")
        val sleepDuration: Int = 28800,

        @ColumnInfo(name = "wakeupEarly")
        val wakeupEarly: Int = 21600,

        @ColumnInfo(name = "wakeupLate")
        val wakeupLate: Int = 32400,

        @ColumnInfo(name = "activeDayOfWeek")
        val activeDayOfWeek: ArrayList<DayOfWeek> = arrayListOf(DayOfWeek.MONDAY)
)
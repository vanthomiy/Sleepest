package com.doitstudio.sleepest_master.storage.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.doitstudio.sleepest_master.model.data.SleepState
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.SleepClassifyEvent

@Entity(tableName = "activity_api_raw_data_table")
data class ActivityApiRawDataEntity(
        @PrimaryKey
        @ColumnInfo(name = "time_stamp_seconds")
        val timestampSeconds: Int,

        @ColumnInfo(name = "activity")
        var activity: Int = DetectedActivity.UNKNOWN,

        @ColumnInfo(name = "duration")
        var duration: Int = 0,
)
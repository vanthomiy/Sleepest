package com.sleepestapp.sleepest.storage.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.location.DetectedActivity

@Entity(tableName = "activity_api_raw_data_table")
data class ActivityApiRawDataEntity(
    @PrimaryKey
    @ColumnInfo(name = "time_stamp_seconds")
    val timestampSeconds: Int,

    @ColumnInfo(name = "activity")
    var activity: Int = DetectedActivity.UNKNOWN,

    @ColumnInfo(name = "transition_type")
    var transitionType: Int,

    )
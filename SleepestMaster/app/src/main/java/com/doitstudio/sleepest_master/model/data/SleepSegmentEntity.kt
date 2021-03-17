package com.doitstudio.sleepest_master.model.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity class (table version of the class) for [SleepSegmentEntity] which represents a sleep
 * classification event including the classification timestamp, the sleep confidence, and the
 * supporting data such as device motion and ambient light level. Classification events are
 * reported regularly.
 */
@Entity(tableName = "sleep_segment_table")
data class SleepSegmentEntity(
        @PrimaryKey
        @ColumnInfo(name = "time_stamp_seconds_start")
        val timestampSecondsStart: Int,

        @ColumnInfo(name = "time_stamp_seconds_end")
        val timestampSecondsEnd: Int,

        @ColumnInfo(name = "sleep_state")
        val sleepState: SleepState,
) {

}



package com.doitstudio.sleepest_master.model.data.sleepcalculation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.doitstudio.sleepest_master.model.data.SleepState

/**
 * Entity class (table version of the class) for [SleepSegmentEntity] which represents a sleep
 * segment including the start/end timestamp and the Sleep State
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
)  {

}




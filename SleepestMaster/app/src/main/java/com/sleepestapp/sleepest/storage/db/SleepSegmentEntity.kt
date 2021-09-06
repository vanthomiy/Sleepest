package com.sleepestapp.sleepest.storage.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sleepestapp.sleepest.model.data.SleepState

/**
 * Entity class (table version of the class) for [SleepSegmentEntity] which represents a sleep
 * segment including the start/end timestamp and the Sleep State
 */
@Entity(tableName = "sleep_segment_table")
data class SleepSegmentEntity(

        @PrimaryKey
        val timestampSecondsStart: Int,
        val timestampSecondsEnd: Int,
        val sleepState: SleepState
)




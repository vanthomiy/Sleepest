package com.doitstudio.sleepest_master.storage.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.location.SleepClassifyEvent

/**
 * Entity class (table version of the class) for [SleepApiRawDataEntity] which represents a sleep
 * classification event [SleepClassifyEvent] including the classification timestamp, the sleep confidence, and the
 * supporting data such as device motion and ambient light level. Classification events are
 * reported regularly.
 *
 * This is only used for storing in the SQL Database. We use other classes later for better handling.
 * E.g the [SleepSegmentEntity] for display sleep states
 */
@Entity(tableName = "sleep_api_raw_data_table")
data class SleepApiRawDataEntity(
        @PrimaryKey
        @ColumnInfo(name = "time_stamp_seconds")
        val timestampSeconds: Int,

        @ColumnInfo(name = "confidence")
        var confidence: Int,

        @ColumnInfo(name = "motion")
        val motion: Int,

        @ColumnInfo(name = "light")
        val light: Int
) {
        companion object {
                fun from(sleepClassifyEvent: SleepClassifyEvent): SleepApiRawDataEntity {
                        return SleepApiRawDataEntity(
                                timestampSeconds = (sleepClassifyEvent.timestampMillis / 1000).toInt(),
                                confidence = sleepClassifyEvent.confidence,
                                motion = sleepClassifyEvent.motion,
                                light = sleepClassifyEvent.light
                        )
                }
        }
}
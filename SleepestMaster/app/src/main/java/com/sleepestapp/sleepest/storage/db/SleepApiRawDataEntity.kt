package com.sleepestapp.sleepest.storage.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sleepestapp.sleepest.model.data.SleepState
import com.google.android.gms.location.SleepClassifyEvent

/**
 * Entity class (table version of the class) for [SleepApiRawDataEntity] which represents a sleep
 * classification event [SleepClassifyEvent] including the classification timestamp, the sleep confidence, and the
 * supporting data such as device motion and ambient light level. Classification events are
 * reported regularly.
 *
 * This is only used for storing in the SQL Database. We use other classes later for better handling.
 */
@Entity(tableName = "sleep_api_raw_data_table")
data class SleepApiRawDataEntity(
        @PrimaryKey
        @ColumnInfo(name = "time_stamp_seconds")
        val timestampSeconds: Int,

        @ColumnInfo(name = "confidence")
        var confidence: Double,

        @ColumnInfo(name = "motion")
        val motion: Double,

        @ColumnInfo(name = "light")
        val light: Double,

        @ColumnInfo(name = "sleepState")
        var sleepState: SleepState = SleepState.NONE,

        @ColumnInfo(name = "oldSleepState")
        var oldSleepState: SleepState = SleepState.NONE,

        @ColumnInfo(name = "wakeUpTime")
        var wakeUpTime: Int = 0

) {
        companion object {
                fun from(sleepClassifyEvent: SleepClassifyEvent): SleepApiRawDataEntity {
                        return SleepApiRawDataEntity(
                                timestampSeconds = (sleepClassifyEvent.timestampMillis / 1000).toInt(),
                                confidence = sleepClassifyEvent.confidence.toDouble(),
                                motion = sleepClassifyEvent.motion.toDouble(),
                                light = sleepClassifyEvent.light.toDouble(),
                                sleepState = SleepState.NONE,
                                oldSleepState = SleepState.NONE
                        )
                }

                /**
                 * Returns the count of sleep in minutes from a list without factors!
                 */
                fun getSleepTime(sleepApiRawDataEntity:List<SleepApiRawDataEntity>) : Int {
                        val sortedList = sleepApiRawDataEntity.sortedBy { x-> x.timestampSeconds }
                        var sleepCount = 0
                        for (i  in 1 until sortedList.count()){
                                sleepCount +=
                                        if(sortedList[i].sleepState == SleepState.SLEEPING ||
                                                sortedList[i].sleepState == SleepState.DEEP ||
                                                sortedList[i].sleepState == SleepState.LIGHT ||
                                                sortedList[i].sleepState == SleepState.REM) sortedList[i].timestampSeconds - sortedList[i-1].timestampSeconds else 0
                        }

                        return sleepCount / 60
                }

                /**
                 * Returns the awake time between sleep times
                 */
                fun getAwakeTime(sleepApiRawDataEntity:List<SleepApiRawDataEntity>) : Int {
                        val sortedList = sleepApiRawDataEntity.sortedBy { x-> x.timestampSeconds }
                        var isSleeping = false
                        var awakeTimePuffer = 0
                        var awakeTime = 0

                        for (i  in 1 until sortedList.count()){

                                isSleeping =   (isSleeping ||
                                                sortedList[i].sleepState == SleepState.SLEEPING ||
                                                sortedList[i].sleepState == SleepState.DEEP ||
                                                sortedList[i].sleepState == SleepState.LIGHT ||
                                                sortedList[i].sleepState == SleepState.REM)

                                awakeTimePuffer +=
                                        if(isSleeping && (sortedList[i].sleepState == SleepState.NONE || sortedList[i].sleepState == SleepState.AWAKE))
                                                sortedList[i].timestampSeconds - sortedList[i-1].timestampSeconds
                                        else 0

                                if(sortedList[i].sleepState == SleepState.SLEEPING ||
                                        sortedList[i].sleepState == SleepState.DEEP ||
                                        sortedList[i].sleepState == SleepState.LIGHT ||
                                        sortedList[i].sleepState == SleepState.REM) {
                                        awakeTime += awakeTimePuffer
                                        awakeTimePuffer = 0
                                }
                        }

                        return awakeTime / 60
                }

                /**
                 * Returns the actual awake time the user has been awake. 0 if sleeping
                 * In Seconds
                 */
                fun getActualAwakeTime(sleepApiRawDataEntity:List<SleepApiRawDataEntity>) : Int {
                        val sortedList = sleepApiRawDataEntity.sortedByDescending { x-> x.timestampSeconds }

                        if (sortedList.first().sleepState != SleepState.AWAKE)
                                return 0

                        val endTime = sortedList.first().timestampSeconds

                        for (i  in 1 until sortedList.count()){

                                if (sortedList[i].sleepState != SleepState.AWAKE) {
                                        return endTime - sortedList[i].timestampSeconds
                                }
                        }

                        return endTime - sortedList.last().timestampSeconds
                }

                /**
                 * Returns the sleep time by a sleep state by minutes
                 */
                fun getSleepTimeByState(sleepApiRawDataEntity:List<SleepApiRawDataEntity>, sleepState: SleepState) : Int {
                        val sortedList = sleepApiRawDataEntity.sortedBy { x-> x.timestampSeconds }
                        var sleepCount = 0
                        for (i  in 1 until sortedList.count()){
                                sleepCount +=
                                        if(sortedList[i].sleepState == sleepState) sortedList[i].timestampSeconds - sortedList[i-1].timestampSeconds else 0
                        }

                        return sleepCount / 60
                }

                /**
                 * Gets the first time as UTC Total seconds when a user sleep was detected
                 */
                fun getSleepStartTime(sleepApiRawDataEntity:List<SleepApiRawDataEntity>) : Int{
                        val sleepList = sleepApiRawDataEntity.filter { x-> x.sleepState != SleepState.NONE && x.sleepState != SleepState.AWAKE }

                        if(sleepList.count() == 0){
                                return 0
                        }

                        return sleepList.minByOrNull { x->x.timestampSeconds }!!.timestampSeconds
                }

                /**
                 * Gets the last time as UTC Total seconds when a user sleep was detected
                 */
                fun getSleepEndTime(sleepApiRawDataEntity:List<SleepApiRawDataEntity>) : Int{
                        val sleepList = sleepApiRawDataEntity.filter { x-> x.sleepState != SleepState.NONE && x.sleepState != SleepState.AWAKE }

                        if(sleepList.count() == 0){
                                return 0
                        }

                        return sleepList.maxByOrNull { x->x.timestampSeconds }!!.timestampSeconds
                }
        }
}

data class SleepApiRawDataRealEntity(
        val timestampSeconds: Int,

        val confidence: Double,

        val motion: Double,

        val light: Double,

        val real: String
)
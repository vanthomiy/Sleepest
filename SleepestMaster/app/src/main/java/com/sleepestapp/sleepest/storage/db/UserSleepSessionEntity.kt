package com.sleepestapp.sleepest.storage.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sleepestapp.sleepest.model.data.LightConditions
import com.sleepestapp.sleepest.model.data.MobilePosition
import com.sleepestapp.sleepest.sleepcalculation.model.SleepTimes
import com.sleepestapp.sleepest.sleepcalculation.model.UserCalculationRating
import com.sleepestapp.sleepest.sleepcalculation.model.UserSleepRating
import java.time.*

/**
 * This is the basic data class for storing the combined user sleep data for each day.
 * It contains a unique [id] with which the associated [SleepApiRawDataEntity] can be retrieved from the sql database.
 */
@Entity(tableName = "user_sleep_session_entity")
data class UserSleepSessionEntity(


        /**
         * Unique key of the data which is created of the first timestamp of the [SleepApiRawDataEntity]
         */
        @PrimaryKey
        val id:Int,

        /**
         * The actual [MobilePosition] for the sleep which was detected by the algorithm or set by the user.
         */
        var mobilePosition: MobilePosition = MobilePosition.UNIDENTIFIED,

        /**
         * The actual [LightConditions] for the sleep which was detected by the algorithm or set by the user.
         */
        var lightConditions: LightConditions = LightConditions.UNIDENTIFIED,

        @Embedded(prefix = "sleepTimes") val sleepTimes: SleepTimes = SleepTimes(),
        @Embedded(prefix = "sleepRating") val userSleepRating: UserSleepRating = UserSleepRating(),
        @Embedded(prefix = "calcRating") val userCalculationRating: UserCalculationRating = UserCalculationRating()
)
{
        companion object{

                /**
                 * Returns the id for the assigned stored data of a sleep from a local date
                 */
                fun getIdByDateTimeWithTimeZone(date : LocalDate) : Int {

                        val time = LocalTime.of(15,0)
                        val datetime = LocalDateTime.of(date.minusDays(0), time)
                        return datetime.atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
                }

                /**
                 * Returns the id for the assigned stored data of a sleep from a timestamp
                 */
                fun getIdByTimeStampWithTimeZone(timestamp: Int) : Int {

                        val actualTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp.toLong()*1000), ZoneOffset.systemDefault())

                        val date = actualTime.toLocalDate()
                        val newTime = LocalTime.of(15,0)

                        val dateTime = LocalDateTime.of(date, newTime)

                        return dateTime.atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
                }

                /**
                 * Returns the id for the assigned stored data of a sleep from a local date
                 */
                @Deprecated("This is the old version which is not Timezone sensitive", ReplaceWith("getIdByDateTimeWithTimeZone(date)"))
                fun getIdByDateTime(date : LocalDate) : Int {

                        val time = LocalTime.of(15,0)
                        val datetime = LocalDateTime.of(date.minusDays(0), time)
                        return datetime.atZone(ZoneOffset.UTC).toEpochSecond().toInt()
                }

                /**
                 * Returns the id for the assigned stored data of a sleep from a timestamp
                 */
                @Deprecated("This is the old version which is not Timezone sensitive", ReplaceWith("getIdByTimeStampWithTimeZone(timestamp)"))
                fun getIdByTimeStamp(timestamp: Int) : Int {

                        val actualTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp.toLong()*1000), ZoneOffset.UTC)

                        val date = actualTime.toLocalDate()
                        val newTime = LocalTime.of(15,0)

                        val dateTime = LocalDateTime.of(date, newTime)

                        return dateTime.atZone(ZoneOffset.UTC).toEpochSecond().toInt()
                }


        }
}




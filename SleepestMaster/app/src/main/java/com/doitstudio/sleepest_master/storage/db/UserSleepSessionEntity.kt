package com.doitstudio.sleepest_master.storage.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.sleepcalculation.model.SleepTimes
import com.doitstudio.sleepest_master.sleepcalculation.model.UserCalculationRating
import com.doitstudio.sleepest_master.sleepcalculation.model.UserSleepRating
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


        @Embedded(prefix = "sleepTimes") val sleepTimes: SleepTimes = SleepTimes(),
        @Embedded(prefix = "sleepRating") val userSleepRating: UserSleepRating = UserSleepRating(),
        @Embedded(prefix = "calcRating") val userCalculationRating: UserCalculationRating = UserCalculationRating()
)
{
        companion object{

                /**
                 * Returns the id for the assigned stored data of a sleep from a local date
                 */
                fun getIdByDateTime(date : LocalDate) : Int {

                        var time = LocalTime.of(15,0)
                        val datetime = LocalDateTime.of(date.minusDays(1), time)
                        return datetime.toEpochSecond(ZoneOffset.UTC).toInt()
                }

                /**
                 * Returns the id for the assigned stored data of a sleep from a timestamp
                 */
                fun getIdByTimeStamp(timestamp: Int) : Int {

                        var actualTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp.toLong()*1000), ZoneOffset.UTC)

                        /*if(actualTime.hour >= 15){
                                actualTime = actualTime.plusDays(1)
                        }*/

                        val date = actualTime.toLocalDate()
                        var newTime = LocalTime.of(15,0)

                        var dateTime = LocalDateTime.of(date, newTime)
                        return dateTime.toEpochSecond(ZoneOffset.UTC).toInt()
                }


        }
}




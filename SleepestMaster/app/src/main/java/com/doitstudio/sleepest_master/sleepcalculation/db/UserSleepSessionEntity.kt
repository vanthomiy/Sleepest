package com.doitstudio.sleepest_master.sleepcalculation.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepTimes
import com.doitstudio.sleepest_master.sleepcalculation.model.userimprovement.UserCalculationRating
import com.doitstudio.sleepest_master.sleepcalculation.model.userimprovement.UserSleepRating
import java.time.*


@Entity(tableName = "user_sleep_session_entity")
data class UserSleepSessionEntity(



        @PrimaryKey
        val id:Int,

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
                fun getIdByDateTime(time : LocalDate) : Int {

                        var newDate = LocalDate.of(time.year, time.month, time.dayOfMonth)
                        var time = LocalTime.of(15,0)
                        val datetime = LocalDateTime.of(newDate, time)
                        return datetime.toEpochSecond(ZoneOffset.UTC).toInt()
                }

                /**
                 * Returns the id for the assigned stored data of a sleep from a timestamp
                 */
                fun getIdByTimeStamp(timestamp: Int) : Int {

                        val actualTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp.toLong()*1000), ZoneOffset.UTC)

                        if(actualTime.hour >= 15){
                                actualTime.plusDays(1)
                        }

                        val date = actualTime.toLocalDate()
                        var newTime = LocalTime.of(15,0)

                        var dateTime = LocalDateTime.of(date, newTime)
                        return dateTime.toEpochSecond(ZoneOffset.UTC).toInt()
                }


        }
}




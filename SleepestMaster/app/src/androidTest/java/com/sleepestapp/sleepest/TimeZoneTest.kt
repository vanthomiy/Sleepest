package com.sleepestapp.sleepest

import android.util.Log
import org.junit.Test
import java.time.*


class TimeZoneTest {
    @Test
    fun checkTimeZone() {

        val date = LocalDate.now().minusDays(10)
        val time = LocalTime.of(15,0)
        val datetime = LocalDateTime.of(date.minusDays(0), time)
        val seconds = datetime.atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()

        val tag = "msg:"

        Log.v(tag, "Day:" + datetime.dayOfMonth)
        Log.v(tag, "Hour:" + datetime.hour)
        Log.v(tag, "Minute:" + datetime.minute)
        Log.v(tag, "Seconds:" + seconds)

        val datetimeUTC = LocalDateTime.of(date.minusDays(0), time)
        val secondsUTC = datetimeUTC.atZone(ZoneOffset.UTC).toEpochSecond().toInt()

        Log.v(tag, "Day:" + datetimeUTC.dayOfMonth)
        Log.v(tag, "Hour:" + datetimeUTC.hour)
        Log.v(tag, "Minute:" + datetimeUTC.minute)
        Log.v(tag, "Seconds:" + secondsUTC)

    }
}
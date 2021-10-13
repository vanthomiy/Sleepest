package com.sleepestapp.sleepest.util;

import java.time.LocalTime;
import java.util.Calendar;

/**
 * Util to convert times to other formats
 */
public class TimeConverterUtil {

    /**
     * Minute to hour and minute format
     * @param minute minutes
     * @return formatted time in array
     */
    public static int[] minuteToTimeFormat(int minute) {
        int[] time = new int[2];
        int rest = minute % 60;
        time[0] = minute / 60;
        time[1] = rest;

        return time;
    }

    /**
     * Seconds to hour and minute format
     * @param seconds seconds
     * @return formatted time in array
     */
    public static int[] millisToTimeFormat(int seconds) {
        int[] time = new int[2];
        int rest = seconds % 3600;
        time[0] = seconds / 3600;
        time[1] = rest / 60;

        return time;
    }

    /**
     * Time to formatted time string for number picker dialog
     * @param hour hour
     * @param minute minute
     * @return formatted String
     */
    public static String toTimeFormat(int hour, int minute) {

        String hourText, minuteText;

        if(hour < 10) {
            hourText = "0" + hour;
        } else {
            hourText = Integer.toString(hour);
        }

        if(minute < 10) {
            minuteText = "0" + minute;
        } else {
            minuteText = Integer.toString(minute);
        }

        return hourText + ":" + minuteText;

    }

    /**
     * Calculates the possible next date of calendar
     * @param day Number between 1 and 14, 1 = Sunday, 7 = Saturday, 8 = On Saturday + 1 = Sunday, ...
     * @param hour Number between 0 and 23
     * @param minute Number between 0 and 59
     * @return Instance of calculated calendar
     */
    public static Calendar getAlarmDate(int day, int hour, int minute) {

        int actualDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.SECOND, 0);

        if (day > Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
        {
            actualDay += day - Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        } else if (day < Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            actualDay += 7 - Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + day;
        } else {
            if (calendar.before(Calendar.getInstance())) {
                actualDay += 7;
            }
        }

        if (actualDay > Calendar.getInstance().getMaximum(Calendar.DAY_OF_YEAR)) {
            actualDay = Calendar.getInstance().getMinimum(Calendar.DAY_OF_YEAR) + Calendar.getInstance().getMaximum(Calendar.DAY_OF_YEAR) - actualDay;
            calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) + 1);
        }

        calendar.set(Calendar.DAY_OF_YEAR, actualDay);

        return calendar;
    }

    /**
     * Calculates the possible next date of calendar
     * @param secondsOfDay The seconds since midnight
     * @return instance of calculated calendar
     */
    public static Calendar getAlarmDate(int secondsOfDay) {

        int actualDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.SECOND, secondsOfDay);

        int day = calendar.get(Calendar.DAY_OF_WEEK);

        LocalTime time = LocalTime.now();

        if (time.toSecondOfDay() >= secondsOfDay) {
            day += 1;
        }

        if (day > Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
        {
            actualDay += day - Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        } else if (day < Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            actualDay += 7 - Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + day;
        } else {
            if (calendar.before(Calendar.getInstance())) {
                actualDay += 7;
            }
        }

        if (actualDay > Calendar.getInstance().getMaximum(Calendar.DAY_OF_YEAR)) {
            actualDay = Calendar.getInstance().getMinimum(Calendar.DAY_OF_YEAR) + Calendar.getInstance().getMaximum(Calendar.DAY_OF_YEAR) - actualDay;
            calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) + 1);
        }

        calendar.set(Calendar.DAY_OF_YEAR, actualDay);

        return calendar;
    }
}

package com.doitstudio.sleepest_master.util;

import java.time.LocalTime;
import java.util.Calendar;

public class TimeConverterUtil {

    public static int[] minuteToTimeFormat(int minute) {
        int[] time = new int[2];
        int rest = minute % 60;
        time[0] = minute / 60;
        time[1] = rest;

        return time;
    }

    public static int[] millisToTimeFormat(int milliseconds) {
        int[] time = new int[2];
        int rest = milliseconds % 3600;
        time[0] = milliseconds / 3600;
        time[1] = rest / 60;

        return time;
    }

    public static String toTimeFormat(int hour, int minute) {

        String hourText = null, minuteText = null;

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
     * Convert the date to secondsOfDay
     * @param calendar date
     * @return secondsOfDay
     */
    public static int calendarToSecondsOfDay(Calendar calendar) {
        int day = calendar.get(Calendar.HOUR_OF_DAY) * 3600;
        int minute = calendar.get(Calendar.MINUTE) * 60;
        int second = calendar.get(Calendar.SECOND);

        return day + minute + second;
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

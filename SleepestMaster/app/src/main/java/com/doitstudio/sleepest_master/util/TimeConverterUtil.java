package com.doitstudio.sleepest_master.util;

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
}

package com.doitstudio.sleepest_master.background;

public class Times {

    private final int startForegroundHour = 20;
    private final int startForegroundMinute = 0;

    private final int sleepTime = 27000; //User Sleep time in seconds

    private final int firstWakeupHour = 7;
    private final int firstWakeupMinute = 0;

    private final int firstWakeupInSeconds = 25200;

    private final int lastWakeupHour = 9;
    private final int lastWakeupMinute = 0;

    private final int firstCalculationHour = 5;
    private final int firstCalculationMinute = 30;

    private final int workmanagerDuration = 16; //LiveSleepData in minutes

    private final int workmanagerCalculationDuration = 16; //Zeitabstände der Kalkulation morgens

    public int getStartForegroundHour() {
        return startForegroundHour;
    }

    public int getStartForegroundMinute() {
        return startForegroundMinute;
    }

    public int getSleepTime() {
        return sleepTime;
    }

    public int getFirstWakeupHour() {
        return firstWakeupHour;
    }

    public int getFirstWakeupMinute() {
        return firstWakeupMinute;
    }

    public int getLastWakeupHour() {
        return lastWakeupHour;
    }

    public int getLastWakeupMinute() {
        return lastWakeupMinute;
    }

    public int getFirstCalculationHour() {
        return firstCalculationHour;
    }

    public int getFirstCalculationMinute() {
        return firstCalculationMinute;
    }

    public int getWorkmanagerDuration() {
        return workmanagerDuration;
    }

    public int getWorkmanagerCalculationDuration() {
        return workmanagerCalculationDuration;
    }

    public int getFirstWakeupInSeconds() {
        return firstWakeupInSeconds;
    }

    public int getStartForegroundInSeconds() {
        return (startForegroundHour * 3600 + startForegroundMinute * 60);
    }
}

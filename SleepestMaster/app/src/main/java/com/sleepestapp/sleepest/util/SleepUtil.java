package com.sleepestapp.sleepest.util;

import android.content.Context;
import android.os.PowerManager;

import com.sleepestapp.sleepest.MainApplication;
import com.sleepestapp.sleepest.model.data.Constants;
import com.sleepestapp.sleepest.storage.DataStoreRepository;
import com.sleepestapp.sleepest.storage.DatabaseRepository;

import java.time.LocalTime;

public class SleepUtil {

    /**
     * Check if time from now to last wakeup is less than the sleep time set
     * @return is sleep time possible
     */
    public static boolean checkSleepTimeReachingPossibility(Context context) {

        //Get PowerManager instance to check if screen is on
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        int difference;

        //Get instance of database and datastore
        DatabaseRepository databaseRepository = ((MainApplication)context.getApplicationContext()).getDataBaseRepository();
        DataStoreRepository dataStoreRepository = DataStoreRepository.Companion.getRepo(context.getApplicationContext());

        //Check if AlarmEntity is not null and the user is interacting on device
        if ((databaseRepository.getNextActiveAlarmJob() != null) && powerManager.isInteractive()) {

            //Check if midnight was reached or not for calculating time difference
            if (databaseRepository.getNextActiveAlarmJob().getWakeupEarly() >= dataStoreRepository.getSleepTimeBeginJob()) {
                difference = databaseRepository.getNextActiveAlarmJob().getWakeupLate() - LocalTime.now().toSecondOfDay();
            } else {
                difference = Constants.DAY_IN_SECONDS - LocalTime.now().toSecondOfDay() + databaseRepository.getNextActiveAlarmJob().getWakeupLate();
            }
        } else {
            return false;
        }

        //Check if difference is less then sleep time
        return difference <= databaseRepository.getNextActiveAlarmJob().getSleepDuration();
    }
}

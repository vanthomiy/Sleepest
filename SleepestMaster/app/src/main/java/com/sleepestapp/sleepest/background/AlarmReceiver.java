package com.sleepestapp.sleepest.background;


import android.app.AlarmManager;
import android.app.NotificationManager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.sleepestapp.sleepest.MainApplication;
import com.sleepestapp.sleepest.R;
import com.sleepestapp.sleepest.model.data.AlarmReceiverUsage;
import com.sleepestapp.sleepest.model.data.Constants;
import com.sleepestapp.sleepest.model.data.NotificationUsage;
import com.sleepestapp.sleepest.sleepcalculation.SleepCalculationHandler;
import com.sleepestapp.sleepest.storage.DataStoreRepository;
import com.sleepestapp.sleepest.storage.DatabaseRepository;
import com.sleepestapp.sleepest.storage.db.AlarmEntity;
import com.sleepestapp.sleepest.util.TimeConverterUtil;
//import com.sleepestapp.sleepest.sleepcalculation.SleepCalculationHandler;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;
/**This class inherits from Broadcastreceiver and starts an alarm at a specific time and date**/

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //Init repos
        DatabaseRepository databaseRepository = ((MainApplication)context.getApplicationContext()).getDataBaseRepository();
        DataStoreRepository dataStoreRepository = ((MainApplication)context.getApplicationContext()).getDataStoreRepository();
        SleepCalculationHandler sleepCalculationHandler = new SleepCalculationHandler(MainApplication.Companion.applicationContext());

        switch (AlarmReceiverUsage.valueOf(intent.getStringExtra((context.getString(R.string.alarmmanager_key))))) {
            case START_FOREGROUND:
                //Starts the cycle of receiving API data and setting alarms
                BackgroundAlarmTimeHandler.Companion.getHandler(context.getApplicationContext()).beginOfSleepTime(true);
                break;
            case STOP_FOREGROUND:
                //Stops the foregroundservice after a sleep session or if sleep time changes to out of sleep time
                BackgroundAlarmTimeHandler.Companion.getHandler(context.getApplicationContext()).stopForegroundService();
                break;
            case DISABLE_ALARM:
                    //Disables the next active alarm temporary
                AlarmEntity nextAlarm = databaseRepository.getNextActiveAlarmJob(dataStoreRepository);
                BackgroundAlarmTimeHandler.Companion.getHandler(context.getApplicationContext()).disableAlarmTemporaryInApp(false, nextAlarm == null || (nextAlarm.getTempDisabled()));
                break;
            case NOT_SLEEPING:
                //Button not Sleeping, only in the first 2 hours of sleep
                sleepCalculationHandler.userNotSleepingJob();
                Toast.makeText(context.getApplicationContext(), context.getApplicationContext().getString(R.string.not_sleeping_message), Toast.LENGTH_LONG).show();
                break;
            case START_WORKMANAGER_CALCULATION:
                //Start the workmanager for the calculation of the sleep
                WorkmanagerCalculation.startPeriodicWorkmanager(Constants.WORK_MANAGER_CALCULATION_DURATION, context.getApplicationContext());
                break;
            case START_WORKMANAGER:
                //In the moment unused, but could be interesting for the future development
                break;
            case STOP_WORKMANAGER:
                //Stops the workmanager outside sleep time
                /*if (databaseRepository.getNextActiveAlarmJob(dataStoreRepository) != null && !databaseRepository.getNextActiveAlarmJob(dataStoreRepository).getWasFired()) {
                    Calendar calendarNewAlarm = TimeConverterUtil.getAlarmDate(LocalTime.now().toSecondOfDay() + 600);
                    AlarmReceiver.startAlarmManager(
                            calendarNewAlarm.get(Calendar.DAY_OF_WEEK),
                            calendarNewAlarm.get(Calendar.HOUR_OF_DAY),
                            calendarNewAlarm.get(Calendar.MINUTE), context, AlarmReceiverUsage.STOP_WORKMANAGER);
                } else {*/
                    BackgroundAlarmTimeHandler.Companion.getHandler(context.getApplicationContext()).endOfSleepTime(true);
               // }

                 break;
            case CURRENTLY_NOT_SLEEPING:
                //Button currently not sleeping, only after 2 hours of sleep
                sleepCalculationHandler.userCurrentlyNotSleepingJob();
                Toast.makeText(context.getApplicationContext(), context.getApplicationContext().getString(R.string.currently_not_sleeping_message), Toast.LENGTH_LONG).show();
                break;
            case SOLVE_API_PROBLEM:
                //Restarts the subscribing of data in case of an receiving error
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.toast_restarted_sleep_data), Toast.LENGTH_LONG).show();
                BackgroundAlarmTimeHandler.Companion.getHandler(context.getApplicationContext()).startWorkmanager();
                NotificationManager notificationManagerApi = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManagerApi.cancel(NotificationUsage.Companion.getCount(NotificationUsage.NOTIFICATION_NO_API_DATA));
            case GO_TO_SLEEP:
                //After clearing notification to inform the user that he should sleep now.
                Toast.makeText(context.getApplicationContext(), context.getApplicationContext().getString(R.string.alarm_clock_user_should_sleep), Toast.LENGTH_LONG).show();
                NotificationManager notificationManagerGoSleep = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManagerGoSleep.cancel(NotificationUsage.Companion.getCount(NotificationUsage.NOTIFICATION_USER_SHOULD_SLEEP));
        }
    }

    /**
     * Start a alarm at a specific time.
     * @param day Number from 1-7, Sunday=1, Saturday=7
     * @param hour Hour from 0-23
     * @param min Minute from 0-59
     * @param alarmContext Application Context
     */
    public static void startAlarmManager(int day, int hour, int min, Context alarmContext, AlarmReceiverUsage alarmReceiverUsage) {

        //Get an instance of calendar and set time, when alarm should be fired
        Calendar calenderAlarm = TimeConverterUtil.getAlarmDate(day, hour, min);

        Intent intent = new Intent(alarmContext, AlarmReceiver.class);
        intent.putExtra(alarmContext.getString(R.string.alarmmanager_key), alarmReceiverUsage.name());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(alarmContext, AlarmReceiverUsage.Companion.getCount(alarmReceiverUsage), intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) alarmContext.getSystemService(ALARM_SERVICE);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calenderAlarm.getTimeInMillis(), pendingIntent);
    }

    /**
     * Cancel a specific alarm by pending intent
     * @param cancelAlarmContext Application Context
     */
    public static void cancelAlarm(Context cancelAlarmContext, AlarmReceiverUsage alarmReceiverUsage) {

        Intent intent = new Intent(cancelAlarmContext, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(cancelAlarmContext, AlarmReceiverUsage.Companion.getCount(alarmReceiverUsage), intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) cancelAlarmContext.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }


}

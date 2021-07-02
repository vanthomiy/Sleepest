package com.doitstudio.sleepest_master.background;

/**This class inherits from Broadcastreceiver and starts an alarm at a specific time and date*/

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.doitstudio.sleepest_master.MainActivity;
import com.doitstudio.sleepest_master.MainApplication;
import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.alarmclock.AlarmClockReceiver;
import com.doitstudio.sleepest_master.model.data.Actions;
import com.doitstudio.sleepest_master.model.data.AlarmReceiverUsage;
import com.doitstudio.sleepest_master.model.data.SleepState;
import com.doitstudio.sleepest_master.sleepapi.SleepHandler;
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler;
import com.doitstudio.sleepest_master.storage.DataStoreRepository;
//import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import kotlin.Unit;
import kotlin.coroutines.Continuation;

import static android.content.Context.ALARM_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // New from Thomas: With other call...
        DataStoreRepository dataStoreRepository = DataStoreRepository.Companion.getRepo(context);
        //DataStoreRepository dataStoreRepository = MainApplication.class.cast(context).getDataStoreRepository();
        //Activity activity = (Activity) context;
        //DataStoreRepository dataStoreRepository = ((MainApplication)activity.getApplication()).getDataStoreRepository();


        SleepHandler sleepHandler = SleepHandler.Companion.getHandler(MainApplication.Companion.applicationContext());
        SleepCalculationHandler sleepCalculationHandler = SleepCalculationHandler.Companion.getHandler(MainApplication.Companion.applicationContext());

        Calendar calendar = Calendar.getInstance();
        SharedPreferences pref = context.getSharedPreferences("AlarmReceiver", 0);
        SharedPreferences.Editor ed = pref.edit();
        ed.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
        ed.putInt("minute", calendar.get(Calendar.MINUTE));
        ed.putString("intent", intent.getStringExtra(context.getString(R.string.alarmmanager_key)));
        ed.apply();

        switch (AlarmReceiverUsage.valueOf(intent.getStringExtra((context.getString(R.string.alarmmanager_key))))) {
            case DEFAULT:
                break;
            case START_FOREGROUND:
                //Start foregroundservice with an activity
                Intent startForegroundIntent = new Intent(context, ForegroundActivity.class);
                startForegroundIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startForegroundIntent.putExtra("intent", 1);
                context.startActivity(startForegroundIntent);
                break;
            case STOP_FOREGROUND:
                //Stop foregorundservice with an activity
                Intent stopForegroundIntent = new Intent(context, ForegroundActivity.class);
                stopForegroundIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                stopForegroundIntent.putExtra("intent", 2);
                context.startActivity(stopForegroundIntent);
                break;
            case DISABLE_ALARM:

                /** Eventuell über ForegroundActivity aufrufen, wenn es nicht geht*/


                /**TODO: Turn Alarm off, set Alarm for the day after or check for the next day
                 * TODO: Stop Foregroundservice and send Toast
                 */
                break;
            case NOT_SLEEPING:
                //Button not Sleeping
                sleepCalculationHandler.userNotSleepingJob();
                break;
            case START_WORKMANAGER_CALCULATION:
                //Start the workmanager for the calculation of the sleep
                WorkmanagerCalculation.startPeriodicWorkmanager(16, context.getApplicationContext());
                break;
            case START_WORKMANAGER:
                //Start Workmanager at sleeptime and subscribe to SleepApi
                //Workmanager.Companion.startPeriodicWorkmanager(16, context);

                PeriodicWorkRequest periodicDataWork =
                        new PeriodicWorkRequest.Builder(Workmanager.class, 16, TimeUnit.MINUTES)
                                .addTag(context.getString(R.string.workmanager1_tag)) //Tag is needed for canceling the periodic work
                                .build();

                WorkManager workManager = WorkManager.getInstance(context);
                workManager.enqueueUniquePeriodicWork(context.getString(R.string.workmanager1_tag), ExistingPeriodicWorkPolicy.KEEP, periodicDataWork);

                Toast.makeText(context, "Workmanager started", Toast.LENGTH_LONG).show();

                sleepHandler.startSleepHandler();

                //Set AlarmManager to stop Workmanager at end of sleeptime
                calendar = AlarmReceiver.getAlarmDate(dataStoreRepository.getSleepTimeEndJob());
                if (LocalTime.now().toSecondOfDay() < dataStoreRepository.getSleepTimeEndJob()) {
                    AlarmReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), context,AlarmReceiverUsage.STOP_FOREGROUND);
                } else {
                    AlarmReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK) + 1, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), context, AlarmReceiverUsage.STOP_FOREGROUND);
                }


                break;
            case STOP_WORKMANAGER:
                //Stop Workmanager at end of sleeptime and unsubscribe to SleepApi
                //Workmanager.Companion.stopPeriodicWorkmanager();

                WorkManager.getInstance(context).cancelAllWorkByTag("Workmanager 1");

                sleepHandler.stopSleepHandler();

                sleepCalculationHandler.defineUserWakeup( null, false);

                //Set AlarmManager to start Workmanager at begin of sleeptime
                calendar = AlarmReceiver.getAlarmDate(dataStoreRepository.getSleepTimeBeginJob());
                AlarmReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), context,AlarmReceiverUsage.START_WORKMANAGER_CALCULATION);
        }
    }

    /**
     * Start a alarm at a specific time
     * @param day Number from 1-7, Sunday=1, Saturday=7
     * @param hour Hour from 0-23
     * @param min Minute from 0-59
     * @param alarmContext Application Context
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT) //Android KITKAT is minimum version!
    public static void startAlarmManager(int day, int hour, int min, Context alarmContext, AlarmReceiverUsage alarmReceiverUsage) {

        //Get an instance of calendar and set time, when alarm should be fired
        Calendar calenderAlarm = getAlarmDate(day, hour, min);

        Intent intent = new Intent(alarmContext, AlarmReceiver.class);
        intent.putExtra(alarmContext.getString(R.string.alarmmanager_key), alarmReceiverUsage.name());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(alarmContext, alarmReceiverUsage.getAlarmReceiverUsageValue(), intent, 0);
        AlarmManager alarmManager = (AlarmManager) alarmContext.getSystemService(ALARM_SERVICE);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calenderAlarm.getTimeInMillis(), pendingIntent);

        Toast.makeText(alarmContext, "AlarmManager set to " + calenderAlarm.get(Calendar.DAY_OF_WEEK) + ": "
                + calenderAlarm.get(Calendar.HOUR_OF_DAY) + ":" + calenderAlarm.get(Calendar.MINUTE) + ", usage: " + alarmReceiverUsage.getAlarmReceiverUsageValue(), Toast.LENGTH_LONG).show();
    }

    /**
     * Cancel a specific alarm by pending intent
     * @param cancelAlarmContext Application Context
     */
    public static void cancelAlarm(Context cancelAlarmContext, AlarmReceiverUsage alarmReceiverUsage) {

        Intent intent = new Intent(cancelAlarmContext, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(cancelAlarmContext, alarmReceiverUsage.getAlarmReceiverUsageValue(), intent, 0);
        AlarmManager alarmManager = (AlarmManager) cancelAlarmContext.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

        Toast.makeText(cancelAlarmContext, "AlarmManager canceled: " + alarmReceiverUsage.getAlarmReceiverUsageValue(), Toast.LENGTH_LONG).show();
    }

    public static boolean isAlarmManagerActive(Context alarmActiveContext, AlarmReceiverUsage alarmReceiverUsage) {
        Intent intent = new Intent(alarmActiveContext, AlarmReceiver.class);

        return (PendingIntent.getBroadcast(alarmActiveContext, alarmReceiverUsage.getAlarmReceiverUsageValue(), intent, PendingIntent.FLAG_NO_CREATE) != null);
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

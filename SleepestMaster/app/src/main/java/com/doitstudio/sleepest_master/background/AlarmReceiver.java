package com.doitstudio.sleepest_master.background;

/**This class inherits from Broadcastreceiver and starts an alarm at a specific time and date*/

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

import com.doitstudio.sleepest_master.MainActivity;
import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.alarmclock.AlarmClockReceiver;
import com.doitstudio.sleepest_master.model.data.Actions;
import com.doitstudio.sleepest_master.sleepapi.SleepHandler;
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler;
import com.doitstudio.sleepest_master.storage.DataStoreRepository;
//import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

import kotlin.Unit;
import kotlin.coroutines.Continuation;

import static android.content.Context.ALARM_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {

    private SleepCalculationHandler sleepCalculationHandler;
    private DataStoreRepository dataStoreRepository;
    private SleepHandler sleepHandler;

    @Override
    public void onReceive(Context context, Intent intent) {

        dataStoreRepository = DataStoreRepository.Companion.getRepo(context);
        sleepHandler = SleepHandler.Companion.getHandler(context);

        Calendar calendar = Calendar.getInstance();
        SharedPreferences pref = context.getSharedPreferences("AlarmReceiver", 0);
        SharedPreferences.Editor ed = pref.edit();
        ed.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
        ed.putInt("minute", calendar.get(Calendar.MINUTE));
        ed.putInt("intent", intent.getIntExtra(context.getString(R.string.alarmmanager_key), 0));
        ed.apply();

        switch (intent.getIntExtra(context.getString(R.string.alarmmanager_key), 0)) {
            case 0:
                break;
            case 1:
                //Start foregroundservice with an activity
                Intent startForegroundIntent = new Intent(context, ForegroundActivity.class);
                startForegroundIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startForegroundIntent.putExtra("intent", 1);
                context.startActivity(startForegroundIntent);
                break;
            case 2:
                //Stop foregorundservice with an activity
                Intent stopForegroundIntent = new Intent(context, ForegroundActivity.class);
                stopForegroundIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                stopForegroundIntent.putExtra("intent", 2);
                context.startActivity(stopForegroundIntent);
                break;
            case 3:

                /** Eventuell über ForegroundActivity aufrufen, wenn es nicht geht*/


                /**TODO: Turn Alarm off, set Alarm for the day after or check for the next day
                 * TODO: Stop Foregroundservice and send Toast
                 */
                break;
            case 4:
                //Button not Sleeping
                sleepCalculationHandler = SleepCalculationHandler.Companion.getHandler(context);
                sleepCalculationHandler.userNotSleepingJob();
                break;
            case 5:
                //Start the workmanager for the calculation of the sleep
                WorkmanagerCalculation.startPeriodicWorkmanager(16, context.getApplicationContext());
                break;
            case 6:
                //Start Workmanager at sleeptime and subscribe to SleepApi
                Workmanager.startPeriodicWorkmanager(16, context);
                sleepHandler.startSleepHandler();

                //Set AlarmManager to stop Workmanager at end of sleeptime
                calendar = AlarmReceiver.getAlarmDate(dataStoreRepository.getSleepTimeEndJob());
                AlarmReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), context,7);
                break;
            case 7:
                //Stop Workmanager at end of sleeptime and unsubscribe to SleepApi
                Workmanager.stopPeriodicWorkmanager();
                sleepHandler.stopSleepHandler();

                //Set AlarmManager to start Workmanager at begin of sleeptime
                calendar = AlarmReceiver.getAlarmDate(dataStoreRepository.getSleepTimeBeginJob());
                AlarmReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), context,6);
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
    public static void startAlarmManager(int day, int hour, int min, Context alarmContext, int usage) {

        //Get an instance of calendar and set time, when alarm should be fired
        Calendar calenderAlarm = getAlarmDate(day, hour, min);

        Intent intent = new Intent(alarmContext, AlarmReceiver.class);
        intent.putExtra(alarmContext.getString(R.string.alarmmanager_key), usage);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(alarmContext, usage, intent, 0);
        AlarmManager alarmManager = (AlarmManager) alarmContext.getSystemService(ALARM_SERVICE);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calenderAlarm.getTimeInMillis(), pendingIntent);

        Toast.makeText(alarmContext, "AlarmManager set to " + calenderAlarm.get(Calendar.DAY_OF_WEEK) + ": "
                + calenderAlarm.get(Calendar.HOUR_OF_DAY) + ":" + calenderAlarm.get(Calendar.MINUTE) + ", usage: " + usage, Toast.LENGTH_LONG).show();
    }

    /**
     * Cancel a specific alarm by pending intent
     * @param cancelAlarmContext Application Context
     */
    public static void cancelAlarm(Context cancelAlarmContext, int usage) {

        Intent intent = new Intent(cancelAlarmContext, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(cancelAlarmContext, usage, intent, 0);
        AlarmManager alarmManager = (AlarmManager) cancelAlarmContext.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
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

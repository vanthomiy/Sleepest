package com.doitstudio.sleepest_master.background;

/**This class inherits from Broadcastreceiver and starts an alarm at a specific time and date*/

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
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
import com.doitstudio.sleepest_master.model.data.Constants;
import com.doitstudio.sleepest_master.model.data.SleepState;
import com.doitstudio.sleepest_master.sleepapi.SleepHandler;
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler;
import com.doitstudio.sleepest_master.storage.DataStoreRepository;
import com.doitstudio.sleepest_master.storage.DatabaseRepository;
import com.doitstudio.sleepest_master.storage.db.AlarmEntity;
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

        DataStoreRepository dataStoreRepository = DataStoreRepository.Companion.getRepo(context);
        //DataStoreRepository dataStoreRepository = MainApplication.class.cast(context).getDataStoreRepository();
        //Activity activity = (Activity) context;
        //DataStoreRepository dataStoreRepository = ((MainApplication)activity.getApplication()).getDataStoreRepository();
        //DatabaseRepository databaseRepository = MainApplication.class.cast(context).getDataBaseRepository();
        DatabaseRepository databaseRepository = ((MainApplication)context.getApplicationContext()).getDataBaseRepository();
        AlarmEntity alarmEntity = databaseRepository.getNextActiveAlarmJob();

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
                /**Intent startForegroundIntent = new Intent(context, ForegroundActivity.class);
                startForegroundIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startForegroundIntent.putExtra("intent", 1);
                context.startActivity(startForegroundIntent);**/
                BackgroundAlarmTimeHandler.Companion.getHandler(context.getApplicationContext()).beginOfSleepTime(true);
                break;
            case STOP_FOREGROUND:
                //Stop foregorundservice with an activity
                /*Intent stopForegroundIntent = new Intent(context, ForegroundActivity.class);
                stopForegroundIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                stopForegroundIntent.putExtra("intent", 2);
                context.startActivity(stopForegroundIntent);*/
                break;
            case DISABLE_ALARM:
                if((databaseRepository.getNextActiveAlarmJob() != null) && !databaseRepository.getNextActiveAlarmJob().getTempDisabled()) {
                    /**databaseRepository.updateAlarmTempDisabledJob(true, databaseRepository.getNextActiveAlarmJob().getId());
                    Calendar calendarStopForeground = Calendar.getInstance();
                    AlarmReceiver.startAlarmManager(calendarStopForeground.get(Calendar.DAY_OF_WEEK), calendarStopForeground.get(Calendar.HOUR_OF_DAY),
                            calendarStopForeground.get(Calendar.MINUTE) + 5, context.getApplicationContext(), AlarmReceiverUsage.STOP_FOREGROUND);
                    Toast.makeText(context.getApplicationContext(), context.getApplicationContext().getString(R.string.disable_alarm_message), Toast.LENGTH_LONG).show();**/
                    BackgroundAlarmTimeHandler.Companion.getHandler(context.getApplicationContext()).disableAlarmTemporaryInApp(false, false);
                } else if ((databaseRepository.getNextActiveAlarmJob() != null) && databaseRepository.getNextActiveAlarmJob().getTempDisabled()) {
                    /**databaseRepository.updateAlarmTempDisabledJob(false, databaseRepository.getNextActiveAlarmJob().getId());
                    AlarmReceiver.cancelAlarm(context.getApplicationContext(), AlarmReceiverUsage.STOP_FOREGROUND);**/
                    BackgroundAlarmTimeHandler.Companion.getHandler(context.getApplicationContext()).disableAlarmTemporaryInApp(false, true);
                }

                break;
            case NOT_SLEEPING:
                //Button not Sleeping
                sleepCalculationHandler.userNotSleepingJob();
                Toast.makeText(context.getApplicationContext(), context.getApplicationContext().getString(R.string.not_sleeping_message), Toast.LENGTH_LONG).show();
                break;
            case START_WORKMANAGER_CALCULATION:
                //Start the workmanager for the calculation of the sleep
                WorkmanagerCalculation.startPeriodicWorkmanager(Constants.WORKMANAGER_CALCULATION_DURATION, context.getApplicationContext());
                break;
            case START_WORKMANAGER: /**Übertragen**/
                //Start Workmanager at sleeptime and subscribe to SleepApi
                /**PeriodicWorkRequest periodicDataWork =
                        new PeriodicWorkRequest.Builder(Workmanager.class, Constants.WORKMANAGER_DURATION, TimeUnit.MINUTES)
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
                }**/
                break;
            case STOP_WORKMANAGER:

                BackgroundAlarmTimeHandler.Companion.getHandler(context.getApplicationContext()).endOfSleepTime();
                /**
                //Stop Workmanager at end of sleeptime and unsubscribe to SleepApi
                //TODO: Überprüfen, ob der Workmanager noch richtig abgebrochen wird
                WorkManager.getInstance(context.getApplicationContext()).cancelAllWorkByTag(context.getApplicationContext().getString(R.string.workmanager1_tag));

                sleepHandler.stopSleepHandler();

                sleepCalculationHandler.defineUserWakeup( null, false);

                //Set AlarmManager to start Workmanager at begin of sleeptime
                calendar = AlarmReceiver.getAlarmDate(dataStoreRepository.getSleepTimeBeginJob());
                AlarmReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), context,AlarmReceiverUsage.START_WORKMANAGER_CALCULATION);
                **/
                 break;
            case CURRENTLY_NOT_SLEEPING:
                sleepCalculationHandler.userCurrentlyNotSleepingJob();
                Toast.makeText(context.getApplicationContext(), context.getApplicationContext().getString(R.string.currently_not_sleeping_message), Toast.LENGTH_LONG).show();
                break;
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

    /**
     * Create the notification to inform the user about problems
     * @return
     */
    public static Notification createInformationNotification(Context context, String information) {
        //Get Channel id
        String notificationChannelId = context.getApplicationContext().getString(R.string.information_notification_channel);

        //Create intent if user tap on notification
        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Create manager and channel
        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                notificationChannelId,
                context.getApplicationContext().getString(R.string.information_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
        );

        //Set channel description
        channel.setDescription(context.getApplicationContext().getString(R.string.information_notification_channel_description));
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }

        //Build the notification and return it
        Notification.Builder builder;
        builder = new Notification.Builder(context.getApplicationContext(), notificationChannelId);

        return builder
                .setContentTitle(context.getApplicationContext().getString(R.string.information_notification_title))
                .setContentText(information) /**TODO: Textauswahl**/
                .setStyle(new Notification.DecoratedCustomViewStyle())
                .setSmallIcon(R.drawable.logo_notification)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .build();
    }

}

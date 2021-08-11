package com.doitstudio.sleepest_master.background;

/**This class inherits from Broadcastreceiver and starts an alarm at a specific time and date**/

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
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.media.app.NotificationCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.doitstudio.sleepest_master.MainActivity;
import com.doitstudio.sleepest_master.MainApplication;
import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.model.data.AlarmReceiverUsage;
import com.doitstudio.sleepest_master.model.data.Constants;
import com.doitstudio.sleepest_master.googleapi.SleepHandler;
import com.doitstudio.sleepest_master.model.data.NotificationUsage;
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler;
import com.doitstudio.sleepest_master.storage.DataStoreRepository;
import com.doitstudio.sleepest_master.storage.DatabaseRepository;
import com.doitstudio.sleepest_master.storage.db.AlarmEntity;
import com.doitstudio.sleepest_master.util.TimeConverterUtil;
//import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static android.content.Context.ALARM_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        DatabaseRepository databaseRepository = ((MainApplication)context.getApplicationContext()).getDataBaseRepository();
        SleepCalculationHandler sleepCalculationHandler = SleepCalculationHandler.Companion.getHandler(MainApplication.Companion.applicationContext());

        Calendar calendar = Calendar.getInstance();
        SharedPreferences pref = context.getSharedPreferences("AlarmReceiver", 0);
        SharedPreferences.Editor ed = pref.edit();
        ed.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
        ed.putInt("minute", calendar.get(Calendar.MINUTE));
        ed.putString("intent", intent.getStringExtra(context.getString(R.string.alarmmanager_key)));
        ed.apply();

        switch (AlarmReceiverUsage.valueOf(intent.getStringExtra((context.getString(R.string.alarmmanager_key))))) {
            case START_FOREGROUND:
                //Starts the cycle of receiving API data and setting alarms
                BackgroundAlarmTimeHandler.Companion.getHandler(context.getApplicationContext()).beginOfSleepTime(true);
                break;
            case STOP_FOREGROUND:
                //Stops the foregroundservice after a sleep session or if sleep time changes to out of sleep time
                BackgroundAlarmTimeHandler.Companion.getHandler(context.getApplicationContext()).stopForegroundService(true);
                break;
            case DISABLE_ALARM:
                    //Disables the next active alarm temporarly
                    if ((databaseRepository.getNextActiveAlarmJob() != null) && (!databaseRepository.getNextActiveAlarmJob().getTempDisabled())) {
                        BackgroundAlarmTimeHandler.Companion.getHandler(context.getApplicationContext()).disableAlarmTemporaryInApp(false, false);
                    } else {
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
            case START_WORKMANAGER:
                //Deprecated
                break;
            case STOP_WORKMANAGER:
                BackgroundAlarmTimeHandler.Companion.getHandler(context.getApplicationContext()).endOfSleepTime(true);
                 break;
            case CURRENTLY_NOT_SLEEPING:
                sleepCalculationHandler.userCurrentlyNotSleepingJob();
                Toast.makeText(context.getApplicationContext(), context.getApplicationContext().getString(R.string.currently_not_sleeping_message), Toast.LENGTH_LONG).show();
                break;
            case SOLVE_API_PROBLEM:
                Toast.makeText(context.getApplicationContext(), "Restarted sleepdata tracking", Toast.LENGTH_LONG).show();
                BackgroundAlarmTimeHandler.Companion.getHandler(context.getApplicationContext()).startWorkmanager();
                NotificationManager notificationManagerApi = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManagerApi.cancel(NotificationUsage.Companion.getCount(NotificationUsage.NOTIFICATION_NO_API_DATA));
            case GO_TO_SLEEP:
                Toast.makeText(context.getApplicationContext(), "Good night", Toast.LENGTH_LONG).show();
                NotificationManager notificationManagerGoSleep = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManagerGoSleep.cancel(NotificationUsage.Companion.getCount(NotificationUsage.NOTIFICATION_NO_API_DATA));

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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(alarmContext, AlarmReceiverUsage.Companion.getCount(alarmReceiverUsage), intent, 0);
        AlarmManager alarmManager = (AlarmManager) alarmContext.getSystemService(ALARM_SERVICE);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calenderAlarm.getTimeInMillis(), pendingIntent);

        Toast.makeText(alarmContext, "AlarmManager set to " + calenderAlarm.get(Calendar.DAY_OF_WEEK) + ": "
                + calenderAlarm.get(Calendar.HOUR_OF_DAY) + ":" + calenderAlarm.get(Calendar.MINUTE) + ", usage: " + AlarmReceiverUsage.Companion.getCount(alarmReceiverUsage), Toast.LENGTH_LONG).show();
    }

    /**
     * Cancel a specific alarm by pending intent
     * @param cancelAlarmContext Application Context
     */
    public static void cancelAlarm(Context cancelAlarmContext, AlarmReceiverUsage alarmReceiverUsage) {

        Intent intent = new Intent(cancelAlarmContext, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(cancelAlarmContext, AlarmReceiverUsage.Companion.getCount(alarmReceiverUsage), intent, 0);
        AlarmManager alarmManager = (AlarmManager) cancelAlarmContext.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

        Toast.makeText(cancelAlarmContext, "AlarmManager canceled: " + AlarmReceiverUsage.Companion.getCount(alarmReceiverUsage), Toast.LENGTH_LONG).show();
    }

    public static boolean isAlarmManagerActive(Context alarmActiveContext, AlarmReceiverUsage alarmReceiverUsage) {
        Intent intent = new Intent(alarmActiveContext, AlarmReceiver.class);

        return (PendingIntent.getBroadcast(alarmActiveContext, AlarmReceiverUsage.Companion.getCount(alarmReceiverUsage), intent, PendingIntent.FLAG_NO_CREATE) != null);
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
                .setSmallIcon(R.drawable.logofulllinesoutlineround)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .build();
    }

}

package com.sleepestapp.sleepest.alarmclock;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import com.sleepestapp.sleepest.MainApplication;
import com.sleepestapp.sleepest.R;
import com.sleepestapp.sleepest.background.BackgroundAlarmTimeHandler;
import com.sleepestapp.sleepest.model.data.AlarmClockReceiverUsage;
import com.sleepestapp.sleepest.model.data.NotificationUsage;
import com.sleepestapp.sleepest.storage.DataStoreRepository;
import com.sleepestapp.sleepest.storage.DatabaseRepository;
import com.sleepestapp.sleepest.storage.db.AlarmEntity;
import com.sleepestapp.sleepest.util.NotificationUtil;
import com.sleepestapp.sleepest.util.TimeConverterUtil;

import java.util.Calendar;
import static android.content.Context.ALARM_SERVICE;

/**This class inherits from Broadcastreceiver and starts an alarm at a specific time and date
 * When alarm was fired, the alarm audio will start */

public class AlarmClockReceiver extends BroadcastReceiver {

    private static Context context;

    /**
     * Callback to receive the alarm
     * @param context Context
     * @param intent Intent
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onReceive(Context context, Intent intent) {

        AlarmClockReceiver.context = context;
        DataStoreRepository dataStoreRepository = DataStoreRepository.Companion.getRepo(context);
        DatabaseRepository databaseRepository = ((MainApplication)context.getApplicationContext()).getDataBaseRepository();
        AlarmEntity alarmEntity = databaseRepository.getNextActiveAlarmJob();

        //Different actions for the alarm clock depending on the usage
        switch (AlarmClockReceiverUsage.valueOf(intent.getStringExtra((context.getString(R.string.alarm_clock_intent_key))))) {
            case START_ALARMCLOCK: //Init Alarmclock

                PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                if (powerManager.isInteractive()) {
                    NotificationUtil notificationsUtil = new NotificationUtil(context.getApplicationContext(), NotificationUsage.NOTIFICATION_ALARM_CLOCK,null);
                    notificationsUtil.chooseNotification();
                } else {
                    //showNotificationOnLockScreen(NotificationUsage.NOTIFICATION_ALARM_CLOCK);
                    NotificationUtil notificationsUtil = new NotificationUtil(context.getApplicationContext(), NotificationUsage.NOTIFICATION_ALARM_CLOCK_LOCK_SCREEN,null);
                    notificationsUtil.chooseNotification();
                }
                break;
            case STOP_ALARMCLOCK: //Stop button of ScreenOn notification
                BackgroundAlarmTimeHandler.Companion.getHandler(context.getApplicationContext()).alarmClockRang(true);
                break;
            case SNOOZE_ALARMCLOCK: //Snooze button of ScreenOn notification
                AlarmClockAudio.getInstance().stopAlarm(true, true);
                break;
            case LATEST_WAKEUP_ALARMCLOCK: //Latest wakeup action
                if (alarmEntity != null && !alarmEntity.getTempDisabled() && !alarmEntity.getWasFired()) {
                    PowerManager powerManagerLate = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    if (powerManagerLate.isInteractive()) {
                        NotificationUtil notificationsUtil = new NotificationUtil(context.getApplicationContext(), NotificationUsage.NOTIFICATION_ALARM_CLOCK,null);
                        notificationsUtil.chooseNotification();
                    } else {
                        NotificationUtil notificationsUtil = new NotificationUtil(context.getApplicationContext(), NotificationUsage.NOTIFICATION_ALARM_CLOCK_LOCK_SCREEN,null);
                        notificationsUtil.chooseNotification();
                    }
                }
                break;
        }
    }

    /**
     * Start a alarm at a specific time
     * @param day Day from 1-7, sunday = 1, saturday = 7
     * @param hour Hour from 0-23
     * @param min Minute from 0-59
     * @param alarmClockContext Context
     */
    public static void startAlarmManager(int day, int hour, int min, Context alarmClockContext, AlarmClockReceiverUsage alarmClockReceiverUsage) {

        //Resets the latest wakeup if the alarm clock rings earlier
        if (AlarmClockReceiver.isAlarmClockActive(alarmClockContext, AlarmClockReceiverUsage.LATEST_WAKEUP_ALARMCLOCK) && (alarmClockReceiverUsage == AlarmClockReceiverUsage.START_ALARMCLOCK)) {
            AlarmClockReceiver.cancelAlarm(alarmClockContext, AlarmClockReceiverUsage.LATEST_WAKEUP_ALARMCLOCK);
        }

        Calendar calendar = TimeConverterUtil.getAlarmDate(day, hour, min);

        //Starts the alarm with a new intent
        Intent intent = new Intent(alarmClockContext, AlarmClockReceiver.class);
        intent.putExtra(alarmClockContext.getString(R.string.alarm_clock_intent_key), alarmClockReceiverUsage.name());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(alarmClockContext, AlarmClockReceiverUsage.Companion.getCount(alarmClockReceiverUsage), intent, 0);
        AlarmManager alarmManager = (AlarmManager) alarmClockContext.getSystemService(ALARM_SERVICE);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Toast.makeText(alarmClockContext, "AlarmClock set to " + calendar.get(Calendar.DAY_OF_WEEK) + ": "
                + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE), Toast.LENGTH_LONG).show();
    }

    /**
     * Start a alarm at a specific time
     * @param snoozeTime Time in millis, restart alarm after that duration
     * @param restartAlarmContext Context
     */
    public static void restartAlarmManager(int snoozeTime, Context restartAlarmContext) {

        Intent intent = new Intent(restartAlarmContext, AlarmClockReceiver.class);
        intent.putExtra(context.getString(R.string.alarm_clock_intent_key), AlarmClockReceiverUsage.START_ALARMCLOCK.name());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(restartAlarmContext, AlarmClockReceiverUsage.Companion.getCount(AlarmClockReceiverUsage.SNOOZE_ALARMCLOCK), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Cancel a running alarm with a pending intent generated with usage
     * @param cancelAlarmContext Context
     */
    public static void cancelAlarm(Context cancelAlarmContext, AlarmClockReceiverUsage alarmClockReceiverUsage) {
        Intent intent = new Intent(cancelAlarmContext, AlarmClockReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(cancelAlarmContext, AlarmClockReceiverUsage.Companion.getCount(alarmClockReceiverUsage), intent, 0);
        AlarmManager alarmManager = (AlarmManager) cancelAlarmContext.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    /**
     * Detects the status of a alarm
     * @param alarmActiveContext Context
     * @param alarmClockReceiverUsage Usage of AlarmClockReceiver
     * @return Alarm clock active status
     */
    public static boolean isAlarmClockActive(Context alarmActiveContext, AlarmClockReceiverUsage alarmClockReceiverUsage) {
        Intent intent = new Intent(alarmActiveContext, AlarmClockReceiver.class);

        return (PendingIntent.getBroadcast(alarmActiveContext, AlarmClockReceiverUsage.Companion.getCount(alarmClockReceiverUsage), intent, PendingIntent.FLAG_NO_CREATE) != null);
    }
}

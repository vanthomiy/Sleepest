package com.sleepestapp.sleepest.alarmclock;

/**This class inherits from Broadcastreceiver and starts an alarm at a specific time and date
 * When alarm was fired, the alarm audio will start */

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.sleepestapp.sleepest.MainActivity;
import com.sleepestapp.sleepest.MainApplication;
import com.sleepestapp.sleepest.R;
import com.sleepestapp.sleepest.background.BackgroundAlarmTimeHandler;
import com.sleepestapp.sleepest.background.ForegroundActivity;
import com.sleepestapp.sleepest.model.data.ActivityIntentUsage;
import com.sleepestapp.sleepest.model.data.AlarmClockReceiverUsage;
import com.sleepestapp.sleepest.model.data.AlarmReceiverUsage;
import com.sleepestapp.sleepest.model.data.Constants;
import com.sleepestapp.sleepest.model.data.NotificationUsage;
import com.sleepestapp.sleepest.storage.DataStoreRepository;
import com.sleepestapp.sleepest.storage.DatabaseRepository;
import com.sleepestapp.sleepest.storage.db.AlarmEntity;
import com.sleepestapp.sleepest.util.NotificationUtil;
import com.sleepestapp.sleepest.util.TimeConverterUtil;
import com.sleepestapp.sleepest.MainActivity;
import com.sleepestapp.sleepest.MainApplication;

import java.util.Calendar;
import static android.content.Context.ALARM_SERVICE;

public class AlarmClockReceiver extends BroadcastReceiver {

    private static Context context;
    private DataStoreRepository dataStoreRepository;

    /**
     * Callback to receive the alarm
     * @param context Context
     * @param intent Intent
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        dataStoreRepository = DataStoreRepository.Companion.getRepo(context);
        DatabaseRepository databaseRepository = ((MainApplication)context.getApplicationContext()).getDataBaseRepository();
        AlarmEntity alarmEntity = databaseRepository.getNextActiveAlarmJob();

        //Different actions for the alarm clock depending on the usage
        switch (AlarmClockReceiverUsage.valueOf(intent.getStringExtra((context.getString(R.string.alarm_clock_intent_key))))) {
            case START_ALARMCLOCK: //Init Alarmclock

                //if (alarmEntity != null && !alarmEntity.getTempDisabled()) {
                    PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    if (powerManager.isInteractive()) {
                        NotificationUtil notificationsUtil = new NotificationUtil(context.getApplicationContext(), NotificationUsage.NOTIFICATION_ALARM_CLOCK,null);
                        notificationsUtil.chooseNotification();
                    } else {
                        //showNotificationOnLockScreen(NotificationUsage.NOTIFICATION_ALARM_CLOCK);
                        NotificationUtil notificationsUtil = new NotificationUtil(context.getApplicationContext(), NotificationUsage.NOTIFICATION_ALARM_CLOCK_LOCK_SCREEN,null);
                        notificationsUtil.chooseNotification();
                    }
               // }

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
                        //showNotificationOnLockScreen(NotificationUsage.NOTIFICATION_ALARM_CLOCK);
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

        //AlarmManager alarmManager = (AlarmManager) restartAlarmContext.getSystemService(ALARM_SERVICE);

        //alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + snoozeTime, pendingIntent);
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

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void showFullscreenNotification() {

        createNotificationChannel();

        // Intent, which starts with tap on the Notification
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Intent, which starts with tap on the cancel button
        Intent cancelAlarmIntent = new Intent(context, AlarmClockReceiver.class);
        cancelAlarmIntent.putExtra(context.getString(R.string.alarm_clock_intent_key), AlarmClockReceiverUsage.STOP_ALARMCLOCK.name());
        cancelAlarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent cancelAlarmPendingIntent = PendingIntent.getBroadcast(context, AlarmClockReceiverUsage.Companion.getCount(AlarmClockReceiverUsage.STOP_ALARMCLOCK), cancelAlarmIntent, PendingIntent.FLAG_ONE_SHOT);

        // Intent, which starts with tap on the snooze button
        Intent snoozeAlarmIntent = new Intent(context, AlarmClockReceiver.class);
        snoozeAlarmIntent.putExtra(context.getString(R.string.alarm_clock_intent_key), AlarmClockReceiverUsage.SNOOZE_ALARMCLOCK.name());
        snoozeAlarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent snoozeAlarmPendingIntent = PendingIntent.getBroadcast(context, AlarmClockReceiverUsage.Companion.getCount(AlarmClockReceiverUsage.SNOOZE_ALARMCLOCK), snoozeAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Build the builder for the notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, context.getString(R.string.alarm_clock_channel))
                        .setSmallIcon(R.drawable.ic_launcher_background) /**TODO: Icon*/
                        .setContentTitle(context.getString(R.string.alarm_notification_title))
                        .setContentText(context.getString(R.string.alarm_notification_text))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setContentIntent(pendingIntent)
                        .setFullScreenIntent(pendingIntent, true)
                        .addAction(R.drawable.ic_launcher_foreground, context.getString(R.string.alarm_notification_button_1), cancelAlarmPendingIntent) /**TODO: Icon*/
                        .addAction(R.drawable.ic_launcher_background, context.getString(R.string.alarm_notification_button_2), snoozeAlarmPendingIntent); /**TODO: Icon*/

        NotificationManagerCompat.from(context).notify(Constants.ALARM_CLOCK_NOTIFICATION_ID, notificationBuilder.build());

        //Starts a new singleton audio class and init it, if not init yet
        AlarmClockAudio.getInstance().init(context);
        AlarmClockAudio.getInstance().startAlarm(true);
    }

    /**
     * A full screen notification is build with the help of Notification Builder. It is shown on the lockscreen.
     * @param notificationUsage Usage of the lock screen notification
     */
    private void showNotificationOnLockScreen(NotificationUsage notificationUsage) {
        createNotificationChannel();

        Intent intent = new Intent(context, LockScreenAlarmActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, ActivityIntentUsage.Companion.getCount(ActivityIntentUsage.LOCKSCREEN_ACTIVITY), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, context.getString(R.string.alarm_clock_channel))
                        .setSmallIcon(R.drawable.ic_launcher_background) /**TODO: Icon*/
                        .setContentTitle(context.getString(R.string.alarm_notification_title))
                        .setContentText(context.getString(R.string.alarm_notification_text))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setContentIntent(pendingIntent)
                        .setFullScreenIntent(pendingIntent, true);
        NotificationManagerCompat.from(context).notify(NotificationUsage.Companion.getCount(notificationUsage), notificationBuilder.build());
    }

    /**
     * Creates a new channel for notifications
     */
    private void createNotificationChannel() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (notificationManager.getNotificationChannel(context.getString(R.string.alarm_clock_channel)) == null) {
            NotificationChannel channel = new NotificationChannel(context.getString(R.string.alarm_clock_channel),
                    context.getString(R.string.alarm_clock_channel_name), NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription(context.getString(R.string.alarm_clock_channel_description));
            notificationManager.createNotificationChannel(channel);
        }
    }


}

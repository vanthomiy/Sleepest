package com.doitstudio.sleepest_master.alarmclock;

/**This class inherits from Broadcastreceiver and starts an alarm at a specific time and date
 * When alarm was fired, the alarm audio will start */

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.doitstudio.sleepest_master.MainActivity;
import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.background.AlarmReceiver;
import com.doitstudio.sleepest_master.background.ForegroundActivity;
import com.doitstudio.sleepest_master.storage.DataStoreRepository;

import java.io.IOException;
import java.util.Calendar;
import static android.content.Context.ALARM_SERVICE;

public class AlarmClockReceiver extends BroadcastReceiver {

    private static int NOTIFICATION_ID = 10;
    private static Context context;
    private DataStoreRepository dataStoreRepository;

    /**
     * Callback to receive the alarm
     * @param context Context
     * @param intent Intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        dataStoreRepository = DataStoreRepository.Companion.getRepo(context);

        switch (intent.getIntExtra(context.getString(R.string.alarm_clock_intent_key), 0)) {
            case 0: break;
            case 1: //Init Alarmclock

                PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                if (powerManager.isInteractive()) {
                    showFullscreenNotification();
                } else {
                    showNotificationOnLockScreen();
                }
                break;
            case 2: //Stop button of ScreenOn notification
                AlarmClockAudio.getInstance().stopAlarm(false);
                //audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);

                Calendar calendarAlarm = AlarmReceiver.getAlarmDate(dataStoreRepository.getSleepTimeBeginJob());
                AlarmReceiver.startAlarmManager(calendarAlarm.get(Calendar.DAY_OF_WEEK), calendarAlarm.get(Calendar.HOUR_OF_DAY), calendarAlarm.get(Calendar.MINUTE), context, 1);

                SharedPreferences pref = context.getSharedPreferences("AlarmReceiver1", 0);
                SharedPreferences.Editor ed = pref.edit();
                ed.putString("usage", "AlarmClockReceiver");
                ed.putInt("day", calendarAlarm.get(Calendar.DAY_OF_WEEK));
                ed.putInt("hour", calendarAlarm.get(Calendar.HOUR_OF_DAY));
                ed.putInt("minute", calendarAlarm.get(Calendar.MINUTE));
                ed.apply();

                Intent stopAlarmIntent = new Intent(context, ForegroundActivity.class);
                stopAlarmIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                stopAlarmIntent.putExtra("intent", 2);
                context.startActivity(stopAlarmIntent);

                Calendar calendar = Calendar.getInstance();
                pref = context.getSharedPreferences("AlarmClock", 0);
                ed = pref.edit();
                ed.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
                ed.putInt("minute", calendar.get(Calendar.MINUTE));
                ed.apply();

                break;
            case 3: //Snooze button of ScreenOn notification
                AlarmClockAudio.getInstance().stopAlarm(true);
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
    public static void startAlarmManager(int day, int hour, int min, Context alarmClockContext, int usage) {

        Calendar calendar = AlarmReceiver.getAlarmDate(day, hour, min);

        Intent intent = new Intent(alarmClockContext, AlarmClockReceiver.class);
        intent.putExtra(alarmClockContext.getString(R.string.alarm_clock_intent_key), usage);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(alarmClockContext, usage, intent, 0);
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
        intent.putExtra(context.getString(R.string.alarm_clock_intent_key), 1);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(restartAlarmContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) restartAlarmContext.getSystemService(ALARM_SERVICE);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + snoozeTime, pendingIntent);
    }

    /**
     * Cancel a running alarm
     * @param cancelAlarmContext Context
     */
    public static void cancelAlarm(Context cancelAlarmContext, int usage) {
        Intent intent = new Intent(cancelAlarmContext, AlarmClockReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(cancelAlarmContext, usage, intent, 0);
        AlarmManager alarmManager = (AlarmManager) cancelAlarmContext.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    public static boolean isAlarmClockActive(Context alarmActiveContext, int usage) {
        Intent intent = new Intent(alarmActiveContext, AlarmClockReceiver.class);

        return (PendingIntent.getBroadcast(alarmActiveContext, usage, intent, PendingIntent.FLAG_NO_CREATE) != null);
    }

    private void showFullscreenNotification() {

        createNotificationChannel();

        // Intent, which starts with tap on the Notification
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Intent, which starts with tap on the cancel button
        Intent cancelAlarmIntent = new Intent(context, AlarmClockReceiver.class);
        cancelAlarmIntent.putExtra(context.getString(R.string.alarm_clock_intent_key), 2);
        cancelAlarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent cancelAlarmPendingIntent = PendingIntent.getBroadcast(context, 0, cancelAlarmIntent, PendingIntent.FLAG_ONE_SHOT);

        // Intent, which starts with tap on the snooze button
        Intent snoozeAlarmIntent = new Intent(context, AlarmClockReceiver.class);
        snoozeAlarmIntent.putExtra(context.getString(R.string.alarm_clock_intent_key), 3);
        snoozeAlarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent snoozeAlarmPendingIntent = PendingIntent.getBroadcast(context, 0, snoozeAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notificationBuilder.build());

        //Starts a new singleton audio class and init it, if not init yet
        AlarmClockAudio.getInstance().init(context);
        AlarmClockAudio.getInstance().startAlarm();
    }

    private void showNotificationOnLockScreen() {
        createNotificationChannel();

        Intent intent = new Intent(context, LockScreenAlarmActivity.class); /**TODO: Design a Lcokscreen view*/
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, context.getString(R.string.alarm_clock_channel))
                        .setSmallIcon(R.drawable.ic_launcher_background) /**TODO: Icon*/
                        .setContentTitle(context.getString(R.string.alarm_notification_title))
                        .setContentText(context.getString(R.string.alarm_notification_text))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setContentIntent(pendingIntent)
                        .setFullScreenIntent(pendingIntent, true);
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void createNotificationChannel() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (notificationManager.getNotificationChannel(context.getString(R.string.alarm_clock_channel)) == null) {
            NotificationChannel channel = new NotificationChannel(context.getString(R.string.alarm_clock_channel),
                    context.getString(R.string.alarm_clock_channel_name), NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription(context.getString(R.string.alarm_clock_channel_description));
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void cancelNotification() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);
    }

}

package com.sleepestapp.sleepest.util;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.sleepestapp.sleepest.MainActivity;
import com.sleepestapp.sleepest.R;
import com.sleepestapp.sleepest.alarmclock.AlarmClockAudio;
import com.sleepestapp.sleepest.alarmclock.AlarmClockReceiver;
import com.sleepestapp.sleepest.alarmclock.LockScreenAlarmActivity;
import com.sleepestapp.sleepest.background.AlarmReceiver;
import com.sleepestapp.sleepest.model.data.ActivityIntentUsage;
import com.sleepestapp.sleepest.model.data.AlarmClockReceiverUsage;
import com.sleepestapp.sleepest.model.data.AlarmReceiverUsage;
import com.sleepestapp.sleepest.model.data.Constants;
import com.sleepestapp.sleepest.model.data.NotificationUsage;
import com.sleepestapp.sleepest.storage.db.AlarmEntity;

import java.util.ArrayList;

public class NotificationUtil {

    private final Context context;
    private NotificationUsage notificationUsage;
    private final ArrayList<Object> arrayList;

    public NotificationUtil(Context context, NotificationUsage notificationUsage, ArrayList<Object> arrayList) {
        this.context = context;
        this.notificationUsage = notificationUsage;
        this.arrayList = arrayList;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void chooseNotification() {

        Notification notification = null;
        switch (notificationUsage) {
            case NOTIFICATION_FOREGROUND_SERVICE:
                notification = createForegroundNotification();
                break;
            case NOTIFICATION_USER_SHOULD_SLEEP:
                notification = createInformationNotification(NotificationUsage.Companion.getCount(NotificationUsage.NOTIFICATION_USER_SHOULD_SLEEP));
                break;
            case NOTIFICATION_NO_API_DATA:
                notification = createInformationNotification(NotificationUsage.Companion.getCount(NotificationUsage.NOTIFICATION_NO_API_DATA));
                break;
            case NOTIFICATION_ALARM_CLOCK:
                notification = createAlarmClockNotification();
                break;
            case NOTIFICATION_ALARM_CLOCK_LOCK_SCREEN:
                notification = createAlarmClockLockScreen();
                break;

        }

        if (notification != null && notificationUsage == NotificationUsage.NOTIFICATION_FOREGROUND_SERVICE) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(1, notification);
        } else if (notification != null) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(NotificationUsage.Companion.getCount(notificationUsage), notification);
        }

    }


    private void createNotificationChannel(String channelId, String channelName, String channelDescription) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
        );

        channel.setDescription(channelDescription);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createInformationNotification(int usage) {
        //Get Channel id
        String notificationChannelId = context.getApplicationContext().getString(R.string.information_notification_channel);

        Intent informationIntent = new Intent(context.getApplicationContext(), AlarmReceiver.class);
        String information;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_remote_view_template);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), AlarmReceiverUsage.Companion.getCount(AlarmReceiverUsage.GO_TO_SLEEP), informationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntentLeft;

        if (usage == NotificationUsage.Companion.getCount(NotificationUsage.NOTIFICATION_NO_API_DATA)) {

            information = SmileySelectorUtil.getSmileyAttention() + context.getString(R.string.information_notification_text_sleep_api_problem);

            informationIntent.putExtra(context.getString(R.string.alarmmanager_key), AlarmReceiverUsage.SOLVE_API_PROBLEM.name());
            informationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            pendingIntentLeft = PendingIntent.getBroadcast(context.getApplicationContext(), AlarmReceiverUsage.Companion.getCount(AlarmReceiverUsage.SOLVE_API_PROBLEM), informationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setViewVisibility(R.id.btnRemoteViewLeft, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.btnRemoteViewRight, View.GONE);

            remoteViews.setTextViewText(R.id.tvContentRemoteView, context.getString(R.string.notification_information_api_problem_content_expanded));
            remoteViews.setTextViewText(R.id.btnRemoteViewLeft, context.getString(R.string.notification_information_api_problem_btn));

            remoteViews.setOnClickPendingIntent(R.id.btnRemoteViewLeft, pendingIntentLeft);

        } else  {

            information = SmileySelectorUtil.getSmileyAttention() + context.getString(R.string.information_notification_text_sleeptime_problem);

            informationIntent.putExtra(context.getString(R.string.alarmmanager_key), AlarmReceiverUsage.GO_TO_SLEEP.name());
            informationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            pendingIntentLeft = PendingIntent.getBroadcast(context.getApplicationContext(), AlarmReceiverUsage.Companion.getCount(AlarmReceiverUsage.GO_TO_SLEEP), informationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


            remoteViews.setViewVisibility(R.id.btnRemoteViewLeft, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.btnRemoteViewRight, View.VISIBLE);

            remoteViews.setTextViewText(R.id.tvContentRemoteView, context.getString(R.string.notification_information_user_should_sleep_content_expanded));
            remoteViews.setTextViewText(R.id.btnRemoteViewLeft, context.getString(R.string.notification_information_user_should_sleep_btn_left));
            remoteViews.setTextViewText(R.id.btnRemoteViewRight, context.getString(R.string.notification_information_user_should_sleep_btn_right));

            remoteViews.setOnClickPendingIntent(R.id.btnRemoteViewLeft, pendingIntentLeft);

            informationIntent = new Intent(context.getApplicationContext(), MainActivity.class);
            informationIntent.putExtra(context.getString(R.string.alarmmanager_key), AlarmReceiverUsage.GO_TO_SLEEP.name());
            informationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), AlarmReceiverUsage.Companion.getCount(AlarmReceiverUsage.GO_TO_SLEEP), informationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.btnRemoteViewRight, pendingIntent);
        }



        //Create manager and channel
        createNotificationChannel(context.getString(R.string.information_notification_channel), context.getString(R.string.information_notification_channel_name), context.getString(R.string.information_notification_channel_description));

        //Build the notification and return it
        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(context.getApplicationContext(), notificationChannelId);

        return builder
                .setContentTitle(context.getApplicationContext().getString(R.string.information_notification_title))
                .setContentText(information)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomBigContentView(remoteViews)
                .setSmallIcon(R.drawable.logofullroundtransparentwhite)
                .setColor(ContextCompat.getColor(context.getApplicationContext(), R.color.accent_text_color))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(pendingIntentLeft)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
    }

    @SuppressLint("IconColors")
    public Notification createForegroundNotification() {

        notificationUsage = NotificationUsage.NOTIFICATION_FOREGROUND_SERVICE;

        //Init remoteView for expanded notification
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.foreground_service_notification);

        //Set button for disable alarm with its intents
        Intent btnClickIntent = new Intent(context.getApplicationContext(), AlarmReceiver.class);
        PendingIntent btnClickPendingIntent;
        AlarmEntity alarmEntity = (AlarmEntity) arrayList.get(0);
        if (alarmEntity != null && alarmEntity.getTempDisabled()) {
            btnClickIntent.putExtra(context.getApplicationContext().getString(R.string.alarmmanager_key), AlarmReceiverUsage.DISABLE_ALARM.name());

            remoteViews.setTextViewText(R.id.btnDisableAlarmNotification, context.getString(R.string.btn_reactivate_alarm));

            btnClickPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), AlarmReceiverUsage.Companion.getCount(AlarmReceiverUsage.DISABLE_ALARM), btnClickIntent, PendingIntent.FLAG_IMMUTABLE);
            remoteViews.setOnClickPendingIntent(R.id.btnDisableAlarmNotification, btnClickPendingIntent);
        } else {
            btnClickIntent.putExtra(context.getApplicationContext().getString(R.string.alarmmanager_key), AlarmReceiverUsage.DISABLE_ALARM.name());

            remoteViews.setTextViewText(R.id.btnDisableAlarmNotification, context.getString(R.string.btn_disable_alarm));

            btnClickPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), AlarmReceiverUsage.Companion.getCount(AlarmReceiverUsage.DISABLE_ALARM), btnClickIntent, PendingIntent.FLAG_IMMUTABLE);
            remoteViews.setOnClickPendingIntent(R.id.btnDisableAlarmNotification, btnClickPendingIntent);
        }

        if(((int) arrayList.get(1) <= Constants.NOT_SLEEP_BUTTON_DELAY) && ((int) arrayList.get(1) > Constants.FOREGROUND_SERVICE_NOTIFICATION_DELAY_SLEEP_TIME)) {
            //Set button for not sleeping
            btnClickIntent = new Intent(context.getApplicationContext(), AlarmReceiver.class);
            btnClickIntent.putExtra(context.getApplicationContext().getString(R.string.alarmmanager_key), AlarmReceiverUsage.NOT_SLEEPING.name());
            remoteViews.setTextViewText(R.id.btnNotSleepingNotification, context.getString(R.string.btn_not_sleeping_text));

            btnClickPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), AlarmReceiverUsage.Companion.getCount(AlarmReceiverUsage.NOT_SLEEPING), btnClickIntent, PendingIntent.FLAG_IMMUTABLE);
            remoteViews.setOnClickPendingIntent(R.id.btnNotSleepingNotification, btnClickPendingIntent);

            remoteViews.setViewVisibility(R.id.btnNotSleepingNotification, View.VISIBLE);
        } else if ((int) arrayList.get(1) <= Constants.FOREGROUND_SERVICE_NOTIFICATION_DELAY_SLEEP_TIME) {
            //Set button for not sleeping
            btnClickIntent = new Intent(context.getApplicationContext(), AlarmReceiver.class);
            btnClickIntent.putExtra(context.getApplicationContext().getString(R.string.alarmmanager_key), AlarmReceiverUsage.NOT_SLEEPING.name());
            remoteViews.setTextViewText(R.id.btnNotSleepingNotification, context.getString(R.string.btn_not_sleeping_text));

            btnClickPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), AlarmReceiverUsage.Companion.getCount(AlarmReceiverUsage.NOT_SLEEPING), btnClickIntent, PendingIntent.FLAG_IMMUTABLE);
            remoteViews.setOnClickPendingIntent(R.id.btnNotSleepingNotification, btnClickPendingIntent);

            remoteViews.setViewVisibility(R.id.btnNotSleepingNotification, View.GONE);
        } else if (!((boolean) arrayList.get(3))) {
            //Set button for currently not sleeping
            btnClickIntent = new Intent(context.getApplicationContext(), AlarmReceiver.class);
            btnClickIntent.putExtra(context.getApplicationContext().getString(R.string.alarmmanager_key), AlarmReceiverUsage.CURRENTLY_NOT_SLEEPING.name());
            remoteViews.setTextViewText(R.id.btnNotSleepingNotification, context.getString(R.string.btn_currently_not_sleeping_text));

            btnClickPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), AlarmReceiverUsage.Companion.getCount(AlarmReceiverUsage.CURRENTLY_NOT_SLEEPING), btnClickIntent, PendingIntent.FLAG_IMMUTABLE);
            remoteViews.setOnClickPendingIntent(R.id.btnNotSleepingNotification, btnClickPendingIntent);

            remoteViews.setViewVisibility(R.id.btnNotSleepingNotification, View.VISIBLE);
        }

        if (alarmEntity != null && alarmEntity.getTempDisabled()) {
            arrayList.set(2, false);
        }

        String contentText;
        if ((boolean) arrayList.get(2)) {
            contentText = SmileySelectorUtil.getSmileyAlarmActive() + context.getString(R.string.alarm_status_true);
        } else {
            contentText = SmileySelectorUtil.getSmileyAlarmNotActive() + context.getString(R.string.alarm_status_false);
        }

        String sleepStateText;
        if ((boolean) arrayList.get(3)) {
            sleepStateText = SmileySelectorUtil.getSmileySleep() + context.getString(R.string.sleep_status_true);
        } else {
            sleepStateText = SmileySelectorUtil.getSmileySleep() + context.getString(R.string.sleep_status_false);
        }

        String sleepTimeText;
        if ((int) arrayList.get(1) >= Constants.FOREGROUND_SERVICE_NOTIFICATION_DELAY_SLEEP_TIME) {
            sleepTimeText = SmileySelectorUtil.getSmileyTime() + context.getString(R.string.foregroundservice_notification_sleeptime)+ " " + TimeConverterUtil.toTimeFormat(TimeConverterUtil.minuteToTimeFormat((int) arrayList.get(1))[0], TimeConverterUtil.minuteToTimeFormat((int) arrayList.get(1))[1]);
        } else {
            sleepTimeText = SmileySelectorUtil.getSmileyTime() + context.getString(R.string.foregroundservice_notification_sleeptime)+ " " + 0 + "h " + "00" + "min";
        }

        String alarmTimeText = SmileySelectorUtil.getSmileyAlarmClock() + context.getString(R.string.foregroundservice_notification_alarmtime)+ " " + TimeConverterUtil.toTimeFormat(TimeConverterUtil.millisToTimeFormat((int) arrayList.get(4))[0], TimeConverterUtil.millisToTimeFormat((int) arrayList.get(4))[1]);

        //Set the text in textview of the expanded notification view
        boolean[] bannerConfig = (boolean[]) arrayList.get(5);

        if (bannerConfig[0]) {
            remoteViews.setTextViewText(R.id.tvBannerAlarmActive, contentText);
            remoteViews.setViewVisibility(R.id.tvBannerAlarmActive, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(R.id.tvBannerAlarmActive, View.GONE);
        }

        if (bannerConfig[1]) {
            remoteViews.setTextViewText(R.id.tvBannerActualWakeup, alarmTimeText);
            remoteViews.setViewVisibility(R.id.tvBannerActualWakeup, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(R.id.tvBannerActualWakeup, View.GONE);
        }

        if (bannerConfig[2]) {
            remoteViews.setTextViewText(R.id.tvBannerActualSleeptime, sleepTimeText);
            remoteViews.setViewVisibility(R.id.tvBannerActualSleeptime, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(R.id.tvBannerActualSleeptime, View.GONE);
        }

        if (bannerConfig[3]) {
            remoteViews.setTextViewText(R.id.tvBannerIsSleeping, sleepStateText);
            remoteViews.setViewVisibility(R.id.tvBannerIsSleeping, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(R.id.tvBannerIsSleeping, View.INVISIBLE);
        }

        //Set the Intent for tap on the notification, it will launch MainActivity
        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), ActivityIntentUsage.Companion.getCount(ActivityIntentUsage.MAIN_ACTIVITY), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Since Oreo there is a Notification Service needed
        createNotificationChannel(context.getString(R.string.foregroundservice_channel), context.getString(R.string.foregroundservice_channel_name), context.getString(R.string.foregroundservice_channel_description));

        Notification.Builder builder;
        builder = new Notification.Builder(context, context.getString(R.string.foregroundservice_channel));

        return builder
                .setContentTitle(context.getString(R.string.foregroundservice_notification_title))
                .setContentText(contentText)
                .setCustomBigContentView(remoteViews)
                .setStyle(new Notification.DecoratedCustomViewStyle())
                .setSmallIcon(R.drawable.logofullroundtransparentwhite)
                .setColor(ContextCompat.getColor(context.getApplicationContext(), R.color.accent_text_color))
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .build();
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private Notification createAlarmClockNotification() {

        createNotificationChannel(context.getString(R.string.alarm_clock_channel), context.getString(R.string.alarm_clock_channel_name), context.getString(R.string.alarm_clock_channel_description));

        // Intent, which starts with tap on the Notification
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, ActivityIntentUsage.Companion.getCount(ActivityIntentUsage.MAIN_ACTIVITY), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Intent, which starts with tap on the cancel button
        Intent cancelAlarmIntent = new Intent(context, AlarmClockReceiver.class);
        cancelAlarmIntent.putExtra(context.getString(R.string.alarm_clock_intent_key), AlarmClockReceiverUsage.STOP_ALARMCLOCK.name());
        cancelAlarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent cancelAlarmPendingIntent = PendingIntent.getBroadcast(context, AlarmClockReceiverUsage.Companion.getCount(AlarmClockReceiverUsage.STOP_ALARMCLOCK), cancelAlarmIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

        // Intent, which starts with tap on the snooze button
        Intent snoozeAlarmIntent = new Intent(context, AlarmClockReceiver.class);
        snoozeAlarmIntent.putExtra(context.getString(R.string.alarm_clock_intent_key), AlarmClockReceiverUsage.SNOOZE_ALARMCLOCK.name());
        snoozeAlarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent snoozeAlarmPendingIntent = PendingIntent.getBroadcast(context, AlarmClockReceiverUsage.Companion.getCount(AlarmClockReceiverUsage.SNOOZE_ALARMCLOCK), snoozeAlarmIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        //Starts a new singleton audio class and init it, if not init yet
        AlarmClockAudio.getInstance().init(context);
        AlarmClockAudio.getInstance().startAlarm(true);

        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(context, context.getString(R.string.alarm_clock_channel));

        //Build the builder for the notification
        return builder
                .setSmallIcon(R.drawable.logofullroundtransparentwhite)
                .setColor(ContextCompat.getColor(context.getApplicationContext(), R.color.accent_text_color))
                .setContentTitle(context.getString(R.string.alarm_notification_title))
                .setContentText(context.getString(R.string.alarm_notification_text))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(cancelAlarmPendingIntent)
                .setDeleteIntent(cancelAlarmPendingIntent)
                .setFullScreenIntent(pendingIntent, true)
                .addAction(R.drawable.logofullroundtransparentwhite, context.getString(R.string.alarm_notification_button_1), cancelAlarmPendingIntent)
                .addAction(R.drawable.logofullroundtransparentwhite, context.getString(R.string.alarm_notification_button_2), snoozeAlarmPendingIntent)
                .build();
    }

    private Notification createAlarmClockLockScreen() {
        createNotificationChannel(context.getString(R.string.alarm_clock_channel), context.getString(R.string.alarm_clock_channel_name), context.getString(R.string.alarm_clock_channel_description));

        Intent fullScreenIntent = new Intent(context, LockScreenAlarmActivity.class);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, ActivityIntentUsage.Companion.getCount(ActivityIntentUsage.LOCKSCREEN_ACTIVITY), fullScreenIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, ActivityIntentUsage.Companion.getCount(ActivityIntentUsage.LOCKSCREEN_ACTIVITY), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(context, context.getString(R.string.alarm_clock_channel));

        return builder
                .setSmallIcon(R.drawable.logofullroundtransparentwhite)
                .setContentTitle(context.getString(R.string.alarm_notification_title))
                .setContentText(context.getString(R.string.alarm_notification_text))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .build();
    }

    /**
     * Cancel an existing notification
     * @param notificationUsage Usage of notification to be canceled
     */
    public static void cancelNotification(NotificationUsage notificationUsage, Context context) {
        NotificationManagerCompat.from(context).cancel(NotificationUsage.Companion.getCount(notificationUsage));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isNotificationActive(NotificationUsage notificationUsage, Context context) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
        for (StatusBarNotification notification : notifications) {
            if (notification.getId() == NotificationUsage.Companion.getCount(notificationUsage)) {
                return true;
            }
        }

        return false;
    }
}

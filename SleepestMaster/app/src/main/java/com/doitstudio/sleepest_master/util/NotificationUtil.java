package com.doitstudio.sleepest_master.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.doitstudio.sleepest_master.MainActivity;
import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.alarmclock.AlarmClockAudio;
import com.doitstudio.sleepest_master.alarmclock.AlarmClockReceiver;
import com.doitstudio.sleepest_master.background.AlarmReceiver;
import com.doitstudio.sleepest_master.model.data.ActivityIntentUsage;
import com.doitstudio.sleepest_master.model.data.AlarmClockReceiverUsage;
import com.doitstudio.sleepest_master.model.data.AlarmReceiverUsage;
import com.doitstudio.sleepest_master.model.data.Constants;
import com.doitstudio.sleepest_master.model.data.NotificationUsage;
import com.doitstudio.sleepest_master.storage.db.AlarmEntity;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class NotificationUtil {

    private Context context;
    private NotificationUsage notificationUsage;
    private ArrayList<Object> arrayList;

    public NotificationUtil(Context context, NotificationUsage notificationUsage, ArrayList<Object> arrayList) {
        this.context = context;
        this.notificationUsage = notificationUsage;
        this.arrayList = arrayList;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void chooseNotification() {

        SmileySelectorUtil smileySelectorUtil = new SmileySelectorUtil();

        Notification notification = null;
        switch (notificationUsage) {
            case NOTIFICATION_FOREGROUND_SERVICE:
                notification = createForegroundNotification();
                break;
            case NOTIFICATION_USER_SHOULD_SLEEP:
                notification = createInformationNotification(smileySelectorUtil.getSmileyAttention() + context.getString(R.string.information_notification_text_sleeptime_problem), NotificationUsage.Companion.getCount(NotificationUsage.NOTIFICATION_USER_SHOULD_SLEEP));
                break;
            case NOTIFICATION_NO_API_DATA:
                notification = createInformationNotification(smileySelectorUtil.getSmileyAttention() + context.getString(R.string.information_notification_text_sleep_api_problem), NotificationUsage.Companion.getCount(NotificationUsage.NOTIFICATION_NO_API_DATA));
                break;
            case NOTIFICATION_ALARM_CLOCK:
                notification = createAlarmClockNotification();
                break;
        }

        if (notification != null && notificationUsage == NotificationUsage.NOTIFICATION_FOREGROUND_SERVICE) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, notification);
        } else if (notification != null) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notificationUsage.Companion.getCount(notificationUsage), notification);
        }

    }


    private void createNotificationChannel(String channelId, String channelName, String channelDescription) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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

    private Notification createInformationNotification(String information, int usage) {
        //Get Channel id
        String notificationChannelId = context.getApplicationContext().getString(R.string.information_notification_channel);

        Intent informationIntent;
        PendingIntent informationPendingIntent;
        String buttonText;

        if (usage == NotificationUsage.Companion.getCount(NotificationUsage.NOTIFICATION_NO_API_DATA)) {

            informationIntent = new Intent(context.getApplicationContext(), AlarmReceiver.class);
            informationIntent.putExtra(context.getString(R.string.alarmmanager_key), AlarmReceiverUsage.SOLVE_API_PROBLEM.name());
            informationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            informationPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), AlarmReceiverUsage.Companion.getCount(AlarmReceiverUsage.SOLVE_API_PROBLEM), informationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            buttonText = "Solve it";

        } else  {

            informationIntent = new Intent(context.getApplicationContext(), AlarmReceiver.class);
            informationIntent.putExtra(context.getString(R.string.alarmmanager_key), AlarmReceiverUsage.SOLVE_API_PROBLEM.name());
            informationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            informationPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), AlarmReceiverUsage.Companion.getCount(AlarmReceiverUsage.SOLVE_API_PROBLEM), informationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            buttonText = "Good night";
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
                .setSmallIcon(R.drawable.logofullroundtransparentwhite)
                .setColor(ContextCompat.getColor(context.getApplicationContext(), R.color.accent_text_color))
                .setAutoCancel(true)
                .setContentIntent(informationPendingIntent)
                .setDeleteIntent(informationPendingIntent)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(R.drawable.logofullroundtransparentwhite, buttonText, informationPendingIntent)
                .build();
    }

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

            btnClickPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), AlarmReceiverUsage.Companion.getCount(AlarmReceiverUsage.DISABLE_ALARM), btnClickIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.btnDisableAlarmNotification, btnClickPendingIntent);
        } else {
            btnClickIntent.putExtra(context.getApplicationContext().getString(R.string.alarmmanager_key), AlarmReceiverUsage.DISABLE_ALARM.name());

            remoteViews.setTextViewText(R.id.btnDisableAlarmNotification, context.getString(R.string.btn_disable_alarm));

            btnClickPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), AlarmReceiverUsage.Companion.getCount(AlarmReceiverUsage.DISABLE_ALARM), btnClickIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.btnDisableAlarmNotification, btnClickPendingIntent);
        }

        if(((int) arrayList.get(1) <= 60) && ((int) arrayList.get(1) > 0)) {
            //Set button for not sleeping
            btnClickIntent = new Intent(context.getApplicationContext(), AlarmReceiver.class);
            btnClickIntent.putExtra(context.getApplicationContext().getString(R.string.alarmmanager_key), AlarmReceiverUsage.NOT_SLEEPING.name());
            remoteViews.setTextViewText(R.id.btnNotSleepingNotification, context.getString(R.string.btn_not_sleeping_text));

            btnClickPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), AlarmReceiverUsage.Companion.getCount(AlarmReceiverUsage.NOT_SLEEPING), btnClickIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.btnNotSleepingNotification, btnClickPendingIntent);

            remoteViews.setViewVisibility(R.id.btnNotSleepingNotification, View.VISIBLE);
        } else if ((int) arrayList.get(1) <= 0) {
            //Set button for not sleeping
            btnClickIntent = new Intent(context.getApplicationContext(), AlarmReceiver.class);
            btnClickIntent.putExtra(context.getApplicationContext().getString(R.string.alarmmanager_key), AlarmReceiverUsage.NOT_SLEEPING.name());
            remoteViews.setTextViewText(R.id.btnNotSleepingNotification, context.getString(R.string.btn_not_sleeping_text));

            btnClickPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), AlarmReceiverUsage.Companion.getCount(AlarmReceiverUsage.NOT_SLEEPING), btnClickIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.btnNotSleepingNotification, btnClickPendingIntent);

            remoteViews.setViewVisibility(R.id.btnNotSleepingNotification, View.GONE);
        } else {
            //Set button for currently not sleeping
            btnClickIntent = new Intent(context.getApplicationContext(), AlarmReceiver.class);
            btnClickIntent.putExtra(context.getApplicationContext().getString(R.string.alarmmanager_key), AlarmReceiverUsage.CURRENTLY_NOT_SLEEPING.name());
            remoteViews.setTextViewText(R.id.btnNotSleepingNotification, context.getString(R.string.btn_currently_not_sleeping_text));

            btnClickPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), AlarmReceiverUsage.Companion.getCount(AlarmReceiverUsage.CURRENTLY_NOT_SLEEPING), btnClickIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.btnNotSleepingNotification, btnClickPendingIntent);

            remoteViews.setViewVisibility(R.id.btnNotSleepingNotification, View.VISIBLE);
        }

        if (alarmEntity != null && alarmEntity.getTempDisabled()) {
            arrayList.set(2, false);
        }

        SmileySelectorUtil smileySelectorUtil = new SmileySelectorUtil();

        String contentText;
        if ((boolean) arrayList.get(2)) {
            contentText = smileySelectorUtil.getSmileyAlarmActive() + context.getString(R.string.alarm_status_true);
        } else {
            contentText = smileySelectorUtil.getSmileyAlarmNotActive() + context.getString(R.string.alarm_status_false);
        }

        String sleepStateText;
        if ((boolean) arrayList.get(3)) {
            sleepStateText = smileySelectorUtil.getSmileySleep() + context.getString(R.string.sleep_status_true);
        } else {
            sleepStateText = smileySelectorUtil.getSmileySleep() + context.getString(R.string.sleep_status_false);
        }

        String sleeptimeText = smileySelectorUtil.getSmileyTime() + "Sleep time: " + TimeConverterUtil.minuteToTimeFormat((int) arrayList.get(1))[0] + "h " + TimeConverterUtil.minuteToTimeFormat((int) arrayList.get(1))[1] + "min";
        String alarmtimeText = smileySelectorUtil.getSmileyAlarmClock() + "Alarm time: " + TimeConverterUtil.millisToTimeFormat((int) arrayList.get(4))[0] + ":" + TimeConverterUtil.millisToTimeFormat((int) arrayList.get(4))[1];

        //Set the text in textview of the expanded notification view
        boolean[] bannerConfig = (boolean[]) arrayList.get(5);

        if (bannerConfig[0]) {
            remoteViews.setTextViewText(R.id.tvBannerAlarmActive, contentText + " sub:" + arrayList.get(6) + ",VA:" + arrayList.get(7));
            remoteViews.setViewVisibility(R.id.tvBannerAlarmActive, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(R.id.tvBannerAlarmActive, View.GONE);
        }

        if (bannerConfig[1]) {
            remoteViews.setTextViewText(R.id.tvBannerActualWakeup, alarmtimeText);
            remoteViews.setViewVisibility(R.id.tvBannerActualWakeup, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(R.id.tvBannerActualWakeup, View.GONE);
        }

        if (bannerConfig[2]) {
            remoteViews.setTextViewText(R.id.tvBannerActualSleeptime, sleeptimeText);
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
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), ActivityIntentUsage.Companion.getCount(ActivityIntentUsage.MAIN_ACTIVITY), intent, PendingIntent.FLAG_UPDATE_CURRENT);

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
        PendingIntent pendingIntent = PendingIntent.getActivity(context, ActivityIntentUsage.Companion.getCount(ActivityIntentUsage.MAIN_ACTIVITY), intent, PendingIntent.FLAG_UPDATE_CURRENT);

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

        Intent autoCancelIntent = new Intent("NOTIFICATION_DELETED");
        autoCancelIntent.putExtra(context.getString(R.string.alarm_clock_intent_key), AlarmClockReceiverUsage.STOP_ALARMCLOCK.name());
        autoCancelIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent autoCancelPendingIntent = PendingIntent.getBroadcast(context, AlarmClockReceiverUsage.Companion.getCount(AlarmClockReceiverUsage.STOP_ALARMCLOCK), autoCancelIntent, PendingIntent.FLAG_ONE_SHOT);


        //Starts a new singleton audio class and init it, if not init yet
        AlarmClockAudio.getInstance().init(context);
        AlarmClockAudio.getInstance().startAlarm();

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

        //NotificationManagerCompat.from(context).notify(Constants.ALARM_CLOCK_NOTIFICATION_ID, notificationBuilder.build());


    }
}

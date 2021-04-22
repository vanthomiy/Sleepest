package com.doitstudio.sleepest_master.background;

/**
 * This class inherits from LifecycleService. It implements all functions of the foreground service
 * like start, stop and foreground notification
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.AlarmClock;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import com.doitstudio.sleepest_master.Alarm;
import com.doitstudio.sleepest_master.LiveUserSleepActivity;
import com.doitstudio.sleepest_master.MainActivity;
import com.doitstudio.sleepest_master.SleepApiData;
import com.doitstudio.sleepest_master.alarmclock.AlarmClockReceiver;
import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.model.data.Actions;
import com.doitstudio.sleepest_master.sleepapi.SleepHandler;
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler;
import com.doitstudio.sleepest_master.storage.DataStoreRepository;

import java.util.Calendar;

public class ForegroundService extends LifecycleService {

    //private final ServiceLifecycleDispatcher mDispatcher = new ServiceLifecycleDispatcher( this);
    private PowerManager.WakeLock wakeLock = null;
    private boolean isServiceStarted = false;
    private SleepCalculationHandler sleepCalculationHandler;
    private SleepHandler sleepHandler;

    private boolean isAlarmActive = false;
    private int sleepValueAmount = 0;
    private boolean isSubscribed = false;
    private int userSleepTime = 0;
    private boolean isSleeping = false;
    private boolean isDataAvailable = false;
    private int userSleepTimeRest = 0;

    private boolean isStartet = false;

    ForegroundObserver foregroundObserver;
    DataStoreRepository dataStoreRepository;


    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        //mDispatcher.onServicePreSuperOnBind();
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags,startId);

        if (intent != null) {
            String action = intent.getAction();

            if (action != null) {

                if (action.equals(Actions.START.name())) {
                    startService();
                } else if (action.equals(Actions.STOP.name())) {
                    stopService();
                }
            }
        }

        return START_STICKY; // by returning this we make sure the service is restarted if the system kills the service
    }



    @Override
    public void onCreate() {
        super.onCreate();

        /**TODO: Variablen initialisieren!!*/

        foregroundObserver = new ForegroundObserver (this);
        foregroundObserver.setAlarmTime(25200);
        userSleepTimeRest = 25200;

        startForeground(1, createNotification("Test")); /** TODO: Id zentral anlegen */

        sleepCalculationHandler = SleepCalculationHandler.Companion.getHandler(getApplicationContext());
        sleepHandler = SleepHandler.Companion.getHandler(getApplicationContext());


    }

    public void OnAlarmChanged(Alarm alarm){

        //ALarm time is seconds of day
        if (userSleepTimeRest != alarm.getAlarmTime()) {

            userSleepTimeRest = (int) alarm.getAlarmTime();

            AlarmReceiver.cancelAlarm(getApplicationContext(), 2);
            AlarmClockReceiver.cancelAlarm(getApplicationContext());
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, userSleepTimeRest);
            AlarmReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), getApplicationContext(), 2);
            AlarmClockReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), getApplicationContext());
        }

        isAlarmActive = alarm.getIsActive();
        updateNotification("test");
    }

    public void OnSleepApiDataChanged(SleepApiData sleepApiData){
        sleepValueAmount = sleepApiData.getSleepApiValuesAmount();
        isSubscribed = sleepApiData.getIsSubscribed();
        updateNotification("test");
    }

    public void OnSleepTimeChanged(LiveUserSleepActivity liveUserSleepActivity){
        isDataAvailable = liveUserSleepActivity.getIsDataAvailable();
        userSleepTime = liveUserSleepActivity.getUserSleepTime();
        isSleeping = liveUserSleepActivity.getIsUserSleeping();
        updateNotification("test");
    }

    @Override
    public void onDestroy() {
        //mDispatcher.onServicePreSuperOnDestroy();
        super.onDestroy();
    }

    // Starts the service and start a thread for foreground processes
    private void startService() {
        // If the service is already running, do nothing.
        if (isServiceStarted) {return;}

        //Set start boolean and save it in preferences
        isServiceStarted = true;
        new ServiceTracker().setServiceState(this, ServiceState.STARTED);

        // lock that service is not affected by Doze Mode
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(1, "EndlessService::lock");
            wakeLock.acquire(60 * 1000L /*1 minute*/);
        }

        // Create a thread and loop while the service is running.
        Thread thread = new Thread(() -> {
            while (isServiceStarted) {
                try {

                    /** TODO: do something if neccessary */
                    Calendar calendar = Calendar.getInstance();
                    if ((calendar.get(Calendar.HOUR_OF_DAY) == 6) && (calendar.get(Calendar.MINUTE) >= 30) && !isStartet) {
                        isStartet = true;
                        test();
                    }
                    Thread.sleep(60000); //milliseconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        // Start thread.
        thread.start();

        /**
         * TEST
         * */
        Calendar calenderAlarm = AlarmReceiver.getAlarmDate(Calendar.getInstance().get(Calendar.DAY_OF_WEEK), 15, 10);
        AlarmReceiver.startAlarmManager(calenderAlarm.get(Calendar.DAY_OF_WEEK), calenderAlarm.get(Calendar.HOUR_OF_DAY), calenderAlarm.get(Calendar.MINUTE), getApplicationContext(), 2);
        Workmanager.startPeriodicWorkmanager(16, getApplicationContext());
        AlarmClockReceiver.startAlarmManager(calenderAlarm.get(Calendar.DAY_OF_WEEK), calenderAlarm.get(Calendar.HOUR_OF_DAY), calenderAlarm.get(Calendar.MINUTE), getApplicationContext());

        sleepCalculationHandler.calculateLiveUserSleepActivityJob();
        sleepHandler.startSleepHandler();
    }

    void test() {
        Thread thread2 = new Thread(() -> {
            while (isServiceStarted) {
                try {

                    /** TODO: do something if neccessary */
                    sleepCalculationHandler.calculateUserWakeupJob();
                    Thread.sleep(60000); //milliseconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        // Start thread.
        thread2.start();
    }

    // Stop the foreground service
    private void stopService() {
        Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show();
        try {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
            stopForeground(true);
            stopSelf();
        } catch (Exception e) {

        }
        //Save state in preferences
        isServiceStarted = false;
        new ServiceTracker().setServiceState(this, ServiceState.STOPPED);

        /**
         * TEST
         */
        Workmanager.stopPeriodicWorkmanager();
        Calendar calendarAlarm = AlarmReceiver.getAlarmDate(Calendar.getInstance().get(Calendar.DAY_OF_WEEK), 20, 0);
        AlarmReceiver.startAlarmManager(calendarAlarm.get(Calendar.DAY_OF_WEEK), calendarAlarm.get(Calendar.HOUR_OF_DAY) , calendarAlarm.get(Calendar.MINUTE), getApplicationContext(), 1);
        sleepCalculationHandler.recalculateUserSleep();
        sleepHandler.stopSleepHandler();
    }

    /**
     * Updates the notification banner with a new text
     * @param text The text at the notification banner
     */
    public void updateNotification(String text) {

        Notification notification = createNotification(text);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notification);

    }

    /**TODO Notification noch selbst machen mit eigenem Layout*/
    /**
     * Creats a notification banner, that is permament to show that the app is still running. Only since Oreo
     * @param text The text in the collapsed notification view
     * @return Notification.Builder
     */
    private Notification createNotification(String text) {
        String notificationChannelId = getString(R.string.foregroundservice_channel);

        //Init remoteView for expanded notification
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.foreground_service_notification);

        //Set button with its intents
        Intent btnClickIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        btnClickIntent.putExtra(getApplicationContext().getString(R.string.alarmmanager_key), 3);

        remoteViews.setTextViewText(R.id.btnEnableAlarmNotification, "Disable Alarm");

        PendingIntent btnClickPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 3, btnClickIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.btnEnableAlarmNotification, btnClickPendingIntent);

        //Set the text in textview of the expanded notification view
        String notificationText = "AlarmActive: " + isAlarmActive + " Value: " + sleepValueAmount
                + "\nIsSubscribed: " + isSubscribed + " SleepTime: " + userSleepTime
                + "\nIsSleeping: " + isSleeping + " Wakeup: " + userSleepTimeRest;
        remoteViews.setTextViewText(R.id.tvTextAlarm, notificationText);

        //Set the progress bar for the sleep progress
        remoteViews.setProgressBar(R.id.pbSleepProgressNotification, 100,
                getSleepProgress(20, 9, Calendar.getInstance().get(Calendar.HOUR_OF_DAY)), false);

        //Set the Intent for tap on the notification, will start app in MainActivity
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Since Oreo there is a Notification Service needed
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                notificationChannelId,
                getString(R.string.foregroundservice_channel_name),
                NotificationManager.IMPORTANCE_HIGH
        );

        channel.setDescription(getString(R.string.foregroundservice_channel_description));

        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }

        Notification.Builder builder;
        builder = new Notification.Builder(this, notificationChannelId);

        return builder
                .setContentTitle(getString(R.string.foregroundservice_notification_title))
                .setContentText(text)
                .setCustomBigContentView(remoteViews)
                .setStyle(new Notification.DecoratedCustomViewStyle())
                /*+ "\nSleepValueAmount: " + sleepValueAmount
                + "\nIsSubscribed: " + isSubscribed
                + "\nSleepTime: " + userSleepTime
                + "\nIsSleeping: " + isSleeping))*/
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Ticker text")
                .setContentIntent(pendingIntent)
                .build();
    }

    /**
     * Starts oder stops the foreground service. This function must be called to start or stop service
     * @param action Enum Action (START or STOP)
     * @param context Application context
     */
    public static void startOrStopForegroundService(Actions action, Context context) {

        Intent intent = new Intent(context, ForegroundService.class);
        intent.setAction(action.name());

        if (new ServiceTracker().getServiceState(context) == ServiceState.STOPPED && action == Actions.STOP)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);

                return;
            }
        context.startService(intent);
    }

    private int getSleepProgress(int beginTime, int endTime, int actualTime) {
        int progress;

        if (beginTime < endTime) {
            progress = (actualTime - beginTime) / (endTime - beginTime) * 100;
        } else {
            if (actualTime <= 23 && actualTime > beginTime) {
                progress = (actualTime - beginTime) / (endTime + 24 - beginTime) * 100;
            } else {
                progress = (actualTime + 24 - beginTime) / (endTime + 24 - beginTime) * 100;
            }
        }

        return progress;
    }

    /*
    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mDispatcher.getLifecycle();
    }*/
}

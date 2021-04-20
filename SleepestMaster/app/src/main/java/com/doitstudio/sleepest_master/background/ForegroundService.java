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
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;
import androidx.lifecycle.LifecycleService;
import com.doitstudio.sleepest_master.Alarm;
import com.doitstudio.sleepest_master.LiveUserSleepActivity;
import com.doitstudio.sleepest_master.MainActivity;
import com.doitstudio.sleepest_master.SleepApiData;
import com.doitstudio.sleepest_master.alarmclock.AlarmClockReceiver;
import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.model.data.Actions;
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler;
import com.doitstudio.sleepest_master.storage.DataStoreRepository;

import java.util.Calendar;

public class ForegroundService extends LifecycleService {

    //private final ServiceLifecycleDispatcher mDispatcher = new ServiceLifecycleDispatcher( this);
    private PowerManager.WakeLock wakeLock = null;
    private boolean isServiceStarted = false;
    public SleepCalculationHandler sleepCalculationHandler;

    private boolean isAlarmActive = false;
    private int sleepValueAmount = 0;
    private boolean isSubscribed = false;
    private int userSleepTime = 0;
    private boolean isSleeping = false;

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

    ForegroundObserver foregroundObserver;

    @Override
    public void onCreate() {
        super.onCreate();

        startForeground(1, createNotification("Test")); /** TODO: Id zentral anlegen */

        foregroundObserver = new ForegroundObserver (this);
    }

    public void OnAlarmChanged(Alarm alarm){
        //isAlarmActive = alarm.getIsActive();
        updateNotification("test");
    }

    public void OnSleepApiDataChanged(SleepApiData sleepApiData){
        sleepValueAmount = sleepApiData.getSleepApiValuesAmount();
        isSubscribed = sleepApiData.getIsSubscribed();
        updateNotification("test");
    }

    public void OnSleepTimeChanged(LiveUserSleepActivity liveUserSleepActivity){
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
                    Thread.sleep(60000); //milliseconds
                    /** TODO: do something if neccessary */
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
        Calendar calenderAlarm = Calendar.getInstance();
        int day = calenderAlarm.get(Calendar.DAY_OF_WEEK) + 1;
        if (day > 7) {
            day = 1;
        }
        AlarmReceiver.startAlarmManager(day,9,0, getApplicationContext(), 2);
        Workmanager.startPeriodicWorkmanager(30, getApplicationContext());
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
        Calendar calenderAlarm = Calendar.getInstance();
        int day = calenderAlarm.get(Calendar.DAY_OF_WEEK);
        AlarmReceiver.startAlarmManager(day,20,0, getApplicationContext(), 1);
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
     * @param text The text at the notification banner
     * @return Notification.Builder
     */
    private Notification createNotification(String text) {
        String notificationChannelId = getString(R.string.foregroundservice_channel);

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
        //channel.enableLights(true);
        //channel.setLightColor(Color.RED);
        //channel.enableVibration(true);
        //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }


        Notification.Builder builder;
        builder = new Notification.Builder(this, notificationChannelId);

        return builder
                .setContentTitle(getString(R.string.foregroundservice_notification_title))
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText("Alarm active: " + isAlarmActive
                + "\nSleepValueAmount: " + sleepValueAmount
                + "\nIsSubscribed: " + isSubscribed
                + "\nSleepTime: " + userSleepTime
                + "\nIsSleeping: " + isSleeping))
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

    /*
    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mDispatcher.getLifecycle();
    }*/
}

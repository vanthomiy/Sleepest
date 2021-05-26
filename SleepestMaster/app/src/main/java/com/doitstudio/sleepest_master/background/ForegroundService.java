package com.doitstudio.sleepest_master.background;

/**
 * This service class inherits from LifecycleService. It implements all functions of the foreground service
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
import android.widget.RemoteViews;
import android.widget.Toast;
import androidx.lifecycle.LifecycleService;

import com.doitstudio.sleepest_master.LiveUserSleepActivity;
import com.doitstudio.sleepest_master.MainActivity;
import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.SleepApiData;
import com.doitstudio.sleepest_master.alarmclock.AlarmClockReceiver;
import com.doitstudio.sleepest_master.model.data.Actions;
import com.doitstudio.sleepest_master.sleepapi.SleepHandler;
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler;
import com.doitstudio.sleepest_master.storage.DataStoreRepository;
import com.doitstudio.sleepest_master.storage.db.AlarmEntity;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Calendar;

public class ForegroundService extends LifecycleService {

    private PowerManager.WakeLock wakeLock = null;
    private boolean isServiceStarted = false;
    private boolean isAlarmActive = false;
    private int sleepValueAmount = 0;
    private boolean isSubscribed = false;
    private int userSleepTime = 0;
    private boolean isSleeping = false;
    private int alarmTimeInSeconds = 0;
    private boolean alarmClockSet = false;

    private DataStoreRepository dataStoreRepository;
    private AlarmEntity alarmEntity = null;
    private SleepCalculationHandler sleepCalculationHandler;
    private SleepHandler sleepHandler;

    public ForegroundObserver foregroundObserver;

    //region service functions

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
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

        foregroundObserver = new ForegroundObserver (this);

        alarmEntity = foregroundObserver.getNextAlarm();
        foregroundObserver.updateAlarmWasFired(false, alarmEntity.getId());
        dataStoreRepository = DataStoreRepository.Companion.getRepo(getApplicationContext());
        sleepHandler =  SleepHandler.Companion.getHandler(getApplicationContext());

        //Check if already subscribed, otherwise subscribe to SleepApi and
        //start AlarmManager to disable at the end of sleeptime
        if (!foregroundObserver.getSubscribeStatus()) {
            sleepHandler.startSleepHandler();
            AlarmReceiver.cancelAlarm(getApplicationContext(), 6);
            AlarmReceiver.cancelAlarm(getApplicationContext(), 7);
            Workmanager.startPeriodicWorkmanager(16, getApplicationContext());
            Calendar calendar = AlarmReceiver.getAlarmDate(dataStoreRepository.getSleepTimeEndJob());
            AlarmReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), getApplicationContext(),7);
        }



        startForeground(1, createNotification("Alarm status: " + isAlarmActive)); /** TODO: Id zentral anlegen */


        sleepCalculationHandler = SleepCalculationHandler.Companion.getHandler(getApplicationContext());
        //sleepHandler = SleepHandler.Companion.getHandler(getApplicationContext());

        //sleepHandler.stopSleepHandler();


    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // Starts the service and start a thread for foreground processes
    private void startService() {
        // If the service is already running, do nothing.
        if (isServiceStarted) {return;}
        //Set start boolean and save it in preferences
        isServiceStarted = true;
        //new ServiceTracker().setServiceState(this, ServiceState.STARTED);
        foregroundObserver.setForegroundStatus(true);

        // lock that service is not affected by Doze Mode
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(1, "EndlessService::lock");
            wakeLock.acquire(60 * 1000L); //1 minute timeout
        }


        Toast.makeText(getApplicationContext(), "Foregroundservice started", Toast.LENGTH_LONG).show();

        //Workmanager.startPeriodicWorkmanager(times.getWorkmanagerDuration(), getApplicationContext());

        //+1
        //Calendar calenderAlarm;
        //calenderAlarm = AlarmReceiver.getAlarmDate(Calendar.getInstance().get(Calendar.DAY_OF_WEEK),13, 27);
        //AlarmReceiver.startAlarmManager(calenderAlarm.get(Calendar.DAY_OF_WEEK), calenderAlarm.get(Calendar.HOUR_OF_DAY), calenderAlarm.get(Calendar.MINUTE), getApplicationContext(), 2);
        //AlarmClockReceiver.startAlarmManager(calenderAlarm.get(Calendar.DAY_OF_WEEK), calenderAlarm.get(Calendar.HOUR_OF_DAY), calenderAlarm.get(Calendar.MINUTE), getApplicationContext(), 1);

        //Last Alarm    +1
        //calenderAlarm = AlarmReceiver.getAlarmDate(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 1, times.getLastWakeupHour(), times.getLastWakeupMinute());
        //AlarmReceiver.startAlarmManager(calenderAlarm.get(Calendar.DAY_OF_WEEK), calenderAlarm.get(Calendar.HOUR_OF_DAY), calenderAlarm.get(Calendar.MINUTE), getApplicationContext(), 4);
        //AlarmClockReceiver.startAlarmManager(calenderAlarm.get(Calendar.DAY_OF_WEEK), calenderAlarm.get(Calendar.HOUR_OF_DAY), calenderAlarm.get(Calendar.MINUTE), getApplicationContext(), 4);

        //Start Calculation    +1
        Calendar calenderCalculation = AlarmReceiver.getAlarmDate(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 1, 0, 0);
        calenderCalculation.set(Calendar.SECOND, 0);
        calenderCalculation.add(Calendar.SECOND, alarmEntity.getWakeupEarly() - 1800);
        AlarmReceiver.startAlarmManager(calenderCalculation.get(Calendar.DAY_OF_WEEK), calenderCalculation.get(Calendar.HOUR_OF_DAY), calenderCalculation.get(Calendar.MINUTE), getApplicationContext(), 5);

        sleepCalculationHandler.checkIsUserSleeping(null);
        //sleepHandler.startSleepHandler();

        Calendar calendar = Calendar.getInstance();
        SharedPreferences pref = getSharedPreferences("StartService", 0);
        SharedPreferences.Editor ed = pref.edit();
        ed.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
        ed.putInt("minute", calendar.get(Calendar.MINUTE));
        ed.apply();
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

            SharedPreferences pref = getSharedPreferences("StopException", 0);
            SharedPreferences.Editor ed = pref.edit();
            ed.putString("exception", "No Exception");
            ed.apply();
        } catch (Exception e) {
            SharedPreferences pref = getSharedPreferences("StopException", 0);
            SharedPreferences.Editor ed = pref.edit();
            ed.putString("exception", e.toString());
            ed.apply();
        }
        //Save state in preferences
        isServiceStarted = false;
        foregroundObserver.setForegroundStatus(false);
        foregroundObserver.updateAlarmWasFired(true, alarmEntity.getId());
        //new ServiceTracker().setServiceState(this, ServiceState.STOPPED);

        //Workmanager.stopPeriodicWorkmanager();
        WorkmanagerCalculation.stopPeriodicWorkmanager();

        AlarmClockReceiver.cancelAlarm(getApplicationContext(), 4);
        AlarmReceiver.cancelAlarm(getApplicationContext(), 4);
        AlarmReceiver.cancelAlarm(getApplicationContext(), 5);

        //sleepHandler.stopSleepHandler();
        Toast.makeText(getApplicationContext(), "Foregroundservice stopped", Toast.LENGTH_LONG).show();

        Calendar calendar = Calendar.getInstance();
        SharedPreferences pref = getSharedPreferences("StopService", 0);
        SharedPreferences.Editor ed = pref.edit();
        ed.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
        ed.putInt("minute", calendar.get(Calendar.MINUTE));
        ed.putInt("sleeptime", userSleepTime);
        ed.apply();
    }

    //endregion

    //region changing values

    public void OnAlarmChanged(AlarmEntity time) {

        Calendar calendar = Calendar.getInstance();

        alarmTimeInSeconds = time.getActualWakeup();

        isAlarmActive = true;

        updateNotification("Alarm status: " + isAlarmActive);

        SharedPreferences pref = getSharedPreferences("AlarmChanged", 0);
        SharedPreferences.Editor ed = pref.edit();
        ed.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
        ed.putInt("minute", calendar.get(Calendar.MINUTE));
        ed.putInt("actualWakeup", time.getActualWakeup());
        ed.apply();

        int secondsOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.SECOND);
        int timeDifference = time.getActualWakeup() - secondsOfDay;

        //Return if the alarm is on the next day or before first calculation
        if((secondsOfDay < (time.getWakeupEarly() - 1800)) || ((time.getWakeupLate() + 3600) < secondsOfDay) || alarmClockSet) {
            return;
        }

        //Check if the next calculation call will be after latest wakeup and set the alarm to latest wakeup
        if (((time.getWakeupLate() - secondsOfDay) < (16 * 60))) {
            Calendar lastestWakeup = Calendar.getInstance();
            lastestWakeup.set(Calendar.HOUR_OF_DAY, 0);
            lastestWakeup.set(Calendar.MINUTE, 0);
            lastestWakeup.set(Calendar.SECOND, 0);
            lastestWakeup.add(Calendar.SECOND, time.getWakeupLate());
            AlarmClockReceiver.startAlarmManager(lastestWakeup.get(Calendar.DAY_OF_WEEK), lastestWakeup.get(Calendar.HOUR_OF_DAY), lastestWakeup.get(Calendar.MINUTE), getApplicationContext(), 1);
            alarmClockSet = true;
            pref = getSharedPreferences("AlarmChanged", 0);
            ed = pref.edit();
            ed.putInt("alarmUse", 1);
            ed.apply();

            pref = getSharedPreferences("AlarmSet", 0);
            ed = pref.edit();
            ed.putInt("hour", lastestWakeup.get(Calendar.HOUR_OF_DAY));
            ed.putInt("minute", lastestWakeup.get(Calendar.MINUTE));
            ed.putInt("hour1", calendar.get(Calendar.HOUR_OF_DAY));
            ed.putInt("minute1", calendar.get(Calendar.MINUTE));
            ed.putInt("actualWakeup", time.getActualWakeup());
            ed.apply();

            return;
        }

        //Check if the actual wakeup is earlier than the next calculation call
        if (timeDifference < (16 * 60)) {

            //Check if the actual wakeup is earlier than the earliest wakeup and set the alarm to earliest wakeup
            if (time.getActualWakeup() < time.getWakeupEarly()) {
                Calendar earliestWakeup = Calendar.getInstance();
                earliestWakeup.set(Calendar.HOUR_OF_DAY, 0);
                earliestWakeup.set(Calendar.MINUTE, 0);
                earliestWakeup.set(Calendar.SECOND, 0);
                earliestWakeup.add(Calendar.SECOND, time.getWakeupEarly());
                AlarmClockReceiver.startAlarmManager(earliestWakeup.get(Calendar.DAY_OF_WEEK), earliestWakeup.get(Calendar.HOUR_OF_DAY), earliestWakeup.get(Calendar.MINUTE), getApplicationContext(), 1);
                alarmClockSet = true;
                pref = getSharedPreferences("AlarmChanged", 0);
                ed = pref.edit();
                ed.putInt("alarmUse", 2);
                ed.apply();

                pref = getSharedPreferences("AlarmSet", 0);
                ed = pref.edit();
                ed.putInt("hour", earliestWakeup.get(Calendar.HOUR_OF_DAY));
                ed.putInt("minute", earliestWakeup.get(Calendar.MINUTE));
                ed.putInt("hour1", calendar.get(Calendar.HOUR_OF_DAY));
                ed.putInt("minute1", calendar.get(Calendar.MINUTE));
                ed.putInt("actualWakeup", time.getActualWakeup());
                ed.apply();

                return;
            }

            //Check if the actual wakeup is later than the latest wakeup and set the alarm to latest wakeup
            if (time.getActualWakeup() > time.getWakeupLate()) {
                Calendar lastestWakeup = Calendar.getInstance();
                lastestWakeup.set(Calendar.HOUR_OF_DAY, 0);
                lastestWakeup.set(Calendar.MINUTE, 0);
                lastestWakeup.set(Calendar.SECOND, 0);
                lastestWakeup.add(Calendar.SECOND, time.getWakeupLate());
                AlarmClockReceiver.startAlarmManager(lastestWakeup.get(Calendar.DAY_OF_WEEK), lastestWakeup.get(Calendar.HOUR_OF_DAY), lastestWakeup.get(Calendar.MINUTE), getApplicationContext(), 1);
                alarmClockSet = true;
                pref = getSharedPreferences("AlarmChanged", 0);
                ed = pref.edit();
                ed.putInt("alarmUse", 3);
                ed.apply();

                pref = getSharedPreferences("AlarmSet", 0);
                ed = pref.edit();
                ed.putInt("hour", lastestWakeup.get(Calendar.HOUR_OF_DAY));
                ed.putInt("minute", lastestWakeup.get(Calendar.MINUTE));
                ed.putInt("hour1", calendar.get(Calendar.HOUR_OF_DAY));
                ed.putInt("minute1", calendar.get(Calendar.MINUTE));
                ed.putInt("actualWakeup", time.getActualWakeup());
                ed.apply();

                return;
            }

            //Check if the actual alarm time is already reached and set the alarm to now
            if (secondsOfDay > time.getActualWakeup()) {
                calendar.add(Calendar.SECOND, 60);
                AlarmClockReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), getApplicationContext(), 1);
                alarmClockSet = true;
                pref = getSharedPreferences("AlarmChanged", 0);
                ed = pref.edit();
                ed.putInt("alarmUse", 4);
                ed.apply();

                pref = getSharedPreferences("AlarmSet", 0);
                ed = pref.edit();
                ed.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
                ed.putInt("minute", calendar.get(Calendar.MINUTE));
                ed.putInt("hour1", Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                ed.putInt("minute1", Calendar.getInstance().get(Calendar.MINUTE));
                ed.putInt("actualWakeup", time.getActualWakeup());
                ed.apply();
            }
            //Check if the actual time is lower than the actual wakeup and add the difference to the actual time and set the alarm to this new time
            else if (secondsOfDay <= time.getActualWakeup()){
                calendar.add(Calendar.SECOND, timeDifference);
                AlarmClockReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE) + 1, getApplicationContext(), 1);
                alarmClockSet = true;
                pref = getSharedPreferences("AlarmChanged", 0);
                ed = pref.edit();
                ed.putInt("alarmUse", 5);
                ed.apply();

                pref = getSharedPreferences("AlarmSet", 0);
                ed = pref.edit();
                ed.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
                ed.putInt("minute", calendar.get(Calendar.MINUTE));
                ed.putInt("hour1", Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                ed.putInt("minute1", Calendar.getInstance().get(Calendar.MINUTE));
                ed.putInt("actualWakeup", time.getActualWakeup());
                ed.apply();
            }
        }

    }

    public void OnSleepApiDataChanged(SleepApiData sleepApiData){
        sleepValueAmount = sleepApiData.getSleepApiValuesAmount();
        isSubscribed = sleepApiData.getIsSubscribed();

        updateNotification("Alarm status: " + isAlarmActive);

    }

    public void OnSleepTimeChanged(LiveUserSleepActivity liveUserSleepActivity) {

        userSleepTime = liveUserSleepActivity.getUserSleepTime();
        isSleeping = liveUserSleepActivity.getIsUserSleeping();

        updateNotification("Alarm status: " + isAlarmActive);

    }

    //endregion

    //region notification
    /**
     * Updates the notification banner with a new text
     * @param text The text at the notification banner
     */
    public void updateNotification(String text) {

        Notification notification = createNotification(text);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);

    }

    /**TODO Notification noch selbst machen mit eigenem Layout*/
    /**
     * Creats a notification banner, that is permanent to show that the app is still running.
     * @param text The text at the notification banner
     * @return Notification.Builder
     */
    private Notification createNotification(String text) {
        String notificationChannelId = getString(R.string.foregroundservice_channel);

        //Init remoteView for expanded notification
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.foreground_service_notification);

        //Set button for disable alarm with its intents
        Intent btnClickIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        btnClickIntent.putExtra(getApplicationContext().getString(R.string.alarmmanager_key), 3);

        remoteViews.setTextViewText(R.id.btnDisableAlarmNotification, "Disable Alarm");

        PendingIntent btnClickPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 3, btnClickIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.btnDisableAlarmNotification, btnClickPendingIntent);

        //Set button for disable alarm with its intents
        btnClickIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        btnClickIntent.putExtra(getApplicationContext().getString(R.string.alarmmanager_key), 4);

        remoteViews.setTextViewText(R.id.btnNotSleepingNotification, "Not Sleeping");

        btnClickPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 4, btnClickIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.btnNotSleepingNotification, btnClickPendingIntent);

        //Set the text in textview of the expanded notification view
        String notificationText = "AlarmActive: " + isAlarmActive + " Value: " + sleepValueAmount
                + "\nIsSubscribed: " + isSubscribed + " SleepTime: " + userSleepTime
                + "\nIsSleeping: " + isSleeping + " Wakeup: " + alarmTimeInSeconds;
        remoteViews.setTextViewText(R.id.tvTextAlarm, notificationText);

        //Set the progress bar for the sleep progress
        remoteViews.setProgressBar(R.id.pbSleepProgressNotification, 100,
                getSleepProgress(dataStoreRepository.getSleepTimeBeginJob(), dataStoreRepository.getSleepTimeEndJob(), Calendar.getInstance().get(Calendar.HOUR_OF_DAY)), false);

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
                .setCustomBigContentView(remoteViews)
                .setStyle(new Notification.DecoratedCustomViewStyle())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .build();
    }


    /**
     * Starts oder stops the foreground service. This function must be called to start or stop service
     * @param action Enum Action (START or STOP)
     * @param //context Application context
     */
    public static void startOrStopForegroundService(Actions action, Context context) {

        Intent intent = new Intent(context, ForegroundService.class);
        intent.setAction(action.name());

        /*if (new ServiceTracker().getServiceState(context) == ServiceState.STOPPED && action == Actions.STOP) {
            return;
        }*/

        context.startForegroundService(intent);
        return;
    }

    /**
     * Calculates the actual progress of the progressbar
     * @param beginTime Start time of sleeptime in secondsOfDay
     * @param endTime Stop time of sleeptime in secondsOfDay
     * @param actualTime Actual time in hours
     * @return
     */
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

    //endregion

}
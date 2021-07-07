package com.doitstudio.sleepest_master.background;

/**
 * This service class inherits from LifecycleService. It implements all functions of the foreground service
 * like start, stop and foreground notification. Further implements this function the observation of data
 * while the user is sleeping
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.RemoteViews;
import android.widget.Toast;
import androidx.lifecycle.LifecycleService;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.doitstudio.sleepest_master.LiveUserSleepActivity;
import com.doitstudio.sleepest_master.MainActivity;
import com.doitstudio.sleepest_master.MainApplication;
import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.SleepApiData;
import com.doitstudio.sleepest_master.alarmclock.AlarmClockReceiver;
import com.doitstudio.sleepest_master.model.data.Actions;
import com.doitstudio.sleepest_master.model.data.AlarmClockReceiverUsage;
import com.doitstudio.sleepest_master.model.data.AlarmReceiverUsage;
import com.doitstudio.sleepest_master.model.data.Constants;
import com.doitstudio.sleepest_master.googleapi.SleepHandler;
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler;
import com.doitstudio.sleepest_master.storage.DataStoreRepository;
import com.doitstudio.sleepest_master.storage.DatabaseRepository;
import com.doitstudio.sleepest_master.storage.db.AlarmEntity;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ForegroundService extends LifecycleService {

    private PowerManager.WakeLock wakeLock = null; //To keep Service active in background
    private boolean isServiceStarted = false; //Detects if Service startet
    private boolean isAlarmActive = false; //Detects Alarm active status
    private int sleepValueAmount = 0; //Shows the amount of received sleep API elements
    private boolean isSubscribed = false; //Detects the subscription of sleep API
    private int userSleepTime = 0; //Shows the actual sleep time of the user
    private boolean isSleeping = false; //Detects sleep state
    private int alarmTimeInSeconds = 0; //Shows the calculated alarm time
    private int actualWakeUp = 0; //Shows the set alarm clock time
    private boolean userInformed = false; //Detects if the user is already informed about problems with reaching sleep time

    private DataStoreRepository dataStoreRepository; //Instance of DataStoreRepo
    private DatabaseRepository databaseRepository; //Instance of DatabaseRepo
    private AlarmEntity alarmEntity = null; //Instance of AlarmEntity, especially next active alarm
    private SleepCalculationHandler sleepCalculationHandler; //Instance of SleepCalculationHandler
    private SleepHandler sleepHandler; //Instance of SleepHandler
    public ForegroundObserver foregroundObserver; //Instance of the ForegroundObserver for live data

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
                    startService(); //Starts the Foregroundservice
                } else if (action.equals(Actions.STOP.name())) {
                    stopService(); //Stops the Foregroundservice
                }
            }
        }

        return START_STICKY; // by returning this we make sure the service is restarted if the system kills the service
    }



    @Override
    public void onCreate() {
        super.onCreate();

        //Create Instances
        foregroundObserver = new ForegroundObserver (this);
        databaseRepository = ((MainApplication)getApplicationContext()).getDataBaseRepository();
        alarmEntity = databaseRepository.getNextActiveAlarmJob();
        dataStoreRepository = DataStoreRepository.Companion.getRepo(getApplicationContext());
        sleepHandler =  SleepHandler.Companion.getHandler(getApplicationContext());
        sleepCalculationHandler = SleepCalculationHandler.Companion.getHandler(MainApplication.Companion.applicationContext());

        //Info: the following getId() can not be null, because alarmEntity can not be null with getNextAlarm()
        if (alarmEntity != null) {
            foregroundObserver.updateAlarmWasFired(false, alarmEntity.getId());
        }

        //Subscribe to sleep API
        sleepHandler.startSleepHandler();
        AlarmReceiver.cancelAlarm(getApplicationContext(), AlarmReceiverUsage.START_WORKMANAGER);

        //Start Workmanager
        PeriodicWorkRequest periodicDataWork =
                new PeriodicWorkRequest.Builder(Workmanager.class, Constants.WORKMANAGER_DURATION, TimeUnit.MINUTES)
                        .addTag(getApplicationContext().getString(R.string.workmanager1_tag)) //Tag is needed for canceling the periodic work
                        .build();

        WorkManager workManager = WorkManager.getInstance(getApplicationContext());
        workManager.enqueueUniquePeriodicWork(getApplicationContext().getString(R.string.workmanager1_tag), ExistingPeriodicWorkPolicy.KEEP, periodicDataWork);

        Toast.makeText(getApplicationContext(), "Workmanager started", Toast.LENGTH_LONG).show();

        //Set Alarm to stop workmanager at end of sleep time
        Calendar calendar = AlarmReceiver.getAlarmDate(dataStoreRepository.getSleepTimeEndJob());
        AlarmReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), getApplicationContext(),AlarmReceiverUsage.STOP_WORKMANAGER);

        //Start the Foregroundservice
        startForeground(Constants.FOREGROUND_SERVICE_ID, createNotification());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // Starts the service and start a thread for foreground processes
    private void startService() {

        // If the service is already running, do nothing.
        if (isServiceStarted) {return;}
        //Set start boolean
        isServiceStarted = true;
        foregroundObserver.setForegroundStatus(true);

        // lock that service is not affected by Doze Mode
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(Constants.LEVEL_AND_FLAGS, getString(R.string.wakelock_tag));
            wakeLock.acquire(Constants.WAKE_LOCK_TIMEOUT);
        }

        Toast.makeText(getApplicationContext(), "Foregroundservice started", Toast.LENGTH_LONG).show();

        //Set Alarm to start calculation
        Calendar calenderCalculation = AlarmReceiver.getAlarmDate(alarmEntity.getWakeupEarly() - 1800);
        AlarmReceiver.startAlarmManager(calenderCalculation.get(Calendar.DAY_OF_WEEK), calenderCalculation.get(Calendar.HOUR_OF_DAY), calenderCalculation.get(Calendar.MINUTE), getApplicationContext(), AlarmReceiverUsage.START_WORKMANAGER_CALCULATION);

        //Call function with null to transfer actual(local) time for correct calculation
        sleepCalculationHandler.checkIsUserSleeping(null);

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
            //Release wakelock in order to lock foregroundservice against automatic restart
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
            stopForeground(true);
            stopSelf();

            SharedPreferences pref = getSharedPreferences("StopException", 0);
            SharedPreferences.Editor ed = pref.edit();
            ed.putString("exception", "No Exception");
            ed.apply();

            isServiceStarted = false;
            foregroundObserver.setForegroundStatus(false);

            if (alarmEntity != null) {
                foregroundObserver.updateAlarmWasFired(true, alarmEntity.getId());
            }

            //Stop the workmanager for calculating wakeup time
            WorkmanagerCalculation.stopPeriodicWorkmanager();

            //Cancel Alarm for starting Workmanager
            AlarmReceiver.cancelAlarm(getApplicationContext(), AlarmReceiverUsage.START_WORKMANAGER_CALCULATION);

            Toast.makeText(getApplicationContext(), "Foregroundservice stopped", Toast.LENGTH_LONG).show();

            Calendar calendar = Calendar.getInstance();
            pref = getSharedPreferences("StopService", 0);
            ed = pref.edit();
            ed.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
            ed.putInt("minute", calendar.get(Calendar.MINUTE));
            ed.apply();

            pref = getSharedPreferences("SleepTime", 0);
            ed = pref.edit();
            ed.putInt("sleeptime", userSleepTime);
            ed.apply();


        } catch (Exception e) {
            SharedPreferences pref = getSharedPreferences("StopException", 0);
            SharedPreferences.Editor ed = pref.edit();
            ed.putString("exception", e.toString());
            ed.apply();

            //Try stopping foregroundservice after 1 minute again if an error occurs
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 60);
            AlarmReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), getApplicationContext(), AlarmReceiverUsage.STOP_FOREGROUND);

        }

    }

    //endregion

    //region changing values

    public void OnAlarmChanged(AlarmEntity time) {

        //Recheck, if alarm already set but AlarmClockReceiver is not active because of an error
        checkAlarmSet();

        //Update instance of AlarmEntity
        alarmEntity = time;

        //Init calendar
        Calendar calendar = Calendar.getInstance();

        //Update wakeup time
        alarmTimeInSeconds = time.getActualWakeup();

        //Alarm must be active, if OnAlarmChanged will be called, so set it to true
        isAlarmActive = true;

        //Update the foreground notification with data
        updateNotification();

        SharedPreferences pref = getSharedPreferences("AlarmChanged", 0);
        SharedPreferences.Editor ed = pref.edit();
        ed.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
        ed.putInt("minute", calendar.get(Calendar.MINUTE));
        ed.putInt("actualWakeup", time.getActualWakeup());
        ed.apply();

        //Calculate the difference between now and the actual wakeup
        int secondsOfDay = LocalTime.now().toSecondOfDay();
        int timeDifference = time.getActualWakeup() - secondsOfDay;

        //Return if the alarm is on the next day or before first calculation
        if((secondsOfDay < (time.getWakeupEarly() - 1800)) || ((time.getWakeupLate() + 3600) < secondsOfDay) || !checkPossibleAlarm() ||
                (secondsOfDay > time.getActualWakeup() && time.getActualWakeup() == time.getWakeupEarly())) {
            return;
        }

        //Check if the actual alarm time is already reached and set the alarm to now
        if ((secondsOfDay > time.getActualWakeup()) && checkPossibleAlarm() && (secondsOfDay > time.getWakeupEarly())) {
            calendar.add(Calendar.SECOND, 60);

            actualWakeUp = calendarToSecondsOfDay(calendar);

            AlarmClockReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), getApplicationContext(), AlarmClockReceiverUsage.START_ALARMCLOCK);

            setPreferences(calendar, alarmTimeInSeconds, 4);

            return;
            
        } else if((secondsOfDay > time.getActualWakeup()) && checkPossibleAlarm() && (secondsOfDay < time.getWakeupEarly())) {
            Calendar earliestWakeup = AlarmReceiver.getAlarmDate(time.getWakeupEarly());

            actualWakeUp = calendarToSecondsOfDay(earliestWakeup);

            AlarmClockReceiver.startAlarmManager(earliestWakeup.get(Calendar.DAY_OF_WEEK), earliestWakeup.get(Calendar.HOUR_OF_DAY), earliestWakeup.get(Calendar.MINUTE), getApplicationContext(), AlarmClockReceiverUsage.START_ALARMCLOCK);

            setPreferences(earliestWakeup, time.getWakeupEarly(), 8);

            return;
        }

        //Check if the next calculation call will be after latest wakeup and set the alarm to latest wakeup
        if (((time.getWakeupLate() - secondsOfDay) < (16 * 60)) && checkPossibleAlarm()) {
            Calendar latestWakeup = Calendar.getInstance();
            latestWakeup.set(Calendar.HOUR_OF_DAY, 0);
            latestWakeup.set(Calendar.MINUTE, 0);
            latestWakeup.set(Calendar.SECOND, 0);
            latestWakeup.add(Calendar.SECOND, time.getWakeupLate());

            actualWakeUp = calendarToSecondsOfDay(latestWakeup);

            AlarmClockReceiver.startAlarmManager(latestWakeup.get(Calendar.DAY_OF_WEEK), latestWakeup.get(Calendar.HOUR_OF_DAY), latestWakeup.get(Calendar.MINUTE), getApplicationContext(), AlarmClockReceiverUsage.START_ALARMCLOCK);

            setPreferences(latestWakeup, time.getWakeupLate(), 1);

            return;
        }

        //Check if the actual wakeup is earlier than the next calculation call
        if (timeDifference < (16 * 60) && checkPossibleAlarm()) {

            //Check if the actual wakeup is earlier than the earliest wakeup and set the alarm to earliest wakeup
            if (time.getActualWakeup() < time.getWakeupEarly() && !(time.getWakeupEarly() < secondsOfDay) && !(time.getActualWakeup() < secondsOfDay)) {
                Calendar earliestWakeup = AlarmReceiver.getAlarmDate(time.getWakeupEarly());

                actualWakeUp = calendarToSecondsOfDay(earliestWakeup);

                AlarmClockReceiver.startAlarmManager(earliestWakeup.get(Calendar.DAY_OF_WEEK), earliestWakeup.get(Calendar.HOUR_OF_DAY), earliestWakeup.get(Calendar.MINUTE), getApplicationContext(), AlarmClockReceiverUsage.START_ALARMCLOCK);

                setPreferences(earliestWakeup, time.getWakeupEarly(), 2);

                return;
            }

            //Check if the actual wakeup is later than the latest wakeup and set the alarm to latest wakeup
            if (time.getActualWakeup() > time.getWakeupLate()) {
                Calendar latestWakeup = AlarmReceiver.getAlarmDate(time.getWakeupLate());

                actualWakeUp = calendarToSecondsOfDay(latestWakeup);

                AlarmClockReceiver.startAlarmManager(latestWakeup.get(Calendar.DAY_OF_WEEK), latestWakeup.get(Calendar.HOUR_OF_DAY), latestWakeup.get(Calendar.MINUTE), getApplicationContext(), AlarmClockReceiverUsage.START_ALARMCLOCK);

                setPreferences(latestWakeup, time.getWakeupLate(), 3);

                return;
            }

            //Check if the actual time is lower than the actual wakeup and add the difference to the actual time and set the alarm to this new time
            if (secondsOfDay <= time.getActualWakeup()){
                calendar.add(Calendar.SECOND, timeDifference);

                actualWakeUp = calendarToSecondsOfDay(calendar);

                AlarmClockReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE) + 1, getApplicationContext(), AlarmClockReceiverUsage.START_ALARMCLOCK);

                setPreferences(calendar, time.getActualWakeup(), 5);
            }
        }
    }

    private void setPreferences(Calendar calendar, int time, int use) {
        SharedPreferences pref = getSharedPreferences("AlarmChanged", 0);
        SharedPreferences.Editor ed = pref.edit();
        ed.putInt("alarmUse", use);
        ed.apply();

        pref = getSharedPreferences("AlarmSet", 0);
        ed = pref.edit();
        ed.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
        ed.putInt("minute", calendar.get(Calendar.MINUTE));
        ed.putInt("hour1", Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        ed.putInt("minute1", Calendar.getInstance().get(Calendar.MINUTE));
        ed.putInt("actualWakeup", time);
        ed.apply();
    }

    /**
     * Check if the alarm set is possible or already set
     * @return
     */
    private boolean checkPossibleAlarm() {
        if (!AlarmClockReceiver.isAlarmClockActive(getApplicationContext(), AlarmClockReceiverUsage.START_ALARMCLOCK) && !alarmEntity.getWasFired()) {
            return true;
        }

        return false;
    }

    /**
     * Check the set alarm and set it to the right time
     */
    private void checkAlarmSet() {
        if ((actualWakeUp != 0) && !AlarmClockReceiver.isAlarmClockActive(getApplicationContext(), AlarmClockReceiverUsage.START_ALARMCLOCK)) {
            Calendar calendar = AlarmReceiver.getAlarmDate(actualWakeUp);
            AlarmClockReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK) ,calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), getApplicationContext(), AlarmClockReceiverUsage.START_ALARMCLOCK);

            setPreferences(calendar, actualWakeUp, 9);
        }
    }

    /**
     * Convert the date to secondsOfDay
     * @param calendar date
     * @return secondsOfDay
     */
    private int calendarToSecondsOfDay(Calendar calendar) {
        int day = calendar.get(Calendar.HOUR_OF_DAY) * 3600;
        int minute = calendar.get(Calendar.MINUTE) * 60;
        int second = calendar.get(Calendar.SECOND);

        return day + minute + second;
    }

    /**
     * Will be called if sleep API data change
     * @param sleepApiData sleepApiData from ForegroundObserver
     */
    public void OnSleepApiDataChanged(SleepApiData sleepApiData) {

        checkAlarmSet();

        sleepValueAmount = sleepApiData.getSleepApiValuesAmount();
        isSubscribed = sleepApiData.getIsSubscribed();

        updateNotification();
        sendUserInformation();

    }

    /**
     * Will be called if sleepstate or time change
     * @param liveUserSleepActivity sleep time data
     */
    public void OnSleepTimeChanged(LiveUserSleepActivity liveUserSleepActivity) {

        checkAlarmSet();

        userSleepTime = liveUserSleepActivity.getUserSleepTime();
        isSleeping = liveUserSleepActivity.getIsUserSleeping();

        SharedPreferences pref = getSharedPreferences("SleepTime", 0);
        SharedPreferences.Editor ed = pref.edit();
        ed.putInt("sleeptime", userSleepTime);
        ed.apply();

        updateNotification();
        sendUserInformation();

    }

    /**
     * Check if time from now to last wakeup is less than the sleep time set
     * @return
     */
    private boolean checkSleeptimeReachingPossibility() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        int difference;

        //Check if AlarmEntity is not null and the user is interacting on device
        if ((databaseRepository.getNextActiveAlarmJob() != null) && !powerManager.isInteractive()) {

            //Check if midnight was reached or not for calculating time difference
            if (databaseRepository.getNextActiveAlarmJob().getWakeupEarly() >= dataStoreRepository.getSleepTimeBeginJob()) {
                difference = dataStoreRepository.getSleepTimeEndJob() - LocalTime.now().toSecondOfDay();
            } else {
                difference = 86400 - LocalTime.now().toSecondOfDay() + dataStoreRepository.getSleepTimeEndJob();
            }
        } else {
            return false;
        }

        //Check if difference is less then sleep time
        if((difference <= databaseRepository.getNextActiveAlarmJob().getSleepDuration()) && !userInformed) {
            userInformed = true;
            return true;
        }

        return false;
    }

    //endregion

    //region notification
    /**
     * Updates the notification banner with a new text
     */
    public void updateNotification() {

        Notification notification = createNotification();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);

    }

    private void sendUserInformation() {
        if (checkSleeptimeReachingPossibility()) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(2, createInformationNotification());

        }
    }

    /**
     * Creats a notification banner, that is permanent to show that the app is still running.
     * @return Notification.Builder
     */
    private Notification createNotification() {
        String notificationChannelId = getString(R.string.foregroundservice_channel);

        //Init remoteView for expanded notification
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.foreground_service_notification);

        //Set button for disable alarm with its intents
        Intent btnClickIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent btnClickPendingIntent;

        if (alarmEntity.getTempDisabled()) {
            btnClickIntent.putExtra(getApplicationContext().getString(R.string.alarmmanager_key), AlarmReceiverUsage.DISABLE_ALARM.name());

            remoteViews.setTextViewText(R.id.btnDisableAlarmNotification, getString(R.string.btn_reactivate_alarm));

            btnClickPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), AlarmReceiverUsage.DISABLE_ALARM.getAlarmReceiverUsageValue(), btnClickIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.btnDisableAlarmNotification, btnClickPendingIntent);
        } else {
            btnClickIntent.putExtra(getApplicationContext().getString(R.string.alarmmanager_key), AlarmReceiverUsage.DISABLE_ALARM.name());

            remoteViews.setTextViewText(R.id.btnDisableAlarmNotification, getString(R.string.btn_disable_alarm));

            btnClickPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), AlarmReceiverUsage.DISABLE_ALARM.getAlarmReceiverUsageValue(), btnClickIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.btnDisableAlarmNotification, btnClickPendingIntent);
        }

        if(userSleepTime <= 60) {
            //Set button for not sleeping
            btnClickIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
            btnClickIntent.putExtra(getApplicationContext().getString(R.string.alarmmanager_key), AlarmReceiverUsage.NOT_SLEEPING.name());
            remoteViews.setTextViewText(R.id.btnNotSleepingNotification, getString(R.string.btn_not_sleeping_text));

            btnClickPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), AlarmReceiverUsage.NOT_SLEEPING.getAlarmReceiverUsageValue(), btnClickIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.btnNotSleepingNotification, btnClickPendingIntent);
        } else {
            //Set button for currently not sleeping
            btnClickIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
            btnClickIntent.putExtra(getApplicationContext().getString(R.string.alarmmanager_key), AlarmReceiverUsage.CURRENTLY_NOT_SLEEPING.name());
            remoteViews.setTextViewText(R.id.btnNotSleepingNotification, getString(R.string.btn_currently_not_sleeping_text));

            btnClickPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), AlarmReceiverUsage.CURRENTLY_NOT_SLEEPING.getAlarmReceiverUsageValue(), btnClickIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.btnNotSleepingNotification, btnClickPendingIntent);
        }

        if (alarmEntity.getTempDisabled()) {
            isAlarmActive = false;
        }

        String contentText;

        if (isAlarmActive) {
            contentText = getString(R.string.alarm_status_true);
        } else {
            contentText = getString(R.string.alarm_status_false);
        }

        //Set the text in textview of the expanded notification view
        String notificationText = "AlarmActive: " + isAlarmActive + " Value: " + sleepValueAmount
                + "\nIsSubscribed: " + isSubscribed + " SleepTime: " + userSleepTime
                + "\nIsSleeping: " + isSleeping + " Wakeup: " + alarmTimeInSeconds;
        remoteViews.setTextViewText(R.id.tvTextAlarm, notificationText);

        //Set the Intent for tap on the notification, it will launch MainActivity
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
                .setContentText(contentText)
                .setCustomBigContentView(remoteViews)
                .setStyle(new Notification.DecoratedCustomViewStyle())
                .setSmallIcon(R.drawable.logo_notification)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .build();
    }

    /**
     * Create the notification to inform the user about sleep time problems
     * @return
     */
    private Notification createInformationNotification() {
        //Get Channel id
        String notificationChannelId = getString(R.string.foregroundservice_channel);

        //Create intent if user tap on notification
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Create manager and channel
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                notificationChannelId,
                getString(R.string.information_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
        );

        //Set channel description
        channel.setDescription(getString(R.string.foregroundservice_channel_description));
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }

        //Build the notification and return it
        Notification.Builder builder;
        builder = new Notification.Builder(this, notificationChannelId);

        return builder
                .setContentTitle(getString(R.string.foregroundservice_notification_title))
                .setContentText(getString(R.string.information_notification_text))
                .setStyle(new Notification.DecoratedCustomViewStyle())
                .setSmallIcon(R.drawable.logo_notification)
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

        context.startForegroundService(intent);
        return;
    }

    //endregion

}
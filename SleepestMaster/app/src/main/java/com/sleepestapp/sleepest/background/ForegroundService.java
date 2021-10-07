package com.sleepestapp.sleepest.background;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LifecycleService;

import com.sleepestapp.sleepest.LiveUserSleepActivity;
import com.sleepestapp.sleepest.MainApplication;
import com.sleepestapp.sleepest.R;
import com.sleepestapp.sleepest.SettingsData;
import com.sleepestapp.sleepest.SleepApiData;
import com.sleepestapp.sleepest.alarmclock.AlarmClockReceiver;
import com.sleepestapp.sleepest.model.data.Actions;
import com.sleepestapp.sleepest.model.data.AlarmClockReceiverUsage;
import com.sleepestapp.sleepest.model.data.AlarmCycleStates;
import com.sleepestapp.sleepest.model.data.AlarmReceiverUsage;
import com.sleepestapp.sleepest.model.data.Constants;
import com.sleepestapp.sleepest.model.data.NotificationUsage;
import com.sleepestapp.sleepest.sleepcalculation.SleepCalculationHandler;
import com.sleepestapp.sleepest.storage.DataStoreRepository;
import com.sleepestapp.sleepest.storage.DatabaseRepository;
import com.sleepestapp.sleepest.storage.db.AlarmEntity;
import com.sleepestapp.sleepest.util.NotificationUtil;
import com.sleepestapp.sleepest.util.SleepUtil;
import com.sleepestapp.sleepest.util.TimeConverterUtil;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
/**
 * This service class inherits from LifecycleService. It implements all functions of the foreground service
 * like start, stop and foreground notification. Further implements this function the observation of data
 * while the user is sleeping
 */
public class ForegroundService extends LifecycleService {

    private PowerManager.WakeLock wakeLock = null; //To keep Service active in background
    private boolean isServiceStarted = false; //Detects if Service started
    private boolean isAlarmActive = false; //Detects Alarm active status
    private int sleepValueAmount = 0; //Shows the amount of received sleep API elements
    private boolean isSubscribed = false; //Detects the subscription of sleep API
    private int userSleepTime = 0; //Shows the actual sleep time of the user
    private boolean isSleeping = false; //Detects sleep state
    private int alarmTimeInSeconds = 0; //Shows the calculated alarm time
    private boolean userInformed = false; //Detects if the user is already informed about problems with reaching sleep time
    private final boolean[] bannerConfig = new boolean[5];
    private static int foregroundServiceStartTime = 0;

    private AlarmEntity alarmEntity = null; //Instance of AlarmEntity, especially next active alarm
    private SleepCalculationHandler sleepCalculationHandler; //Instance of SleepCalculationHandler
    public ForegroundObserver foregroundObserver; //Instance of the ForegroundObserver for live data
    private NotificationUtil notificationUtil;
    private AlarmCycleState alarmCycleState;

    //region service functions

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



    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onCreate() {
        super.onCreate();

        //Create Instances
        foregroundObserver = new ForegroundObserver (this);
        //Instance of DatabaseRepo
        DatabaseRepository databaseRepository = ((MainApplication) getApplicationContext()).getDataBaseRepository();

        //Instance of Datastore Repo
        DataStoreRepository dataStoreRepository = ((MainApplication) getApplicationContext()).getDataStoreRepository();

        alarmEntity = databaseRepository.getNextActiveAlarmJob(dataStoreRepository);
        //Instance of SleepHandler
        sleepCalculationHandler = new SleepCalculationHandler(getApplicationContext());
        alarmCycleState = new AlarmCycleState(getApplicationContext());

        foregroundObserver.resetSleepTime();

        if (alarmEntity != null) {
            foregroundObserver.updateAlarmWasFired(false, alarmEntity.getId());
            alarmTimeInSeconds = alarmEntity.getWakeupEarly();
        }

        setForegroundServiceStartTime();

        notificationUtil = new NotificationUtil(getApplicationContext(), NotificationUsage.NOTIFICATION_FOREGROUND_SERVICE, fillList());

        //Start the Foregroundservice
        startForeground(Constants.FOREGROUND_SERVICE_ID, notificationUtil.createForegroundNotification());
        sendUserInformation();
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

        //Call function with null to transfer actual(local) time for correct calculation
        sleepCalculationHandler.checkIsUserSleepingJob(null);
        userSleepTime = 0;
    }

    // Stop the foreground service
    private void stopService() {

        try {
            //Release wakelock in order to lock foregroundservice against automatic restart
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
            stopForeground(true);
            stopSelf();

            isServiceStarted = false;
            foregroundObserver.setForegroundStatus(false);

        } catch (Exception e) {

            //Try stopping foregroundservice after 1 minute again if an error occurs
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 60);
            AlarmReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), getApplicationContext(), AlarmReceiverUsage.STOP_FOREGROUND);

        }

    }

    /**
     * Set the start time of the foreground service.
     * Needed to check if data are received.
     */
    private static void setForegroundServiceStartTime() {
        foregroundServiceStartTime = LocalTime.now().toSecondOfDay();
    }

    /**
     * Get the foreground service duration since start time
     * @return Duration since start
     */
    public static int getForegroundServiceTime(Context context) {
        int time;
        if (foregroundServiceStartTime <= LocalTime.now().toSecondOfDay()) {
            time = LocalTime.now().toSecondOfDay() - foregroundServiceStartTime;
        } else {
            time = Constants.DAY_IN_SECONDS - foregroundServiceStartTime + LocalTime.now().toSecondOfDay();
        }

        return time;
    }

    //endregion

    //region changing values

    /**
     * Alarm live data
     * @param time Instance of next active alarm
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    public void OnAlarmChanged(AlarmEntity time) {

        if (time.getAlreadyAwake()) {
            BackgroundAlarmTimeHandler.Companion.getHandler(getApplicationContext()).stopForegroundService();
            return;
        }

        //Update instance of AlarmEntity
        alarmEntity = time;

        //Init calendar
        Calendar calendar = Calendar.getInstance();

        //Update wakeup time

        alarmTimeInSeconds = time.getActualWakeup();

        //Alarm must be active, if OnAlarmChanged will be called, so set it to true
        isAlarmActive = true;

        //Update the foreground notification with data
        showForegroundNotification();
        sendUserInformation();

        //Calculate the difference between now and the actual wakeup
        int secondsOfDay = LocalTime.now().toSecondOfDay();
        int timeDifference = time.getActualWakeup() - secondsOfDay;

        //Return if the alarm is on the next day or before first calculation
        if((secondsOfDay < (time.getWakeupEarly() - 1800)) || ((time.getWakeupLate() + 3600) < secondsOfDay) || !checkPossibleAlarm() ||
                (secondsOfDay > time.getActualWakeup() && time.getActualWakeup() == time.getWakeupEarly())) {
            return;
        }

        //Check if the actual alarm time is already reached and set the alarm to now
        //Shows the set alarm clock time
        if ((secondsOfDay > time.getActualWakeup()) && checkPossibleAlarm() && (secondsOfDay > time.getWakeupEarly())) {
            calendar.add(Calendar.SECOND, 60);

            AlarmClockReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), getApplicationContext(), AlarmClockReceiverUsage.START_ALARMCLOCK);

            return;
            
        } else if((secondsOfDay > time.getActualWakeup()) && checkPossibleAlarm() && (secondsOfDay < time.getWakeupEarly())) {
            Calendar earliestWakeup = TimeConverterUtil.getAlarmDate(time.getWakeupEarly());

            AlarmClockReceiver.startAlarmManager(earliestWakeup.get(Calendar.DAY_OF_WEEK), earliestWakeup.get(Calendar.HOUR_OF_DAY), earliestWakeup.get(Calendar.MINUTE), getApplicationContext(), AlarmClockReceiverUsage.START_ALARMCLOCK);

            return;
        }

        //Check if the next calculation call will be after latest wakeup and set the alarm to latest wakeup
        if (((time.getWakeupLate() - secondsOfDay) < (16 * 60)) && checkPossibleAlarm()) {
            Calendar latestWakeup = Calendar.getInstance();
            latestWakeup.set(Calendar.HOUR_OF_DAY, 0);
            latestWakeup.set(Calendar.MINUTE, 0);
            latestWakeup.set(Calendar.SECOND, 0);
            latestWakeup.add(Calendar.SECOND, time.getWakeupLate());

            AlarmClockReceiver.startAlarmManager(latestWakeup.get(Calendar.DAY_OF_WEEK), latestWakeup.get(Calendar.HOUR_OF_DAY), latestWakeup.get(Calendar.MINUTE), getApplicationContext(), AlarmClockReceiverUsage.START_ALARMCLOCK);

            return;
        }

        //Check if the actual wakeup is earlier than the next calculation call
        if (timeDifference < (16 * 60) && checkPossibleAlarm()) {

            //Check if the actual wakeup is earlier than the earliest wakeup and set the alarm to earliest wakeup
            if (time.getActualWakeup() < time.getWakeupEarly() && !(time.getWakeupEarly() < secondsOfDay) && !(time.getActualWakeup() < secondsOfDay)) {
                Calendar earliestWakeup = TimeConverterUtil.getAlarmDate(time.getWakeupEarly());

                AlarmClockReceiver.startAlarmManager(earliestWakeup.get(Calendar.DAY_OF_WEEK), earliestWakeup.get(Calendar.HOUR_OF_DAY), earliestWakeup.get(Calendar.MINUTE), getApplicationContext(), AlarmClockReceiverUsage.START_ALARMCLOCK);

                return;
            }

            //Check if the actual wakeup is later than the latest wakeup and set the alarm to latest wakeup
            if (time.getActualWakeup() > time.getWakeupLate()) {
                Calendar latestWakeup = TimeConverterUtil.getAlarmDate(time.getWakeupLate());

                AlarmClockReceiver.startAlarmManager(latestWakeup.get(Calendar.DAY_OF_WEEK), latestWakeup.get(Calendar.HOUR_OF_DAY), latestWakeup.get(Calendar.MINUTE), getApplicationContext(), AlarmClockReceiverUsage.START_ALARMCLOCK);

                return;
            }

            //Check if the actual time is lower than the actual wakeup and add the difference to the actual time and set the alarm to this new time
            if (secondsOfDay <= time.getActualWakeup()){
                calendar.add(Calendar.SECOND, timeDifference);

                AlarmClockReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE) + 1, getApplicationContext(), AlarmClockReceiverUsage.START_ALARMCLOCK);

            }
        }
    }

    /**
     * Check if the alarm set is possible or already set
     */
    private boolean checkPossibleAlarm() {
        return !AlarmClockReceiver.isAlarmClockActive(getApplicationContext(), AlarmClockReceiverUsage.START_ALARMCLOCK) && !alarmEntity.getWasFired();
    }

    /**
     * Will be called if sleep API data change
     * @param sleepApiData sleepApiData from ForegroundObserver
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    public void OnSleepApiDataChanged(SleepApiData sleepApiData) {

        sleepValueAmount = sleepApiData.getSleepApiValuesAmount();
        isSubscribed = sleepApiData.getIsSubscribed();

        showForegroundNotification();
        sendUserInformation();

    }

    /**
     * Will be called if sleepstate or time change
     * @param liveUserSleepActivity sleep time data
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    public void OnSleepTimeChanged(LiveUserSleepActivity liveUserSleepActivity) {

        userSleepTime = liveUserSleepActivity.getUserSleepTime();
        isSleeping = liveUserSleepActivity.getIsUserSleeping();

        showForegroundNotification();
        sendUserInformation();

    }

    /**
     * Check if banner configuration was changed
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    public void OnBannerConfigChanged(SettingsData settingsData) {
        bannerConfig[0] = settingsData.getBannerShowAlarmActiv();
        bannerConfig[1] = settingsData.getBannerShowActualWakeUpPoint();
        bannerConfig[2] = settingsData.getBannerShowActualSleepTime();
        bannerConfig[3] = settingsData.getBannerShowSleepState();

        showForegroundNotification();
    }

    //endregion

    //region notification

    /**
     * Sends information to the user like missing data or user should go to sleep
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void sendUserInformation() {

        if (SleepUtil.checkSleepTimeReachingPossibility(getApplicationContext()) && !userInformed) {

            userInformed = true;

            NotificationUtil notificationUtilSleepProblem = new NotificationUtil(getApplicationContext(), NotificationUsage.NOTIFICATION_USER_SHOULD_SLEEP, null);
            notificationUtilSleepProblem.chooseNotification();

        }
    }

    /**
     * Initiate and update the foreground service notification
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void showForegroundNotification() {
        ArrayList<Object> arrayList = fillList();
        notificationUtil = new NotificationUtil(getApplicationContext(), NotificationUsage.NOTIFICATION_FOREGROUND_SERVICE, arrayList);
        notificationUtil.chooseNotification();
    }

    /**
     * Fills the list for the information of the foregroundservice notification
     * @return filled list
     */
    private ArrayList<Object> fillList() {
        ArrayList<Object> arrayList = new ArrayList<>();
        arrayList.add(alarmEntity);
        arrayList.add(userSleepTime);
        arrayList.add(isAlarmActive);
        arrayList.add(isSleeping);
        arrayList.add(getRealAlarmTime(alarmTimeInSeconds));
        arrayList.add(bannerConfig);
        arrayList.add(isSubscribed);
        arrayList.add(sleepValueAmount);

        return arrayList;
    }

    /**
     * Workaround to show to correct alarm time in the notification
     * @param actualTime Actual alarm time in seconds of day
     * @return Real alarm time in seconds of day
     */
    private int getRealAlarmTime(int actualTime) {
        if (alarmCycleState.getState() == AlarmCycleStates.BETWEEN_SLEEPTIME_START_AND_CALCULATION && alarmEntity != null) {
            return alarmEntity.getWakeupEarly();
        } else if (alarmEntity != null && (actualTime > alarmEntity.getWakeupLate())) {
            return alarmEntity.getWakeupLate();
        }

        return actualTime;
    }

    /**
     * Starts or stops the foreground service. This function must be called to start or stop service
     * @param action Enum Action (START or STOP)
     * @param context Application context
     */
    public static void startOrStopForegroundService(Actions action, Context context) {

        Intent intent = new Intent(context, ForegroundService.class);
        intent.setAction(action.name());

        context.startForegroundService(intent);
    }

    //endregion

}
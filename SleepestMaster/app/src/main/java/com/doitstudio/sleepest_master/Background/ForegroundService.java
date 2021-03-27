package com.doitstudio.sleepest_master.Background;

/** This class inherits from Service. It implements all functions of the foreground service
 * like start, stop and foreground notification
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.doitstudio.sleepest_master.Alarm;
import com.doitstudio.sleepest_master.MainApplication;
import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.model.data.Actions;
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler;
import com.doitstudio.sleepest_master.storage.DataStoreRepository;


public class ForegroundService extends Service {

    private PowerManager.WakeLock wakeLock = null;
    private boolean isServiceStarted = false;
    public SleepCalculationHandler sleepCalculationHandler;

    private DataStoreRepository storeRepository;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

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

    ForeHelper fh;

    @Override
    public void onCreate() {
        super.onCreate();

        // repo holen
        storeRepository = ((MainApplication)getApplicationContext()).getDataStoreRepository();

        // kotlin handler starten
        fh = new ForeHelper(storeRepository,(LifecycleOwner) this);
        fh.ObserveAlarm(this);

        startForeground(1, createNotification("Test")); /** TODO: Id zentral anlegen */
    }

    public void OnAlarmChanged(Alarm alarm){

        // hier sollte dann der aufruf kommen evtl.

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
    }

    public void updateNotification(String text) {

        Notification notification = createNotification(text);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notification);

    }

    /**TODO Notification noch selbst machen mit eigenem Layout*/
    //Creats a notification banner, that is permament to show that the app is still running. Only since Oreo
    private Notification createNotification(String text) {
        String notificationChannelId = "ENDLESS SERVICE CHANNEL"; /**TODO: zentral definieren*/

        // Since Oreo there is a Notification Service needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                    notificationChannelId,
                    "Endless Service notifications channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Endless Service channel");
            //channel.enableLights(true);
            //channel.setLightColor(Color.RED);
            //channel.enableVibration(true);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(
                    this,
                    notificationChannelId
            );
        } else {
            builder = new Notification.Builder(this);
        }

        return builder
                .setContentTitle("Endless Service")
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Ticker text")
                .setPriority(Notification.PRIORITY_HIGH) // for under android Oreo (26) compatibility
                .build();
    }

    /** Starts oder stops the foreground service. This function must be called to start or stop service
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
}

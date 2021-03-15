package com.doitstudio.backgroundingtestproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

public class EndlessService extends Service {

    private PowerManager.WakeLock wakeLock = null;
    private boolean isServiceStarted = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String action = intent.getAction();

            if (action != null) {
                if (action.equals(Actions.START.name())) {startService();}
                else if (action.equals(Actions.STOP.name())) {stopService();}
            }
        }


        return START_STICKY; // by returning this we make sure the service is restarted if the system kills the service
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1, createNotification());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show();
    }

    private void startService() {
        // If the service already running, do nothing.
        if (isServiceStarted) {return;}

        Toast.makeText(this, "Service starting its task", Toast.LENGTH_SHORT).show();
        isServiceStarted = true;
        new ServiceTracker().setServiceState(this, ServiceState.STARTED);

        // we need this lock so our service gets not affected by Doze Mode
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(1, "EndlessService::lock");
            wakeLock.acquire(60 * 1000L /*1 minute*/);
        }

        /** TODO: Test Thread **/
        // Create a thread and loop while the service is running.
        Thread thread = new Thread(() -> {
            while (isServiceStarted) {
                try {
                    Thread.sleep(2000);
                    //pingFakeServer();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        // Start thread.
        thread.start();
    }

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
        isServiceStarted = false;
        new ServiceTracker().setServiceState(this, ServiceState.STOPPED);
    }

    private Notification createNotification() {
        String notificationChannelId = "ENDLESS SERVICE CHANNEL";

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
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
                .setContentText("This is your favorite endless service working")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Ticker text")
                .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
                .build();
    }
}

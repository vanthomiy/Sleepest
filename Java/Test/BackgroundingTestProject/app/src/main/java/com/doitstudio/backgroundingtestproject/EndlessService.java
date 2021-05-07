package com.doitstudio.backgroundingtestproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LifecycleService;

import java.util.Calendar;
import java.util.GregorianCalendar;

enum Actions {
    START,
    STOP
}

public class EndlessService extends LifecycleService {

    private PowerManager.WakeLock wakeLock = null;
    private boolean isServiceStarted = false;
    private String notificationChannelId = "ENDLESS SERVICE CHANNEL";

    Test test;

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1, createNotification("Hallo"));
        test = new Test(1,2);
        //Observe einfügen
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show();
    }




    @RequiresApi(api = Build.VERSION_CODES.O)
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

        // Create a thread and loop while the service is running.
        Thread thread = new Thread(() -> {
            while (isServiceStarted) {
                try {
                    Thread.sleep(10000);
                    /*showNotification(getApplicationContext());

                    int oldTest1 = test.getTest1();
                    int oldTest2 = test.getTest2();

                    test.setTest1(++oldTest1);
                    test.setTest2(++oldTest2);

                    updateNotification(Integer.toString(oldTest1) + "," + Integer.toString(oldTest2));
                    //pingFakeServer();*/
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        // Start thread.
        thread.start();

        /*Calendar calenderAlarm = Calendar.getInstance();
        int day = calenderAlarm.get(Calendar.DAY_OF_WEEK);
        if (day > 7) {
            day = 1;
        }
        AlarmReceiver.startAlarmManager(day,10,55, EndlessService.this, 2);
        Workmanager.startPeriodicWorkmanager(30);*/
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

        /*Workmanager.stopPeriodicWorkmanager();
        Calendar calenderAlarm = Calendar.getInstance();
        int day = calenderAlarm.get(Calendar.DAY_OF_WEEK);
        AlarmReceiver.startAlarmManager(day,20,0, getApplicationContext(), 1);*/
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateNotification(String text) {

        Notification notification = createNotification(text);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notification);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification createNotification(String text) {

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
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Ticker text")
                .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
                .build();

        /*Intent notificationIntent = new Intent(this, EndlessService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 123, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this, notificationChannelId)
                        .setContentTitle("Endless Service")
                        .setContentText("Hallo")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pendingIntent)
                        .setTicker("Ticker text")
                        .build();

        return notification;*/

    }

    private void showNotification(Context context) {

        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(System.currentTimeMillis());
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "Channel")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("My notification")
                .setContentText(hour + ":" + minute)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(hour + ":" + minute))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel_name";
            String description = "description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Channel", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(100, mBuilder.build());
    }

    static void startForegroundService(Actions action, Context context) {
        Intent intent = new Intent(context, EndlessService.class);
        intent.setAction(action.name());
        if (new ServiceTracker().getServiceState(context) == ServiceState.STOPPED && action == Actions.STOP)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);

                return;
            }
        context.startService(intent);
    }
}

package com.doitstudio.backgroundingtestproject;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.AlarmClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Workmanager extends Worker {

    private static final String TAG = Workmanager.class.getSimpleName();
    public static final String CHANNEL_ID = "VERBOSE_NOTIFICATION" ;
    Context context;

    public Workmanager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    //Workmanager do his work here at the desired time intervals
    @NonNull
    @Override
    public Result doWork() {

        Context context = getApplicationContext();
        Log.i(TAG, "doWork");
        /**Hinweis: Hier dürfen nur Prozesse stattfinden, die nicht länger als 10 Minuten dauern
         * Allerdings werden Notifications erst angezeigt, wenn der Bildschirm angeht. Somit bricht
         * der Workmanager ab, sobald die Notification nicht innerhalb 10 Minuten nach Triggerung
         * angeschaut wird. Prozesse, die den Nutzer nicht benötigen, sind hier aber im Normalfall
         * Problemlos möglich.
         */
        //showNotification(getApplicationContext());
        
        saveActualTime();
        //setTimer();

        return Result.success();
    }

    //Intent aus Background starten, bisher nicht möglich...
    private void setTimer() {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                /*Intent intent = new Intent(mcontext, NewScreen.class);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                mcontext.startActivity(intent);*/

                Calendar cal = new GregorianCalendar();
                cal.setTimeInMillis(System.currentTimeMillis());
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);

                Intent intent2 = new Intent(context, AlarmClock.ACTION_SET_ALARM.getClass());
                //Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
                intent2.putExtra(AlarmClock.EXTRA_HOUR, hour);
                intent2.putExtra(AlarmClock.EXTRA_MINUTES, minute + 13);
                intent2.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
                context.startActivity(intent2);
            }
        });
    }

    private void showNotification(Context context) {

        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(System.currentTimeMillis());
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
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
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(100, mBuilder.build());
    }

    private void saveActualTime() {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(System.currentTimeMillis());
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        SharedPreferences timePref = context.getSharedPreferences("time", 0);
        SharedPreferences.Editor editor = timePref.edit();
        editor.putString("hour", Integer.toString(hour));
        editor.putString("minute", Integer.toString(minute));
        editor.apply();
    }
}

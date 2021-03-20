package com.doitstudio.sleepest_master.Background;

/** This Workmanager is for periodic work. The minimum duration is 15 minutes.
 * You can handle only processes with a maximal duration of 10 minutes, otherwise the
 * workmanager stops.
 */

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class Workmanager extends Worker {

    private static final String TAG = Workmanager.class.getSimpleName();
    public static final String CHANNEL_ID = "VERBOSE_NOTIFICATION" ;
    private static final String TAG_WORK = "Workmanager 1";
    private static Context context;
    private SleepCalculationHandler sleepCalculationHandler;

    public Workmanager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        //sleepCalculationHandler = SleepCalculationHandler.Companion.getDatabase(context);
        //showNotification(context);
    }

    //Workmanager do his work here at the desired time intervals
    @NonNull
    @Override
    public Result doWork() {

        /**Hinweis: Hier dürfen nur Prozesse stattfinden, die nicht länger als 10 Minuten dauern
         * Allerdings werden Notifications erst angezeigt, wenn der Bildschirm angeht. Somit bricht
         * der Workmanager ab, sobald die Notification nicht innerhalb 10 Minuten nach Triggerung
         * angeschaut wird. Prozesse, die den Nutzer nicht benötigen, sind hier aber im Normalfall
         * problemlos möglich.
         */

        //sleepCalculationHandler.calculateSleepData();
        showNotification(context);

        return Result.success();
    }

    /** Start the workmanager with a specific duration
     * @param duration Number <=15 stands for duration in minutes
     */
    public static void startPeriodicWorkmanager(int duration, Context context1) {

        //Constraints not necessary, but useful
        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true) //Trigger fires only, when battery is not low
                .setRequiresStorageNotLow(true) //Trigger fires only, when enough storage is left
                .build();

        PeriodicWorkRequest periodicDataWork =
                new PeriodicWorkRequest.Builder(Workmanager.class, duration, TimeUnit.MINUTES)
                        .addTag(TAG_WORK) //Tag is needed for canceling the periodic work
                        .setConstraints(constraints)
                        .build();

        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueueUniquePeriodicWork(TAG_WORK, ExistingPeriodicWorkPolicy.KEEP, periodicDataWork);

        Toast.makeText(context1, "Workmanager started", Toast.LENGTH_LONG).show();

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


    public static void stopPeriodicWorkmanager() {
        //Cancel periodic work by tag
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG_WORK);
    }
}

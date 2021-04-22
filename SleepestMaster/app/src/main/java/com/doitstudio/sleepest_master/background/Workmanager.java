package com.doitstudio.sleepest_master.background;

/**
 * This Workmanager is for periodic work. The minimum duration is 15 minutes.
 * You can handle only processes with a maximal duration of 10 minutes, otherwise the
 * workmanager stops.
 */

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.alarmclock.AlarmClockReceiver;
import com.doitstudio.sleepest_master.model.data.Actions;
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class Workmanager extends Worker {

    private static Context context;
    private SleepCalculationHandler sleepCalculationHandler;

    public Workmanager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;

        //sleepCalculationHandler = SleepCalculationHandler.Companion.getDatabase(context);
        //showNotification(context);

        sleepCalculationHandler = SleepCalculationHandler.Companion.getHandler(context);

    }

    //Workmanager do his work here at the desired time intervals
    @NonNull
    @Override
    public Result doWork() {

        /**
         * Hinweis: Hier dürfen nur Prozesse stattfinden, die nicht länger als 10 Minuten dauern
         * Allerdings werden Notifications erst angezeigt, wenn der Bildschirm angeht. Somit bricht
         * der Workmanager ab, sobald die Notification nicht innerhalb 10 Minuten nach Triggerung
         * angeschaut wird. Prozesse, die den Nutzer nicht benötigen, sind hier aber im Normalfall
         * problemlos möglich.
         */

        sleepCalculationHandler.calculateLiveUserSleepActivityJob();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 29);

        /*if (calendar.before(Calendar.getInstance().getTime())) {
            sleepCalculationHandler.calculateUserWakeupJob();
        }*/

        if (calendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
            //sleepCalculationHandler.calculateUserWakeupJob();
        }

        return Result.success();
    }

    /**
     * Start the workmanager with a specific duration
     * @param duration Number <=15 stands for duration in minutes
     */
    public static void startPeriodicWorkmanager(int duration, Context context1) {

        //Constraints not necessary, but useful
        /*Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true) //Trigger fires only, when battery is not low
                .setRequiresStorageNotLow(true) //Trigger fires only, when enough storage is left
                .build();*/

        PeriodicWorkRequest periodicDataWork =
                new PeriodicWorkRequest.Builder(Workmanager.class, duration, TimeUnit.MINUTES)
                        .addTag(context1.getString(R.string.workmanager1_tag)) //Tag is needed for canceling the periodic work
                        //.setConstraints(constraints)
                        .build();

        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueueUniquePeriodicWork(context1.getString(R.string.workmanager1_tag), ExistingPeriodicWorkPolicy.KEEP, periodicDataWork);

        Toast.makeText(context1, "Workmanager started", Toast.LENGTH_LONG).show();

    }

    public static void stopPeriodicWorkmanager() {

        //Cancel periodic work by tag
        WorkManager.getInstance(context).cancelAllWorkByTag(context.getString(R.string.workmanager1_tag));
    }
}

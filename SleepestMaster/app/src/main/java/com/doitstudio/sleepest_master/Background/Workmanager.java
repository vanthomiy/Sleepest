package com.doitstudio.sleepest_master.Background;

/** This Workmanager is for periodic work. The minimum duration is 15 minutes.
 * You can handle only processes with a maximal duration of 10 minutes, otherwise the
 * workmanager stops.
 */

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler;

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
        sleepCalculationHandler = SleepCalculationHandler.Companion.getDatabase(context);
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

        sleepCalculationHandler.calculateSleepData();

        return Result.success();
    }

    /** Start the workmanager with a specific duration
     * @param duration Number <=15 stands for duration in minutes
     */
    public static void startPeriodicWorkmanager(int duration) {

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

    }

    public static void stopPeriodicWorkmanager() {
        //Cancel periodic work by tag
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG_WORK);
    }
}

package com.doitstudio.sleepest_master.background;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class WorkmanagerCalculation extends Worker {

    private static Context context;
    private SleepCalculationHandler sleepCalculationHandler;

    public WorkmanagerCalculation(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;


        //sleepCalculationHandler = SleepCalculationHandler.Companion.getHandler(context);


    }

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
        sleepCalculationHandler = SleepCalculationHandler.Companion.getHandler(context);
        sleepCalculationHandler.defineUserWakeup(null);

        Calendar calendar = Calendar.getInstance();

        SharedPreferences pref = context.getSharedPreferences("WorkmanagerCalculation", 0);
        SharedPreferences.Editor ed = pref.edit();
        ed.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
        ed.putInt("minute", calendar.get(Calendar.MINUTE));
        ed.apply();

        return Result.success();
    }

    public static void startPeriodicWorkmanager(int duration, Context context1) {

        //Constraints not necessary, but useful
        /*Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true) //Trigger fires only, when battery is not low
                .setRequiresStorageNotLow(true) //Trigger fires only, when enough storage is left
                .build();*/

        PeriodicWorkRequest periodicDataWork =
                new PeriodicWorkRequest.Builder(WorkmanagerCalculation.class, duration, TimeUnit.MINUTES)
                        .addTag(context1.getString(R.string.workmanager2_tag)) //Tag is needed for canceling the periodic work
                        //.setConstraints(constraints)
                        .build();

        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueueUniquePeriodicWork(context1.getString(R.string.workmanager2_tag), ExistingPeriodicWorkPolicy.KEEP, periodicDataWork);

        Toast.makeText(context1, "WorkmanagerCalculation started", Toast.LENGTH_LONG).show();

    }

    public static void stopPeriodicWorkmanager() {

        //Cancel periodic work by tag
        WorkManager.getInstance(context).cancelAllWorkByTag("Workmanager 2");
    }
}

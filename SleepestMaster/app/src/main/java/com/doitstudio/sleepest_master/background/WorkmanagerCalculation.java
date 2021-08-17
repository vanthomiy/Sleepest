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

import com.doitstudio.sleepest_master.MainApplication;
import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.model.data.AlarmCycleStates;
import com.doitstudio.sleepest_master.model.data.AlarmReceiverUsage;
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler;
import com.doitstudio.sleepest_master.util.TimeConverterUtil;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class WorkmanagerCalculation extends Worker {

    private static Context context;
    private SleepCalculationHandler sleepCalculationHandler;

    public WorkmanagerCalculation(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
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

        BackgroundAlarmTimeHandler.Companion.getHandler(context).defineNewUserWakeup(null, true);

        Calendar calendar = Calendar.getInstance();
        SharedPreferences pref = context.getSharedPreferences("WorkmanagerCalculation", 0);
        SharedPreferences.Editor ed = pref.edit();
        ed.putInt("day", calendar.get(Calendar.DAY_OF_WEEK));
        ed.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
        ed.putInt("minute", calendar.get(Calendar.MINUTE));
        ed.apply();

        return Result.success();
    }

    public static void startPeriodicWorkmanager(int duration, Context context1) {

        AlarmCycleState alarmCycleState = new AlarmCycleState(context1);
        if (alarmCycleState.getState() == AlarmCycleStates.BETWEEN_CALCULATION_AND_FIRST_WAKEUP ||
            alarmCycleState.getState() == AlarmCycleStates.BETWEEN_FIRST_AND_LAST_WAKEUP) {
            PeriodicWorkRequest periodicDataWork =
                    new PeriodicWorkRequest.Builder(WorkmanagerCalculation.class, duration, TimeUnit.MINUTES)
                            .addTag(context1.getString(R.string.workmanager2_tag)) //Tag is needed for canceling the periodic work
                            .build();

            WorkManager workManager = WorkManager.getInstance(context1);
            workManager.enqueueUniquePeriodicWork(context1.getString(R.string.workmanager2_tag), ExistingPeriodicWorkPolicy.KEEP, periodicDataWork);

            Toast.makeText(context1, "WorkmanagerCalculation started", Toast.LENGTH_LONG).show();
        } else {
            Calendar calendar = TimeConverterUtil.getAlarmDate(LocalTime.now().toSecondOfDay() + 300);
            AlarmReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), context1, AlarmReceiverUsage.START_WORKMANAGER_CALCULATION);
        }
    }

    public static void stopPeriodicWorkmanager() {

        //Cancel periodic work by tag
        WorkManager.getInstance(context).cancelAllWorkByTag("Workmanager 2");
    }
}

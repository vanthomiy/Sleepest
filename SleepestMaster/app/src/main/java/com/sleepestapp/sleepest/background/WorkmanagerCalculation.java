package com.sleepestapp.sleepest.background;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.sleepestapp.sleepest.R;
import com.sleepestapp.sleepest.model.data.AlarmCycleStates;
import com.sleepestapp.sleepest.model.data.AlarmReceiverUsage;
import com.sleepestapp.sleepest.sleepcalculation.SleepCalculationHandler;
import com.sleepestapp.sleepest.util.TimeConverterUtil;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * This Workmanager calculates the alarm time depending on the tracked sleep API data.
 * It is started 30 minutes before earliest wakeup and calculates until alarm rings
 */

public class WorkmanagerCalculation extends Worker {

    private static Context context;
    private SleepCalculationHandler sleepCalculationHandler;

    public WorkmanagerCalculation(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        WorkmanagerCalculation.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        try {
            //Defines the new wakeup
            BackgroundAlarmTimeHandler.Companion.getHandler(context).defineNewUserWakeup(null, true);

            Calendar calendar = Calendar.getInstance();
            SharedPreferences pref = context.getSharedPreferences("WorkmanagerCalculation", 0);
            SharedPreferences.Editor ed = pref.edit();
            ed.putInt("day", calendar.get(Calendar.DAY_OF_WEEK));
            ed.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
            ed.putInt("minute", calendar.get(Calendar.MINUTE));
            ed.apply();

            return Result.success();
        } catch (Exception e) {
            Result.retry();
        }

        return Result.success();
    }

    /**
     * Start the Workmanager for calculation
     * @param duration Period time
     * @param context1 Context
     */
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

    /**
     * Stops the Workmanager
     */
    public static void stopPeriodicWorkmanager() {
        //Cancel periodic work by tag
        WorkManager.getInstance(context).cancelAllWorkByTag("Workmanager 2");
    }
}

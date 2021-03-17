package com.doitstudio.sleepest_master.Background;

/**This class inherits from Broadcastreceiver and starts an alarm at a specific time and date*/

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.RequiresApi;
import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {

    private static Context context;
    public static final int REQUEST_CODE = 12345;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        /** TODO: High Sensitive Notification for Alarm, like System Alarm (Snooze, turn off) **/

    }

    /**Start a alarm at a specific time
     * @param day Number from 1-7, Sunday=1, Saturday=7
     * @param hour Hour from 0-23
     * @param min Minute from 0-59
     * @param alarmContext Application Context
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT) //Android KITKAT is minimum version!
    static void startAlarmManager(int day, int hour, int min, Context alarmContext) {

        //Get an instance of calendar and set time, when alarm should be fired
        Calendar calenderAlarm = Calendar.getInstance();
        calenderAlarm.set(Calendar.HOUR_OF_DAY, hour);
        calenderAlarm.set(Calendar.MINUTE, min);
        calenderAlarm.set(Calendar.SECOND, 0);
        calenderAlarm.set(Calendar.MILLISECOND, 0);

        //Move date passed to next date
        if (calenderAlarm.before(Calendar.getInstance())) {
            calenderAlarm.add(Calendar.DATE, day); //Set date day
        }

        Intent intent = new Intent(alarmContext, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(alarmContext, 1, intent, 0); /** TODO: Request Code zentral anlegen */
        AlarmManager alarmManager = (AlarmManager) alarmContext.getSystemService(ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calenderAlarm.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calenderAlarm.getTimeInMillis(), pendingIntent);
        }

        //Toast.makeText(context, "Alarm set to " + days[spDay.getSelectedItemPosition()] + ": " + hour + ":" + min, Toast.LENGTH_LONG).show();
    }

    /** Cancel a specific alarm by pending Intent
     * @param context1 Application Context
     */
    static void cancelAlarm(Context context1) {

        Intent intent = new Intent(context1, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context1, 1, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context1.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}

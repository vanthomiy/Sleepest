package com.doitstudio.sleepest_master.background;

/**This class inherits from Broadcastreceiver and starts an alarm at a specific time and date*/

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.model.data.Actions;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static android.content.Context.ALARM_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {

    private static Context context;
    public static final int REQUEST_CODE = 12345;

    public static final String CHANNEL_ID = "AlarmNotification" ;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context.getApplicationContext();

        switch (intent.getIntExtra(context.getString(R.string.alarmmanager_key), 0)) {
            case 0:
                break;
            case 1:
                ForegroundService.startOrStopForegroundService(Actions.START, context);
                break;
            case 2:
                ForegroundService.startOrStopForegroundService(Actions.STOP, context);
                break;
            case 3:
                Toast.makeText(context.getApplicationContext(), "No Action yet", Toast.LENGTH_LONG).show();
                break;
        }
    }

    /**
     * Start a alarm at a specific time
     * @param day Number from 1-7, Sunday=1, Saturday=7
     * @param hour Hour from 0-23
     * @param min Minute from 0-59
     * @param alarmContext Application Context
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT) //Android KITKAT is minimum version!
    public static void startAlarmManager(int day, int hour, int min, Context alarmContext, int usage) {

        //Get an instance of calendar and set time, when alarm should be fired
        Calendar calenderAlarm = getAlarmDate(day, hour, min);

        Intent intent = new Intent(alarmContext, AlarmReceiver.class);
        intent.putExtra(alarmContext.getString(R.string.alarmmanager_key), usage);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(alarmContext, usage, intent, 0); /** TODO: Request Code zentral anlegen */
        AlarmManager alarmManager = (AlarmManager) alarmContext.getSystemService(ALARM_SERVICE);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calenderAlarm.getTimeInMillis(), pendingIntent);

        Toast.makeText(alarmContext, "Alarm set to " + calenderAlarm.get(Calendar.DAY_OF_WEEK) + ": "
                + calenderAlarm.get(Calendar.HOUR_OF_DAY) + ":" + calenderAlarm.get(Calendar.MINUTE), Toast.LENGTH_LONG).show();
    }

    /**
     * Cancel a specific alarm by pending Intent
     * @param context1 Application Context
     */
    static void cancelAlarm(Context context1, int usage) {

        Intent intent = new Intent(context1, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context1, usage, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context1.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public static Calendar getAlarmDate(int day, int hour, int minute) {

        int actualDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);

        if (day > Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
        {
            actualDay += day - Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        } else if (day < Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            actualDay += 7 - Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + day;
        } else {
            if (calendar.before(Calendar.getInstance())) {
                actualDay += 7;
            }
        }

        if (actualDay > Calendar.getInstance().getMaximum(Calendar.DAY_OF_YEAR)) {
            actualDay = Calendar.getInstance().getMinimum(Calendar.DAY_OF_YEAR) + Calendar.getInstance().getMaximum(Calendar.DAY_OF_YEAR) - actualDay;
            calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) + 1);
        }

        calendar.set(Calendar.DAY_OF_YEAR, actualDay);

        return calendar;
    }
}

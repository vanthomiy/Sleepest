package com.doitstudio.backgroundingtestproject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.content.ComponentName;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static android.content.Context.ALARM_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {

    static MediaPlayer mediaPlayer;
    private static Context context;
    public static final int REQUEST_CODE = 12345;
    AudioManager audioManager;



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        /** TODO: High Sensitive Notification for Alarm, like System Alarm (Snooze, turn off) **/

        //Audio only with media sound on!!
        mediaPlayer = MediaPlayer.create(context, R.raw.single_beep);
        mediaPlayer.start();

        //endlessService.updateNotification("Chillige Sache");

    }

    //Start a alarm at a specific time
    @RequiresApi(api = Build.VERSION_CODES.KITKAT) //KITKAT is minimum version!
    static void startAlarmManager(int day, int hour, int min, Context context1) {

        Calendar cal_alarm = Calendar.getInstance();
        cal_alarm.set(Calendar.HOUR_OF_DAY, hour);
        cal_alarm.set(Calendar.MINUTE, min);
        cal_alarm.set(Calendar.SECOND, 0);
        cal_alarm.set(Calendar.MILLISECOND, 0);

        if (cal_alarm.before(Calendar.getInstance())) {
            Toast.makeText(context1, "Date passed moved to next date.", Toast.LENGTH_SHORT).show();
            cal_alarm.add(Calendar.DATE, day);
        }

        Intent intent = new Intent(context1, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context1, 1, intent, 0);
        AlarmManager am = (AlarmManager) context1.getSystemService(ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal_alarm.getTimeInMillis(), pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, cal_alarm.getTimeInMillis(), pi);
        }

        //Toast.makeText(context, "Alarm set to " + days[spDay.getSelectedItemPosition()] + ": " + hour + ":" + min, Toast.LENGTH_LONG).show();
    }

    static void cancelAlarm(Context context1) {
        Intent intent = new Intent(context1, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context1, 1, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context1.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

}

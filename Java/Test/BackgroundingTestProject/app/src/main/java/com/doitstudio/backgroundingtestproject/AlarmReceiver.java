package com.doitstudio.backgroundingtestproject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

        this.context = context.getApplicationContext();

        /** TODO: High Sensitive Notification for Alarm, like System Alarm (Snooze, turn off) **/

        //Audio only with media sound on!!
        //mediaPlayer = MediaPlayer.create(context, R.raw.single_beep);
        //mediaPlayer.start();

        //endlessService.updateNotification("Chillige Sache");

        switch (intent.getIntExtra("com.doitstudio.backgroundingtestproject.AlarmIntentKey", 0)) {
            case 0: break;
            case 1:
                EndlessService.startForegroundService(Actions.START, context);
                Calendar calenderAlarm = getAlarmDate(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 1, 9, 0);

                /*int day = calenderAlarm.get(Calendar.DAY_OF_WEEK);
                if ((hour < calenderAlarm.get(Calendar.HOUR_OF_DAY)) || ((hour == calenderAlarm.get(Calendar.HOUR_OF_DAY) && (minute < calenderAlarm.get(Calendar.MINUTE))))) {
                    day += 1;
                }
                if (day > 7) {
                    day = 1;
                }*/
                AlarmReceiver.startAlarmManager(calenderAlarm.get(Calendar.DAY_OF_WEEK), calenderAlarm.get(Calendar.HOUR_OF_DAY), calenderAlarm.get(Calendar.MINUTE), context, 2);
                Workmanager.startPeriodicWorkmanager(30);
                break;
            case 2:
                EndlessService.startForegroundService(Actions.STOP, context);
                Workmanager.stopPeriodicWorkmanager();
                Calendar calenderbla = getAlarmDate(Calendar.getInstance().get(Calendar.DAY_OF_WEEK), 20, 0);
                //int day1 = calenderbla.get(Calendar.DAY_OF_WEEK);
                AlarmReceiver.startAlarmManager(calenderbla.get(Calendar.DAY_OF_WEEK), calenderbla.get(Calendar.HOUR_OF_DAY) , calenderbla.get(Calendar.MINUTE), context, 1);
                break;
        }

        saveActualAlarmDate();

    }

    private void saveActualAlarmDate() {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(System.currentTimeMillis());
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int day = cal.get(Calendar.DAY_OF_WEEK);

        SharedPreferences timePref = context.getSharedPreferences("lastalarm", 0);
        SharedPreferences.Editor editor = timePref.edit();
        editor.putString("day", Integer.toString(day));
        editor.putString("hour", Integer.toString(hour));
        editor.putString("minute", Integer.toString(minute));
        editor.apply();
    }

    //Start a alarm at a specific time
    @RequiresApi(api = Build.VERSION_CODES.KITKAT) //KITKAT is minimum version!
    public static void startAlarmManager(int day, int hour, int min, Context context1, int usage) {

        Calendar cal_alarm = getAlarmDate(day, hour, min);
        /*if (usage == 2) {
            cal_alarm.set(Calendar.DATE, 11);
        }

        cal_alarm.set(Calendar.HOUR_OF_DAY, hour);
        cal_alarm.set(Calendar.MINUTE, min);
        cal_alarm.set(Calendar.SECOND, 0);
        cal_alarm.set(Calendar.MILLISECOND, 0);

        /*if (cal_alarm.before(Calendar.getInstance())) {
            Toast.makeText(context1, "Date passed moved to next date.", Toast.LENGTH_SHORT).show();
            cal_alarm.add(Calendar.DATE, day);
        }*/

        Intent intent = new Intent(context1, AlarmReceiver.class);
        intent.putExtra("com.doitstudio.backgroundingtestproject.AlarmIntentKey", usage);
        PendingIntent pi = PendingIntent.getBroadcast(context1, usage, intent, 0);
        AlarmManager am = (AlarmManager) context1.getSystemService(ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal_alarm.getTimeInMillis(), pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, cal_alarm.getTimeInMillis(), pi);
        }

        Toast.makeText(context1, "Alarm set to " + cal_alarm.get(Calendar.DAY_OF_WEEK) + ": "
                + cal_alarm.get(Calendar.HOUR_OF_DAY) + ":" + cal_alarm.get(Calendar.MINUTE), Toast.LENGTH_LONG).show();

        SharedPreferences timePref = context1.getSharedPreferences("nextdate", 0);
        SharedPreferences.Editor editor = timePref.edit();
        editor.putString("week",Integer.toString(cal_alarm.get(Calendar.DAY_OF_YEAR)));
        editor.putString("day", Integer.toString(cal_alarm.get(Calendar.DAY_OF_WEEK)));
        editor.putString("hour", Integer.toString(cal_alarm.get(Calendar.HOUR_OF_DAY)));
        editor.putString("minute", Integer.toString(cal_alarm.get(Calendar.MINUTE)));
        editor.apply();
    }

    public static void cancelAlarm(Context context1, int usage) {
        Intent intent = new Intent(context1, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context1, usage, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context1.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private static Calendar getAlarmDate(int day, int hour, int minute) {

        int actualDay;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);

        if (calendar.before(Calendar.getInstance()) && day > 7) {
            actualDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + day - Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        } else if (calendar.before(Calendar.getInstance()) && day <= 7) {
            actualDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + day + 7 - Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        } else {
            actualDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - day;
        }

        if (actualDay > Calendar.getInstance().getMaximum(Calendar.DAY_OF_YEAR)) {
            actualDay = Calendar.getInstance().getMinimum(Calendar.DAY_OF_YEAR) + Calendar.getInstance().getMaximum(Calendar.DAY_OF_YEAR) - actualDay;
            calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) + 1);
        }

        calendar.set(Calendar.DAY_OF_YEAR, actualDay);

        return calendar;
    }

}

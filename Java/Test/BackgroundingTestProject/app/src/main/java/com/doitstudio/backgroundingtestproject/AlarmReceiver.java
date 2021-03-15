package com.doitstudio.backgroundingtestproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

public class AlarmReceiver extends BroadcastReceiver {

    static MediaPlayer mediaPlayer;
    Context context;
    public static final int REQUEST_CODE = 12345;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        /** TODO: High Sensitive Notification for Alarm, like System Alarm (Snooze, turn off) **/

        //Audio only with media sound on!!
        mediaPlayer = MediaPlayer.create(context, R.raw.single_beep);
        mediaPlayer.start();

        //Hier könnte man alles triggern, was nach einer bestimmten Zeit passieren soll!

        //Tried to set System Timer, but failed :-(
        /*Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(System.currentTimeMillis());
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        Intent intent2 = new Intent(AlarmClock.ACTION_SET_ALARM);
        intent2.putExtra(AlarmClock.EXTRA_HOUR, hour);
        intent2.putExtra(AlarmClock.EXTRA_MINUTES, minute + 13);
        intent2.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        //startActivity(i);
        context.startActivity(intent2);*/

    }
}

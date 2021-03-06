package com.doitstudio.backgroundingtestproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

public class AlarmReceiver extends BroadcastReceiver {

    static MediaPlayer mediaPlayer;

    @Override
    public void onReceive(Context context, Intent intent) {

        mediaPlayer = MediaPlayer.create(context, R.raw.alarm_beep);
        mediaPlayer.start();

        //Hier könnte man alles triggern, was nach einer bestimmten Zeit passieren soll!
/*
        //Tried to set System Timer, but failed :-(
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(System.currentTimeMillis());
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        Intent intent2 = new Intent(AlarmClock.ACTION_SET_ALARM);
        intent2.putExtra(AlarmClock.EXTRA_HOUR, hour);
        intent2.putExtra(AlarmClock.EXTRA_MINUTES, minute + 13);
        intent2.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        //startActivity(i);
        ((Activity) context).startActivity(intent2);

 */
    }
}

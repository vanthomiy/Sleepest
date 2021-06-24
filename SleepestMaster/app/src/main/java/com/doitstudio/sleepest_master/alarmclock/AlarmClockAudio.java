package com.doitstudio.sleepest_master.alarmclock;

/** This class is singleton and you can start the alarm audio from everywhere */

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.doitstudio.sleepest_master.R;
import com.kevalpatel.ringtonepicker.RingtonePickerDialog;
import com.kevalpatel.ringtonepicker.RingtonePickerListener;

import java.io.IOException;
import java.util.stream.Stream;

public class AlarmClockAudio {

    static MediaPlayer mediaPlayer;
    static Ringtone ringtoneManager;
    private AudioManager audioManager;
    private static int audioVolume = 0;
    private CountDownTimer countDownTimer;
    private int ringerMode;

    private Context appContext;
    private static AlarmClockAudio alarmClockAudioInstance;


    /**
     * Initialize the singleton class
     * @param context Application context
     */
    public void init(Context context) {
        if(appContext == null) {
            this.appContext = context;

        }
    }

    /**
     * Application context
     * @return Context
     */
    private Context getContext() {
        return appContext;
    }

    /**
     * Instance context
     * @return Context
     */
    public static Context getInstanceContext() {
        return getInstance().getContext();
    }

    /**
     * Get Instance of the singleton class
     * @return Instance
     */
    public static synchronized AlarmClockAudio getInstance() {

        if (alarmClockAudioInstance == null) {
            alarmClockAudioInstance = new AlarmClockAudio();
        }

        return alarmClockAudioInstance;
    }

    /**
     * Get Instance of the static singleton MediaPlayer
     * @return Instance
     */
    private static MediaPlayer getMediaPlayer() {
        /**TODO: Verschiedene Alarme einfügen, über Einstellungen anpassbar */

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            return mediaPlayer;
        }

        mediaPlayer = null; //Workaround: New instance every time mediaPlayer starts, otherwise audio isn't playing
        mediaPlayer = MediaPlayer.create(getInstanceContext(), R.raw.alarm_beep);

        return mediaPlayer;
    }

    private static Ringtone getRingtoneManager() {
        /**TODO: Verschiedene Alarme einfügen, über Einstellungen anpassbar */

        if (ringtoneManager == null) {
            ringtoneManager = RingtoneManager.getRingtone(getInstanceContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        }

        return ringtoneManager;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void startAlarm() {
        NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if(notificationManager.isNotificationPolicyAccessGranted()) {

            audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
            ringerMode = audioManager.getRingerMode();
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

            ringtoneManager = AlarmClockAudio.getRingtoneManager();
            ringtoneManager.setVolume((float) audioManager.getStreamVolume(AudioManager.STREAM_ALARM) / (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
            ringtoneManager.play();

            countDownTimer = new CountDownTimer(60000, 1000) {

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {

                    if (ringtoneManager.isPlaying()) {
                        stopAlarm(true);
                    }
                }

            }.start();

        } else {


            audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);

            mediaPlayer = AlarmClockAudio.getInstance().getMediaPlayer();

            mediaPlayer.start();
            mediaPlayer.setLooping(true);


            //Saves the actual audio volume height
            SharedPreferences pref = appContext.getSharedPreferences("Audio", 0);
            SharedPreferences.Editor ed = pref.edit();
            ed.putInt("volume", audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            ed.apply();

            audioVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2), 0);

            /**TODO: VIBRATION, if necessary */

            //Timer of 1 minute, which snoozes the alarm after finishing
            countDownTimer = new CountDownTimer(60000, 1000) {

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {

                    if (mediaPlayer.isPlaying()) {
                        stopAlarm(true);
                    }
                }

            }.start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void test() {
        final Ringtone r = RingtoneManager.getRingtone(appContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        r.setLooping(true);
        //r.play();
        countDownTimer = new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) { }

            public void onFinish() {

                if (r.isPlaying()) {
                    r.stop();
                }
            }

        }.start();
    }

    /**
     * Stop and restart the alarm if necessary
     * @param restart True = restart, False = no restart
     */
    public void stopAlarm(boolean restart) {

        if (restart) {
            //Snoozes the alarm for 10 minutes
            AlarmClockReceiver.restartAlarmManager(600000, getInstanceContext());
        }

        NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if(notificationManager.isNotificationPolicyAccessGranted()) {
            ringtoneManager.stop();
            audioManager.setRingerMode(ringerMode);
        } else {
            //restore the audio volume height and cancel countdown and notification
            audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, audioVolume, 0);

            mediaPlayer.stop();
            mediaPlayer.setLooping(false);
        }

        countDownTimer.cancel();
        AlarmClockReceiver.cancelNotification();


    }

    /*private void selectRingTone() {
        RingtonePickerDialog.Builder ringtonePickerBuilder = new RingtonePickerDialog.Builder(this, getSupportFragmentManager())
                .setTitle("Select your ringtone")
                .displayDefaultRingtone(true)
                .setPositiveButtonText("Set")
                .setCancelButtonText("Cancel")
                .setPlaySampleWhileSelection(true)
                .setListener(new RingtonePickerListener() {
                    @Override
                    public void OnRingtoneSelected(@NonNull String ringtoneName, @Nullable Uri ringtoneUri) {
                        String uri = ringtoneUri.toString();
                        Toast.makeText(appContext, uri, Toast.LENGTH_LONG).show();
                    }
                });
        ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_ALARM);
        ringtonePickerBuilder.show();
    }*/

}

package com.doitstudio.sleepest_master.AlarmClock;

/** This class is singleton and you can start the alarm audio from everywhere */

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;

import com.doitstudio.sleepest_master.R;

public class AlarmClockAudio {

    static MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private int audioVolume;
    private CountDownTimer countDownTimer;

    private Context appContext;
    private static AlarmClockAudio alarmClockAudioInstance;

    /**
     * Initialize the singleton class
     * @param context Application context
     */
    public void init(Context context) {
        if(appContext == null) {
            this.appContext = context;
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
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

    public void startAlarm() {

        mediaPlayer = AlarmClockAudio.getInstance().getMediaPlayer();
        mediaPlayer.start();
        mediaPlayer.setLooping(true);

        //Saves the actual audio volume height
        audioVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2), 0); /**TODO: Settings of Alarm height*/

        /**TODO: VIBRATION, if necessary */

        //Timer of 1 minute, which snoozes the alarm after finishing
        countDownTimer = new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) { }

            public void onFinish() {

                if (mediaPlayer.isPlaying()) {
                    stopAlarm(true);
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

        //mediaPlayer = SingleTonExample.getInstance().getMediaPlayer();
        mediaPlayer.stop();
        mediaPlayer.setLooping(false);

        //restore the audio volume height and cancel countdown and notification
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioVolume, 0);
        countDownTimer.cancel();
        AlarmClockReceiver.cancelNotification();
    }

}

package com.sleepestapp.sleepest.alarmclock;


import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.RequiresApi;

import com.sleepestapp.sleepest.R;
import com.sleepestapp.sleepest.model.data.AlarmClockReceiverUsage;
import com.sleepestapp.sleepest.model.data.Constants;
import com.sleepestapp.sleepest.model.data.NotificationUsage;
import com.sleepestapp.sleepest.storage.DataStoreRepository;
import com.sleepestapp.sleepest.util.NotificationUtil;
import com.sleepestapp.sleepest.util.TimeConverterUtil;

import java.time.LocalTime;
import java.util.Calendar;

/** This class is singleton and you can start the alarm audio from everywhere */

public class AlarmClockAudio {

    static MediaPlayer mediaPlayer;
    static Ringtone ringtoneManager;
    static Vibrator vibrator;
    static AudioManager audioManager;
    private int audioVolume;
    private CountDownTimer countDownTimer;
    private int ringerMode;

    private Context appContext;
    private static AlarmClockAudio alarmClockAudioInstance;
    private DataStoreRepository dataStoreRepository;


    /**
     * Initialize the singleton class
     * @param context Application context
     */
    public void init(Context context) {
        if(appContext == null) {
            this.appContext = context;
            dataStoreRepository = DataStoreRepository.Companion.getRepo(appContext);
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

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            return mediaPlayer;
        }

        mediaPlayer = null; //Workaround: New instance every time mediaPlayer starts, otherwise audio isn't playing
        mediaPlayer = MediaPlayer.create(getInstanceContext(), R.raw.alarm_beep);

        return mediaPlayer;
    }

    /**
     * Get instance of ringtone
     * @return instance
     */
    private static Ringtone getRingtoneManager(String tone) {

        if (ringtoneManager == null) {
            if (tone.equals("null")) {
                ringtoneManager = RingtoneManager.getRingtone(getInstanceContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
            } else {
                ringtoneManager = RingtoneManager.getRingtone(getInstanceContext(), Uri.parse(tone));
            }
 }

        return ringtoneManager;
    }

    /**
     * Get instance of vibrator
     * @return instance
     */
    private static Vibrator getVibrator() {
        if (vibrator == null) {
            vibrator = (Vibrator) getInstanceContext().getSystemService(Context.VIBRATOR_SERVICE);
        }

        return vibrator;
    }

    /**
     * Get instance of audio manager
     * @return instance
     */
    private static AudioManager getAudioManager() {
        if (audioManager == null) {
            audioManager = (AudioManager) getInstanceContext().getSystemService(Context.AUDIO_SERVICE);
        }
        return audioManager;
    }

    /**
     * Start the alarm and check the settings for the alarm
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    public void startAlarm(boolean screenOn) {
        NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

        //Check for do not disturb permission, otherwise play sound with help of music stream
        if(notificationManager.isNotificationPolicyAccessGranted()) {
            audioManager = getAudioManager();
            ringerMode = audioManager.getRingerMode();
            switch(dataStoreRepository.getAlarmArtJob()) {
                case 0:
                    startRingtoneWithPermission();
                    break;
                case 1:
                    startRingtoneWithPermissionAndVibrate();
                    break;
                case 2:
                    startVibration();
                    break;
            }
        } else {
            startRingtoneWithoutPermission();
        }

        if (screenOn) {

            //Timer of 1 minute, which snoozes the alarm after finishing
            countDownTimer = new CountDownTimer(Constants.MILLIS_UNTIL_SNOOZE, Constants.COUNTDOWN_TICK_INTERVAL) {

                public void onTick(long millisUntilFinished) {
                    //Unused at the moment
                }

                public void onFinish() {

                    stopAlarm(true, true);
                }

            }.start();
        }
    }

    /**
     * Start the vibration
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void startVibration() {

        //Get instance of audio manager and save the actual ringer mode and set vibration mode
        audioManager = getAudioManager();
        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

        //Init waveform for vibration and start vibration
        long[] waveform = Constants.VIBRATION_WAVEFORM;
        vibrator = AlarmClockAudio.getVibrator();
        vibrator.vibrate(VibrationEffect.createWaveform(waveform, 0));
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void startRingtoneWithPermission() {
        //Get instance of audio manager and save the actual ringer mode and deactivate silence mode
        audioManager = getAudioManager();
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

        //Get instance of ringtone
        ringtoneManager = getRingtoneManager(dataStoreRepository.getAlarmToneJob());

        //Convert integer volume to float volume 0.0 - 1.0 for ringtone
        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) > 0) {
            ringtoneManager.setVolume((float) audioManager.getStreamVolume(AudioManager.STREAM_ALARM) / (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        } else {
            ringtoneManager.setVolume(0.0f);
        }

        //play ringtone
        ringtoneManager.setLooping(true);
        ringtoneManager.play();
    }

    /**
     * Start the alarm tone with do not disturb permission
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void startRingtoneWithPermissionAndVibrate() {

        //Get instance of audio manager and save the actual ringer mode and deactivate silence mode
        audioManager = getAudioManager();
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

        //Get instance of ringtone
        ringtoneManager = getRingtoneManager(dataStoreRepository.getAlarmToneJob());

        //Convert integer volume to float volume 0.0 - 1.0 for ringtone
        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) > 0) {
            ringtoneManager.setVolume((float) audioManager.getStreamVolume(AudioManager.STREAM_ALARM) / (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        } else {
            ringtoneManager.setVolume(0.0f);
        }

        //play ringtone
        ringtoneManager.setLooping(true);
        ringtoneManager.play();

        long[] waveform = Constants.VIBRATION_WAVEFORM;
        vibrator = AlarmClockAudio.getVibrator();
        vibrator.vibrate(VibrationEffect.createWaveform(waveform, 0));



    }

    /**
     * Start ring tone without permission
     */
    private void startRingtoneWithoutPermission() {
        audioManager = getAudioManager();

        //Play sound with mediaplayer
        mediaPlayer = getMediaPlayer();
        mediaPlayer.start();
        mediaPlayer.setLooping(true);

        //Set audio volume to half of max volume
        audioVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2), 0);
    }

    /**
     * Stop and restart the alarm if necessary
     * @param restart True = restart, False = no restart
     */
    public void stopAlarm(boolean restart, boolean screenOn) {

        if (restart) {
            //Snoozes the alarm for 10 minutes
            Calendar calendar = TimeConverterUtil.getAlarmDate(LocalTime.now().toSecondOfDay() + Constants.MILLIS_SNOOZE/1000);
            AlarmClockReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), getContext(), AlarmClockReceiverUsage.START_ALARMCLOCK);
        }

        NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        audioManager = getAudioManager();

        if(notificationManager.isNotificationPolicyAccessGranted()) {
            audioManager.setRingerMode(ringerMode);

            //Resets the ringtone or vibrator depending on the settings
            switch(dataStoreRepository.getAlarmArtJob()) {
                case 0:
                    ringtoneManager = getRingtoneManager(dataStoreRepository.getAlarmToneJob());
                    ringtoneManager.stop();
                    break;
                case 1:
                    ringtoneManager = getRingtoneManager(dataStoreRepository.getAlarmToneJob());
                    ringtoneManager.stop();
                    vibrator = getVibrator();
                    vibrator.cancel();
                    break;
                case 2:
                    vibrator = getVibrator();
                    vibrator.cancel();
                    break;
            }
        } else {
            //restore the audio volume height
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioVolume, 0);

            mediaPlayer = getMediaPlayer();
            mediaPlayer.stop();
            mediaPlayer.setLooping(false);
        }

        //cancel countdown and notification
        if (screenOn) {
            countDownTimer.cancel();
        }

        NotificationUtil.cancelNotification(NotificationUsage.NOTIFICATION_ALARM_CLOCK, appContext);
        NotificationUtil.cancelNotification(NotificationUsage.NOTIFICATION_ALARM_CLOCK_LOCK_SCREEN, appContext);
    }
}

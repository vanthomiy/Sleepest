package com.doitstudio.sleepest_master.alarmclock;

/** This class is singleton and you can start the alarm audio from everywhere */

import android.app.NotificationManager;
import android.app.Service;
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
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.doitstudio.sleepest_master.MainApplication;
import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.storage.DataStoreRepository;
import com.kevalpatel.ringtonepicker.RingtonePickerDialog;
import com.kevalpatel.ringtonepicker.RingtonePickerListener;

import java.io.IOException;
import java.util.stream.Stream;

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
            dataStoreRepository = ((MainApplication)appContext).getDataStoreRepository();

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

    /**
     * Get instance of ringtone
     * @return instance
     */
    private static Ringtone getRingtoneManager(String tone) {
        /**TODO: Verschiedene Alarme einfügen, über Einstellungen anpassbar */

        if (ringtoneManager == null) {
            if (tone == "null") {
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
    public void startAlarm() {
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
                    startVibration();
                    startRingtoneWithPermission();
                    break;
                case 2:
                    startVibration();
                    break;
            }
        } else {

            startRingtoneWithoutPermission();
        }

        //Timer of 1 minute, which snoozes the alarm after finishing
        countDownTimer = new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {

                if (ringtoneManager.isPlaying()) {
                    stopAlarm(true);
                }
            }

        }.start();
    }

    /**
     * Start the vibration
     */
    private void startVibration() {

        //Get instance of audio manager and save the actual ringer mode and set vibration mode
        audioManager = getAudioManager();
        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

        //Init waveform for vibration and start vibration
        long[] waveform = {0, 400, 800, 400, 800, 400, 800};
        vibrator = AlarmClockAudio.getVibrator();
        vibrator.vibrate(VibrationEffect.createWaveform(waveform, 0));
    }

    /**
     * Start the alarm tone with do not disturb permission
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void startRingtoneWithPermission() {

        //Get instance of audio manager and save the actual ringer mode and deactivate silence mode
        audioManager = getAudioManager();
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

        //Get instance of ringtone
        ringtoneManager = getRingtoneManager(dataStoreRepository.getAlarmToneJob());
        /**TODO: Abfrage, ob Uri noch mit dem eingestellten übereinstimmt*/

        //Convert integer volume to float volume 0.0 - 1.0 for ringtone
        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) > 0) {
            ringtoneManager.setVolume((float) audioManager.getStreamVolume(AudioManager.STREAM_ALARM) / (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        } else {
            ringtoneManager.setVolume(0.0f);
        }

        //play ringtone
        ringtoneManager.play();



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
    public void stopAlarm(boolean restart) {

        if (restart) {
            //Snoozes the alarm for 10 minutes
            AlarmClockReceiver.restartAlarmManager(600000, getInstanceContext());
        }

        NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        audioManager = getAudioManager();

        if(notificationManager.isNotificationPolicyAccessGranted()) {
            audioManager.setRingerMode(ringerMode);


            /**TODO: Abfrage Vibration*/

            vibrator = getVibrator();
            vibrator.cancel();

            //ringtoneManager = getRingtoneManager();
            //ringtoneManager.stop();

        } else {
            //restore the audio volume height
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioVolume, 0);

            mediaPlayer = getMediaPlayer();
            mediaPlayer.stop();
            mediaPlayer.setLooping(false);
        }

        //cancel countdown and notification
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

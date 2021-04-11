package com.doitstudio.backgroundingtestproject;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG_WORK = "Workmanager 1";
    public static final String CHANNEL_ID = "VERBOSE_NOTIFICATION" ;
    //private AudioManager audioManager;

    Button btnAddAlarm, btnStartWorkmanager, btnStopForegroundservice;
    Spinner spHour, spMinute, spDay;
    EditText etDuration;
    TextView tvLastTime;
    String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    static MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAddAlarm = (Button) findViewById(R.id.btnAddAlarm);
        btnStartWorkmanager = (Button) findViewById(R.id.btnStartWorkmanager);
        btnStopForegroundservice = (Button) findViewById(R.id.btnStopForegroundservice);
        spHour = (Spinner) findViewById(R.id.spHour);
        spMinute = (Spinner) findViewById(R.id.spMinute);
        spDay = (Spinner) findViewById(R.id.spDay);
        etDuration = (EditText) findViewById(R.id.etDuration);
        tvLastTime = (TextView) findViewById(R.id.tvLastTime);

        btnAddAlarm.setOnClickListener(this);
        btnStartWorkmanager.setOnClickListener(this);
        btnStopForegroundservice.setOnClickListener(this);

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, days);
        spDay.setAdapter(dayAdapter);

        Integer[] hours = new Integer[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23};
        ArrayAdapter<Integer> hoursAdapter = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item, hours);
        spHour.setAdapter(hoursAdapter);

        Integer[] minutes = new Integer[60];
        for (int i = 0; i<60; i++) {
            minutes[i] = i;
        }
        ArrayAdapter<Integer> minutesAdapter = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item, minutes);
        spMinute.setAdapter(minutesAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences timePref = getSharedPreferences("time", 0);
        SharedPreferences nextDatePref = getSharedPreferences("nextdate", 0);
        SharedPreferences lastDatePref = getSharedPreferences("lastalarm", 0);
        tvLastTime.setText("Actual Day Number: " + Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + "\n" +
                "Last Workmanager Call: " + timePref.getString("hour", "XX") + ":" + timePref.getString("minute", "XX") + "\n" +
                "Next Alarm at: " + nextDatePref.getString("week", "XX") + ", " + nextDatePref.getString("day", "XX") + ", " + nextDatePref.getString("hour", "XX") + ":" + nextDatePref.getString("minute", "XX") + "\n" +
                "Last Alarm at: " + lastDatePref.getString("day", "XX") + ", " + lastDatePref.getString("hour", "XX") + ":" + lastDatePref.getString("minute", "XX"));


    }

    //Geht bisher garnicht, nur sporadisch
    public void scheduleAlarm() {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every fifteen minutes from this point onwards
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pIntent); //16:28

        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.single_beep);
        mediaPlayer.start();
    }

    public void adjustStreamVolume() {

        AudioManager audioManager =
                (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(audioManager.STREAM_ALARM), AudioManager.FLAG_PLAY_SOUND);
        
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddAlarm:
                int day = spDay.getSelectedItemPosition() + 1;
                AlarmReceiver.startAlarmManager(day, (int) spHour.getSelectedItem(), (int) spMinute.getSelectedItem(), MainActivity.this, 1);

                //EndlessService.startForegroundService(Actions.START, getApplicationContext());
                //AlarmReceiver.startAlarmManager(day, (int) spHour.getSelectedItem(), (int) spMinute.getSelectedItem(), getApplicationContext(), 2);
                //adjustStreamVolume();
                break;
            /*case R.id.btnStartWorkmanager:

                int duration;

                if (etDuration.getText().toString().trim().length() <= 0) {
                    duration = 15;
                } else {
                    duration = Integer.parseInt(etDuration.getText().toString());
                    if (duration < 15) { duration = 15; }
                }

                Workmanager.startPeriodicWorkmanager(duration);
                EndlessService.startForegroundService(Actions.START, getApplicationContext());
                //EndlessService.startForegroundService(Actions.STOP, getApplicationContext());
                // scheduleAlarm();
                break;
            case R.id.btnStopForegroundservice:
                EndlessService.startForegroundService(Actions.STOP, getApplicationContext());
                break;*/
        }
    }
}



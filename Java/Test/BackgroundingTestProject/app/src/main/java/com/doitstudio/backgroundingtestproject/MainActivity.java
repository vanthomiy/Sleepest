package com.doitstudio.backgroundingtestproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG_WORK = "Workmanager 1";
    public static final String CHANNEL_ID = "VERBOSE_NOTIFICATION" ;

    Button btnAddAlarm, btnStartWorkmanager;
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
        spHour = (Spinner) findViewById(R.id.spHour);
        spMinute = (Spinner) findViewById(R.id.spMinute);
        spDay = (Spinner) findViewById(R.id.spDay);
        etDuration = (EditText) findViewById(R.id.etDuration);
        tvLastTime = (TextView) findViewById(R.id.tvLastTime);

        btnAddAlarm.setOnClickListener(this);
        btnStartWorkmanager.setOnClickListener(this);


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
        tvLastTime.setText(timePref.getString("hour", "00") + ":" + timePref.getString("minute", "00"));

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddAlarm:

                int day = spDay.getSelectedItemPosition();
                AlarmReceiver.startAlarmManager(day, (int) spHour.getSelectedItem(), (int) spMinute.getSelectedItem(), getApplicationContext());
                break;
            case R.id.btnStartWorkmanager:

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
        }
    }
}



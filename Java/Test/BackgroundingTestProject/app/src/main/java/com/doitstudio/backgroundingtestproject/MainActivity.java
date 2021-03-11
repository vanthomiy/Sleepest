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
    Spinner spHour, spMinute;
    EditText etDuration;

    static MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAddAlarm = (Button) findViewById(R.id.btnAddAlarm);
        btnStartWorkmanager = (Button) findViewById(R.id.btnStartWorkmanager);
        spHour = (Spinner) findViewById(R.id.spHour);
        spMinute = (Spinner) findViewById(R.id.spMinute);
        etDuration = (EditText) findViewById(R.id.etDuration);

        btnAddAlarm.setOnClickListener(this);
        btnStartWorkmanager.setOnClickListener(this);

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

    //Funktioniert ziemlich gut, Auslösung an bestimmter Uhrzeit
    @RequiresApi(api = Build.VERSION_CODES.KITKAT) //KITKAT is minimum version!
    public void startAlarmManager(int hour, int min) {

        Calendar cal_alarm = Calendar.getInstance();
        cal_alarm.set(Calendar.HOUR_OF_DAY, hour);
        cal_alarm.set(Calendar.MINUTE, min);
        cal_alarm.set(Calendar.SECOND, 0);
        cal_alarm.set(Calendar.MILLISECOND, 0);

        if (cal_alarm.before(Calendar.getInstance())) {
            Toast.makeText(getApplicationContext(), "Date passed moved to next date.", Toast.LENGTH_SHORT).show();
            cal_alarm.add(Calendar.DATE, 1);
        }

        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal_alarm.getTimeInMillis(), pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, cal_alarm.getTimeInMillis(), pi);
        }

        Toast.makeText(getApplicationContext(), "Alarm set", Toast.LENGTH_SHORT).show();
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

    //Geht nur eine bestimmte Zeit lang, dann hörts auf einmal auf
    private void startPeriodicWorkmanager() {

        int duration;

        if (etDuration.getText().toString().trim().length() <= 0) {
            duration = 15;
        } else {
            duration = Integer.parseInt(etDuration.getText().toString());
            if (duration < 15) { duration = 15; }
        }

        //Constraints not necessary, but useful
        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .build();

        PeriodicWorkRequest periodicDataWork =
                new PeriodicWorkRequest.Builder(Workmanager.class, duration, TimeUnit.MINUTES)
                        .addTag(TAG_WORK)
                        .setConstraints(constraints)
                        // setting a backoff on case the work needs to retry
                        //.setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                        .build();

        WorkManager workManager = WorkManager.getInstance(this);
        //workManager.enqueue(periodicDataWork);
        workManager.enqueueUniquePeriodicWork(TAG_WORK, ExistingPeriodicWorkPolicy.KEEP, periodicDataWork);
        showNotification(getApplicationContext());
    }

    private void showNotification(Context context) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("My notification")
                .setContentText("ddd")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("ddd"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel_name";
            String description = "description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(100, mBuilder.build());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddAlarm:
                startAlarmManager( (int) spHour.getSelectedItem(), (int) spMinute.getSelectedItem() );
                break;
            case R.id.btnStartWorkmanager:
                startPeriodicWorkmanager();
                // scheduleAlarm();
                break;
        }
    }
}



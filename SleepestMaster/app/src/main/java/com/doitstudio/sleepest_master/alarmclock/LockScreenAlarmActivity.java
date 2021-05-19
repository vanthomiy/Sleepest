package com.doitstudio.sleepest_master.alarmclock;

/**This Activity shows a view on the lock screen when alarm was fired. The user can
 * cancel or snooze the alarm with buttons.
 */

import androidx.appcompat.app.AppCompatActivity;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;

import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.background.AlarmReceiver;
import com.doitstudio.sleepest_master.background.ForegroundService;
import com.doitstudio.sleepest_master.background.Times;
import com.doitstudio.sleepest_master.model.data.Actions;

import java.util.Calendar;

public class LockScreenAlarmActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnTurnAlarmOffLockScreen, btnSnoozeAlarmLockScreen;
    Times times;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen_alarm);

        // Init buttons
        btnSnoozeAlarmLockScreen = (Button) findViewById(R.id.btnTurnAlarmOffLockScreen);
        btnTurnAlarmOffLockScreen = (Button) findViewById(R.id.btnSnoozeAlarmLockScreen);

        btnTurnAlarmOffLockScreen.setOnClickListener(this);
        btnSnoozeAlarmLockScreen.setOnClickListener(this);

        //Enable view when screen is locked
        setShowWhenLocked(true);
        setTurnScreenOn(true);

        //Dismiss Keyguard for this Activity
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        keyguardManager.requestDismissKeyguard(LockScreenAlarmActivity.this, null);

        times = new Times();
    }

    @Override
    protected void onResume() {
        super.onResume();



        AlarmClockAudio.getInstance().init(getApplicationContext());
        AlarmClockAudio.getInstance().startAlarm();


        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) { }

            public void onFinish() {
                finish();
            }

        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Lock screen again after finishing actions
        setShowWhenLocked(false);
        setTurnScreenOn(false);

    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.btnTurnAlarmOffLockScreen:
                AlarmClockAudio.getInstance().stopAlarm(false);

                Calendar calendarAlarm = AlarmReceiver.getAlarmDate(Calendar.getInstance().get(Calendar.DAY_OF_WEEK), times.getStartForegroundHour(), times.getStartForegroundMinute());
                AlarmReceiver.startAlarmManager(calendarAlarm.get(Calendar.DAY_OF_WEEK), calendarAlarm.get(Calendar.HOUR_OF_DAY), calendarAlarm.get(Calendar.MINUTE), getApplicationContext(), 1);
                ForegroundService.startOrStopForegroundService(Actions.STOP, getApplicationContext());

                Calendar calendar = Calendar.getInstance();
                SharedPreferences pref = getSharedPreferences("AlarmClock", 0);
                SharedPreferences.Editor ed = pref.edit();
                ed.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
                ed.putInt("minute", calendar.get(Calendar.MINUTE));
                ed.apply();

                finish();
                break;
            case R.id.btnSnoozeAlarmLockScreen:
                AlarmClockAudio.getInstance().stopAlarm(true);
                finish();
                break;
        }
    }
}
package com.doitstudio.sleepest_master.alarmclock;

/**This Activity shows a view on the lock screen when alarm was fired. The user can
 * cancel or snooze the alarm with buttons.
 */

import androidx.appcompat.app.AppCompatActivity;

import android.app.KeyguardManager;
import android.content.Context;
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
    }

    @Override
    protected void onResume() {
        super.onResume();

        Times times = new Times();

        AlarmClockAudio.getInstance().init(getApplicationContext());
        AlarmClockAudio.getInstance().startAlarm();

        ForegroundService.startOrStopForegroundService(Actions.STOP, getApplicationContext());

        Calendar calendarAlarm = AlarmReceiver.getAlarmDate(Calendar.getInstance().get(Calendar.DAY_OF_WEEK), times.getStartForegroundHour(), times.getStartForegroundMinute());
        AlarmReceiver.startAlarmManager(calendarAlarm.get(Calendar.DAY_OF_WEEK), calendarAlarm.get(Calendar.HOUR_OF_DAY), calendarAlarm.get(Calendar.MINUTE), getApplicationContext(), 1);

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
                finish();
                break;
            case R.id.btnSnoozeAlarmLockScreen:
                AlarmClockAudio.getInstance().stopAlarm(true);
                finish();
                break;
        }
    }
}
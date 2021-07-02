package com.doitstudio.sleepest_master.alarmclock;

/**This Activity shows a view on the lock screen when alarm was fired. The user can
 * cancel or snooze the alarm with buttons.
 */

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.background.AlarmReceiver;
import com.doitstudio.sleepest_master.background.ForegroundService;
import com.doitstudio.sleepest_master.model.data.Actions;
import com.doitstudio.sleepest_master.model.data.AlarmReceiverUsage;
import com.doitstudio.sleepest_master.storage.DataStoreRepository;

import java.io.IOException;
import java.util.Calendar;

public class LockScreenAlarmActivity extends AppCompatActivity {

    private Button btnSnoozeAlarmLockScreen;
    private SwipeListener swipeListener;
    private TextView tvSwipeUp;
    private ImageView ivSwipeUpArrow;
    private DataStoreRepository dataStoreRepository;
    private boolean isStarted = false;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen_alarm);

        dataStoreRepository = DataStoreRepository.Companion.getRepo(getApplicationContext());

        RelativeLayout relativeLayout = findViewById(R.id.layoutLockscreen);

        ivSwipeUpArrow = findViewById(R.id.ivSwipeUpArrow);
        tvSwipeUp = findViewById(R.id.tvSwipeUpText);

        // Init buttons
        btnSnoozeAlarmLockScreen = (Button) findViewById(R.id.btnSnoozeAlarmLockScreen);
        btnSnoozeAlarmLockScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmClockAudio.getInstance().stopAlarm(true);
                finish();
            }
        });

        //Enable view when screen is locked
        setShowWhenLocked(true);
        setTurnScreenOn(true);

        //Dismiss Keyguard for this Activity
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        keyguardManager.requestDismissKeyguard(LockScreenAlarmActivity.this, null);

        swipeListener = new SwipeListener(relativeLayout);


    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onResume() {
        super.onResume();

        if (!isStarted) {
            isStarted = true;
            fadeColor(tvSwipeUp);

            AlarmClockAudio.getInstance().init(getApplicationContext());
            AlarmClockAudio.getInstance().startAlarm();



            new CountDownTimer(1000, 1000) {

                public void onTick(long millisUntilFinished) { }

                public void onFinish() {
                    moveView(ivSwipeUpArrow);
                }

            }.start();


            new CountDownTimer(60000, 1000) {

                public void onTick(long millisUntilFinished) { }

                public void onFinish() {
                    finish();
                }

            }.start();
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Lock screen again after finishing actions
        setShowWhenLocked(false);
        setTurnScreenOn(false);

    }

    private void moveView(View view )
    {
        RelativeLayout root = findViewById(R.id.layoutLockscreen);
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics( dm );

        int[] originalPos = new int[2];
        view.getLocationOnScreen(originalPos);

        int xDest = dm.widthPixels / 2;
        xDest -= view.getMeasuredWidth() / 2;
        int yDest = view.getMeasuredHeight();

        TranslateAnimation translateAnimation = new TranslateAnimation( 0, xDest - originalPos[0] , 0, yDest - originalPos[1] );
        translateAnimation.setDuration(1000);
        translateAnimation.setRepeatCount(Animation.INFINITE);
        translateAnimation.setRepeatMode(Animation.RESTART);
        translateAnimation.setFillAfter( true );
        view.startAnimation(translateAnimation);
    }

    private void fadeColor(TextView textView) {

        int colorFrom = getResources().getColor(R.color.cyan_aqua, getTheme());
        int colorTo = getResources().getColor(R.color.blue_chill, getTheme());

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);

        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {

                tvSwipeUp.setTextColor((Integer)animator.getAnimatedValue());

            }
        });

        colorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                //moveViewToScreenCenter(imageView);
            }
        });
        colorAnimation.setDuration(2000);
        colorAnimation.setRepeatCount(ValueAnimator.INFINITE);
        colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
        colorAnimation.start();
    }

    private class SwipeListener implements View.OnTouchListener {

        GestureDetector gestureDetector;

        SwipeListener(View view) {
            int threshold = 100;
            int velocity = 100;

            GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                    float xDiff = e2.getX() - e1.getX();
                    float yDiff = e2.getY() - e1.getY();

                    try {
                        if(Math.abs(xDiff) > Math.abs((yDiff))) {
                            if ((Math.abs(xDiff) > threshold) && (Math.abs(velocityX) > velocity)) {
                                if (xDiff > 0) {
                                    //Swipe right
                                } else {
                                    //Swipe left
                                }
                                return true;
                            }
                        } else {
                            if ((Math.abs(yDiff) > threshold) && (Math.abs(velocityY) > velocity)) {
                                if (yDiff > 0) {
                                    //Swipe down
                                } else {

                                    AlarmClockAudio.getInstance().stopAlarm(false);

                                    Calendar calendarAlarm = AlarmReceiver.getAlarmDate(dataStoreRepository.getSleepTimeBeginJob());
                                    AlarmReceiver.startAlarmManager(calendarAlarm.get(Calendar.DAY_OF_WEEK), calendarAlarm.get(Calendar.HOUR_OF_DAY), calendarAlarm.get(Calendar.MINUTE), getApplicationContext(), AlarmReceiverUsage.START_FOREGROUND);
                                    ForegroundService.startOrStopForegroundService(Actions.STOP, getApplicationContext());

                                    Calendar calendar = Calendar.getInstance();
                                    SharedPreferences pref = getSharedPreferences("AlarmClock", 0);
                                    SharedPreferences.Editor ed = pref.edit();
                                    ed.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
                                    ed.putInt("minute", calendar.get(Calendar.MINUTE));
                                    ed.apply();

                                    pref = getSharedPreferences("AlarmReceiver1", 0);
                                    ed = pref.edit();
                                    ed.putString("usage", "LockScreenAlarmActivity");
                                    ed.putInt("day", calendarAlarm.get(Calendar.DAY_OF_WEEK));
                                    ed.putInt("hour", calendarAlarm.get(Calendar.HOUR_OF_DAY));
                                    ed.putInt("minute", calendarAlarm.get(Calendar.MINUTE));
                                    ed.apply();

                                    finish();
                                }
                                return true;
                            }
                        }
                    } catch (Exception e) {

                    }
                    return false;

                }
            };

            gestureDetector = new GestureDetector(listener);
            view.setOnTouchListener(this);
        }



        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }
    }
}
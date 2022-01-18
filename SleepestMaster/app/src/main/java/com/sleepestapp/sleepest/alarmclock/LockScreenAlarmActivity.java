package com.sleepestapp.sleepest.alarmclock;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.motion.widget.TransitionAdapter;
import androidx.core.content.ContextCompat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.sleepestapp.sleepest.R;
import com.sleepestapp.sleepest.background.BackgroundAlarmTimeHandler;
import com.sleepestapp.sleepest.databinding.ActivityLockScreenAlarmBinding;
import com.sleepestapp.sleepest.databinding.ActivityLockScreenAlarmV2Binding;
import com.sleepestapp.sleepest.model.data.Constants;

/**
 * This Activity shows a view on the lock screen when alarm was fired. The user can
 * cancel or snooze the alarm with buttons.
 */
@SuppressWarnings("unused")
public class LockScreenAlarmActivity extends AppCompatActivity {

    //public SwipeListener swipeListener;
    private boolean isStarted = false;
    private CountDownTimer countDownTimer = null;
    private ActivityLockScreenAlarmV2Binding binding;

    private AlarmClockSleepCalculationHandling asch;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen_alarm_v2);

        asch = new AlarmClockSleepCalculationHandling(this);

        binding = ActivityLockScreenAlarmV2Binding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.btnSnoozeAlarmLockScreen.setOnClickListener(v -> {
            AlarmClockAudio.getInstance().stopAlarm(true, false);
            finish();
        });

        binding.layLockMain.setTransitionListener(new TransitionAdapter() {
            public void onTransitionChange(MotionLayout motionLayout, int startId, int endId, float progress) {
            }

            public void onTransitionCompleted(MotionLayout motionLayout, int currentId) {
                if (motionLayout.getProgress() > 0)
                {
                    BackgroundAlarmTimeHandler.Companion.getHandler(getApplicationContext()).alarmClockRang(false);
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }

                    if(asch != null){
                        asch.defineNewUserWakeup(null, false);
                    }

                    finish();

                }
            }
            public void onTransitionStarted(MotionLayout motionLayout, int startId, int endId) {
            }
            public void onTransitionTrigger(MotionLayout motionLayout, int triggerId, boolean positive, float progress) {
            }
        });


        //Enable view when screen is locked
        setShowWhenLocked(true);
        setTurnScreenOn(true);

        //Dismiss Keyguard for this Activity
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        keyguardManager.requestDismissKeyguard(LockScreenAlarmActivity.this, null);

        //Init swipe listener
        //swipeListener = new SwipeListener(binding.layoutLockscreen);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onResume() {
        super.onResume();

        if (!isStarted) {
            isStarted = true; //Workaround because OnResume is sometimes calling twice (Android bug)
            //fadeColor(binding.tvSwipeUpText);

            //Start the ring tone
            AlarmClockAudio.getInstance().init(getApplicationContext());
            AlarmClockAudio.getInstance().startAlarm(false);

            //Delay for motion, workaround
            new CountDownTimer(Constants.DELAY, Constants.COUNTDOWN_TICK_INTERVAL) {

                public void onTick(long millisUntilFinished) { }

                public void onFinish() {
                    //moveView(ivSwipeUpArrow);
                }

            }.start();

            //Countdown for going into snooze mode if not action is detected
            countDownTimer = new CountDownTimer(Constants.MILLIS_UNTIL_SNOOZE, Constants.COUNTDOWN_TICK_INTERVAL) {

                public void onTick(long millisUntilFinished) { }

                public void onFinish() {
                    AlarmClockAudio.getInstance().stopAlarm(true, false);
                    finish();
                }

            }.start();
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Locks the screen again after finishing actions
        setShowWhenLocked(false);
        setTurnScreenOn(false);

    }

    /**
     * Fades the color of text
     * @param textView TextView to be faded
     */
    private void fadeColor(TextView textView) {

        int colorFrom = getResources().getColor(R.color.primary_text_color, getTheme());
        int colorTo = getResources().getColor(R.color.error_color, getTheme());

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);

        colorAnimation.addUpdateListener(animator -> textView.setTextColor((Integer)animator.getAnimatedValue()));

        colorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
            }
        });
        colorAnimation.setDuration(Constants.LOCKSCREEN_COLOR_ANIMATION_DURATION);
        colorAnimation.setRepeatCount(ValueAnimator.INFINITE);
        colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
        colorAnimation.start();
    }


    /**
     * Class to detect swiping on the lockscreen
     */
    /*
    @SuppressWarnings("unused")
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

                                return true;
                            }
                        } else {
                            if ((Math.abs(yDiff) > threshold) && (Math.abs(velocityY) > velocity)) {
                                if (!(yDiff > 0)) {
                                    //Swipe up -> Cancel alarm
                                    BackgroundAlarmTimeHandler.Companion.getHandler(getApplicationContext()).alarmClockRang(false);
                                    if (countDownTimer != null) {
                                        countDownTimer.cancel();
                                    }
                                    finish();
                                }
                                return true;
                            }
                        }
                    } catch (Exception e) {
                        //Not needed, after countdown alarm will snooze
                    }
                    return false;

                }
            };

            gestureDetector = new GestureDetector(getApplicationContext(), listener);
            view.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            v.performClick(); // without is warning
            return gestureDetector.onTouchEvent(event);
        }
    }
    */
}
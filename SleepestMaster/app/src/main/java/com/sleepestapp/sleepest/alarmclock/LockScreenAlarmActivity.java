package com.sleepestapp.sleepest.alarmclock;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sleepestapp.sleepest.MainApplication;
import com.sleepestapp.sleepest.R;
import com.sleepestapp.sleepest.background.BackgroundAlarmTimeHandler;
import com.sleepestapp.sleepest.model.data.Actions;
import com.sleepestapp.sleepest.model.data.AlarmReceiverUsage;
import com.sleepestapp.sleepest.model.data.Constants;
import com.sleepestapp.sleepest.model.data.NotificationUsage;
import com.sleepestapp.sleepest.storage.DataStoreRepository;
import com.sleepestapp.sleepest.storage.DatabaseRepository;
import com.sleepestapp.sleepest.background.BackgroundAlarmTimeHandler;

public class LockScreenAlarmActivity extends AppCompatActivity {

    private Button btnSnoozeAlarmLockScreen;
    private SwipeListener swipeListener;
    private TextView tvSwipeUp;
    private ImageView ivSwipeUpArrow;
    private DataStoreRepository dataStoreRepository;
    private DatabaseRepository databaseRepository;
    private boolean isStarted = false;
    private CountDownTimer countDownTimer = null;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen_alarm);

        //Init
        dataStoreRepository = DataStoreRepository.Companion.getRepo(getApplicationContext());

        //Init Resources
        LinearLayout layout = findViewById(R.id.layoutLockscreen);
        //ivSwipeUpArrow = findViewById(R.id.ivSwipeUpArrow);
        tvSwipeUp = findViewById(R.id.tvSwipeUpText);
        btnSnoozeAlarmLockScreen = (Button) findViewById(R.id.btnSnoozeAlarmLockScreen);
        btnSnoozeAlarmLockScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmClockAudio.getInstance().stopAlarm(true, false);
                finish();
            }
        });

        //Enable view when screen is locked
        setShowWhenLocked(true);
        setTurnScreenOn(true);

        //Dismiss Keyguard for this Activity
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        keyguardManager.requestDismissKeyguard(LockScreenAlarmActivity.this, null);

        //Init swipe listener
        swipeListener = new SwipeListener(layout);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onResume() {
        super.onResume();

        if (!isStarted) {
            isStarted = true; //Workaround because OnResume is sometimes calling twice (Android bug)
            fadeColor(tvSwipeUp);

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
     * Moves a view up and reset it after reaching a special point
     * @param view View to be moved
     */
    private void moveView(View view )
    {
        LinearLayout root = findViewById(R.id.layoutLockscreen);
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics( dm );

        int[] originalPos = new int[2];
        view.getLocationOnScreen(originalPos);

        int xDest = dm.widthPixels / 2;
        xDest -= view.getMeasuredWidth() / 2;
        int yDest = view.getMeasuredHeight();

        TranslateAnimation translateAnimation = new TranslateAnimation( 0, xDest - originalPos[0] , 0, yDest - originalPos[1] );
        translateAnimation.setDuration(Constants.LOCKSCREEN_ANIMATION_DURATION);
        translateAnimation.setRepeatCount(Animation.INFINITE);
        translateAnimation.setRepeatMode(Animation.RESTART);
        translateAnimation.setFillAfter( true );
        view.startAnimation(translateAnimation);
    }

    /**
     * Fades the color of text
     * @param textView TextView to be faded
     */
    private void fadeColor(TextView textView) {

        int colorFrom = getResources().getColor(R.color.primary_text_color, getTheme());
        int colorTo = getResources().getColor(R.color.error_color, getTheme());

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
        colorAnimation.setDuration(Constants.LOCKSCREEN_COLOR_ANIMATION_DURATION);
        colorAnimation.setRepeatCount(ValueAnimator.INFINITE);
        colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
        colorAnimation.start();
    }

    /**
     * Class to detect swiping on the lockscreen
     */
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

            gestureDetector = new GestureDetector(listener);
            view.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }
    }
}
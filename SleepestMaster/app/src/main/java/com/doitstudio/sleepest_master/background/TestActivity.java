package com.doitstudio.sleepest_master.background;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.model.data.Actions;

import java.util.Calendar;

public class TestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            int use = extras.getInt("intent", 0);
            if (use == 1) {
                ForegroundService.startOrStopForegroundService(Actions.START, getApplicationContext());
            } else if (use == 2) {
                ForegroundService.startOrStopForegroundService(Actions.STOP, getApplicationContext());
            }
        }
        finish();
    }
}
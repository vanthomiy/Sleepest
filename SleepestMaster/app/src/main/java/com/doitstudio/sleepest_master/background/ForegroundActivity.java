package com.doitstudio.sleepest_master.background;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.model.data.Actions;

public class ForegroundActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foreground);

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
package com.doitstudio.sleepest_master.onboarding;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.doitstudio.sleepest_master.MainActivity;
import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.util.TimeConverterUtil;

import java.util.ArrayList;

public class OnboardingActivity extends AppCompatActivity {

    public static ViewPager viewPager;
    OnboardingViewPagerAdapter onboardingViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        Bundle bundle = getIntent().getExtras();

        boolean fromApp = false;

        if (bundle != null && bundle.getBoolean(getString(R.string.onboarding_intent_not_first_app_start))) {
            fromApp = true;
        }

        if (notFirstAppStart() && !fromApp)
        {
            Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            SharedPreferences.Editor editor = getSharedPreferences("FirstAppStart",MODE_PRIVATE).edit();
            editor.putBoolean("started",true);
            editor.apply();
        }

        ArrayList<Object> arrayList = new ArrayList<>();

        if (bundle != null) {
            if(bundle.getBoolean(getString(R.string.onboarding_intent_not_first_app_start))) {

                arrayList.add(bundle.getInt(getString(R.string.onboarding_intent_starttime)));
                arrayList.add(bundle.getInt(getString(R.string.onboarding_intent_endtime)));
                arrayList.add(TimeConverterUtil.millisToTimeFormat(bundle.getInt(getString(R.string.onboarding_intent_duration)))[0]);
                arrayList.add(TimeConverterUtil.millisToTimeFormat(bundle.getInt(getString(R.string.onboarding_intent_duration)))[1]);
                arrayList.add(TimeConverterUtil.millisToTimeFormat(bundle.getInt(getString(R.string.onboarding_intent_starttime)))[0] + ":" +
                        TimeConverterUtil.millisToTimeFormat(bundle.getInt(getString(R.string.onboarding_intent_starttime)))[1]);
                arrayList.add(TimeConverterUtil.millisToTimeFormat(bundle.getInt(getString(R.string.onboarding_intent_endtime)))[0] + ":" +
                        TimeConverterUtil.millisToTimeFormat(bundle.getInt(getString(R.string.onboarding_intent_endtime)))[1]);
            }
        }

        viewPager = findViewById(R.id.onboarding_viewpager);
        onboardingViewPagerAdapter = new OnboardingViewPagerAdapter(this, arrayList);
        viewPager.setAdapter(onboardingViewPagerAdapter);






    }

    private boolean notFirstAppStart() {

        SharedPreferences sharedPreferences=getSharedPreferences("FirstAppStart",MODE_PRIVATE);
        return sharedPreferences.getBoolean("started",false);
    }

    public ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });
}
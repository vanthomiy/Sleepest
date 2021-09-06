package com.sleepestapp.sleepest.onboarding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.sleepestapp.sleepest.MainActivity;
import com.sleepestapp.sleepest.R;
import com.sleepestapp.sleepest.util.TimeConverterUtil;

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

        ArrayList<Object> arrayList = new ArrayList<>();

        if (bundle != null) {
            if(bundle.getBoolean(getString(R.string.onboarding_intent_not_first_app_start))) {

                arrayList.add(bundle.getInt(getString(R.string.onboarding_intent_starttime)));
                arrayList.add(bundle.getInt(getString(R.string.onboarding_intent_endtime)));
                arrayList.add(TimeConverterUtil.millisToTimeFormat(bundle.getInt(getString(R.string.onboarding_intent_duration)))[0]);
                arrayList.add(TimeConverterUtil.millisToTimeFormat(bundle.getInt(getString(R.string.onboarding_intent_duration)))[1]);
                arrayList.add(TimeConverterUtil.toTimeFormat(TimeConverterUtil.millisToTimeFormat(bundle.getInt(getString(R.string.onboarding_intent_starttime)))[0],
                        TimeConverterUtil.millisToTimeFormat(bundle.getInt(getString(R.string.onboarding_intent_starttime)))[1]));
                arrayList.add(TimeConverterUtil.toTimeFormat(TimeConverterUtil.millisToTimeFormat(bundle.getInt(getString(R.string.onboarding_intent_endtime)))[0],
                        TimeConverterUtil.millisToTimeFormat(bundle.getInt(getString(R.string.onboarding_intent_endtime)))[1]));
                arrayList.add(true);
            }
        }

        viewPager = findViewById(R.id.onboarding_viewpager);
        onboardingViewPagerAdapter = new OnboardingViewPagerAdapter(OnboardingActivity.this, arrayList);
        viewPager.setAdapter(onboardingViewPagerAdapter);
    }
}
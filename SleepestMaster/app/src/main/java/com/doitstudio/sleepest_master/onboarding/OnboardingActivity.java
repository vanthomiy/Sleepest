package com.doitstudio.sleepest_master.onboarding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;

import com.doitstudio.sleepest_master.MainActivity;
import com.doitstudio.sleepest_master.R;

public class OnboardingActivity extends AppCompatActivity {

    public static ViewPager viewPager;
    OnboaringViewPagerAdapter onboaringViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.onboarding_viewpager);
        onboaringViewPagerAdapter = new OnboaringViewPagerAdapter(this);
        viewPager.setAdapter(onboaringViewPagerAdapter);

    }
}
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
import com.doitstudio.sleepest_master.storage.DataStoreRepository;
import com.doitstudio.sleepest_master.util.TimeConverterUtil;

import java.sql.Time;
import java.util.ArrayList;

public class OnboardingActivity extends AppCompatActivity {

    public static ViewPager viewPager;
    private DataStoreRepository dataStoreRepository; //Instance of DataStoreRepo
    OnboardingViewPagerAdapter onboardingViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        dataStoreRepository = DataStoreRepository.Companion.getRepo(getApplicationContext());
        ArrayList<Object> arrayList = new ArrayList<>();

        if (dataStoreRepository.getTutorialCompletedJob()) {
            arrayList.add(dataStoreRepository.getSleepTimeBeginJob());
            arrayList.add(dataStoreRepository.getSleepTimeEndJob());
            arrayList.add(TimeConverterUtil.millisToTimeFormat(dataStoreRepository.getSleepDurationJob())[0]);
            arrayList.add(TimeConverterUtil.millisToTimeFormat(dataStoreRepository.getSleepDurationJob())[1]);
            arrayList.add(TimeConverterUtil.toTimeFormat(TimeConverterUtil.millisToTimeFormat(dataStoreRepository.getSleepTimeBeginJob())[0],
                    TimeConverterUtil.millisToTimeFormat(dataStoreRepository.getSleepTimeBeginJob())[1]));
            arrayList.add(TimeConverterUtil.toTimeFormat(TimeConverterUtil.millisToTimeFormat(dataStoreRepository.getSleepTimeEndJob())[0],
                    TimeConverterUtil.millisToTimeFormat(dataStoreRepository.getSleepTimeEndJob())[1]));
            arrayList.add(true);
        }

        viewPager = findViewById(R.id.onboarding_viewpager);
        onboardingViewPagerAdapter = new OnboardingViewPagerAdapter(OnboardingActivity.this, arrayList);
        viewPager.setAdapter(onboardingViewPagerAdapter);
    }
}
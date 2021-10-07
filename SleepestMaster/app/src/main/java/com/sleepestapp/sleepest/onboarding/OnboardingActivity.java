package com.sleepestapp.sleepest.onboarding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import com.sleepestapp.sleepest.R;
import com.sleepestapp.sleepest.storage.DataStoreRepository;
import com.sleepestapp.sleepest.util.TimeConverterUtil;

import java.util.ArrayList;

/**
 * Activity for the tutorial viewpager
 */
public class OnboardingActivity extends AppCompatActivity {

    public static ViewPager viewPager;
    OnboardingViewPagerAdapter onboardingViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        //Get the viewpager
        viewPager = findViewById(R.id.onboarding_viewpager);

        //Instance of DataStoreRepo
        DataStoreRepository dataStoreRepository = DataStoreRepository.Companion.getRepo(getApplicationContext());
        ArrayList<Object> arrayList = new ArrayList<>();

        //Fill the list with values if it is not the first app start. The values are for the set sleep times
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

        //Set the adapter
        onboardingViewPagerAdapter = new OnboardingViewPagerAdapter(OnboardingActivity.this, OnboardingActivity.this, arrayList);
        viewPager.setAdapter(onboardingViewPagerAdapter);
    }
}
package com.doitstudio.sleepest_master.onboarding;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.doitstudio.sleepest_master.MainActivity;
import com.doitstudio.sleepest_master.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OnboaringViewPagerAdapter extends PagerAdapter {

    Context context;
    List<ImageView> indicators = new ArrayList<>();

    public OnboaringViewPagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(@NonNull @NotNull View view, @NonNull @NotNull Object object) {
        return view == object;
    }

    @NonNull
    @NotNull
    @Override
    public Object instantiateItem(@NonNull @NotNull ViewGroup container, int position) {

        LayoutInflater layoutInflater= (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.onboarding_notice_screen,container,false);

        LinearLayout linearLayout = view.findViewById(R.id.layoutOnboardingPermissions);
        ImageView imageView = view.findViewById(R.id.ivOnboadingNoticeImage);

        ImageView ivInditacor1 = view.findViewById(R.id.ivOnboardingIndicator1);
        ImageView ivInditacor2 = view.findViewById(R.id.ivOnboardingIndicator2);
        ImageView ivInditacor3 = view.findViewById(R.id.ivOnboardingIndicator3);

        indicators.add(view.findViewById(R.id.ivOnboardingIndicator1));
        indicators.add(view.findViewById(R.id.ivOnboardingIndicator2));
        indicators.add(view.findViewById(R.id.ivOnboardingIndicator3));

        Button btnEndOnboarding = view.findViewById(R.id.btnEndOnboarding);
        btnEndOnboarding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context , MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        ImageView ivNextPage = view.findViewById(R.id.ivOnboardingNextPage);
        ImageView ivPreviousPage = view.findViewById(R.id.ivOnboardingPreviousPage);
        ivNextPage.setVisibility(View.VISIBLE);
        ivPreviousPage.setVisibility(View.VISIBLE);


        ivNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position < getCount()) {
                    OnboardingActivity.viewPager.setCurrentItem(position+1);
                }
            }
        });

        ivPreviousPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position > 0) {
                    OnboardingActivity.viewPager.setCurrentItem(position-1);
                }
            }
        });

        if (position <= 0) {
            ivPreviousPage.setVisibility(View.INVISIBLE);
        } else if (position >= (getCount() - 1)) {
            ivNextPage.setVisibility(View.INVISIBLE);
        }

        //setIndicatorImages(position);

        //setView(position, view);

        switch (position) {
            case 0:
                linearLayout.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                break;
            case 1:
                imageView.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.GONE);

                break;
            case 2:
                linearLayout.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                break;
        }

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull @NotNull ViewGroup container, int position, @NonNull @NotNull Object object) {
        container.removeView((View) object);
    }


    private void setNoticeWindow() {

    }

    private void setPermissionWindow() {

    }

    private void setIndicatorImages(int position) {

        for (int i = 0; i < indicators.size(); i++) {
            if (i == position) {
                indicators.get(position).setImageResource(R.drawable.onboarding_indicator_selected);
            } else {
                indicators.get(position).setImageResource(R.drawable.onboarding_indicator_unselected);
            }
        }
    }

    private void setView(int position, View view) {
        LinearLayout linearLayout = view.findViewById(R.id.layoutOnboardingPermissions);
        ImageView imageView = view.findViewById(R.id.ivOnboadingNoticeImage);

        linearLayout.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);


        switch (position) {
            case 0:

                linearLayout.setVisibility(View.VISIBLE);
            case 1:

                imageView.setVisibility(View.VISIBLE);
            case 2:
                linearLayout.setVisibility(View.VISIBLE);
        }
    }
}

package com.doitstudio.sleepest_master.onboarding;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.doitstudio.sleepest_master.DontKillMyAppFragment;
import com.doitstudio.sleepest_master.MainActivity;
import com.doitstudio.sleepest_master.R;
import com.doitstudio.sleepest_master.util.PermissionsUtil;
import com.doitstudio.sleepest_master.util.TimeConverterUtil;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class OnboardingViewPagerAdapter extends PagerAdapter {

    private Context context;
    private List<ImageView> indicators = new ArrayList<>();

    private int starttime = 72000;
    private int endtime = 32400;

    private int durationHours = 7;
    private int durationMinutes = 30;

    private String startTimeText = "Select start time";
    private String endTimeText = "Select end time";
    private String startTimeValueText;
    private String endTimeValueText;

    public OnboardingViewPagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return 9;
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

        LinearLayout linearLayoutPermission = view.findViewById(R.id.layoutOnboardingPermissionsPage8);
        LinearLayout linearLayoutSettings = view.findViewById(R.id.layoutOnboardingSettingsPage7);

        FrameLayout frameLayoutStartTime = view.findViewById(R.id.frameLayoutStartTime);
        FrameLayout frameLayoutEndTime = view.findViewById(R.id.frameLayoutEndTime);

        ImageView imageView = view.findViewById(R.id.ivOnboadingNoticeImage);
        ImageView ivPermission4 = view.findViewById(R.id.ivPermission4);

        LottieAnimationView lottieAnimationViewSearch = view.findViewById(R.id.animationSearch);

        com.shawnlin.numberpicker.NumberPicker npDurationHours = view.findViewById(R.id.npDurationHour);
        com.shawnlin.numberpicker.NumberPicker npDurationMinutes = view.findViewById(R.id.npDurationMinutes);

        TextView tvTitle = view.findViewById(R.id.tvOnboardingTitle);
        TextView tvContent = view.findViewById(R.id.tvOnboardingContent);
        TextView tvOnboardingStartTime = view.findViewById(R.id.tvOnboardingStartTime);
        TextView tvOnboardingEndTime = view.findViewById(R.id.tvOnboardingEndTime);
        TextView tvOnboardingStartTimeValue = view.findViewById(R.id.tvOnboardingStartTimeValue);
        TextView tvOnboardingEndTimeValue = view.findViewById(R.id.tvOnboardingEndTimeValue);

        Button btnOnboardingNotificationPrivacyPermission = view.findViewById(R.id.btnOnboardingNotificationPrivacyPermission);

        linearLayoutPermission.setVisibility(View.GONE);
        linearLayoutSettings.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        lottieAnimationViewSearch.setVisibility(View.GONE);

        ImageView ivInditacor1 = view.findViewById(R.id.ivOnboardingIndicator1);
        ImageView ivInditacor2 = view.findViewById(R.id.ivOnboardingIndicator2);
        ImageView ivInditacor3 = view.findViewById(R.id.ivOnboardingIndicator3);
        ImageView ivInditacor4 = view.findViewById(R.id.ivOnboardingIndicator4);
        ImageView ivInditacor5 = view.findViewById(R.id.ivOnboardingIndicator5);
        ImageView ivInditacor6 = view.findViewById(R.id.ivOnboardingIndicator6);
        ImageView ivInditacor7 = view.findViewById(R.id.ivOnboardingIndicator7);
        ImageView ivInditacor8 = view.findViewById(R.id.ivOnboardingIndicator8);
        ImageView ivInditacor9 = view.findViewById(R.id.ivOnboardingIndicator9);

        ivInditacor1.setImageResource(R.drawable.onboarding_indicator_unselected);
        ivInditacor2.setImageResource(R.drawable.onboarding_indicator_unselected);
        ivInditacor3.setImageResource(R.drawable.onboarding_indicator_unselected);
        ivInditacor4.setImageResource(R.drawable.onboarding_indicator_unselected);
        ivInditacor5.setImageResource(R.drawable.onboarding_indicator_unselected);
        ivInditacor6.setImageResource(R.drawable.onboarding_indicator_unselected);
        ivInditacor7.setImageResource(R.drawable.onboarding_indicator_unselected);
        ivInditacor8.setImageResource(R.drawable.onboarding_indicator_unselected);
        ivInditacor9.setImageResource(R.drawable.onboarding_indicator_unselected);

        /*indicators.add(view.findViewById(R.id.ivOnboardingIndicator1));
        indicators.add(view.findViewById(R.id.ivOnboardingIndicator2));
        indicators.add(view.findViewById(R.id.ivOnboardingIndicator3));*/

        Button btnEndOnboarding = view.findViewById(R.id.btnEndOnboarding);
        btnEndOnboarding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(context , MainActivity.class);
                intent.putExtra(context.getString(R.string.onboarding_intent_show_dontkillmyapp), true);
                intent.putExtra(context.getString(R.string.onboarding_intent_starttime), starttime);
                intent.putExtra(context.getString(R.string.onboarding_intent_endtime), endtime);
                intent.putExtra(context.getString(R.string.onboarding_intent_duration), (durationHours * 60 + durationMinutes) * 60);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        btnOnboardingNotificationPrivacyPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //PermissionsUtil.setNotificationPolicyAccess(context);
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

        frameLayoutStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        starttime = (hourOfDay * 60 + minute) * 60;
                        startTimeText = "Start";
                        startTimeValueText = hourOfDay + ":" + minute;
                        tvOnboardingStartTime.setText(startTimeText);
                        tvOnboardingStartTimeValue.setText(startTimeValueText);

                    }
                }, 20, 0, true);
                timePickerDialog.show();
            }
        });

        frameLayoutEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endtime = (hourOfDay * 60 + minute) * 60;
                        endTimeText = "End";
                        endTimeValueText = hourOfDay + ":" + minute;
                        tvOnboardingEndTime.setText(endTimeText);
                        tvOnboardingEndTimeValue.setText(endTimeValueText);
                    }
                }, 9, 0, true);
                timePickerDialog.show();
            }
        });

        npDurationHours.setOnValueChangedListener(new com.shawnlin.numberpicker.NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(com.shawnlin.numberpicker.NumberPicker picker, int oldVal, int newVal) {
                durationHours = newVal;
            }
        });

        npDurationMinutes.setOnValueChangedListener(new com.shawnlin.numberpicker.NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(com.shawnlin.numberpicker.NumberPicker picker, int oldVal, int newVal) {
                durationMinutes = newVal;
            }
        });

        switch (position) {
            case 0:
                tvTitle.setText("Sleepest");
                tvContent.setText("We want to give you the best possible sleep");
                imageView.setVisibility(View.VISIBLE);
                ivInditacor1.setImageResource(R.drawable.onboarding_indicator_selected);
                break;
            case 1:
                tvTitle.setText("Sleep detection");
                tvContent.setText("The app detects when you fall asleep");
                lottieAnimationViewSearch.setVisibility(View.VISIBLE);
                ivInditacor2.setImageResource(R.drawable.onboarding_indicator_selected);
                break;
            case 2:
                tvTitle.setText("Sleep tracking");
                tvContent.setText("The app tracks your sleep and differs awake and light or deep sleep");
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.drawable.analytics);
                ivInditacor3.setImageResource(R.drawable.onboarding_indicator_selected);
                break;
            case 3:
                tvTitle.setText("Calculating wakeup");
                tvContent.setText("The wake-up time is calculated based on the quality of sleep in order to achieve the set sleep duration");
                lottieAnimationViewSearch.setVisibility(View.VISIBLE);
                lottieAnimationViewSearch.setAnimation(R.raw.animation_alarm_clock);
                ivInditacor4.setImageResource(R.drawable.onboarding_indicator_selected);
                break;
            case 4:
                tvTitle.setText("Illustration of sleep");
                tvContent.setText("You can look at your past sleep and analyze it");
                imageView.setVisibility(View.VISIBLE);
                ivInditacor5.setImageResource(R.drawable.onboarding_indicator_selected);
                break;
            case 5:
                tvTitle.setText("Phone position");
                tvContent.setText("To track your sleep correctly, you should position your phone in your bed, preferably next or under your pillow. The sleep is tracked with the help of your phone sensors");
                imageView.setImageResource(R.drawable.phone_position_tim);
                imageView.setVisibility(View.VISIBLE);
                ivInditacor6.setImageResource(R.drawable.onboarding_indicator_selected);
                break;
            case 6:
                tvTitle.setText("Settings");
                tvContent.setText("Set your ordinary sleep time and your prefered sleep duration\n" +
                        "You can change them later.\n" +
                        "Tip: Set your ordinary sleep time generously to achieve a good sleep detection");
                linearLayoutSettings.setVisibility(View.VISIBLE);
                tvOnboardingStartTime.setText(startTimeText);
                tvOnboardingStartTimeValue.setText(startTimeValueText);
                tvOnboardingEndTime.setText(endTimeText);
                tvOnboardingEndTimeValue.setText(endTimeValueText);
                npDurationHours.setValue(durationHours);
                npDurationMinutes.setValue(durationMinutes);
                ivInditacor7.setImageResource(R.drawable.onboarding_indicator_selected);
                break;
            case 7:
                tvTitle.setText("Permissions");
                tvContent.setText("To guarantee that the app works correct you must accept the permissions above");
                linearLayoutPermission.setVisibility(View.VISIBLE);
                ivPermission4.setImageResource(isPermissionNotificationPrivacyGranted() ? R.drawable.ic_baseline_gpp_good_24 : R.drawable.ic_baseline_gpp_bad_24);
                ivInditacor8.setImageResource(R.drawable.onboarding_indicator_selected);
                break;
            case 8:
                tvTitle.setText("Battery optimization");
                tvContent.setText("The app must run in the background to track your sleep. That's why you must follow the instructions to disable battery optimization for your device model.\n" +
                        "Please read carefully");
                linearLayoutPermission.setVisibility(View.VISIBLE);
                ivInditacor9.setImageResource(R.drawable.onboarding_indicator_selected);
                break;
        }




        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull @NotNull ViewGroup container, int position, @NonNull @NotNull Object object) {
        container.removeView((View) object);
    }

    public boolean isPermissionNotificationPrivacyGranted() {
        return PermissionsUtil.isNotificationPolicyAccessGranted(context);
    }
}

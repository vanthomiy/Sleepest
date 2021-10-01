package com.sleepestapp.sleepest.onboarding;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import com.airbnb.lottie.LottieAnimationView;
import com.sleepestapp.sleepest.MainActivity;
import com.sleepestapp.sleepest.R;
import com.sleepestapp.sleepest.storage.DataStoreRepository;
import com.sleepestapp.sleepest.util.PermissionsUtil;
import com.sleepestapp.sleepest.util.TimeConverterUtil;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class for the hole layout and the handling of the tutorial pages
 */
public class OnboardingViewPagerAdapter extends PagerAdapter implements View.OnClickListener {

    private final Context context;
    private final Activity activityContext;

    private boolean enableStartApp = false;

    private int starttime = 72000;
    private int endtime = 32400;

    private int durationHours = 7;
    private int durationMinutes = 30;

    private String startTimeText;
    private String endTimeText;
    private String startTimeValueText;
    private String endTimeValueText;

    private final List<ImageView> dots = new ArrayList<>();

    final Timer timer;

    public OnboardingViewPagerAdapter(Context context, Activity activityContext, ArrayList<Object> arrayList) {

        this.context = context;
        this.activityContext = activityContext;

        timer = new Timer();

        startTimeText = context.getString(R.string.onboarding_start_time_text_first_start);
        endTimeText = context.getString(R.string.onboarding_end_time_text_first_start);

        if (arrayList.size() > 0) {
            starttime = (int) arrayList.get(0);
            endtime = (int) arrayList.get(1);

            durationHours = (int) arrayList.get(2);
            durationMinutes = (int) arrayList.get(3);

            startTimeText = context.getString(R.string.onboarding_start_time_text_not_first_start);
            startTimeText = context.getString(R.string.onboarding_end_time_text_not_first_start);
            startTimeValueText = (String) arrayList.get(4);
            endTimeValueText = (String) arrayList.get(5);

            enableStartApp = true;

        }
    }

    @Override
    public int getCount() {
        return 10;
    }

    @Override
    public boolean isViewFromObject(@NonNull @NotNull View view, @NonNull @NotNull Object object) {
        return view == object;
    }

    @NonNull
    @NotNull
    @Override
    public Object instantiateItem(@NonNull @NotNull ViewGroup container, int position) {

        /* Initiate all layout elements **/
        LayoutInflater layoutInflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.onboarding_notice_screen,container,false);

        FrameLayout linearLayoutPermission = view.findViewById(R.id.layoutOnboardingPermissionsPage8);
        FrameLayout linearLayoutSettings = view.findViewById(R.id.layoutOnboardingSettingsPage7);

        FrameLayout frameLayoutStartTime = view.findViewById(R.id.frameLayoutStartTime);
        FrameLayout frameLayoutEndTime = view.findViewById(R.id.frameLayoutEndTime);

        ImageView imageView = view.findViewById(R.id.ivOnboadingNoticeImage);
        ImageView ivPermission1 = view.findViewById(R.id.ivPermission1);
        ImageView ivPermission2 = view.findViewById(R.id.ivPermission2);
        ImageView ivPermission3 = view.findViewById(R.id.ivPermission3);
        ImageView ivPermission4 = view.findViewById(R.id.ivPermission4);

        ImageView ivNextPage = view.findViewById(R.id.ivOnboardingNextPage);
        ImageView ivPreviousPage = view.findViewById(R.id.ivOnboardingPreviousPage);

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
        Button btnOnboardingOverlayPermission = view.findViewById(R.id.btnOnboardingOverlayPermission);
        Button btnOnboardingActivityRecognitionPermission = view.findViewById(R.id.btnOnboardingSleepdataPermission);
        Button btnOnboardingActivityTransitionPermission = view.findViewById(R.id.btnOnboardingDailyActivityPermission);
        Button btnEndOnboarding = view.findViewById(R.id.btnEndOnboarding);

        linearLayoutPermission.setVisibility(View.GONE);
        linearLayoutSettings.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        lottieAnimationViewSearch.setVisibility(View.GONE);

        dots.clear();
        dots.add(view.findViewById(R.id.ivOnboardingIndicator1));
        dots.add(view.findViewById(R.id.ivOnboardingIndicator2));
        dots.add(view.findViewById(R.id.ivOnboardingIndicator3));
        dots.add(view.findViewById(R.id.ivOnboardingIndicator4));
        dots.add(view.findViewById(R.id.ivOnboardingIndicator5));
        dots.add(view.findViewById(R.id.ivOnboardingIndicator6));
        dots.add(view.findViewById(R.id.ivOnboardingIndicator7));
        dots.add(view.findViewById(R.id.ivOnboardingIndicator8));
        dots.add(view.findViewById(R.id.ivOnboardingIndicator9));
        dots.add(view.findViewById(R.id.ivOnboardingIndicator10));

        //Set the unselected images for the dots
        for(int i = 0; i < dots.size(); i++) {
            dots.get(i).setImageResource(R.drawable.onboarding_indicator_unselected);
        }
        //Set the OnClickListener
        btnEndOnboarding.setOnClickListener(this);
        btnOnboardingNotificationPrivacyPermission.setOnClickListener(this);
        btnOnboardingActivityRecognitionPermission.setOnClickListener(this);
        btnOnboardingActivityTransitionPermission.setOnClickListener(this);
        btnOnboardingOverlayPermission.setOnClickListener(this);

        ivNextPage.setVisibility(View.VISIBLE);
        ivPreviousPage.setVisibility(View.VISIBLE);

        //Button for going to the left page
        ivNextPage.setOnClickListener(v -> {
            if (position < getCount()) {
                OnboardingActivity.viewPager.setCurrentItem(position+1);
            }
        });

        //Button for going to the right page
        ivPreviousPage.setOnClickListener(v -> {
            if (position > 0) {
                OnboardingActivity.viewPager.setCurrentItem(position-1);
            }
        });

        //Check position possibility
        if (position <= 0) {
            ivPreviousPage.setVisibility(View.INVISIBLE);
        } else if (position >= (getCount() - 1)) {
            ivNextPage.setVisibility(View.INVISIBLE);
        }

        //Picker dialog for the start time
        frameLayoutStartTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(view.getContext(), R.style.TimePickerTheme, (view1, hourOfDay, minute) -> {
                starttime = (hourOfDay * 60 + minute) * 60;
                startTimeText = "Start";
                startTimeValueText = TimeConverterUtil.toTimeFormat(hourOfDay, minute);
                tvOnboardingStartTime.setText(startTimeText);
                tvOnboardingStartTimeValue.setText(startTimeValueText);

            }, 20, 0, true);
            timePickerDialog.show();
        });

        //Picker dialog for the end time
        frameLayoutEndTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(view.getContext(), R.style.TimePickerTheme, (view12, hourOfDay, minute) -> {
                endtime = (hourOfDay * 60 + minute) * 60;
                endTimeText = "End";
                endTimeValueText = TimeConverterUtil.toTimeFormat(hourOfDay, minute);
                tvOnboardingEndTime.setText(endTimeText);
                tvOnboardingEndTimeValue.setText(endTimeValueText);
            }, 9, 0, true);
            timePickerDialog.show();
        });

        npDurationHours.setOnValueChangedListener((picker, oldVal, newVal) -> durationHours = newVal);
        npDurationMinutes.setOnValueChangedListener((picker, oldVal, newVal) -> durationMinutes = newVal);

        //Choose the layouts for the different pages of the viewpager depending on the position
        switch (position) {
            case 0: //Page with logo
                tvTitle.setText(context.getString(R.string.onboarding_title_page_1));
                tvContent.setText(context.getString(R.string.onboarding_content_page_1));
                imageView.setImageResource(R.drawable.logofullroundtransparent);
                imageView.setVisibility(View.VISIBLE);
                dots.get(position).setImageResource(R.drawable.onboarding_indicator_selected);
                break;
            case 1: //Page with sleep detection
                tvTitle.setText(context.getString(R.string.onboarding_title_page_2));
                tvContent.setText(context.getString(R.string.onboarding_content_page_2));
                lottieAnimationViewSearch.setVisibility(View.VISIBLE);
                dots.get(position).setImageResource(R.drawable.onboarding_indicator_selected);
                break;
            case 2: //Page with sleep tracking
                tvTitle.setText(context.getString(R.string.onboarding_title_page_3));
                tvContent.setText(context.getString(R.string.onboarding_content_page_3));
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.drawable.analytics);
                dots.get(position).setImageResource(R.drawable.onboarding_indicator_selected);
                break;
            case 3: //Page with wakeup time calculation
                tvTitle.setText(context.getString(R.string.onboarding_title_page_4));
                tvContent.setText(context.getString(R.string.onboarding_content_page_4));
                lottieAnimationViewSearch.setVisibility(View.VISIBLE);
                lottieAnimationViewSearch.setAnimation(R.raw.animation_alarm_clock);
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) lottieAnimationViewSearch.getLayoutParams();
                layoutParams.leftMargin = 80;
                dots.get(position).setImageResource(R.drawable.onboarding_indicator_selected);
                break;
            case 4: //Page with sleep analysis
                tvTitle.setText(context.getString(R.string.onboarding_title_page_5));
                tvContent.setText(context.getString(R.string.onboarding_content_page_5));
                imageView.setVisibility(View.VISIBLE);
                if (Locale.getDefault().getLanguage().equals("de")) {
                    imageView.setImageResource(R.drawable.history_fragment_german);
                } else {
                    imageView.setImageResource(R.drawable.history_fragment_german);
                }

                dots.get(position).setImageResource(R.drawable.onboarding_indicator_selected);
                break;
            case 5: //Page with best phone position
                tvTitle.setText(context.getString(R.string.onboarding_title_page_6));
                tvContent.setText(context.getString(R.string.onboarding_content_page_6));
                imageView.setImageResource(R.drawable.phone_position_final);
                imageView.setVisibility(View.VISIBLE);
                dots.get(position).setImageResource(R.drawable.onboarding_indicator_selected);
                break;
            case 6: //Page with foreground service notification
                tvTitle.setText(context.getString(R.string.onboarding_title_page_7));
                tvContent.setText(context.getString(R.string.onboarding_content_page_7));
                imageView.setVisibility(View.VISIBLE);
                if (Locale.getDefault().getLanguage().equals("de")) {
                    imageView.setImageResource(R.drawable.banner_foregroundservice_german);
                } else {
                    imageView.setImageResource(R.drawable.banner_foregroundservice_english);
                }
                dots.get(position).setImageResource(R.drawable.onboarding_indicator_selected);
                break;
            case 7: //Page with sleep time initiation
                tvTitle.setText(context.getString(R.string.onboarding_title_page_8));
                tvContent.setText(context.getString(R.string.onboarding_content_page_8));
                linearLayoutSettings.setVisibility(View.VISIBLE);
                tvOnboardingStartTime.setText(startTimeText);
                tvOnboardingStartTimeValue.setText(startTimeValueText);
                tvOnboardingEndTime.setText(endTimeText);
                tvOnboardingEndTimeValue.setText(endTimeValueText);
                npDurationHours.setValue(durationHours);
                npDurationMinutes.setValue(durationMinutes);
                dots.get(position).setImageResource(R.drawable.onboarding_indicator_selected);
                break;
            case 8: //Page with permissions
                tvTitle.setText(context.getString(R.string.onboarding_title_page_9));
                tvContent.setText(context.getString(R.string.onboarding_content_page_9));
                linearLayoutPermission.setVisibility(View.VISIBLE);
                int colorError = ContextCompat.getColor(context, R.color.error_color);
                int colorGood = ContextCompat.getColor(context, R.color.accent_text_color);

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ivPermission1.setImageResource(PermissionsUtil.isActivityRecognitionPermissionGranted(context) ? R.drawable.ic_baseline_gpp_good_24 : R.drawable.ic_baseline_gpp_bad_24);
                        ivPermission2.setImageResource(PermissionsUtil.isOverlayPermissionGranted(context) ? R.drawable.ic_baseline_gpp_good_24 : R.drawable.ic_baseline_gpp_bad_24);
                        ivPermission3.setImageResource(PermissionsUtil.isActivityRecognitionPermissionGranted(context) ? R.drawable.ic_baseline_gpp_good_24 : R.drawable.ic_baseline_gpp_bad_24);
                        ivPermission4.setImageResource(PermissionsUtil.isNotificationPolicyAccessGranted(context) ? R.drawable.ic_baseline_gpp_good_24 : R.drawable.ic_baseline_gpp_bad_24);
                        ivPermission1.setColorFilter((PermissionsUtil.isActivityRecognitionPermissionGranted(context) ? colorGood : colorError), android.graphics.PorterDuff.Mode.SRC_IN);
                        ivPermission2.setColorFilter((PermissionsUtil.isOverlayPermissionGranted(context) ? colorGood : colorError), android.graphics.PorterDuff.Mode.SRC_IN);
                        ivPermission3.setColorFilter((PermissionsUtil.isActivityRecognitionPermissionGranted(context) ? colorGood : colorError), android.graphics.PorterDuff.Mode.SRC_IN);
                        ivPermission4.setColorFilter((PermissionsUtil.isNotificationPolicyAccessGranted(context) ? colorGood : colorError), android.graphics.PorterDuff.Mode.SRC_IN);
                    }
                }, 0, 1000);//wait 0 ms before doing the action and do it every 1000ms (1second)

                dots.get(position).setImageResource(R.drawable.onboarding_indicator_selected);
                break;
            case 9: //Page with battery optimization
                tvTitle.setText(context.getString(R.string.onboarding_title_page_10));
                tvContent.setText(context.getString(R.string.onboarding_content_page_10));
                lottieAnimationViewSearch.setVisibility(View.VISIBLE);
                lottieAnimationViewSearch.setAnimation(R.raw.animation_battery_optimization);
                dots.get(position).setImageResource(R.drawable.onboarding_indicator_selected);

                enableStartApp = true;
                break;
        }

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull @NotNull ViewGroup container, int position, @NonNull @NotNull Object object) {
        container.removeView((View) object);
    }

    /**
     * Check if all permissions are granted
     * @return Is granted
     */
    private boolean checkAllPermissions() {
        return PermissionsUtil.isActivityRecognitionPermissionGranted(context) && PermissionsUtil.isNotificationPolicyAccessGranted(context) &&
                PermissionsUtil.isNotificationPolicyAccessGranted(context);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOnboardingDailyActivityPermission:
            case R.id.btnOnboardingSleepdataPermission:
                if (!PermissionsUtil.isActivityRecognitionPermissionGranted(context)) {
                    PermissionsUtil.setActivityRecognitionPermission(context);
                } else {
                    Toast.makeText(context, context.getString(R.string.onboarding_toast_permission_already_granted), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btnOnboardingNotificationPrivacyPermission:
                if (!PermissionsUtil.isNotificationPolicyAccessGranted(context)) {
                    PermissionsUtil.setNotificationPolicyAccess(activityContext);
                } else {
                    Toast.makeText(context, context.getString(R.string.onboarding_toast_permission_already_granted), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btnOnboardingOverlayPermission:
                if (!PermissionsUtil.isOverlayPermissionGranted(context)) {
                    PermissionsUtil.setOverlayPermission(activityContext);
                } else {
                    Toast.makeText(context, context.getString(R.string.onboarding_toast_permission_already_granted), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btnEndOnboarding:
                if (!checkAllPermissions()) {
                    Toast.makeText(context, context.getString(R.string.onboarding_toast_permissions), Toast.LENGTH_LONG).show();
                    OnboardingActivity.viewPager.setCurrentItem(8);
                } else if (enableStartApp) {
                    DataStoreRepository dataStoreRepository = DataStoreRepository.Companion.getRepo(context);
                    dataStoreRepository.updateTutorialCompletedJob(true);
                    Intent intent=new Intent(context , MainActivity.class);
                    intent.putExtra(context.getString(R.string.onboarding_intent_data_available), true);
                    intent.putExtra(context.getString(R.string.onboarding_intent_starttime), starttime);
                    intent.putExtra(context.getString(R.string.onboarding_intent_endtime), endtime);
                    intent.putExtra(context.getString(R.string.onboarding_intent_duration), (durationHours * 60 + durationMinutes) * 60);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, context.getString(R.string.onboarding_toast_read_to_end), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}

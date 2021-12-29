package com.sleepestapp.sleepest.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.sleepestapp.sleepest.MainActivity
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.databinding.OnboardingViewBinding
import com.sleepestapp.sleepest.model.data.SleepSleepChangeFrom
import com.sleepestapp.sleepest.onboarding.entity.OnBoardingPage
import com.sleepestapp.sleepest.ui.sleep.SleepFragment
import com.sleepestapp.sleepest.ui.sleep.SleepViewModel
import com.sleepestapp.sleepest.util.PermissionsUtil
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil
import com.sleepestapp.sleepest.util.StringUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import setParallaxTransformation
import java.time.LocalTime




class OnBoardingActivity : AppCompatActivity() {

    /**
     * Binding XML Code to Fragment
     */
    private lateinit var binding: OnboardingViewBinding

    val actualContext: Context by lazy{ application.applicationContext }

    var factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            // Workaround because we know that we can cast to T
            return  (SleepViewModel(
                (actualContext as MainApplication).dataStoreRepository,
                (actualContext as MainApplication).dataBaseRepository
            ) as T)
        }
    }

    /**
     * View model of the [SleepFragment]
     */
    private val viewModel by lazy { ViewModelProvider(this, factory).get(SleepViewModel::class.java)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_view)
        binding = OnboardingViewBinding.inflate(layoutInflater)
        binding.onBoardingViewModel = viewModel
        binding.lifecycleOwner = this

        val view = binding.root
        setContentView(view)

        setUpSlider()
        addingButtonsClickListeners()

        val minData = SleepTimeValidationUtil.createMinutePickerHelper()
        binding.npMinutes.minValue = 1
        binding.npMinutes.maxValue = minData.size
        binding.npMinutes.displayedValues = minData

        viewModel.is24HourFormat = SleepTimeValidationUtil.is24HourFormat(actualContext)

        viewModel.phonePositionSelections.value = (mutableListOf(
            StringUtil.getStringXml(R.string.sleep_phoneposition_inbed,application),
            StringUtil.getStringXml(R.string.sleep_phoneposition_ontable, application),
            StringUtil.getStringXml(R.string.sleep_phoneposition_auto, application)
        ))

        viewModel.lightConditionSelections.value = (mutableListOf(
            StringUtil.getStringXml(R.string.sleep_lightcondidition_dark,application),
            StringUtil.getStringXml(R.string.sleep_lightcondidition_light, application),
            StringUtil.getStringXml(R.string.sleep_lightcondidition_auto, application)
        ))

        // Hours changed from the duration changer
        binding.npHours.setOnValueChangedListener { _, _, newVal -> onDurationChange(
            newVal,
            binding.npMinutes.value
        )
        }

        // Minutes changed from the duration changer
        binding.npMinutes.setOnValueChangedListener { _, _, newVal -> onDurationChange(
            binding.npHours.value,
            newVal
        )
        }

        // Used to update the sleep end and start time if it changes from the alarms fragments
        viewModel.sleepParameterLiveData.observe(this){

            viewModel.sleepStartTime = LocalTime.ofSecondOfDay(it.sleepTimeStart.toLong())
            viewModel.sleepEndTime = LocalTime.ofSecondOfDay(it.sleepTimeEnd.toLong())

            //TODO()
            val sleepDuration = LocalTime.ofSecondOfDay(it.sleepDuration.toLong())
            binding.npHours.value = sleepDuration.hour
            binding.npMinutes.value = (sleepDuration.minute / 15) + 1
            viewModel.sleepDuration = sleepDuration.toSecondOfDay()

            viewModel.sleepStartValue.value = ((if (viewModel.sleepStartTime.hour < 10) "0" else "") + viewModel.sleepStartTime.hour.toString() + ":" + (if (viewModel.sleepStartTime.minute < 10) "0" else "") + viewModel.sleepStartTime.minute.toString())
            viewModel.sleepEndValue.value = ((if (viewModel.sleepEndTime.hour < 10) "0" else "") + viewModel.sleepEndTime.hour.toString() + ":" + (if (viewModel.sleepEndTime.minute < 10) "0" else "") + viewModel.sleepEndTime.minute.toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // check if the request code is same as what is passed  here it is 2
        if (requestCode == 282) {
            //ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
            if(PermissionsUtil.isNotificationPolicyAccessGranted(applicationContext))
                lifecycleScope.launch {
                    delay(500)
                    navigateToNextSlide()
                }
        } else if (requestCode == 283) {
            lifecycleScope.launch {
                delay(1000)
                var color = R.color.accent_text_color
                var text = resources.getText(R.string.next)
                binding.permissionBtn.background.setTint(resources.getColor(color))
                binding.permissionBtn.text = text
            }
        }
    }

    /**
     * Sleep duration changed handler
     */
    fun onDurationChange(hour: Int, minute: Int) {

        var hourSetter = hour
        if(hour >= 24)
            hourSetter = 23

        val time = LocalTime.of(hourSetter, (minute-1) * 15)

        lifecycleScope.launch {
            SleepTimeValidationUtil.checkSleepActionIsAllowedAndDoAction(
                actualContext,
                viewModel.dataStoreRepository,
                viewModel.dataBaseRepository,
                viewModel.sleepStartTime.toSecondOfDay(),
                viewModel.sleepEndTime.toSecondOfDay(),
                time.toSecondOfDay(),
                viewModel.autoSleepTime.value == true,
                SleepSleepChangeFrom.DURATION
            )
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                navigateToNextSlide()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.

            }
        }


    private val numberOfPages by lazy { OnBoardingPage.values().size }
    private var actualPage = -1

    private fun setUpSlider() {
        with(binding.slider) {
            adapter = OnBoardingPagerAdapter()
            setPageTransformer { page, position ->
                setParallaxTransformation(page, position)
            }

            addSlideChangeListener()

            binding.pageIndicator.setViewPager2(this)
        }
    }

    private fun addSlideChangeListener() {
        binding.slider.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (numberOfPages > 1) {

                    slideChangedAction(position, false)
                    binding.onboardingMotion.progress = positionOffset
                }
            }
        })
    }

    private fun slideChangedAction(position:Int, future:Boolean){

        if(!future)
            actualPage = position
        var color = R.color.accent_text_color
        var text = resources.getText(R.string.next)

        if(position == 0)
        {
            binding.onboardingMotion.setTransition(R.id.state1, R.id.state2)
        }
        else if(position == 1)
        {
            binding.onboardingMotion.setTransition(R.id.state2, R.id.state3)

            if (!PermissionsUtil.isActivityRecognitionPermissionGranted(this)){
                color = R.color.error_color
                text = resources.getText(R.string.settings_sleepdata).toString()
                text += resources.getText(R.string.permission_string)
            }
        }
        else if(position == 2)
        {
            binding.onboardingMotion.setTransition(R.id.state3, R.id.state4)

            if (!PermissionsUtil.isActivityRecognitionPermissionGranted(this)){
                navigateToPreviousSlide()
            }
        }
        else if(position == 3)
        {
            binding.onboardingMotion.setTransition(R.id.state4, R.id.state5)

            if (!PermissionsUtil.isOverlayPermissionGranted(this)){
                color = R.color.error_color
                text = resources.getText(R.string.settings_overlapp_app).toString()
                text += resources.getText(R.string.permission_string)
            }
        }
        else if(position == 4)
        {
            binding.onboardingMotion.setTransition(R.id.state5, R.id.state6)
            if (!PermissionsUtil.isOverlayPermissionGranted(this))
                navigateToPreviousSlide()

            if (!PermissionsUtil.isNotificationPolicyAccessGranted(this)){
                color = R.color.error_color
                text = resources.getText(R.string.settings_notification_privacy).toString()
                text += " " + resources.getText(R.string.permission_string)
            }
        }
        else if(position == 5)
        {
            if (!PermissionsUtil.isNotificationPolicyAccessGranted(this))
                navigateToPreviousSlide()

            text = resources.getText(R.string.get_started)
        }
        binding.permissionBtn.background.setTint(resources.getColor(color))
        binding.permissionBtn.text = text

    }

    private fun addingButtonsClickListeners() {
        binding.skipBtn.setOnClickListener { navigateToMainActivity() }
        binding.permissionBtn.setOnClickListener {
            when (actualPage) {
                1 -> {
                    if (!PermissionsUtil.isActivityRecognitionPermissionGranted(this))
                        requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    else
                        navigateToNextSlide()
                }
                3 -> {
                    if (!PermissionsUtil.isOverlayPermissionGranted(this))
                        PermissionsUtil.setOverlayPermission(this)
                    else
                        navigateToNextSlide()
                }
                4 -> {
                    if (!PermissionsUtil.isNotificationPolicyAccessGranted(this))
                        PermissionsUtil.setNotificationPolicyAccess(this)
                    else
                        navigateToNextSlide()
                }
                5 -> navigateToMainActivity()
                else -> navigateToNextSlide()
            }
        }
    }

    private fun navigateToNextSlide() {
        val nextSlidePos: Int = binding.slider?.currentItem?.plus(1) ?: 0
        binding.slider?.setCurrentItem(nextSlidePos, true)
    }

    private fun navigateToPreviousSlide() {
        val previousSlidePos: Int = binding.slider?.currentItem?.minus(1) ?: 0
        binding.slider?.setCurrentItem(previousSlidePos, true)
    }

    private fun navigateToMainActivity() {
        lifecycleScope.launch {
            viewModel.dataStoreRepository.updateTutorialCompleted(true)

            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra("from_onboarding", true)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

    }

}
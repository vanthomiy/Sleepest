package com.sleepestapp.sleepest.onboarding.customView

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import com.sleepestapp.sleepest.MainActivity
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.onboarding.OnBoardingPagerAdapter
import com.sleepestapp.sleepest.onboarding.entity.OnBoardingPage
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import kotlinx.android.synthetic.main.onboarding_view.view.*
import setParallaxTransformation

class OnBoardingView @JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val numberOfPages by lazy { OnBoardingPage.values().size }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.onboarding_view, this, true)
        setUpSlider(view)
        addingButtonsClickListeners()
    }

    private fun setUpSlider(view: View) {
        with(slider) {
            adapter = OnBoardingPagerAdapter()
            setPageTransformer { page, position ->
                setParallaxTransformation(page, position)
            }

            addSlideChangeListener()

            val wormDotsIndicator = view.findViewById<WormDotsIndicator>(R.id.page_indicator)
            wormDotsIndicator.setViewPager2(this)
        }
    }

    private fun addSlideChangeListener() {

        slider.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (numberOfPages > 1) {
                    if(position == 0)
                    {
                        onboardingMotion.setTransition(R.id.empty, R.id.state1)
                    }
                    else if(position == 1)
                    {
                        onboardingMotion.setTransition(R.id.state1, R.id.state2)

                    }
                    else if(position == 2)
                    {
                        onboardingMotion.setTransition(R.id.state2, R.id.state3)
                    }
                    else if(position == 3)
                    {
                        onboardingMotion.setTransition(R.id.state3, R.id.empty)
                    }
                    else if(position == 4)
                    {
                        onboardingMotion.setTransition(R.id.empty, R.id.state4)
                    }

                    onboardingMotion.progress = positionOffset
                }
            }
        })
    }

    private fun addingButtonsClickListeners() {
        nextBtn.setOnClickListener { navigateToNextSlide() }
        skipBtn.setOnClickListener {

        }

    }

    private fun navigateToNextSlide() {
        val nextSlidePos: Int = slider?.currentItem?.plus(1) ?: 0
        slider?.setCurrentItem(nextSlidePos, true)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
    }

    private fun SaveData(){
        /*
        lifecycleScope.launch {
            if (viewModel.dataStoreRepository.tutorialStatusFlow.first().tutorialCompleted && !viewModel.dataStoreRepository.tutorialStatusFlow.first().energyOptionsShown) {
                DontKillMyAppFragment.show(this@MainActivity)
            }
            //Start a alarm for the new foreground service start time
            val calendar = TimeConverterUtil.getAlarmDate(bundle.getInt(getString(R.string.onboarding_intent_starttime)))
            AlarmReceiver.startAlarmManager(
                calendar[Calendar.DAY_OF_WEEK],
                calendar[Calendar.HOUR_OF_DAY],
                calendar[Calendar.MINUTE],
                applicationContext, AlarmReceiverUsage.START_FOREGROUND)

            SleepTimeValidationUtil.checkSleepActionIsAllowedAndDoAction(
                applicationContext,
                viewModel.dataStoreRepository,
                viewModel.dataBaseRepository,
                bundle.getInt(getString(R.string.onboarding_intent_starttime)),
                bundle.getInt(getString(R.string.onboarding_intent_endtime)),
                bundle.getInt(getString(R.string.onboarding_intent_duration)),
                false,
                SleepSleepChangeFrom.DURATION
            )

            SleepTimeValidationUtil.checkSleepActionIsAllowedAndDoAction(
                applicationContext,
                viewModel.dataStoreRepository,
                viewModel.dataBaseRepository,
                bundle.getInt(getString(R.string.onboarding_intent_starttime)),
                bundle.getInt(getString(R.string.onboarding_intent_endtime)),
                bundle.getInt(getString(R.string.onboarding_intent_duration)),
                false,
                SleepSleepChangeFrom.SLEEPTIMEEND
            )

            SleepTimeValidationUtil.checkSleepActionIsAllowedAndDoAction(
                applicationContext,
                viewModel.dataStoreRepository,
                viewModel.dataBaseRepository,
                bundle.getInt(getString(R.string.onboarding_intent_starttime)),
                bundle.getInt(getString(R.string.onboarding_intent_endtime)),
                bundle.getInt(getString(R.string.onboarding_intent_duration)),
                false,
                SleepSleepChangeFrom.SLEEPTIMESTART
            )
        }*/
    }
}
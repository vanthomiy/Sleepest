package com.sleepestapp.sleepest.onboarding.entity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.sleepestapp.sleepest.R

enum class OnBoardingPage(
    @StringRes val titleResource: Int,
    @StringRes val subTitleResource: Int,
    @StringRes val descriptionResource: Int,
    @DrawableRes val logoResource: Int
) {

    ONE(R.string.onboarding_slide1_title, R.string.onboarding_slide1_subtitle,R.string.onboarding_slide1_desc, R.drawable.info1),
    TWO(R.string.onboarding_slide2_title, R.string.onboarding_slide2_subtitle,R.string.onboarding_slide2_desc, R.drawable.info2),
    THREE(R.string.onboarding_slide3_title, R.string.onboarding_slide3_subtitle,R.string.onboarding_slide3_desc, R.drawable.info3),
    FOUR(R.string.onboarding_slide4_title, R.string.onboarding_slide4_subtitle,R.string.onboarding_slide4_desc, R.drawable.info4),
    FIFE(R.string.onboarding_slide5_title, R.string.onboarding_slide5_subtitle,R.string.onboarding_slide5_desc, R.drawable.info5),
    SIX(R.string.onboarding_slide6_title, R.string.onboarding_slide6_subtitle,R.string.onboarding_slide6_desc, R.drawable.info6)

}
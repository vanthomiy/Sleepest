package com.doitstudio.sleepest_master.ui.sleep

import android.R.attr.animation
import android.annotation.SuppressLint
import android.app.Application
import android.app.TimePickerDialog
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.AnimationDrawable
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.widget.NestedScrollView
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.googleapi.ActivityTransitionHandler
import com.doitstudio.sleepest_master.model.data.*
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.util.IconAnimatorUtil
import com.doitstudio.sleepest_master.util.SleepTimeValidationUtil
import com.doitstudio.sleepest_master.util.SleepTimeValidationUtil.Is24HourFormat
import com.doitstudio.sleepest_master.util.StringUtil.getStringXml
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.math.abs


class SleepViewModel(application: Application) : AndroidViewModel(application) {

    //region binding values

    private val scope: CoroutineScope = MainScope()
    private val context by lazy{ getApplication<Application>().applicationContext }
    lateinit var transitionsContainer : ViewGroup
    private val dataStoreRepository: DataStoreRepository by lazy {
        (context as MainApplication).dataStoreRepository
    }
    private val dataBaseRepository: DatabaseRepository by lazy {
        (context as MainApplication).dataBaseRepository
    }

    var sleepDuration : Int = 0

    fun onDurationChange(hour: Int, minute: Int) {

        var hourSetter = hour
        if(hour >= 24)
            hourSetter = 23

        val time = LocalTime.of(hourSetter, (minute-1) * 15)

        scope.launch {
            SleepTimeValidationUtil.checkSleepActionIsAllowedAndDoAction(
                dataStoreRepository,
                dataBaseRepository,
                context,
                sleepStartTime.toSecondOfDay(),
                sleepEndTime.toSecondOfDay(),
                time.toSecondOfDay(),
                autoSleepTime.get() == true,
                SleepSleepChangeFrom.DURATION
            )
        }
    }

    val sleepStartValue = ObservableField("07:30")
    val sleepEndValue = ObservableField("07:30")
    var sleepStartTime = LocalTime.now()
    var sleepEndTime = LocalTime.now()


    fun onAlarmStartClicked(view: View){

        val hour = (sleepStartTime.hour)
        val minute = (sleepStartTime.minute)

        val tpd = TimePickerDialog(
                view.context,
            R.style.TimePickerTheme,
                { view, h, m ->

                    val tempWakeup = LocalTime.of(h, m)

                    scope.launch {

                        SleepTimeValidationUtil.checkSleepActionIsAllowedAndDoAction(
                            dataStoreRepository,
                            dataBaseRepository,
                            view.context,
                            tempWakeup.toSecondOfDay(),
                            sleepEndTime.toSecondOfDay(),
                            sleepDuration,
                            autoSleepTime.get() == true,
                            SleepSleepChangeFrom.SLEEPTIMESTART
                        )
                    }
                },
                hour,
                minute,
            Is24HourFormat(context)
        )

        tpd.show()
    }

    fun onAlarmEndClicked(view: View){
        val hour = (sleepEndTime.hour)
        val minute = (sleepEndTime.minute)

        val tpd = TimePickerDialog(
            view.context,
            R.style.TimePickerTheme,
            { view, h, m ->

                val tempWakeup = LocalTime.of(h, m)

                scope.launch {

                    SleepTimeValidationUtil.checkSleepActionIsAllowedAndDoAction(
                        dataStoreRepository,
                        dataBaseRepository,
                        view.context,
                        sleepStartTime.toSecondOfDay(),
                        tempWakeup.toSecondOfDay(),
                        sleepDuration,
                        autoSleepTime.get() == true,
                        SleepSleepChangeFrom.SLEEPTIMEEND
                    )
                }
            },
                hour,
                minute,
            Is24HourFormat(context)
        )

        tpd.show()
    }


    val autoSleepTime = ObservableField(true)
    val manualSleepTime = ObservableField(true)
    val manualSleepTimeVisibility = ObservableField(View.GONE)

    fun SleepTimeToogled(view: View){
        scope.launch{
            autoSleepTime.get()?.let {
                dataStoreRepository.updateAutoSleepTime(it)
                manualSleepTime.set(!it)
            }
        }

        TransitionManager.beginDelayedTransition(transitionsContainer);

        autoSleepTime.get()?.let {
            manualSleepTimeVisibility.set(if (it) View.GONE else View.VISIBLE)
        }
    }


    val actualExpand = ObservableField(-1)
    val goneState = ObservableField(View.GONE)
    val visibleState = ObservableField(View.VISIBLE)
    private var lastView: ImageView? = null
    fun onInfoClicked(view: View){
        updateInfoChanged(view.tag.toString(), true)

        // Check if its an image view
        if(view.tag.toString() != "7"){
            IconAnimatorUtil.animateView(view as ImageView)

                IconAnimatorUtil.resetView(lastView)

            lastView = if(lastView != view)
                (view as ImageView)
            else
                null
        }
        else{
            IconAnimatorUtil.resetView(lastView)
            lastView = null
        }
    }

    private fun updateInfoChanged(value: String, toggle: Boolean = false) {

        TransitionManager.beginDelayedTransition(transitionsContainer);


        actualExpand.set(if(actualExpand.get() == value.toIntOrNull()) -1 else value.toIntOrNull() )
    }

    val phoneUsageValueString = ObservableField("")
    val phoneUsageValue = ObservableField<Int>(2)
    fun onPhoneUsageChanged(seekBar: SeekBar, progresValue: Int, fromUser: Boolean){

        val mf = MobileUseFrequency.getCount(progresValue)
        phoneUsageValueString.set(mf.toString().toLowerCase().capitalize())
        phoneUsageValue.set(progresValue)
        scope.launch {
            dataStoreRepository.updateUserMobileFequency(mf.ordinal)

        }

        sleepCalculateFactorCalculation()
    }

    val phonePositionSelections = ObservableArrayList<String>()
    val mobilePosition = ObservableField(0)

    fun onMobilePositionChanged(
            parent: AdapterView<*>?,
            selectedItemView: View,
            position: Int,
            id: Long
    ){
        scope.launch {
            dataStoreRepository.updateStandardMobilePosition(position)
            sleepCalculateFactorCalculation()
        }

    }

    val lightConditionSelections = ObservableArrayList<String>()
    val lightCondition = ObservableField(0)

    fun onLightConditionChanged(
            parent: AdapterView<*>?,
            selectedItemView: View,
            position: Int,
            id: Long
    ){
        scope.launch {
            dataStoreRepository.updateLigthCondition(position)
            sleepCalculateFactorCalculation()

        }

    }


    val activityTracking = ObservableField(false)
    val includeActivityInCalculation = ObservableField(false)
    val activityTrackingView = ObservableField(View.GONE)


    fun onActivityTrackingChanged(buttonView: View) {
        TransitionManager.beginDelayedTransition(transitionsContainer);

        scope.launch {
            activityTracking.get()?.let {
                dataStoreRepository.updateActivityTracking(it)
                activityTrackingView.set(if (it) View.VISIBLE else View.GONE)

            }

            sleepCalculateFactorCalculation()

        }

        TransitionManager.beginDelayedTransition(transitionsContainer);

        activityTracking.get()?.let {
            activityTrackingView.set(if (it) View.VISIBLE else View.GONE)

            if(it)
                ActivityTransitionHandler.getHandler(getApplication()).startActivityHandler()
            else
                ActivityTransitionHandler.getHandler(getApplication()).stopActivityHandler()
        }
    }

    fun onActivityInCalcChanged(buttonView: View) {
        scope.launch {
            includeActivityInCalculation.get()?.let { dataStoreRepository.updateActivityInCalculation(
                it
            ) }

            sleepCalculateFactorCalculation()
        }
    }

    val sleepScoreValue = ObservableField("50")
    val sleepScoreText = ObservableField("50")

    /**
     *     defines how good the sleep can be messured
     *     100 is max and 30 is lowest
     */
    fun sleepCalculateFactorCalculation() {

        // phone position
        // phone usage
        // sleep with light / in dark

        var factor = when(mobilePosition.get()?.let { MobilePosition.getCount(it) })
        {
            MobilePosition.INBED -> 1f
            MobilePosition.ONTABLE -> 0f
            else -> 0.5f
        }*2

        factor += when(phoneUsageValue.get()?.let { MobileUseFrequency.getCount(it) })
        {
            MobileUseFrequency.VERYOFTEN -> 1f
            MobileUseFrequency.OFTEN -> 0.75f
            MobileUseFrequency.LESS -> 0.25f
            MobileUseFrequency.VERYLESS -> 0f
            else -> 0.5f
        }*3

        factor += when(lightCondition.get()?.let { LightConditions.getCount(it) })
        {
            LightConditions.DARK -> 1f
            LightConditions.LIGHT -> 0f
            else -> 0.5f
        }*1

        val endFactor = factor / 6
        val score = 50 + endFactor * 50

        sleepScoreValue.set(score.toInt().toString())

        sleepScoreText.set(when {
            score < 60 -> {
                getStringXml(R.string.sleep_score_text_60, getApplication())
                //getStringXml(R.string.sleep_score_text_60)
            }
            score < 70 -> {
                getStringXml(R.string.sleep_score_text_70, getApplication())
                //getStringXml(R.string.sleep_score_text_70)
            }
            score < 80 -> {
                getStringXml(R.string.sleep_score_text_80, getApplication())
                //getStringXml(R.string.sleep_score_text_80)
            }
            score < 90 -> {
                getStringXml(R.string.sleep_score_text_90, getApplication())
                //getStringXml(R.string.sleep_score_text_90)
            }
            else -> {
                getStringXml(R.string.sleep_score_text_100, getApplication())
                //getStringXml(R.string.sleep_score_text_100)
            }
        }

        )

        // activity tracking



    }


    //endregion

    init {

        scope.launch {
            var sleepParams = dataStoreRepository.sleepParameterFlow.first()

            sleepStartTime = LocalTime.ofSecondOfDay(sleepParams.sleepTimeStart.toLong())
            sleepEndTime = LocalTime.ofSecondOfDay(sleepParams.sleepTimeEnd.toLong())

            sleepStartValue.set((if (sleepStartTime.hour < 10) "0" else "") + sleepStartTime.hour.toString() + ":" + (if (sleepStartTime.minute < 10) "0" else "") + sleepStartTime.minute.toString())
            sleepEndValue.set((if (sleepEndTime.hour < 10) "0" else "") + sleepEndTime.hour.toString() + ":" + (if (sleepEndTime.minute < 10) "0" else "") + sleepEndTime.minute.toString())

            phoneUsageValue.set(sleepParams.mobileUseFrequency)

            manualSleepTime.set(!sleepParams.autoSleepTime)
            autoSleepTime.set(sleepParams.autoSleepTime)
            manualSleepTimeVisibility.set(if (sleepParams.autoSleepTime) View.GONE else View.VISIBLE)

            phonePositionSelections.addAll(arrayListOf<String>(getStringXml(R.string.sleep_phoneposition_inbed, getApplication()), getStringXml(R.string.sleep_phoneposition_ontable, getApplication()), getStringXml(R.string.sleep_phoneposition_auto, getApplication())))
            mobilePosition.set(sleepParams.standardMobilePosition)

            lightConditionSelections.addAll(arrayListOf<String>(getStringXml(R.string.sleep_lightcondidition_dark, getApplication()), getStringXml(R.string.sleep_lightcondidition_light, getApplication()), getStringXml(R.string.sleep_lightcondidition_auto, getApplication())))
            lightCondition.set(sleepParams.standardLightCondition)

            activityTracking.set(sleepParams.userActivityTracking)
            includeActivityInCalculation.set(sleepParams.implementUserActivityInSleepTime)
            activityTrackingView.set(if (sleepParams.userActivityTracking) View.VISIBLE else View.GONE)

            sleepCalculateFactorCalculation()

        }
    }

}


package com.doitstudio.sleepest_master.ui.sleep

import android.app.Application
import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.transition.TransitionManager
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.widget.NestedScrollView
import androidx.databinding.Observable
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import com.doitstudio.Activityest_master.sleepapi.ActivityHandler
import com.doitstudio.Activityest_master.sleepapi.ActivityTransitionHandler
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.model.data.LightConditions
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.MobileUseFrequency
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.kevalpatel.ringtonepicker.RingtonePickerDialog
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
    private val dataStoreRepository: DataStoreRepository by lazy {
        (context as MainApplication).dataStoreRepository
    }

    val sleepDurationString = ObservableField("7h")
    val sleepDurationValue = ObservableField<Int>(7)
    fun onSleepDurationChanged(seekBar: SeekBar, progresValue: Int, fromUser: Boolean) {

        val time = getSleepCountFromProgress(progresValue)

        sleepDurationString.set(time.toString())
        scope.launch {
            dataStoreRepository.updateUserWantedSleepTime(time.toSecondOfDay())
        }
    }

    private fun getSleepCountFromProgress(count: Int) : LocalTime{
        var hour = 2 + count / 4
        var minute = (count % 4) * 15
        return LocalTime.of(hour, minute)
    }
    private fun getProgressFromSleepCount(time: LocalTime) : Int{
        var count = (time.hour-2) * 4
        count += time.minute / 15
        return count
    }

    val sleepStartValue = ObservableField("07:30")
    val sleepCompleteValue = ObservableField("07:30 to 7:30")
    val sleepEndValue = ObservableField("07:30")
    var sleepStartTime = LocalTime.now()
    var sleepEndTime = LocalTime.now()


    fun onAlarmStartClicked(view: View){

        val hour = (sleepStartTime.hour)
        val minute = (sleepStartTime.minute)

        val tpd = TimePickerDialog(
                view.context,
                TimePickerDialog.OnTimeSetListener(function = { view, h, m ->

                    sleepStartValue.set((if (h < 10) "0" else "") + h.toString() + ":" + (if (m < 10) "0" else "") + m.toString())
                    sleepStartTime = LocalTime.of(h, m)

                    sleepCompleteValue.set(sleepStartValue.get() + " to " + sleepEndValue.get())

                    scope.launch {
                        dataStoreRepository.updateSleepTimeStart(sleepStartTime.toSecondOfDay())
                    }
                }),
                hour,
                minute,
                false
        )

        tpd.show()
    }

    fun onAlarmEndClicked(view: View){
        val hour = (sleepEndTime.hour)
        val minute = (sleepEndTime.minute)

        val tpd = TimePickerDialog(
                view.context,
                TimePickerDialog.OnTimeSetListener(function = { view, h, m ->

                    sleepEndValue.set((if (h < 10) "0" else "") + h.toString() + ":" + (if (m < 10) "0" else "") + m.toString())

                    sleepCompleteValue.set(sleepStartValue.get() + " to " + sleepEndValue.get())

                    sleepEndTime = LocalTime.of(h, m)

                    scope.launch {
                        dataStoreRepository.updateSleepTimeEnd(sleepEndTime.toSecondOfDay())
                    }
                }),
                hour,
                minute,
                false
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

    val sleepTimeInfoExpand = ObservableField(View.GONE)
    val phonePositionExpand = ObservableField(View.GONE)
    val phoneUsageExpand = ObservableField(View.GONE)
    val activityTrackingExpand = ObservableField(View.GONE)
    val alarmExpand = ObservableField(View.GONE)
    val sleepDurationExpand = ObservableField(View.GONE)
    val lightConditionExpand = ObservableField(View.GONE)
    val sleepHeaderExpand = ObservableField(View.GONE)

    fun onInfoClicked(view: View){
        updateInfoChanged(view.tag.toString(), true)
    }



    private fun updateInfoChanged(value: String, toggle: Boolean = false) {

        TransitionManager.beginDelayedTransition(transitionsContainer);

        sleepTimeInfoExpand.set(if (value == "0" && sleepTimeInfoExpand.get() == View.GONE) View.VISIBLE else View.GONE)
        sleepHeaderExpand.set(if (value == "7" && sleepHeaderExpand.get() == View.GONE) View.VISIBLE else View.GONE)
        lightConditionExpand.set(if (value == "6" && lightConditionExpand.get() == View.GONE) View.VISIBLE else View.GONE)
        sleepDurationExpand.set(if (value == "1" && sleepDurationExpand.get() == View.GONE) View.VISIBLE else View.GONE)
        phonePositionExpand.set(if (value == "2" && phonePositionExpand.get() == View.GONE) View.VISIBLE else View.GONE)
        phoneUsageExpand.set(if (value == "3" && phoneUsageExpand.get() == View.GONE) View.VISIBLE else View.GONE)
        activityTrackingExpand.set(if (value == "4" && activityTrackingExpand.get() == View.GONE) View.VISIBLE else View.GONE)
        alarmExpand.set(if (value == "5" && alarmExpand.get() == View.GONE) View.VISIBLE else View.GONE)
    }

    val phoneUsageValueString = ObservableField("Normal")
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
    val cancelAlarmWhenAwake = ObservableField(false)


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
                ActivityHandler.getHandler(getApplication()).startActivityHandler()
            else
                ActivityHandler.getHandler(getApplication()).stopActivityHandler()
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

    fun onEndAlarmAfterFiredChanged(buttonView: View) {
        scope.launch {
            cancelAlarmWhenAwake.get()?.let { dataStoreRepository.updateEndAlarmAfterFired(it) }
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
                "Really bad sleep detection"
            }
            score < 70 -> {
                "Bad sleep detection"
            }
            score < 80 -> {
                "Sleep detection possible"
            }
            score < 90 -> {
                "Good sleep detection possible"
            }
            else -> {
                "Perfect sleep detection possible"
            }
        }

        )

        // activity tracking



    }


    //endregion

    init {

        scope.launch {
            var sleepParams = dataStoreRepository.sleepParameterFlow.first()
            val time = LocalTime.ofSecondOfDay(sleepParams.normalSleepTime.toLong())
            sleepDurationValue.set(getProgressFromSleepCount(time))
            sleepDurationString.set(time.toString())

            sleepStartTime = LocalTime.ofSecondOfDay(sleepParams.sleepTimeStart.toLong())
            sleepEndTime = LocalTime.ofSecondOfDay(sleepParams.sleepTimeEnd.toLong())

            sleepStartValue.set((if (sleepStartTime.hour < 10) "0" else "") + sleepStartTime.hour.toString() + ":" + (if (sleepStartTime.minute < 10) "0" else "") + sleepStartTime.minute.toString())
            sleepEndValue.set((if (sleepEndTime.hour < 10) "0" else "") + sleepEndTime.hour.toString() + ":" + (if (sleepEndTime.minute < 10) "0" else "") + sleepEndTime.minute.toString())

            phoneUsageValue.set(sleepParams.mobileUseFrequency)

            manualSleepTime.set(!sleepParams.autoSleepTime)
            autoSleepTime.set(sleepParams.autoSleepTime)
            manualSleepTimeVisibility.set(if (sleepParams.autoSleepTime) View.GONE else View.VISIBLE)

            phonePositionSelections.addAll(arrayListOf<String>("In bed", "On table", "Auto detect"))
            mobilePosition.set(sleepParams.standardMobilePosition)

            lightConditionSelections.addAll(arrayListOf<String>("Dark", "Light", "Auto detect"))
            mobilePosition.set(sleepParams.standardLightCondition)

            activityTracking.set(sleepParams.userActivityTracking)
            includeActivityInCalculation.set(sleepParams.implementUserActivityInSleepTime)
            cancelAlarmWhenAwake.set(sleepParams.endAlarmAfterFired)
            activityTrackingView.set(if (sleepParams.userActivityTracking) View.VISIBLE else View.GONE)

            sleepCalculateFactorCalculation()


        }
    }

    //region animation

    lateinit var transitionsContainer : ViewGroup
    lateinit var transitionsContainerTop : ViewGroup
    lateinit var animatedTopView : MotionLayout
    lateinit var imageMoonView : AppCompatImageView

    fun onShowTips(view: View){
        animateTop(true)

    }

    var lastScroll = 0
    var lastScrollDelta = 0
    var progress = 0f
    var newProgress = 0f
    fun onScrollChanged(v: NestedScrollView, l: Int, t: Int, oldl: Int, oldt: Int) {
        //Log.d(TAG, "scroll changed: " + this.getTop() + " "+t);
        val scrollY: Int = v.scrollY // For ScrollView hprizontal use getScrollX()
        val b = l
        val c = t
        val d  = oldl
        //TransitionManager.beginDelayedTransition(transitionsContainerTop);

        newProgress = (1f / 500f) * scrollY
        animatedTopView.progress = newProgress

        if(abs(progress - newProgress) > 0.25 ) {
            progress = newProgress
        }

        lastScroll = scrollY
    }

    var lastMotionEvent : Int = MotionEvent.ACTION_UP


    val pictureScale = ObservableField(1.0f)


    private fun animateTop(expand: Boolean){


        if(expand)
        {
            pictureScale.set(0.25f)
        }
        else
        {
            pictureScale.set(1f)
        }
    }


    //endregion
}


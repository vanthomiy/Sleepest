package com.doitstudio.sleepest_master.ui.sleep

import android.app.Application
import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.transition.TransitionManager
import android.view.MotionEvent
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
import com.doitstudio.sleepest_master.model.data.MobileUseFrequency
import com.doitstudio.sleepest_master.storage.DataStoreRepository
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
                manualSleepTimeVisibility.set(if (it) View.VISIBLE else View.GONE)
            }
        }
    }

    val sleepTimeInfoExpand = ObservableField(View.GONE)
    val phonePositionExpand = ObservableField(View.GONE)
    val phoneUsageExpand = ObservableField(View.GONE)
    val activityTrackingExpand = ObservableField(View.GONE)
    val alarmExpand = ObservableField(View.GONE)
    val sleepDurationExpand = ObservableField(View.GONE)

    fun onInfoClicked(view: View){
        updateInfoChanged(view.tag.toString(), true)
    }



    private fun updateInfoChanged(value: String, toggle: Boolean = false) {

        TransitionManager.beginDelayedTransition(transitionsContainer);

        sleepTimeInfoExpand.set(if (value == "0" && sleepTimeInfoExpand.get() == View.GONE) View.VISIBLE else View.GONE)
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
        scope.launch {
            dataStoreRepository.updateUserMobileFequency(mf)
        }
    }

    val phonePositionSelections = ObservableArrayList<String>()
    val mobilePosition = ObservableField(0)

    val colorNormal by lazy {Color.parseColor("#9D6CCF") }
    val colorDisabled by lazy {Color.parseColor("#116CCF") }
    val buttonTint = ObservableField(ColorStateList.valueOf(colorNormal))

    fun onMobilePositionChanged(
        parent: AdapterView<*>?,
        selectedItemView: View,
        position: Int,
        id: Long
    ){
        scope.launch {
            dataStoreRepository.updateStandardMobilePosition(position)
        }
    }


    val activityTracking = ObservableField(false)
    val includeActivityInCalculation = ObservableField(false)
    val cancelAlarmWhenAwake = ObservableField(false)


    fun onActivityTrackingChanged(buttonView: View) {
        scope.launch {
            activityTracking.get()?.let { dataStoreRepository.updateActivityTracking(it) }
        }
    }

    fun onActivityInCalcChanged(buttonView: View) {
        scope.launch {
            includeActivityInCalculation.get()?.let { dataStoreRepository.updateActivityInCalculation(
                it
            ) }
        }
    }

    fun onEndAlarmAfterFiredChanged(buttonView: View) {
        scope.launch {
            cancelAlarmWhenAwake.get()?.let { dataStoreRepository.updateEndAlarmAfterFired(it) }
        }
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
            manualSleepTimeVisibility.set(if (sleepParams.autoSleepTime) View.VISIBLE else View.GONE)

            phonePositionSelections.addAll(arrayListOf<String>("In bed", "On table", "Auto detect"))
            mobilePosition.set(sleepParams.standardMobilePosition)

            activityTracking.set(sleepParams.userActivityTracking)
            includeActivityInCalculation.set(sleepParams.implementUserActivityInSleepTime)
            cancelAlarmWhenAwake.set(sleepParams.endAlarmAfterFired)

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


        /*
        if(scrollY < lastScroll && direction == 1 ) {
            direction = 0
            lastScrollDelta = scrollY
            val params = FrameLayout.LayoutParams(
                    200,
                    200
            )
            imageMoonView.layoutParams = params

        } else if(scrollY  > lastScroll && direction == 0){
            direction = 1
            lastScrollDelta = scrollY
            val params = FrameLayout.LayoutParams(
                    75,
                    75
            )
            imageMoonView.layoutParams = params
        }*/



        lastScroll = scrollY
    }

    var lastMotionEvent : Int = MotionEvent.ACTION_UP

    var touchY = 0f
    var touchYOld = 0f

    fun onTouch(v: View?, event: MotionEvent): Boolean {


        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchY = event.y

                if(touchYOld == 0f)
                    touchYOld = event.y
            }
            MotionEvent.ACTION_UP -> {
                touchYOld = 0f
                //touchY = 0f
            }
        }

        if(abs(touchYOld-touchY) > 50 && event.action == MotionEvent.ACTION_SCROLL)
        {
            if(touchYOld < touchY)
                animatedTopView.transitionToEnd()
            else
                animatedTopView.transitionToStart()

            touchYOld = touchY
        }

        return false
    }
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
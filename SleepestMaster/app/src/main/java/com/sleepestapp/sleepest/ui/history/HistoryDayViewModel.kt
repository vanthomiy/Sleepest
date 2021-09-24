package com.sleepestapp.sleepest.ui.history

import android.app.TimePickerDialog
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.model.data.MoodType
import com.sleepestapp.sleepest.sleepcalculation.SleepCalculationHandler
import com.sleepestapp.sleepest.util.IconAnimatorUtil
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil
import com.sleepestapp.sleepest.util.SmileySelectorUtil
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

/**  */
class HistoryDayViewModel(val sleepCalculationHandler : SleepCalculationHandler) : ViewModel() {

    /** Contains information about the fall asleep time. */
    var beginOfSleep = ObservableField("")

    /** Contains information about the fall asleep time in epoch seconds. */
    var beginOfSleepEpoch = ObservableField(0L)

    /** Contains information about the wakeup time. */
    var endOfSeep = ObservableField("")

    /** Contains information about the wakeup time in epoch seconds. */
    var endOfSleepEpoch = ObservableField(0L)

    /** */
    var sessionId = 0

    /** Contains information about the amount of time the user spend awake. */
    var awakeTime = ObservableField("")

    /** Contains information about the amount of time the user spend in light sleep phase. */
    var lightSleepTime = ObservableField("")

    /** Contains information about the amount of time the user spend in deep sleep phase. */
    var deepSleepTime = ObservableField("")

    /** Contains information about the amount of time the user spend in rem sleep phase. */
    var remSleepTime = ObservableField("")

    /** Contains information about the amount of time the user slept. */
    var sleepTime = ObservableField("")

    /** Contains the current smiley used to indicate the users activity level. */
    var activitySmiley = ObservableField(SmileySelectorUtil.getSmileyActivity(0))

    /** Contains the smiley which was picked by the user to assess it's mood. */
    var sleepMoodSmiley = ObservableField(MoodType.NONE)

    /** */
    var sleepMoodSmileyTag = ObservableField(0)

    /** This will prevent the daily sleep analysis diagrams from reloading when the sleep rating was altered. */
    var sleepRatingUpdate = false

    val actualExpand = ObservableField(-1)
    val goneState = ObservableField(View.GONE)
    val visibleState = ObservableField(View.VISIBLE)

    var is24HourFormat : Boolean = false

    lateinit var transitionsContainer : ViewGroup

    init {

    }

    fun sleepRating(view: View) {
        sleepRatingUpdate = true
        val mood = when (view.tag.toString().toInt()) {
            1 -> MoodType.BAD
            2 -> MoodType.GOOD
            3 -> MoodType.EXCELLENT
            4 -> MoodType.EMPOWERED
            5 -> MoodType.TIRED
            else -> MoodType.NONE
        }

        sleepMoodSmiley.set(mood)
        sleepMoodSmileyTag.set(view.tag.toString().toInt())
    }

    private var lastView: ImageView? = null
    fun onInfoClicked(view: View){
        updateInfoChanged(view.tag.toString())

        // Check if its an image view
        IconAnimatorUtil.animateView(view as ImageView)

        IconAnimatorUtil.resetView(lastView)

        lastView = if(lastView != view)
            view
        else
            null
    }

    private fun updateInfoChanged(value: String) {
        TransitionManager.beginDelayedTransition(transitionsContainer)
        actualExpand.set(if(actualExpand.get() == value.toIntOrNull()) -1 else value.toIntOrNull())
    }

    fun manualChangeSleepTimes(view: View) {
        val time : LocalDateTime = if (view.tag == "BeginOfSleep") {
            //Set the fall asleep time.
            LocalDateTime.ofInstant(
                beginOfSleepEpoch.get()?.let { Instant.ofEpochMilli(it) },
                ZoneOffset.systemDefault()
            )
        } else {
            LocalDateTime.ofInstant(
                endOfSleepEpoch.get()?.let { Instant.ofEpochMilli(it) },
                ZoneOffset.systemDefault()
            )
        }

        createPickerDialogue(view, time, view.tag == "BeginOfSleep")
    }

    private fun createPickerDialogue(view: View, dateTime: LocalDateTime, startOfSleep: Boolean) {
        val tpd = TimePickerDialog(
            view.context,
            R.style.TimePickerTheme,
            { _, h, m ->
                viewModelScope.launch {
                    val tempTime = LocalTime.of(h, m)
                    val newDateTime = dateTime.toLocalDate().atTime(tempTime)
                    val epochTime = newDateTime.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli().div(1000)

                    if(startOfSleep)
                        sleepCalculationHandler.updateSleepSessionManually(epochTime.toInt(), (endOfSleepEpoch.get()!! / 1000).toInt(), sessionId = sessionId)
                    else
                        sleepCalculationHandler.updateSleepSessionManually((beginOfSleepEpoch.get()!! / 1000).toInt(), epochTime.toInt(), sessionId = sessionId)
                }
            },
            dateTime.hour,
            dateTime.minute,
            is24HourFormat
        )
        tpd.show()
    }
}
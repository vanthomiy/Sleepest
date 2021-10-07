package com.sleepestapp.sleepest.ui.history


import android.app.TimePickerDialog
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.model.data.MoodType
import com.sleepestapp.sleepest.sleepcalculation.SleepCalculationHandler
import com.sleepestapp.sleepest.storage.db.UserSleepSessionEntity.Companion.getIdByDateTimeWithTimeZone
import com.sleepestapp.sleepest.util.SmileySelectorUtil
import kotlinx.coroutines.launch
import java.time.*

class HistoryDayViewModel(
    val sleepCalculationHandler : SleepCalculationHandler
    ) : ViewModel() {

    /**
     * Contains information about the fall asleep time as a string for the current analysis date.
     */
    var beginOfSleep = MutableLiveData("")

    /**
     * Contains information about the fall asleep time in epoch seconds for the current analysis date.
     */
    var beginOfSleepEpoch = MutableLiveData(0L)

    /**
     * Contains information about the wakeup time as string for the current analysis date.
     */
    var endOfSeep = MutableLiveData("")

    /**
     * Contains information about the wakeup time in epoch seconds for the current analysis date.
     */
    var endOfSleepEpoch = MutableLiveData(0L)

    /**
     * Stores the sleep session id of the current analysis date.
     */
    var sessionId = 0

    /**
     * Contains amount of time the user spend awake for the current analysis date.
     */
    var awakeTime = MutableLiveData("")

    /**
     * Contains amount of time the user spend in the light sleep phase for the current analysis date.
     */
    var lightSleepTime = MutableLiveData("")

    /**
     * Contains amount of time the user spend in the deep sleep phase for the current analysis date.
     */
    var deepSleepTime = MutableLiveData("")

    /**
     * Contains amount of time the user spend in the rem sleep phase for the current analysis date.
     */
    var remSleepTime = MutableLiveData("")

    /**
     * Contains amount of time the user slept for the current analysis date.
     */
    var sleepTime = MutableLiveData("")

    /**
     * Manages the visibility of the text field which give further information about the time spent in each sleep phase.
     */
    var timeInSleepPhaseTextField = MutableLiveData(View.INVISIBLE)

    /**
     * Contains the tag of the current smiley used to indicate the users activity level.
     */
    var activitySmileyTag = MutableLiveData(SmileySelectorUtil.getSmileyActivity(0))

    /**
     * Contains the smiley which was picked by the user to assess it's mood after sleep.
     */
    var sleepMoodSmiley = MutableLiveData<MoodType>()

    /**
     * Contains the tag of the selected mood smiley for alternating it in the corresponding xml files.
     */
    var sleepMoodSmileyTag = MutableLiveData(0)

    /**
     * Indicates that the sleep rating (mood after sleep) was altered by the user.
     */
    var sleepRatingUpdate = false

    /**
     * Contains information about the time zone's time formatting standards.
     */
    var is24HourFormat : Boolean = false

    /**
     * Maintains the visibility of the information buttons and its text fields.
     */
    val actualExpand = MutableLiveData(-1)

    val goneState = MutableLiveData(View.GONE)

    val visibleState = MutableLiveData(View.VISIBLE)

    fun onInfoClicked(
        view: View
    ){
        val value = view.tag.toString()
        actualExpand.value = if(actualExpand.value == value.toIntOrNull()) -1 else value.toIntOrNull()
    }

    /**
     * Auxiliary function to determine the sleep session id of the passed date.
     */
    fun getSleepSessionId(
        time: LocalDate
    ) {
        sessionId = getIdByDateTimeWithTimeZone(time)
    }

    /**
     * Auxiliary function for alternating the sleep rating (mood after sleep).
     */
    fun sleepRating(
        view: View
    ) {
        sleepRatingUpdate = true
        val mood = when (view.tag.toString().toInt()) {
            1 -> MoodType.BAD
            2 -> MoodType.GOOD
            3 -> MoodType.EXCELLENT
            4 -> MoodType.EMPOWERED
            5 -> MoodType.TIRED
            else -> MoodType.NONE
        }

        sleepMoodSmiley.value = mood
        sleepMoodSmileyTag.value = view.tag.toString().toInt()
    }

    /**
     * Allows the manual alternation of the time at which the user fell asleep and woke up.
     */
    fun manualChangeSleepTimes(
        view: View
    ) {
        val time : LocalDateTime = if (view.tag == "BeginOfSleep") {
            //Set the fall asleep time.
            LocalDateTime.ofInstant(
                beginOfSleepEpoch.value?.let { Instant.ofEpochMilli(it) },
                ZoneOffset.systemDefault()
            )
        } else {
            LocalDateTime.ofInstant(
                endOfSleepEpoch.value?.let { Instant.ofEpochMilli(it) },
                ZoneOffset.systemDefault()
            )
        }

        createPickerDialogue(view, time, view.tag == "BeginOfSleep")
    }

    /**
     * Auxiliary function for creating a [TimePickerDialog] the function [manualChangeSleepTimes].
     */
    private fun createPickerDialogue(
        view: View,
        dateTime: LocalDateTime,
        startOfSleep: Boolean
    ) {
        val tpd = TimePickerDialog(
            view.context,
            R.style.TimePickerTheme,
            { _, h, m ->
                viewModelScope.launch {
                    val tempTime = LocalTime.of(h, m)
                    val newDateTime = dateTime.toLocalDate().atTime(tempTime)
                    val epochTime = newDateTime.atZone(
                        ZoneOffset.systemDefault()
                    ).toInstant().toEpochMilli().div(1000)

                    if(startOfSleep)
                        sleepCalculationHandler.updateSleepSessionManually(
                            epochTime.toInt(),
                            (endOfSleepEpoch.value!! / 1000).toInt(),
                            sessionId = sessionId
                        )
                    else
                        sleepCalculationHandler.updateSleepSessionManually(
                            (beginOfSleepEpoch.value!! / 1000).toInt(),
                            epochTime.toInt(),
                            sessionId = sessionId
                        )
                }
            },
            dateTime.hour,
            dateTime.minute,
            is24HourFormat
        )
        tpd.show()
    }
}
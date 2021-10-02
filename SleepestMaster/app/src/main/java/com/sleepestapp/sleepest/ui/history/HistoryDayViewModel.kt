package com.sleepestapp.sleepest.ui.history


import android.app.TimePickerDialog
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.model.data.MoodType
import com.sleepestapp.sleepest.sleepcalculation.SleepCalculationHandler
import com.sleepestapp.sleepest.storage.db.UserSleepSessionEntity
import com.sleepestapp.sleepest.util.SmileySelectorUtil
import kotlinx.coroutines.launch
import java.time.*

class HistoryDayViewModel(
    val sleepCalculationHandler : SleepCalculationHandler
    ) : ViewModel() {

    /**
     * Contains information about the fall asleep time. TODO
     */
    var beginOfSleep = MutableLiveData("")

    /**
     * Contains information about the fall asleep time in epoch seconds. TODO
     */
    var beginOfSleepEpoch = MutableLiveData(0L)

    /**
     * Contains information about the wakeup time. TODO
     */
    var endOfSeep = MutableLiveData("")

    /**
     * Contains information about the wakeup time in epoch seconds. TODO
     */
    var endOfSleepEpoch = MutableLiveData(0L)

    /**
     * Stores the ID of the current SleepSession. TODO
     */
    var sessionId = 0

    /**
     * Contains information about the amount of time the user spend awake. TODO
     */
    var awakeTime = MutableLiveData("")

    /**
     * Contains information about the amount of time the user spend in light sleep phase. TODO
     */
    var lightSleepTime = MutableLiveData("")

    /**
     * Contains information about the amount of time the user spend in deep sleep phase. TODO
     */
    var deepSleepTime = MutableLiveData("")

    /**
     * Contains information about the amount of time the user spend in rem sleep phase. TODO
     */
    var remSleepTime = MutableLiveData("")

    /**
     * Contains information about the amount of time the user slept. TODO
     */
    var sleepTime = MutableLiveData("")

    /**
     * Contains the visibility status of the time analysis of each sleep phase. TODO
     */
    var timeInSleepPhaseTextField = MutableLiveData(View.INVISIBLE)

    /**
     * Contains the current smiley used to indicate the users activity level. TODO
     */
    var activitySmiley = MutableLiveData(SmileySelectorUtil.getSmileyActivity(0))

    /**
     * Contains the smiley which was picked by the user to assess it's mood. TODO
     */
    var sleepMoodSmiley = MutableLiveData<MoodType>()

    /**
     * Contains the tag of the selected mood smiley. TODO
     */
    var sleepMoodSmileyTag = MutableLiveData(0)

    /**
     * This will prevent the daily sleep analysis diagrams from reloading when the sleep rating was altered.TODO
     */
    var sleepRatingUpdate = false

    /**
     * Maintains the visibility of the information buttons and its text fields.TODO
     */
    val actualExpand = MutableLiveData(-1)

    val goneState = MutableLiveData(View.GONE)

    val visibleState = MutableLiveData(View.VISIBLE)

    /**
     * Contains information about the current time zone and its time formatting standards. TODO
     */
    var is24HourFormat : Boolean = false

    fun getSleepSessionId(
        time: LocalDate
    ) {
        sessionId = UserSleepSessionEntity.getIdByDateTime(time)
    }

    /**
     * Allows to alter the sleepRating (MoodAfterSleep) and saves it to its MutableLiveData. TODO
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

        sleepMoodSmiley.value = (mood)
        sleepMoodSmileyTag.value = (view.tag.toString().toInt())
    }

    fun onInfoClicked(
        view: View
    ){
        val value = view.tag.toString()
        actualExpand.value = if(actualExpand.value == value.toIntOrNull()) -1 else value.toIntOrNull()
    }

    /**
     * Allows the manual alternation of the time in at which the user fell asleep and woke up. TODO
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
     * Creates a TimePickerDialogue as auxiliary function for [manualChangeSleepTimes]. TODO
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
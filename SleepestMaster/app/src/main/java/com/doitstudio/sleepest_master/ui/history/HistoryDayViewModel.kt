package com.doitstudio.sleepest_master.ui.history

import android.app.Application
import android.app.TimePickerDialog
import android.content.Context
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.model.data.MoodType
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler
import com.doitstudio.sleepest_master.util.IconAnimatorUtil
import com.doitstudio.sleepest_master.util.SleepTimeValidationUtil
import com.doitstudio.sleepest_master.util.SmileySelectorUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

/**  */
class HistoryDayViewModel(application: Application) : AndroidViewModel(application) {

    val context: Context by lazy { getApplication<Application>().applicationContext }

    private val scope: CoroutineScope = MainScope()

    val sleepCalculationHandler: SleepCalculationHandler by lazy { SleepCalculationHandler.getHandler(context) }

    /**  */
    var beginOfSleep = ObservableField("")

    var beginOfSleepEpoch = ObservableField(0L)

    /**  */
    var endOfSeep = ObservableField("")

    var endOfSleepEpoch = ObservableField(0L)

    var sessionId = 0

    /**  */
    var awakeTime = ObservableField("")

    /**  */
    var lightSleepTime = ObservableField("")

    /**  */
    var deepSleepTime = ObservableField("")

    /**  */
    var sleepTime = ObservableField("")

    /**  */
    var activitySmiley = ObservableField(SmileySelectorUtil.getSmileyActivity(0))

    /** */
    var sleepMoodSmiley = ObservableField(MoodType.NONE)

    /** */
    var sleepMoodSmileyTag = ObservableField(0)

    /** This will prevent the daily sleep analysis diagrams from reloading when the sleep rating was altered. */
    var sleepRatingUpdate = false

    val actualExpand = ObservableField(-1)
    val goneState = ObservableField(View.GONE)
    val visibleState = ObservableField(View.VISIBLE)

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
        updateInfoChanged(view.tag.toString(), true)

        // Check if its an image view
        IconAnimatorUtil.animateView(view as ImageView)

        IconAnimatorUtil.resetView(lastView)

        lastView = if(lastView != view)
            (view as ImageView)
        else
            null
    }

    private fun updateInfoChanged(value: String, toggle: Boolean = false) {
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
        //Unterscheidung zwischen Einschlaf und Aufwachzeitpunkt.
    }

    private fun createPickerDialogue(view: View, dateTime: LocalDateTime, startOfSleep:Boolean) {
        val tpd = TimePickerDialog(
            view.context,
            R.style.TimePickerTheme,
            { _, h, m ->
                scope.launch {
                    val tempTime = LocalTime.of(h, m)
                    val newDatTime = dateTime.toLocalDate().atTime(tempTime)
                    //val epochTime = newDatTime.toEpochSecond(ZoneOffset.systemDefault())
                    val epochTime =
                        newDatTime.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli()
                            .div(1000)

                    if(startOfSleep)
                        sleepCalculationHandler.updateSleepSessionManually(context, epochTime.toInt(), (endOfSleepEpoch.get()!! / 1000).toInt(), sessionId = sessionId)
                    else
                        sleepCalculationHandler.updateSleepSessionManually(context, (beginOfSleepEpoch.get()!! / 1000).toInt(), epochTime.toInt(), sessionId = sessionId)
                }
            },
            dateTime.hour,
            dateTime.minute,
            SleepTimeValidationUtil.Is24HourFormat(context)
        )
        tpd.show()
    }
}
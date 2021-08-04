package com.doitstudio.sleepest_master.ui.history

import android.app.Application
import android.content.Context
import android.graphics.BlendMode
import android.graphics.Color
import android.graphics.PorterDuff
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.model.data.MoodType
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.util.SmileySelectorUtil

/**  */
class HistoryDayViewModel(application: Application) : AndroidViewModel(application) {

    val context: Context by lazy { getApplication<Application>().applicationContext }

    val dataBaseRepository: DatabaseRepository by lazy { (context as MainApplication).dataBaseRepository }

    /**  */
    var beginOfSleep = ObservableField("22:00")

    /**  */
    var endOfSeep = ObservableField("06:00")

    /**  */
    var awakeTime = ObservableField("Awake: 1 hour 30 minutes")

    /**  */
    var lightSleepTime = ObservableField(" Light: 1 hour 30 minutes")

    /**  */
    var deepSleepTime = ObservableField("Deep: 1 hour 30 minutes")

    /**  */
    var sleepTime = ObservableField("Sleep: 1 hour 30 minutes")

    /**  */
    var activitySmiley = ObservableField(SmileySelectorUtil.getSmileyActivity(0))

    /** */
    var sleepMoodSmiley = ObservableField(MoodType.NONE)

    /** */
    var sleepMoodSmileyTag = ObservableField(0)

    val actualExpand = ObservableField(-1)
    val goneState = ObservableField(View.GONE)
    val visibleState = ObservableField(View.VISIBLE)

    lateinit var transitionsContainer : ViewGroup

    init {

    }

    fun sleepRating(view: View) {
        val mood = when (view.tag.toString().toInt()) {
            1 -> MoodType.BAD
            2 -> MoodType.GOOD
            3 -> MoodType.EXCELLENT
            4 -> MoodType.LAZY
            5 -> MoodType.TIRED
            else -> MoodType.NONE
        }

        sleepMoodSmiley.set(mood)
        sleepMoodSmileyTag.set(view.tag.toString().toInt())
    }

    fun onInfoClicked(view: View){
        updateInfoChanged(view.tag.toString(), true)
    }

    private fun updateInfoChanged(value: String, toggle: Boolean = false) {
        TransitionManager.beginDelayedTransition(transitionsContainer)
        actualExpand.set(if(actualExpand.get() == value.toIntOrNull()) -1 else value.toIntOrNull())
    }
}
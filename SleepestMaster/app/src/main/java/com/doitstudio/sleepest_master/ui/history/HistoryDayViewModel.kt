package com.doitstudio.sleepest_master.ui.history

import android.app.Application
import android.content.Context
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.model.data.MoodType
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.util.IconAnimatorUtil
import com.doitstudio.sleepest_master.util.SmileySelectorUtil

/**  */
class HistoryDayViewModel(application: Application) : AndroidViewModel(application) {

    val context: Context by lazy { getApplication<Application>().applicationContext }

    val dataBaseRepository: DatabaseRepository by lazy { (context as MainApplication).dataBaseRepository }

    /**  */
    var beginOfSleep = ObservableField("")

    /**  */
    var endOfSeep = ObservableField("")

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
}
package com.sleepestapp.sleepest.ui.alarms

import android.app.Application
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.airbnb.lottie.LottieAnimationView
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmsViewModel(application: Application) : AndroidViewModel(application) {

    //region Init

    private val scope: CoroutineScope = MainScope()
    private val actualContext by lazy{ getApplication<Application>().applicationContext }
    /**
     * The database Repository
     */
    val databaseRepository by lazy { (actualContext as MainApplication).dataBaseRepository }

    /**
     * The datastore Repository
     */
    val dataStoreRepository by lazy { (actualContext as MainApplication).dataStoreRepository }

    // endregion

    //region Alarms Settings

    /**
     * Observable live data of the alarms flow
     */
    val activeAlarmsLiveData by lazy {  databaseRepository.activeAlarmsFlow().asLiveData() }


    val alarmExpandId = ObservableInt(0)
    val noAlarmsView = ObservableField(View.GONE)

    val actualExpand = ObservableField(View.GONE)
    val rotateState = ObservableField(0)

    var lottie : LottieAnimationView? = null

    /**
     * Expands the alarm settings of the alarms view
     */
    fun onExpandClicked(view: View) {
        TransitionManager.beginDelayedTransition(transitionsContainer);

        actualExpand.set(if (actualExpand.get() == View.GONE) View.VISIBLE else View.GONE)
        rotateState.set(if (actualExpand.get() == View.GONE) 0 else 180)

        alarmExpandId.set(-1)

        lottie = view as LottieAnimationView

        if(actualExpand.get() == View.GONE)
            lottie?.playAnimation()
        else
            lottie?.pauseAnimation()

    }

    /**
     * When another expand is clicked, we also update the lottie animation to start again
     */
    fun updateExpandChanged(isExpaned : Boolean) {

        TransitionManager.beginDelayedTransition(transitionsContainer);

        if(isExpaned)
        {
            actualExpand.set(View.GONE)
            rotateState.set(0)
        }

        if(actualExpand.get() == View.GONE)
            lottie?.playAnimation()
        else
            lottie?.pauseAnimation()
    }

    val cancelAlarmWhenAwake = ObservableField(false)
    val alarmArtSelections = ObservableArrayList<String>()
    val alarmArt = ObservableField(0)

    /**
     * When the alarm type has changed (Vibration/Sound etc.)
     */
    fun onAlarmTypeChanged(
        parent: AdapterView<*>?,
        selectedItemView: View,
        art: Int,
        id: Long)
    {

        scope.launch {
            dataStoreRepository.updateAlarmType(art)
        }
    }

    /**
     * When the alarm end after awake was changed
     */
    fun onEndAlarmAfterFiredChanged(view: View) {
        scope.launch {
            cancelAlarmWhenAwake.get()?.let { dataStoreRepository.updateEndAlarmAfterFired(it) }
        }
    }
    val alarmSoundName = ObservableField("")

    //endregion


    init {

        /**
         * Loads all the init values from the datastore and passes the values to the bindings
         */
        scope.launch {
            var settings = dataStoreRepository.alarmParameterFlow.first()

            cancelAlarmWhenAwake.set(settings.endAlarmAfterFired)
            alarmArtSelections.addAll(arrayListOf<String>((actualContext.getString(R.string.alarms_type_selection_only_alarm)), (actualContext.getString(R.string.alarms_type_selection_alarm_vibration)), (actualContext.getString(R.string.alarms_type_selection_only_vibration))))
            alarmArt.set(settings.alarmArt)

            if(settings.alarmName != "") {
                alarmSoundName.set(settings.alarmName)
            } else{
                alarmSoundName.set(actualContext.getString(R.string.alarms_type_selection_default))
            }

        }
    }

    //region animation

    lateinit var transitionsContainer : ViewGroup


    //endregion
}


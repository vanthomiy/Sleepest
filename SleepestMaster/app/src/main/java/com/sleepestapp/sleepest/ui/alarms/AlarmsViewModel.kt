package com.sleepestapp.sleepest.ui.alarms

import android.app.Application
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SeekBar
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.AndroidViewModel
import com.airbnb.lottie.LottieAnimationView
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.storage.DataStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmsViewModel(application: Application) : AndroidViewModel(application) {

    private val scope: CoroutineScope = MainScope()
    private val context by lazy{ getApplication<Application>().applicationContext }
    private val dataStoreRepository: DataStoreRepository by lazy {
        (context as MainApplication).dataStoreRepository
    }

    //region Alarms Settings

    val alarmExpandId = ObservableInt(0)
    val noAlarmsView = ObservableField(View.GONE)

    val actualExpand = ObservableField(View.GONE)
    val rotateState = ObservableField(0)

    var lottie : LottieAnimationView? = null

    fun onExpandClicked(view: View) {
        TransitionManager.beginDelayedTransition(transitionsContainer);

        actualExpand.set(if (actualExpand.get() == View.GONE) View.VISIBLE else View.GONE)
        rotateState.set(if (actualExpand.get() == View.GONE) 0 else 180)

        alarmExpandId.set(-1)

        lottie = view as LottieAnimationView

        //lottie.loop(actualExpand.get() == View.GONE)
        if(actualExpand.get() == View.GONE)
            lottie?.playAnimation()
        else
            lottie?.pauseAnimation()

    }

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

    fun onEndAlarmAfterFiredChanged(view: View) {
        scope.launch {
            cancelAlarmWhenAwake.get()?.let { dataStoreRepository.updateEndAlarmAfterFired(it) }
        }
    }
    val alarmSoundName = ObservableField("")

    //endregion


    init {
        scope.launch {
            var settings = dataStoreRepository.alarmParameterFlow.first()

            cancelAlarmWhenAwake.set(settings.endAlarmAfterFired)
            alarmArtSelections.addAll(arrayListOf<String>((context.getString(R.string.alarms_type_selection_only_alarm)), (context.getString(R.string.alarms_type_selection_alarm_vibration)), (context.getString(R.string.alarms_type_selection_only_vibration))))
            alarmArt.set(settings.alarmArt)

            if(settings.alarmName != "") {
                alarmSoundName.set(settings.alarmName)
            } else{
                alarmSoundName.set(context.getString(R.string.alarms_type_selection_default))
            }

        }
    }

    //region animation

    lateinit var transitionsContainer : ViewGroup


    //endregion
}


package com.doitstudio.sleepest_master.ui.alarms

import android.app.Application
import android.app.TimePickerDialog
import android.net.Uri
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.AndroidViewModel
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.util.SleepTimeValidationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalTime

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

    fun onExpandClicked(view: View) {
        TransitionManager.beginDelayedTransition(transitionsContainer);

        actualExpand.set(if (actualExpand.get() == View.GONE) View.VISIBLE else View.GONE)
        rotateState.set(if (actualExpand.get() == View.GONE) 0 else 180)

        alarmExpandId.set(-1)
    }

    fun updateExpandChanged(isExpaned : Boolean) {

        TransitionManager.beginDelayedTransition(transitionsContainer);

        if(isExpaned)
        {
            actualExpand.set(View.GONE)
            rotateState.set(0)
        }
    }

    val cancelAlarmWhenAwake = ObservableField(false)
    val alarmArtSelections = ObservableArrayList<String>()
    val alarmArt = ObservableField(0)

    fun onAlarmArtChanged(
        parent: AdapterView<*>?,
        selectedItemView: View,
        art: Int,
        id: Long
    ){
        scope.launch {
            dataStoreRepository.updateAlarmArt(art)
        }
    }

    fun onEndAlarmAfterFiredChanged(buttonView: View) {
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
            alarmArtSelections.addAll(arrayListOf<String>(("Nur Alarm"), ("Alarm und Vibration"), ("Nur Vibration")))
            alarmArt.set(settings.alarmArt)

            if(settings.alarmName != "") {
                alarmSoundName.set(settings.alarmName)
            } else{
                alarmSoundName.set("default")
            }

        }
    }

    //region animation

    lateinit var transitionsContainer : ViewGroup


    //endregion
}


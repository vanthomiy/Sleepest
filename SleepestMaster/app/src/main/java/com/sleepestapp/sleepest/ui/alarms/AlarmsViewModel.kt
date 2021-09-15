package com.sleepestapp.sleepest.ui.alarms

import android.app.Application
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.*
import com.airbnb.lottie.LottieAnimationView
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.storage.db.AlarmEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmsViewModel(
    val dataStoreRepository: DataStoreRepository,
    val dataBaseRepository: DatabaseRepository
    ) : ViewModel() {


    //region Alarms Settings

    /**
     * All actual setup alarms
     */
    lateinit var allAlarms: MutableList<AlarmEntity>
    /**
     * All ids of the setup alarms
     */
    lateinit var usedIds: MutableSet<Int>
    /**
     * All transactions for each id of the setup alarms
     */
    lateinit var transactions: MutableMap<Int, FragmentTransaction>
    /**
     * All fragments for each id of the setup alarms
     */
    lateinit var fragments: MutableMap<Int, AlarmInstanceFragment>

    /**
     * Observable live data of the alarms flow
     */
    val activeAlarmsLiveData by lazy {  dataBaseRepository.activeAlarmsFlow().asLiveData() }


    val alarmExpandId = MutableLiveData(0)
    val noAlarmsView = MutableLiveData(View.GONE)

    val actualExpand = MutableLiveData(View.GONE)
    val rotateState = MutableLiveData(0)

    var lottie : LottieAnimationView? = null

    /**
     * Expands the alarm settings of the alarms view
     */
    fun onExpandClicked(view: View) {
        TransitionManager.beginDelayedTransition(transitionsContainer);

        actualExpand.value = (if (actualExpand.value == View.GONE) View.VISIBLE else View.GONE)
        rotateState.value = (if (actualExpand.value == View.GONE) 0 else 180)

        alarmExpandId.value = (-1)

        lottie = view as LottieAnimationView

        if(actualExpand.value == View.GONE)
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
            actualExpand.value = (View.GONE)
            rotateState.value = (0)
        }

        if(actualExpand.value == View.GONE)
            lottie?.playAnimation()
        else
            lottie?.pauseAnimation()
    }

    val cancelAlarmWhenAwake = MutableLiveData(false)
    val alarmArtSelections = MutableLiveData<MutableList<String>>()
    val alarmArt = MutableLiveData(0)

    /**
     * When the alarm type has changed (Vibration/Sound etc.)
     */
    fun onAlarmTypeChanged(
        parent: AdapterView<*>?,
        selectedItemView: View,
        art: Int,
        id: Long)
    {

        viewModelScope.launch {
            dataStoreRepository.updateAlarmType(art)
        }
    }

    /**
     * When the alarm end after awake was changed
     */
    fun onEndAlarmAfterFiredChanged(view: View) {
        viewModelScope.launch {
            cancelAlarmWhenAwake.value?.let { dataStoreRepository.updateEndAlarmAfterFired(it) }
        }
    }
    val alarmSoundName = MutableLiveData("")

    val tempDisabledVisible = MutableLiveData(false)
    val isTempDisabled = MutableLiveData(false)

    //endregion


    init {

        /**
         * Loads all the init values from the datastore and passes the values to the bindings
         */
        viewModelScope.launch {
            var settings = dataStoreRepository.alarmParameterFlow.first()

            cancelAlarmWhenAwake.value = (settings.endAlarmAfterFired)
            alarmArt.value = (settings.alarmArt)

            if(settings.alarmName != "") {
                alarmSoundName.value = (settings.alarmName)
            }
        }
    }

    //region animation

    lateinit var transitionsContainer : ViewGroup


    //endregion
}


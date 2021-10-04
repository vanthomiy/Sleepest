package com.sleepestapp.sleepest.ui.alarms

import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.*
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.storage.db.AlarmEntity
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
    val alarmsLiveData by lazy {
        dataBaseRepository.alarmFlow.asLiveData()
    }

    /**
     * Observable live data of the sleep parameter flow
     */
    val sleepParameterLiveData by lazy {
        dataStoreRepository.sleepParameterFlow.asLiveData()
    }


    val alarmExpandId = MutableLiveData(0)
    val noAlarmsView = MutableLiveData(View.GONE)

    val actualExpand = MutableLiveData(View.GONE)
    val rotateState = MutableLiveData(0)
    val expandToggled = MutableLiveData(false)
    /**
     * Expands the alarm settings of the alarms view
     */
    @Suppress("UNUSED_PARAMETER")
    fun onExpandClicked(view: View) {

        actualExpand.value = (if (actualExpand.value == View.GONE) View.VISIBLE else View.GONE)
        rotateState.value = (if (actualExpand.value == View.GONE) 0 else 180)

        alarmExpandId.value = (-1)

        expandToggled.value = expandToggled.value == false
    }

    /**
     * When another expand is clicked, we also update the lottie animation to start again
     */
    fun updateExpandChanged(isExpanded : Boolean) {

        expandToggled.value = expandToggled.value == false

        if(isExpanded)
        {
            actualExpand.value = (View.GONE)
            rotateState.value = (0)
        }

    }

    val cancelAlarmWhenAwake = MutableLiveData(false)
    val alarmArtSelections = MutableLiveData<MutableList<String>>()
    val alarmArt = MutableLiveData(0)

    /**
     * When the alarm type has changed (Vibration/Sound etc.)
     */
    @Suppress("UNUSED_PARAMETER")
    fun onAlarmTypeChanged(parent: AdapterView<*>?, selectedItemView: View, art: Int, id: Long)
    {
        viewModelScope.launch {
            dataStoreRepository.updateAlarmType(art)
        }
    }

    /**
     * When the alarm end after awake was changed
     */
    @Suppress("UNUSED_PARAMETER")
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
            val settings = dataStoreRepository.alarmParameterFlow.first()

            cancelAlarmWhenAwake.value = (settings.endAlarmAfterFired)
            alarmArt.value = (settings.alarmArt)

            if(settings.alarmName != "") {
                alarmSoundName.value = (settings.alarmName)
            }
        }
    }
}


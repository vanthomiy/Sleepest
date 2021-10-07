package com.sleepestapp.sleepest.ui.sleep
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.view.View
import android.widget.AdapterView
import android.widget.SeekBar
import androidx.lifecycle.*
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.model.data.LightConditions
import com.sleepestapp.sleepest.model.data.MobilePosition
import com.sleepestapp.sleepest.model.data.MobileUseFrequency
import com.sleepestapp.sleepest.model.data.SleepSleepChangeFrom
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.*


class SleepViewModel(
    val dataStoreRepository: DataStoreRepository,
    val dataBaseRepository: DatabaseRepository
) : ViewModel() {

    //region Init

    val sleepParameterLiveData by lazy{
        dataStoreRepository.sleepParameterFlow.asLiveData()
    }

    //endregion

    //region binding values

    var sleepDuration : Int = 0

    /**
     * Sleep duration changed handler
     */
    fun onDurationChange(hour: Int, minute: Int) {

        var hourSetter = hour
        if(hour >= 24)
            hourSetter = 23

        val time = LocalTime.of(hourSetter, (minute-1) * 15)

        viewModelScope.launch {
            SleepTimeValidationUtil.checkSleepActionIsAllowedAndDoAction(
                dataStoreRepository,
                dataBaseRepository,
                sleepStartTime.toSecondOfDay(),
                sleepEndTime.toSecondOfDay(),
                time.toSecondOfDay(),
                autoSleepTime.value == true,
                SleepSleepChangeFrom.DURATION
            )
        }
    }

    val sleepStartValue = MutableLiveData("07:30")
    val sleepEndValue = MutableLiveData("07:30")
    var sleepStartTime: LocalTime = LocalTime.now()
    var sleepEndTime: LocalTime = LocalTime.now()
    var is24HourFormat : Boolean = false

    /**
     * Alarm start time changed click
     */
    fun onAlarmStartClicked(view: View){

        val hour = (sleepStartTime.hour)
        val minute = (sleepStartTime.minute)

        val tpd = TimePickerDialog(
                view.context,
                R.style.TimePickerTheme,
                { _, h, m ->

                    val tempWakeup = LocalTime.of(h, m)

                    viewModelScope.launch {

                        SleepTimeValidationUtil.checkSleepActionIsAllowedAndDoAction(
                            dataStoreRepository,
                            dataBaseRepository,
                            tempWakeup.toSecondOfDay(),
                            sleepEndTime.toSecondOfDay(),
                            sleepDuration,
                            autoSleepTime.value == true,
                            SleepSleepChangeFrom.SLEEPTIMESTART
                        )
                    }
                },
                hour,
                minute,
            is24HourFormat
        )

        tpd.show()
    }

    /**
     * Alarm end time changed click
     */
    fun onAlarmEndClicked(view: View){
        val hour = (sleepEndTime.hour)
        val minute = (sleepEndTime.minute)

        val tpd = TimePickerDialog(
            view.context,
            R.style.TimePickerTheme,
            { _, h, m ->

                val tempWakeup = LocalTime.of(h, m)

                viewModelScope.launch {

                    SleepTimeValidationUtil.checkSleepActionIsAllowedAndDoAction(
                        dataStoreRepository,
                        dataBaseRepository,
                        sleepStartTime.toSecondOfDay(),
                        tempWakeup.toSecondOfDay(),
                        sleepDuration,
                        autoSleepTime.value == true,
                        SleepSleepChangeFrom.SLEEPTIMEEND
                    )
                }
            },
                hour,
                minute,
            is24HourFormat
        )

        tpd.show()
    }


    val autoSleepTime = MutableLiveData(true)

    /**
     * Auto sleep time toggled
     */
    @Suppress("UNUSED_PARAMETER")
    fun sleepTimeToggled(view: View) {
        viewModelScope.launch{
            autoSleepTime.value?.let {
                dataStoreRepository.updateAutoSleepTime(it)
            }
        }

        //TransitionManager.beginDelayedTransition(transitionsContainer)

    }


    val actualExpand = MutableLiveData(-1)
    @SuppressLint("StaticFieldLeak")
    //private var lastView: ImageView? = null

    /**
     * Info button click by tag
     */
    fun onInfoClicked(view: View){
        val value = view.tag.toString()
        actualExpand.value =(if(actualExpand.value == value.toIntOrNull()) -1 else value.toIntOrNull() )
    }

    val phoneUsageValueString = MutableLiveData("")
    val phoneUsageValue = MutableLiveData(2)

    /**
     * Phone usage value of slider changed
     */
    @Suppress("UNUSED_PARAMETER")
    fun onPhoneUsageChanged(seekBar: SeekBar, progressValue: Int, fromUser: Boolean){

        val mf = MobileUseFrequency.getCount(progressValue)
        phoneUsageValueString.value = (mf.toString().lowercase(Locale.getDefault())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
        phoneUsageValue.value = (progressValue)
        viewModelScope.launch {
            dataStoreRepository.updateUserMobileFrequency(mf.ordinal)

        }

        sleepCalculateFactorCalculation()
    }

    val phonePositionSelections = MutableLiveData<MutableList<String>>()
    val mobilePosition = MutableLiveData(-1)
    var mobilePositionStart = -1


    /**
     * Mobile position selected
     */
    @Suppress("UNUSED_PARAMETER")
    fun onMobilePositionChanged(
        parent: AdapterView<*>?,
        selectedItemView: View,
        position: Int,
        id: Long
    ){
        // Bad workaround... we should find a better solution for that..
        // Its always called once with default values = 0.
        if(mobilePositionStart > 0 && mobilePositionStart != position)
        {
            mobilePosition.value = mobilePositionStart
            mobilePositionStart = -1

        }
        else{
            viewModelScope.launch {
                mobilePosition.value = position
                dataStoreRepository.updateStandardMobilePosition(position)
                sleepCalculateFactorCalculation()
            }
        }
    }

    val lightConditionSelections = MutableLiveData<MutableList<String>>()
    val lightCondition = MutableLiveData(0)
    var lightConditionStart = -1

    /**
     * Light condition selected
     */
    @Suppress("UNUSED_PARAMETER")
    fun onLightConditionChanged(
            parent: AdapterView<*>?,
            selectedItemView: View,
            position: Int,
            id: Long
    ) {
        // Bad workaround... we should find a better solution for that..
        // Its always called once with default values = 0.
        if (lightConditionStart > 0 && lightConditionStart != position) {
            lightCondition.value = lightConditionStart
            lightConditionStart = -1

        } else {
            viewModelScope.launch {
                lightCondition.value = position
                dataStoreRepository.updateLightCondition(position)
                sleepCalculateFactorCalculation()
            }
        }
    }



    val activityTracking = MutableLiveData(false)
    val includeActivityInCalculation = MutableLiveData(false)
    val activityTrackingView = MutableLiveData(View.GONE)

    /**
     * Activity tracking switched
     */
    @Suppress("UNUSED_PARAMETER")
    fun onActivityTrackingChanged(view:View) {
        //TransitionManager.beginDelayedTransition(transitionsContainer)

        viewModelScope.launch {

            activityTracking.value?.let {
                dataStoreRepository.updateActivityTracking(it)
                activityTrackingView.value = (if (it) View.VISIBLE else View.GONE)

            }

            sleepCalculateFactorCalculation()

        }
    }

    /**
     * Activity tracking use in calculation switched
     */
    @Suppress("UNUSED_PARAMETER")
    fun onActivityInCalcChanged(view:View) {
        viewModelScope.launch {
            includeActivityInCalculation.value?.let { dataStoreRepository.updateActivityInCalculation(
                it
            ) }

            sleepCalculateFactorCalculation()
        }
    }

    val sleepScoreValue = MutableLiveData("50")
    val sleepScoreText = MutableLiveData("50")

    /**
     *     defines how good the sleep can be measured
     *     100 is max and 30 is lowest
     */
    fun sleepCalculateFactorCalculation() {

        // phone position
        // phone usage
        // sleep with light / in dark

        var factor = when(mobilePosition.value?.let { MobilePosition.getCount(it) })
        {
            MobilePosition.INBED -> 1f
            MobilePosition.ONTABLE -> 0f
            else -> 0.5f
        }*2

        factor += when(phoneUsageValue.value?.let { MobileUseFrequency.getCount(it) })
        {
            MobileUseFrequency.VERYOFTEN -> 1f
            MobileUseFrequency.OFTEN -> 0.75f
            MobileUseFrequency.LESS -> 0.25f
            MobileUseFrequency.VERYLESS -> 0f
            else -> 0.5f
        }*3

        factor += when(lightCondition.value?.let { LightConditions.getCount(it) })
        {
            LightConditions.DARK -> 1f
            LightConditions.LIGHT -> 0f
            else -> 0.5f
        }*1

        val endFactor = factor / 6
        val score = 50 + endFactor * 50

        sleepScoreValue.value = (score.toInt().toString())


    }


    //endregion

    init {
        /**
         * Loads all the init values from the datastore and passes the values to the bindings
         */
        viewModelScope.launch {
            val sleepParams = dataStoreRepository.sleepParameterFlow.first()

            sleepStartTime = LocalTime.ofSecondOfDay(sleepParams.sleepTimeStart.toLong())
            sleepEndTime = LocalTime.ofSecondOfDay(sleepParams.sleepTimeEnd.toLong())

            sleepStartValue.value = ((if (sleepStartTime.hour < 10) "0" else "") + sleepStartTime.hour.toString() + ":" + (if (sleepStartTime.minute < 10) "0" else "") + sleepStartTime.minute.toString())
            sleepEndValue.value =((if (sleepEndTime.hour < 10) "0" else "") + sleepEndTime.hour.toString() + ":" + (if (sleepEndTime.minute < 10) "0" else "") + sleepEndTime.minute.toString())

            phoneUsageValue.value = (sleepParams.mobileUseFrequency)

            autoSleepTime.value =(sleepParams.autoSleepTime)

            mobilePosition.value = (sleepParams.standardMobilePosition)
            mobilePositionStart = (sleepParams.standardMobilePosition)

            lightCondition.value = (sleepParams.standardLightCondition)
            lightConditionStart = (sleepParams.standardLightCondition)

            activityTracking.value = (sleepParams.userActivityTracking)
            includeActivityInCalculation.value = (sleepParams.implementUserActivityInSleepTime)
            activityTrackingView.value = (if (sleepParams.userActivityTracking) View.VISIBLE else View.GONE)

            sleepCalculateFactorCalculation()

        }
    }

}


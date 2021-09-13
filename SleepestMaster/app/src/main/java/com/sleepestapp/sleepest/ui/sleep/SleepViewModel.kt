package com.sleepestapp.sleepest.ui.sleep

import android.app.Application
import android.app.TimePickerDialog
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.googleapi.ActivityTransitionHandler
import com.sleepestapp.sleepest.model.data.*
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.util.IconAnimatorUtil
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil.Is24HourFormat
import com.sleepestapp.sleepest.util.StringUtil.getStringXml
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.*


class SleepViewModel(application: Application) : AndroidViewModel(application) {

    //region Init
    private val scope = MainScope()
    private val context by lazy{ getApplication<Application>().applicationContext }
    lateinit var transitionsContainer : ViewGroup
    val dataStoreRepository: DataStoreRepository by lazy {
        (context as MainApplication).dataStoreRepository
    }
    private val dataBaseRepository: DatabaseRepository by lazy {
        (context as MainApplication).dataBaseRepository
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

        scope.launch {
            SleepTimeValidationUtil.checkSleepActionIsAllowedAndDoAction(
                dataStoreRepository,
                dataBaseRepository,
                context,
                sleepStartTime.toSecondOfDay(),
                sleepEndTime.toSecondOfDay(),
                time.toSecondOfDay(),
                autoSleepTime.get() == true,
                SleepSleepChangeFrom.DURATION
            )
        }
    }

    val sleepStartValue = ObservableField("07:30")
    val sleepEndValue = ObservableField("07:30")
    var sleepStartTime = LocalTime.now()
    var sleepEndTime = LocalTime.now()


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

                    scope.launch {

                        SleepTimeValidationUtil.checkSleepActionIsAllowedAndDoAction(
                            dataStoreRepository,
                            dataBaseRepository,
                            view.context,
                            tempWakeup.toSecondOfDay(),
                            sleepEndTime.toSecondOfDay(),
                            sleepDuration,
                            autoSleepTime.get() == true,
                            SleepSleepChangeFrom.SLEEPTIMESTART
                        )
                    }
                },
                hour,
                minute,
            Is24HourFormat(context)
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

                scope.launch {

                    SleepTimeValidationUtil.checkSleepActionIsAllowedAndDoAction(
                        dataStoreRepository,
                        dataBaseRepository,
                        view.context,
                        sleepStartTime.toSecondOfDay(),
                        tempWakeup.toSecondOfDay(),
                        sleepDuration,
                        autoSleepTime.get() == true,
                        SleepSleepChangeFrom.SLEEPTIMEEND
                    )
                }
            },
                hour,
                minute,
            Is24HourFormat(context)
        )

        tpd.show()
    }


    val autoSleepTime = ObservableField(true)
    val manualSleepTime = ObservableField(true)
    val manualSleepTimeVisibility = ObservableField(View.GONE)

    /**
     * Auto sleep time toggled
     */
    fun SleepTimeToogled(view: View) {
        scope.launch{
            autoSleepTime.get()?.let {
                dataStoreRepository.updateAutoSleepTime(it)
                manualSleepTime.set(!it)
            }
        }

        TransitionManager.beginDelayedTransition(transitionsContainer);

        autoSleepTime.get()?.let {
            manualSleepTimeVisibility.set(if (it) View.GONE else View.VISIBLE)
        }
    }


    val actualExpand = ObservableField(-1)
    val goneState = ObservableField(View.GONE)
    val visibleState = ObservableField(View.VISIBLE)
    private var lastView: ImageView? = null

    /**
     * Info button click by tag
     */
    fun onInfoClicked(view: View){
        updateInfoChanged(view.tag.toString())

        // Check if its an image view
        if(view.tag.toString() != "7"){
            IconAnimatorUtil.animateView(view as ImageView)

                IconAnimatorUtil.resetView(lastView)

            lastView = if(lastView != view)
                (view as ImageView)
            else
                null
        }
        else{
            IconAnimatorUtil.resetView(lastView)
            lastView = null
        }
    }

    /**
     * Update the info layouts hide/show
     */
    private fun updateInfoChanged(value: String) {

        TransitionManager.beginDelayedTransition(transitionsContainer);


        actualExpand.set(if(actualExpand.get() == value.toIntOrNull()) -1 else value.toIntOrNull() )
    }

    val phoneUsageValueString = ObservableField("")
    val phoneUsageValue = ObservableField<Int>(2)

    /**
     * Phone usage value of slider changed
     */
    fun onPhoneUsageChanged(seekBar: SeekBar, progresValue: Int, fromUser: Boolean){

        val mf = MobileUseFrequency.getCount(progresValue)
        phoneUsageValueString.set(mf.toString().lowercase(Locale.getDefault())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
        phoneUsageValue.set(progresValue)
        scope.launch {
            dataStoreRepository.updateUserMobileFequency(mf.ordinal)

        }

        sleepCalculateFactorCalculation()
    }

    val phonePositionSelections = ObservableArrayList<String>()
    val mobilePosition = ObservableField(0)

    /**
     * Mobile position selected
     */
    fun onMobilePositionChanged(
        parent: AdapterView<*>?,
        selectedItemView: View,
        position: Int,
        id: Long
    ){
        scope.launch {
            dataStoreRepository.updateStandardMobilePosition(position)
            sleepCalculateFactorCalculation()
        }
    }

    val lightConditionSelections = ObservableArrayList<String>()
    val lightCondition = ObservableField(0)

    /**
     * Light condition selected
     */
    fun onLightConditionChanged(
            parent: AdapterView<*>?,
            selectedItemView: View,
            position: Int,
            id: Long
    ){
        scope.launch {
            dataStoreRepository.updateLigthCondition(position)
            sleepCalculateFactorCalculation()

        }

    }


    val activityTracking = ObservableField(false)
    val includeActivityInCalculation = ObservableField(false)
    val activityTrackingView = ObservableField(View.GONE)

    /**
     * Activity tracking switched
     */
    fun onActivityTrackingChanged(view:View) {
        TransitionManager.beginDelayedTransition(transitionsContainer);

        scope.launch {
            activityTracking.get()?.let {
                dataStoreRepository.updateActivityTracking(it)
                activityTrackingView.set(if (it) View.VISIBLE else View.GONE)

            }

            sleepCalculateFactorCalculation()

        }

        TransitionManager.beginDelayedTransition(transitionsContainer);

        activityTracking.get()?.let {
            activityTrackingView.set(if (it) View.VISIBLE else View.GONE)

            if(it)
                ActivityTransitionHandler.getHandler(getApplication()).startActivityHandler()
            else
                ActivityTransitionHandler.getHandler(getApplication()).stopActivityHandler()
        }
    }

    /**
     * Activity tracking use in calculation switched
     */
    fun onActivityInCalcChanged(view:View) {
        scope.launch {
            includeActivityInCalculation.get()?.let { dataStoreRepository.updateActivityInCalculation(
                it
            ) }

            sleepCalculateFactorCalculation()
        }
    }

    val sleepScoreValue = ObservableField("50")
    val sleepScoreText = ObservableField("50")

    /**
     *     defines how good the sleep can be messured
     *     100 is max and 30 is lowest
     */
    fun sleepCalculateFactorCalculation() {

        // phone position
        // phone usage
        // sleep with light / in dark

        var factor = when(mobilePosition.get()?.let { MobilePosition.getCount(it) })
        {
            MobilePosition.INBED -> 1f
            MobilePosition.ONTABLE -> 0f
            else -> 0.5f
        }*2

        factor += when(phoneUsageValue.get()?.let { MobileUseFrequency.getCount(it) })
        {
            MobileUseFrequency.VERYOFTEN -> 1f
            MobileUseFrequency.OFTEN -> 0.75f
            MobileUseFrequency.LESS -> 0.25f
            MobileUseFrequency.VERYLESS -> 0f
            else -> 0.5f
        }*3

        factor += when(lightCondition.get()?.let { LightConditions.getCount(it) })
        {
            LightConditions.DARK -> 1f
            LightConditions.LIGHT -> 0f
            else -> 0.5f
        }*1

        val endFactor = factor / 6
        val score = 50 + endFactor * 50

        sleepScoreValue.set(score.toInt().toString())

        sleepScoreText.set(when {
            score < 60 -> {
                getStringXml(R.string.sleep_score_text_60, getApplication())
                //getStringXml(R.string.sleep_score_text_60)
            }
            score < 70 -> {
                getStringXml(R.string.sleep_score_text_70, getApplication())
                //getStringXml(R.string.sleep_score_text_70)
            }
            score < 80 -> {
                getStringXml(R.string.sleep_score_text_80, getApplication())
                //getStringXml(R.string.sleep_score_text_80)
            }
            score < 90 -> {
                getStringXml(R.string.sleep_score_text_90, getApplication())
                //getStringXml(R.string.sleep_score_text_90)
            }
            else -> {
                getStringXml(R.string.sleep_score_text_100, getApplication())
                //getStringXml(R.string.sleep_score_text_100)
            }
        }

        )

        // activity tracking



    }


    //endregion

    init {
        /**
         * Loads all the init values from the datastore and passes the values to the bindings
         */
        scope.launch {
            var sleepParams = dataStoreRepository.sleepParameterFlow.first()

            sleepStartTime = LocalTime.ofSecondOfDay(sleepParams.sleepTimeStart.toLong())
            sleepEndTime = LocalTime.ofSecondOfDay(sleepParams.sleepTimeEnd.toLong())

            sleepStartValue.set((if (sleepStartTime.hour < 10) "0" else "") + sleepStartTime.hour.toString() + ":" + (if (sleepStartTime.minute < 10) "0" else "") + sleepStartTime.minute.toString())
            sleepEndValue.set((if (sleepEndTime.hour < 10) "0" else "") + sleepEndTime.hour.toString() + ":" + (if (sleepEndTime.minute < 10) "0" else "") + sleepEndTime.minute.toString())

            phoneUsageValue.set(sleepParams.mobileUseFrequency)

            manualSleepTime.set(!sleepParams.autoSleepTime)
            autoSleepTime.set(sleepParams.autoSleepTime)
            manualSleepTimeVisibility.set(if (sleepParams.autoSleepTime) View.GONE else View.VISIBLE)

            phonePositionSelections.addAll(arrayListOf<String>(getStringXml(R.string.sleep_phoneposition_inbed, getApplication()), getStringXml(R.string.sleep_phoneposition_ontable, getApplication()), getStringXml(R.string.sleep_phoneposition_auto, getApplication())))
            mobilePosition.set(sleepParams.standardMobilePosition)

            lightConditionSelections.addAll(arrayListOf<String>(getStringXml(R.string.sleep_lightcondidition_dark, getApplication()), getStringXml(R.string.sleep_lightcondidition_light, getApplication()), getStringXml(R.string.sleep_lightcondidition_auto, getApplication())))
            lightCondition.set(sleepParams.standardLightCondition)

            activityTracking.set(sleepParams.userActivityTracking)
            includeActivityInCalculation.set(sleepParams.implementUserActivityInSleepTime)
            activityTrackingView.set(if (sleepParams.userActivityTracking) View.VISIBLE else View.GONE)

            sleepCalculateFactorCalculation()

        }
    }

}


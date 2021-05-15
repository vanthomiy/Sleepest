package com.doitstudio.sleepest_master.ui.sleep

import android.app.Application
import android.app.TimePickerDialog
import android.content.Context
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.doitstudio.sleepest_master.SleepParameters
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.MobileUseFrequency
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationStoreRepository
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.db.AlarmEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*


class SleepViewModel(application:Application) : AndroidViewModel (application) {

    private val context by lazy{ getApplication<Application>().applicationContext }
    private val scope: CoroutineScope = MainScope()
    private val dataStoreRepository by lazy {  DataStoreRepository.getRepo(context)}

    val sleepDurationString = ObservableField("7h")
    val sleepDurationValue = ObservableField<Int>(7)
    fun onSleepDurationChanged(seekBar: SeekBar, progresValue: Int, fromUser: Boolean) {

        val time = getSleepCountFromProgress(progresValue)

        sleepDurationString.set(time.toString())
        scope.launch {
            dataStoreRepository.updateUserWantedSleepTime(time.toSecondOfDay())
        }
    }

    private fun getSleepCountFromProgress(count:Int) : LocalTime{
        var hour = 2 + count / 4
        var minute = (count % 4) * 15
        return LocalTime.of(hour, minute)
    }

    private fun getProgressFromSleepCount(time:LocalTime) : Int{
        var count = (time.hour-2) * 4
        count += time.minute / 15
        return count
    }


    val phoneUsageString = ObservableField("Less")
    val phoneUsageValue = ObservableField(1)

    fun onPhoneUsageChanged(seekBar: SeekBar, progresValue: Int, fromUser: Boolean) {

        if(progresValue < 1){
            phoneUsageValue.set(1)
            return
        }
        else if(progresValue > 4){
            phoneUsageValue.set(4)
            return
        }


        phoneUsageString.set(when(progresValue){
            1 ->  "very less "
            2 ->  "less     "
            3 ->  "often    "
            else -> "very often"
        })

        val mobileUse = when(progresValue){
            1 ->  MobileUseFrequency.VERYLESS
            2 ->  MobileUseFrequency.LESS
            3 ->  MobileUseFrequency.OFTEN
            else -> MobileUseFrequency.VERYOFTEN
        }

        scope.launch {
            dataStoreRepository.updateUserMobileFequency(mobileUse)
        }

    }


    val onTableValue = ObservableField<Boolean>(false)

    fun onTableValueChanged(view: View) {
        scope.launch {
            dataStoreRepository.updateStandardMobilePosition(if(onTableValue.get()==true) MobilePosition.ONTABLE else MobilePosition.INBED)
        }
    }

    val sleepStartValue = ObservableField("07:30")
    val sleepEndValue = ObservableField("07:30")
    var sleepStartTime = LocalTime.now()
    var sleepEndTime = LocalTime.now()

    fun onAlarmStartClicked(view: View){

        val hour = (sleepStartTime.hour)
        val minute = (sleepStartTime.minute)

        val tpd = TimePickerDialog(
            view.context,
            TimePickerDialog.OnTimeSetListener(function = { view, h, m ->

                sleepStartValue.set((if(h < 10) "0" else "") + h.toString() + ":" + (if(m < 10) "0" else "") + m.toString())
                sleepStartTime = LocalTime.of(h,m)

                scope.launch {
                    dataStoreRepository.updateSleepTimeStart(sleepStartTime.toSecondOfDay())
                }
            }),
            hour,
            minute,
            false
        )

        tpd.show()
    }

    fun onAlarmEndClicked(view: View){
        val hour = (sleepEndTime.hour)
        val minute = (sleepEndTime.minute)

        val tpd = TimePickerDialog(
            view.context,
            TimePickerDialog.OnTimeSetListener(function = { view, h, m ->

                sleepEndValue.set((if(h < 10) "0" else "") + h.toString() + ":" + (if(m < 10) "0" else "") + m.toString())
                sleepEndTime = LocalTime.of(h,m)

                scope.launch {
                    dataStoreRepository.updateSleepTimeEnd(sleepEndTime.toSecondOfDay())
                }
            }),
            hour,
            minute,
            false
        )

        tpd.show()
    }

    init {
        scope.launch {
            var sleepParams = dataStoreRepository.sleepParameterFlow.first()
            val time = LocalTime.ofSecondOfDay(sleepParams.normalSleepTime.toLong())
            sleepDurationValue.set(getProgressFromSleepCount(time))
            sleepDurationString.set(time.toString())

            phoneUsageString.set(when(sleepParams.mobileUseFrequency){
                1 ->  "very less "
                2 ->  "less     "
                3 ->  "often    "
                else -> "very often"
            })
            phoneUsageValue.set(sleepParams.mobileUseFrequency)

            onTableValue.set(sleepParams.standardMobilePosition == 1)

            sleepStartTime = LocalTime.ofSecondOfDay(sleepParams.sleepTimeStart.toLong())
            sleepEndTime = LocalTime.ofSecondOfDay(sleepParams.sleepTimeEnd.toLong())

            sleepStartValue.set((if(sleepStartTime.hour < 10) "0" else "") + sleepStartTime.hour.toString() + ":" + (if(sleepStartTime.minute < 10) "0" else "") + sleepStartTime.minute.toString())
            sleepEndValue.set((if(sleepEndTime.hour < 10) "0" else "") + sleepEndTime.hour.toString() + ":" + (if(sleepEndTime.minute < 10) "0" else "") + sleepEndTime.minute.toString())


        }
    }
}
package com.doitstudio.sleepest_master.ui.sleep

import android.app.TimePickerDialog
import android.content.Context
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.doitstudio.sleepest_master.SleepParameters
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationStoreRepository
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*


class SleepViewModel(val context: Context) : ViewModel () {

    private val scope: CoroutineScope = MainScope()
    private val dataStoreRepository by lazy {  DataStoreRepository.getRepo(context)}
    val sleepParamsLiveData by lazy { dataStoreRepository.sleepParameterFlow.asLiveData()}

    var sleepDurationValue = ObservableField("7h")
    fun onSleepDurationChanged(seekBar: SeekBar, progresValue: Int, fromUser: Boolean) {
        sleepDurationValue.set(progresValue.toString() + "h")
    }

    var phoneUsageValue = ObservableField("Less")
    fun onPhoneUsageChanged(seekBar: SeekBar, progresValue: Int, fromUser: Boolean) {
        phoneUsageValue.set(
            if (progresValue == 1) "very less "
            else if (progresValue == 2) "less     "
            else if (progresValue == 3) "normal   "
            else if (progresValue == 4) "often    "
            else "very often"
        )
    }

    var onTableValue = false

    var sleepStartValue = ObservableField("07:30")
    var sleepEndValue = ObservableField("07:30")
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
            }),
            hour,
            minute,
            false
        )

        tpd.show()
    }



    suspend fun loadFields() {

        var sleepParams = dataStoreRepository.sleepParameterFlow.first()


    }

}
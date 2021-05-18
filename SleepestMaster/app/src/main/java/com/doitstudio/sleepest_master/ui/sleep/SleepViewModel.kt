package com.doitstudio.sleepest_master.ui.sleep

import android.app.Application
import android.app.TimePickerDialog
import android.view.View
import android.widget.SeekBar
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.databinding.FragmentSleepBinding
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime


class SleepViewModel(application: Application) : AndroidViewModel(application) {

    //private lateinit var binding: FragmentSleepBinding

    private val scope: CoroutineScope = MainScope()
    private val context by lazy{ getApplication<Application>().applicationContext }
    private val dataStoreRepository: DataStoreRepository by lazy {
        (context as MainApplication).dataStoreRepository
    }

    val sleepDurationString = ObservableField("7h")
    val sleepDurationValue = ObservableField<Int>(7)
    fun onSleepDurationChanged(seekBar: SeekBar, progresValue: Int, fromUser: Boolean) {

        val time = getSleepCountFromProgress(progresValue)

        sleepDurationString.set(time.toString())
        scope.launch {
            dataStoreRepository.updateUserWantedSleepTime(time.toSecondOfDay())
        }
    }

    private fun getSleepCountFromProgress(count: Int) : LocalTime{
        var hour = 2 + count / 4
        var minute = (count % 4) * 15
        return LocalTime.of(hour, minute)
    }
    private fun getProgressFromSleepCount(time: LocalTime) : Int{
        var count = (time.hour-2) * 4
        count += time.minute / 15
        return count
    }

    val sleepStartValue = ObservableField("07:30")
    val sleepCompleteValue = ObservableField("07:30 to 7:30")
    val sleepEndValue = ObservableField("07:30")
    var sleepStartTime = LocalTime.now()
    var sleepEndTime = LocalTime.now()

    fun onAlarmStartClicked(view: View){

        val hour = (sleepStartTime.hour)
        val minute = (sleepStartTime.minute)

        val tpd = TimePickerDialog(
            view.context,
            TimePickerDialog.OnTimeSetListener(function = { view, h, m ->

                sleepStartValue.set((if (h < 10) "0" else "") + h.toString() + ":" + (if (m < 10) "0" else "") + m.toString())
                sleepStartTime = LocalTime.of(h, m)

                sleepCompleteValue.set(sleepStartValue.get() + " to " + sleepEndValue.get())

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

                sleepEndValue.set((if (h < 10) "0" else "") + h.toString() + ":" + (if (m < 10) "0" else "") + m.toString())

                sleepCompleteValue.set(sleepStartValue.get() + " to " + sleepEndValue.get())

                sleepEndTime = LocalTime.of(h, m)

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

            sleepStartTime = LocalTime.ofSecondOfDay(sleepParams.sleepTimeStart.toLong())
            sleepEndTime = LocalTime.ofSecondOfDay(sleepParams.sleepTimeEnd.toLong())

            sleepStartValue.set((if (sleepStartTime.hour < 10) "0" else "") + sleepStartTime.hour.toString() + ":" + (if (sleepStartTime.minute < 10) "0" else "") + sleepStartTime.minute.toString())
            sleepEndValue.set((if (sleepEndTime.hour < 10) "0" else "") + sleepEndTime.hour.toString() + ":" + (if (sleepEndTime.minute < 10) "0" else "") + sleepEndTime.minute.toString())

        }
    }
    // region Movement

    val sleepSettingsExpanded = ObservableField(View.VISIBLE)
    val sleepSettingsNotExpanded = ObservableField(View.GONE)


    /**
     * Shows all controls for editing the sleep settings
     */
    fun onEditSleepSettings(view: View){
        sleepSettingsExpanded.set(View.VISIBLE)
        sleepSettingsNotExpanded.set(View.GONE)
    }

    /**
     * Hides the controls for the sleep settings. Only shows the summary of the settings
     */
    fun onHideSleepSettings(view: View){
        sleepSettingsExpanded.set(View.GONE)
        sleepSettingsNotExpanded.set(View.VISIBLE)
    }

    // endregion

    // region Charts

    fun setUpSleepTimeChar() : LineDataSet {

        var entries = mutableListOf<Entry>()
        entries.add(Entry(1f, 3f))
        entries.add(Entry(2f, 10f))
        entries.add(Entry(3f, 5f))
        entries.add(Entry(4f, 4f))
        var dataSet = LineDataSet(entries, "Label") // add entries to dataset
        return dataSet
    }


    // endregion
}
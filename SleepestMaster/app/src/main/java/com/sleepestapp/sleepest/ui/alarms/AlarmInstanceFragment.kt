package com.sleepestapp.sleepest.ui.alarms

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.databinding.AlarmEntityBinding
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil
import java.time.LocalTime

class AlarmInstanceFragment(val applicationContext: Context, private var alarmId: Int) : Fragment() {

    private val databaseRepository by lazy { (applicationContext as MainApplication).dataBaseRepository }

    private lateinit var binding: AlarmEntityBinding
    private val viewModel by lazy { ViewModelProvider(this).get(AlarmInstanceViewModel::class.java) }
    private val alarmsViewModel by lazy { ViewModelProvider(requireActivity()).get(AlarmsViewModel::class.java) }

    private lateinit var usedIds : MutableSet<Int>

    private fun deleteAlarmEntity() {
        AlarmsFragment.getAlarmFragment().removeAlarmEntity(alarmId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = AlarmEntityBinding.inflate(inflater, container, false)
        binding.alarmInstanceViewModel = viewModel
        binding.alarmsViewModel = alarmsViewModel
        viewModel.alarmId = alarmId
        viewModel.transitionsContainer = (binding.cLAlarmEntityInnerLayer)

        val minData = SleepTimeValidationUtil.createMinutePickerHelper()
        binding.npMinutes.minValue = 1;
        binding.npMinutes.maxValue = minData.size;
        binding.npMinutes.displayedValues = minData;

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usedIds = mutableSetOf()

        binding.btnDeleteAlarm.setOnClickListener {
            deleteAlarmEntity()
        }

        binding.npHours.setOnValueChangedListener { _, _, newVal -> viewModel.onDurationChange(
            newVal,
            binding.npMinutes.value
        ) }

        binding.npMinutes.setOnValueChangedListener { _, _, newVal -> viewModel.onDurationChange(
            binding.npHours.value,
            newVal
        )  }

        // Used to update the sleep end and start time if it changes from the alarms fragments
        databaseRepository.getAlarmById(viewModel.alarmId).asLiveData().observe(viewLifecycleOwner){

            viewModel.wakeUpEarly = LocalTime.ofSecondOfDay(it.wakeupEarly.toLong())
            viewModel.wakeUpLate = LocalTime.ofSecondOfDay(it.wakeupLate.toLong())

            val sleepDuration = LocalTime.ofSecondOfDay(it.sleepDuration.toLong())

            binding.npHours.value = sleepDuration.hour
            binding.npMinutes.value = (sleepDuration.minute / 15) + 1
            viewModel.sleepDuration = sleepDuration.toSecondOfDay()
            viewModel.sleepDurationString.set(sleepDuration.toString() + " " + getString(R.string.alarm_instance_alarm_header))

            viewModel.wakeUpEarlyValue.set((if (viewModel.wakeUpEarly.hour < 10) "0" else "") + viewModel.wakeUpEarly.hour.toString() + ":" + (if (viewModel.wakeUpEarly.minute < 10) "0" else "") + viewModel.wakeUpEarly.minute.toString())
            viewModel.wakeUpLateValue.set((if (viewModel.wakeUpLate.hour < 10) "0" else "") + viewModel.wakeUpLate.hour.toString() + ":" + (if (viewModel.wakeUpLate.minute < 10) "0" else "") + viewModel.wakeUpLate.minute.toString())

        }

        binding.cLAlarmEntityInnerLayer.setOnClickListener{

            viewModel.onAlarmNameClick(it)

            alarmsViewModel.alarmExpandId.set(alarmId)

            alarmsViewModel.updateExpandChanged(viewModel.extendedAlarmEntity.get() == true)

        }

        alarmsViewModel.alarmExpandId.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                if(alarmsViewModel.alarmExpandId.get() != alarmId)
                    viewModel.extendedAlarmEntity.set(false)
            }
        })
    }
}



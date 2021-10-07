package com.sleepestapp.sleepest.ui.alarms

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.databinding.AlarmEntityBinding
import com.sleepestapp.sleepest.storage.db.AlarmEntity
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil
import com.sleepestapp.sleepest.util.StringUtil
import com.sleepestapp.sleepest.util.WeekDaysUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime

class AlarmInstanceFragment(val applicationContext: Context, private var alarmId: Int) : Fragment() {

    // region init

    var factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            // Workaround because we know that we can cast to T
            return  AlarmsViewModel(
                (actualContext as MainApplication).dataStoreRepository,
                (actualContext as MainApplication).dataBaseRepository
            ) as T
        }
    }
    var instanceFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            // Workaround because we know that we can cast to T
            return  AlarmInstanceViewModel(
                (actualContext as MainApplication).dataStoreRepository,
                (actualContext as MainApplication).dataBaseRepository,
                alarmId
            ) as T
        }
    }

    private lateinit var binding: AlarmEntityBinding
    private val viewModel by lazy { ViewModelProvider(this, instanceFactory).get(AlarmInstanceViewModel::class.java) }
    private val alarmsViewModel by lazy { ViewModelProvider(requireActivity(), factory).get(AlarmsViewModel::class.java) }

    /**
     * Get actual context
     */
    private val actualContext: Context by lazy { requireActivity().applicationContext }

    // endregion


    /**
     * Delete the alarm, for that we have to call the function in the alarms fragment
     */
    private fun deleteAlarmEntity() {
        AlarmsFragment.getAlarmFragment().removeAlarmEntity(alarmId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = AlarmEntityBinding.inflate(inflater, container, false)
        binding.alarmInstanceViewModel = viewModel
        binding.alarmsViewModel = alarmsViewModel
        binding.lifecycleOwner = this

        val minData = SleepTimeValidationUtil.createMinutePickerHelper()
        binding.npMinutes.minValue = 1
        binding.npMinutes.maxValue = minData.size
        binding.npMinutes.displayedValues = minData

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // delete alarm click
        binding.btnDeleteAlarm.setOnClickListener {
            deleteAlarmEntity()
        }

        // hours of alarm changed
        binding.npHours.setOnValueChangedListener { _, _, newVal -> viewModel.onDurationChange(
            newVal,
            binding.npMinutes.value
        ) }

        // minutes of alarm changed
        binding.npMinutes.setOnValueChangedListener { _, _, newVal -> viewModel.onDurationChange(
            binding.npHours.value,
            newVal
        )  }

        // Used to update the sleep end and start time if it changes from the alarms fragments
        viewModel.actualAlarmLiveData.observe(viewLifecycleOwner){
            alarm ->
            alarm?.let{
                setUpAlarm(it)
            }
        }

        viewModel.actualAlarmParameterLiveData.observe(viewLifecycleOwner){
            lifecycleScope.launch{
                val alarm = viewModel.dataBaseRepository.getAlarmById(alarmId).first()
                alarm?.let{
                    setUpAlarm(alarm)
                }
            }
        }




        binding.cLAlarmEntityInnerLayer.setOnClickListener{

            viewModel.onAlarmNameClick(it)

            alarmsViewModel.alarmExpandId.value = (alarmId)

            alarmsViewModel.updateExpandChanged(viewModel.extendedAlarmEntity.value == true)

        }

        // Expand an alarm view
        alarmsViewModel.alarmExpandId.observe(viewLifecycleOwner){
            if(it != alarmId)
                viewModel.extendedAlarmEntity.value = (false)
        }

        viewModel.alarmName.value = StringUtil.getStringXml(R.string.alarm_instance_alarm, requireActivity().application)
        viewModel.is24HourFormat = SleepTimeValidationUtil.is24HourFormat(actualContext)

        viewModel.selectedDays.observe(viewLifecycleOwner){
            setDaysSelectedString(it)
        }
    }

    private fun setUpAlarm(alarm : AlarmEntity){
        viewModel.wakeUpEarly = LocalTime.ofSecondOfDay(alarm.wakeupEarly.toLong())
        viewModel.wakeUpLate = LocalTime.ofSecondOfDay(alarm.wakeupLate.toLong())

        val sleepDuration = LocalTime.ofSecondOfDay(alarm.sleepDuration.toLong())

        binding.npHours.value = sleepDuration.hour
        binding.npMinutes.value = (sleepDuration.minute / 15) + 1
        viewModel.sleepDuration = sleepDuration.toSecondOfDay()
        viewModel.sleepDurationString.value = (sleepDuration.toString() + " " + getString(R.string.alarm_instance_alarm_header))

        viewModel.wakeUpEarlyValue.value = ((if (viewModel.wakeUpEarly.hour < 10) "0" else "") + viewModel.wakeUpEarly.hour.toString() + ":" + (if (viewModel.wakeUpEarly.minute < 10) "0" else "") + viewModel.wakeUpEarly.minute.toString())
        viewModel.wakeUpLateValue.value = ((if (viewModel.wakeUpLate.hour < 10) "0" else "") + viewModel.wakeUpLate.hour.toString() + ":" + (if (viewModel.wakeUpLate.minute < 10) "0" else "") + viewModel.wakeUpLate.minute.toString())

    }

    /**
     * Update the selected days string
     */
    private fun setDaysSelectedString(selectedDays:MutableList<Int>){
        var info = ""

        if(selectedDays.isEmpty()){
            info = StringUtil.getStringXml(R.string.alarm_instance_no_day_choosen, requireActivity().application)
        }
        else if(selectedDays.count() >= 7)
        {
            info = StringUtil.getStringXml(R.string.alarm_instance_daily, requireActivity().application)
        }
        else if(selectedDays.count() == 2 && selectedDays.contains(5) && selectedDays.contains(6))
        {
            info = StringUtil.getStringXml(R.string.alarm_instance_weekend, requireActivity().application)
        }
        else if(selectedDays.count() == 5 && !selectedDays.contains(5) && !selectedDays.contains(6))
        {
            info = StringUtil.getStringXml(R.string.alarm_instance_working_day, requireActivity().application)
        }
        else{



            selectedDays.toList().sorted().forEach{
                if(info == ""){
                    info = WeekDaysUtil.getWeekDayByNumber(it)
                }
                else{
                    info +=  ", "+ WeekDaysUtil.getWeekDayByNumber(it)
                }
            }
        }

        viewModel.selectedDaysInfo.value = (info)
    }
}



package com.sleepestapp.sleepest.ui.sleep

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.databinding.FragmentSleepBinding
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil
import java.time.LocalTime


class SleepFragment : Fragment() {


    private val viewModel by lazy { ViewModelProvider(this).get(SleepViewModel::class.java)}
    private lateinit var binding: FragmentSleepBinding
    private val actualContext: Context by lazy {requireActivity().applicationContext}

    private val dataStoreRepository: DataStoreRepository by lazy {
        (actualContext as MainApplication).dataStoreRepository
    }

    companion object {
        fun newInstance() = SleepFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSleepBinding.inflate(inflater, container, false)
        viewModel.transitionsContainer = (binding.linearAnimationlayout)
        binding.sleepViewModel = viewModel

        val minData = SleepTimeValidationUtil.createMinutePickerHelper()
        binding.npMinutes.minValue = 1;
        binding.npMinutes.maxValue = minData.size;
        binding.npMinutes.displayedValues = minData;

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.npHours.setOnValueChangedListener { _, _, newVal -> viewModel.onDurationChange(
            newVal,
            binding.npMinutes.value
        )
        }

        binding.npMinutes.setOnValueChangedListener { _, _, newVal -> viewModel.onDurationChange(
            binding.npHours.value,
            newVal
        )
        }

        // Used to update the sleep end and start time if it changes from the alarms fragments
        dataStoreRepository.sleepParameterFlow.asLiveData().observe(viewLifecycleOwner){

            viewModel.sleepStartTime = LocalTime.ofSecondOfDay(it.sleepTimeStart.toLong())
            viewModel.sleepEndTime = LocalTime.ofSecondOfDay(it.sleepTimeEnd.toLong())

            val sleepDuration = LocalTime.ofSecondOfDay(it.sleepDuration.toLong())
            binding.npHours.value = sleepDuration.hour
            binding.npMinutes.value = (sleepDuration.minute / 15) + 1
            viewModel.sleepDuration = sleepDuration.toSecondOfDay()

            viewModel.sleepStartValue.set((if (viewModel.sleepStartTime.hour < 10) "0" else "") + viewModel.sleepStartTime.hour.toString() + ":" + (if (viewModel.sleepStartTime.minute < 10) "0" else "") + viewModel.sleepStartTime.minute.toString())
            viewModel.sleepEndValue.set((if (viewModel.sleepEndTime.hour < 10) "0" else "") + viewModel.sleepEndTime.hour.toString() + ":" + (if (viewModel.sleepEndTime.minute < 10) "0" else "") + viewModel.sleepEndTime.minute.toString())
        }

    }


}
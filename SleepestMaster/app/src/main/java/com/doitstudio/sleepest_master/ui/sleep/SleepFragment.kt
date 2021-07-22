package com.doitstudio.sleepest_master.ui.sleep

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.databinding.FragmentSleepBinding
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.util.SleepTimeValidationUtil
import com.kevalpatel.ringtonepicker.RingtonePickerDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
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
        viewModel.transitionsContainerTop = (binding.topLayout)
        viewModel.imageMoonView = binding.animHeaderLogo
        viewModel.animatedTopView = binding.animatedTopView
        binding.sleepViewModel = viewModel

        val minData = SleepTimeValidationUtil.createMinutePickerHelper()
        binding.npMinutes.minValue = 1;
        binding.npMinutes.maxValue = minData.size;
        binding.npMinutes.displayedValues = minData;

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.npHours.setOnValueChangedListener { picker, oldVal, newVal -> viewModel.onDurationChange(
            newVal,
            binding.npMinutes.value
        )
        }

        binding.npMinutes.setOnValueChangedListener { picker, oldVal, newVal -> viewModel.onDurationChange(
            binding.npHours.value,
            newVal
        )
        }

        // Used to update the sleep end and start time if it changes from the alarms fragments
        dataStoreRepository.sleepParameterFlow.asLiveData().observe(viewLifecycleOwner){

            viewModel.sleepStartTime = LocalTime.ofSecondOfDay(it.sleepTimeStart.toLong())
            viewModel.sleepEndTime = LocalTime.ofSecondOfDay(it.sleepTimeEnd.toLong())

            val sleepDuration = LocalTime.ofSecondOfDay(it.normalSleepTime.toLong())
            binding.npHours.value = sleepDuration.hour
            binding.npMinutes.value = (sleepDuration.minute / 15) + 1
            viewModel.sleepDuration = sleepDuration.toSecondOfDay()

            viewModel.sleepStartValue.set((if (viewModel.sleepStartTime.hour < 10) "0" else "") + viewModel.sleepStartTime.hour.toString() + ":" + (if (viewModel.sleepStartTime.minute < 10) "0" else "") + viewModel.sleepStartTime.minute.toString())
            viewModel.sleepEndValue.set((if (viewModel.sleepEndTime.hour < 10) "0" else "") + viewModel.sleepEndTime.hour.toString() + ":" + (if (viewModel.sleepEndTime.minute < 10) "0" else "") + viewModel.sleepEndTime.minute.toString())
        }

    }


}
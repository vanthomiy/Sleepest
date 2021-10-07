package com.sleepestapp.sleepest.ui.sleep

import android.content.Context
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.databinding.FragmentSleepBinding
import com.sleepestapp.sleepest.googleapi.ActivityTransitionHandler
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil.is24HourFormat
import com.sleepestapp.sleepest.util.StringUtil
import java.time.LocalTime


class SleepFragment : Fragment() {

    var factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            // Workaround because we know that we can cast to T
            return  SleepViewModel(
                (actualContext as MainApplication).dataStoreRepository,
                (actualContext as MainApplication).dataBaseRepository
            ) as T
        }
    }

    /**
     * View model of the [SleepFragment]
     */
    private val viewModel:SleepViewModel by lazy { ViewModelProvider(this, factory).get(SleepViewModel::class.java)}

    /**
     * Get actual context
     */
    private val actualContext: Context by lazy { requireActivity().applicationContext }

    private lateinit var binding: FragmentSleepBinding

    companion object {
        fun newInstance() = SleepFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSleepBinding.inflate(inflater, container, false)
        //viewModel.transitionsContainer = (binding.linearAnimationlayout)

        binding.sleepViewModel = viewModel
        binding.lifecycleOwner = this

        val minData = SleepTimeValidationUtil.createMinutePickerHelper()
        binding.npMinutes.minValue = 1
        binding.npMinutes.maxValue = minData.size
        binding.npMinutes.displayedValues = minData

        viewModel.is24HourFormat = is24HourFormat(actualContext)

        viewModel.phonePositionSelections.value = (mutableListOf(
            StringUtil.getStringXml(R.string.sleep_phoneposition_inbed,requireActivity().application),
            StringUtil.getStringXml(R.string.sleep_phoneposition_ontable, requireActivity().application),
            StringUtil.getStringXml(R.string.sleep_phoneposition_auto, requireActivity().application)
        ))

        viewModel.lightConditionSelections.value = (mutableListOf(
            StringUtil.getStringXml(R.string.sleep_lightcondidition_dark,requireActivity().application),
            StringUtil.getStringXml(R.string.sleep_lightcondidition_light, requireActivity().application),
            StringUtil.getStringXml(R.string.sleep_lightcondidition_auto, requireActivity().application)
        ))


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hours changed from the duration changer
        binding.npHours.setOnValueChangedListener { _, _, newVal -> viewModel.onDurationChange(
            newVal,
            binding.npMinutes.value
        )
        }

        // Minutes changed from the duration changer
        binding.npMinutes.setOnValueChangedListener { _, _, newVal -> viewModel.onDurationChange(
            binding.npHours.value,
            newVal
        )
        }

        // Used to update the sleep end and start time if it changes from the alarms fragments
        viewModel.sleepParameterLiveData.observe(viewLifecycleOwner){

            viewModel.sleepStartTime = LocalTime.ofSecondOfDay(it.sleepTimeStart.toLong())
            viewModel.sleepEndTime = LocalTime.ofSecondOfDay(it.sleepTimeEnd.toLong())

            //TODO()
            val sleepDuration = LocalTime.ofSecondOfDay(it.sleepDuration.toLong())
            binding.npHours.value = sleepDuration.hour
            binding.npMinutes.value = (sleepDuration.minute / 15) + 1
            viewModel.sleepDuration = sleepDuration.toSecondOfDay()

            viewModel.sleepStartValue.value = ((if (viewModel.sleepStartTime.hour < 10) "0" else "") + viewModel.sleepStartTime.hour.toString() + ":" + (if (viewModel.sleepStartTime.minute < 10) "0" else "") + viewModel.sleepStartTime.minute.toString())
            viewModel.sleepEndValue.value = ((if (viewModel.sleepEndTime.hour < 10) "0" else "") + viewModel.sleepEndTime.hour.toString() + ":" + (if (viewModel.sleepEndTime.minute < 10) "0" else "") + viewModel.sleepEndTime.minute.toString())
        }

        viewModel.activityTracking.observe(viewLifecycleOwner){
            TransitionManager.beginDelayedTransition(binding.linearAnimationlayout)

            if(it)
                ActivityTransitionHandler(actualContext).startActivityHandler()
            else
                ActivityTransitionHandler(actualContext).stopActivityHandler()
        }

        viewModel.sleepScoreValue.observe(viewLifecycleOwner){

            val score = it.toInt()

            viewModel.sleepScoreText.value = (when {
                score < 60 -> {
                    StringUtil.getStringXml(R.string.sleep_score_text_60, requireActivity().application)
                }
                score < 70 -> {
                    StringUtil.getStringXml(R.string.sleep_score_text_70, requireActivity().application)
                }
                score < 80 -> {
                    StringUtil.getStringXml(R.string.sleep_score_text_80, requireActivity().application)
                }
                score < 90 -> {
                    StringUtil.getStringXml(R.string.sleep_score_text_90, requireActivity().application)
                }
                else -> {
                    StringUtil.getStringXml(R.string.sleep_score_text_100, requireActivity().application)
                }
            })

            viewModel.autoSleepTime.observe(viewLifecycleOwner){
                TransitionManager.beginDelayedTransition(binding.linearAnimationlayout)
            }

            viewModel.actualExpand.observe(viewLifecycleOwner){
                TransitionManager.beginDelayedTransition(binding.linearAnimationlayout)
            }


        }
    }
}
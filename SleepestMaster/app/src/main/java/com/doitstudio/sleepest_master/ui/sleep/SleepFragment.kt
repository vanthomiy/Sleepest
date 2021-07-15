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
import com.kevalpatel.ringtonepicker.RingtonePickerDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.LocalTime


class SleepFragment : Fragment() {


    private val viewModel by lazy { ViewModelProvider(this).get(SleepViewModel::class.java)}
    private lateinit var binding: FragmentSleepBinding
    private val actualContext: Context by lazy {requireActivity().applicationContext}


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

        // Used to update the sleep end and start time if it changes from the alarms fragments
        dataStoreRepository.sleepParameterFlow.asLiveData().observe(requireActivity()){
            viewModel.sleepStartTime = LocalTime.ofSecondOfDay(it.sleepTimeStart.toLong())
            viewModel.sleepEndTime = LocalTime.ofSecondOfDay(it.sleepTimeEnd.toLong())

            viewModel.sleepStartValue.set((if (viewModel.sleepStartTime.hour < 10) "0" else "") + viewModel.sleepStartTime.hour.toString() + ":" + (if (viewModel.sleepStartTime.minute < 10) "0" else "") + viewModel.sleepStartTime.minute.toString())
            viewModel.sleepEndValue.set((if (viewModel.sleepEndTime.hour < 10) "0" else "") + viewModel.sleepEndTime.hour.toString() + ":" + (if (viewModel.sleepEndTime.minute < 10) "0" else "") + viewModel.sleepEndTime.minute.toString())

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    // Function to check and request permission.
    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(actualContext, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), requestCode)
        } else {
            Toast.makeText(actualContext, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

    private val scope: CoroutineScope = MainScope()
    private val dataStoreRepository: DataStoreRepository by lazy {
        (actualContext as MainApplication).dataStoreRepository
    }

    private fun onAlarmSoundChange(view: View){
        //check if audio volume is 0

            val audioManager = actualContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) <= 0) {
                Toast.makeText(actualContext, "Increase volume to hear sounds", Toast.LENGTH_LONG).show()
            }

            var savedRingtoneUri = Uri.parse(dataStoreRepository.getAlarmToneJob())

            if(dataStoreRepository.getAlarmToneJob() == "null") {
                savedRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }


            val ringtonePickerBuilder = RingtonePickerDialog.Builder(actualContext, parentFragmentManager)
                .setTitle("Select your ringtone")
                .displayDefaultRingtone(true)
                .setCurrentRingtoneUri(savedRingtoneUri)
                .setPositiveButtonText("Set")
                .setCancelButtonText("Cancel")
                .setPlaySampleWhileSelection(true)
                .setListener { ringtoneName, ringtoneUri ->  scope.launch{ dataStoreRepository.updateAlarmTone(ringtoneUri.toString()) }}

            ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_ALARM)
            ringtonePickerBuilder.show()

    }
    
}
package com.doitstudio.sleepest_master.ui.sleep

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import com.doitstudio.sleepest_master.databinding.FragmentSleepBinding
import com.kevalpatel.ringtonepicker.RingtonePickerDialog


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


        binding.soundChange.setOnClickListener {
            //your implementation goes here
            onAlarmSoundChange(it)
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

    private fun onAlarmSoundChange(view: View){

        checkPermission(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            101)
        checkPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            102)

        /*
        var soundUri : Uri = Uri.EMPTY

        val ringtonePickerBuilder: RingtonePickerDialog.Builder = RingtonePickerDialog.Builder(
            actualContext,
            childFragmentManager
        ) //Set title of the dialog.
            //If set null, no title will be displayed.
            .setTitle("Select ringtone") //set the currently selected uri, to mark that ringtone as checked by default.
            //If no ringtone is currently selected, pass null.
            .setCurrentRingtoneUri(soundUri) //Set true to allow allow user to select default ringtone set in phone settings.
            .displayDefaultRingtone(true) //Set true to allow user to select silent (i.e. No ringtone.).
            .displaySilentRingtone(true) //set the text to display of the positive (ok) button.
            //If not set OK will be the default text.
            .setPositiveButtonText("SET RINGTONE") //set text to display as negative button.
            //If set null, negative button will not be displayed.
            .setCancelButtonText("CANCEL") //Set flag true if you want to play the sample of the clicked tone.
            .setPlaySampleWhileSelection(true) //Set the callback listener.
            .setListener { ringtoneName, ringtoneUri ->
                TODO("Not yet implemented")

            }

//Add the desirable ringtone types.

//Add the desirable ringtone types.
        ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_MUSIC)
        ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_NOTIFICATION)
        ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_RINGTONE)
        ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_ALARM)

//Display the dialog.

//Display the dialog.
        ringtonePickerBuilder.show()*/
    }



}
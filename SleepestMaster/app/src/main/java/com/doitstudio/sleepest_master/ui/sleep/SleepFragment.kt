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

    }



}
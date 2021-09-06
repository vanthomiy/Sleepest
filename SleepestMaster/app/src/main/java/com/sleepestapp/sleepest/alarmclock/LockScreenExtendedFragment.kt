package com.sleepestapp.sleepest.alarmclock


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sleepestapp.sleepest.databinding.ActivityLockScreenAlarmExtendedBinding
import java.io.*


class LockScreenExtendedFragment : Fragment() {

    private lateinit var binding: ActivityLockScreenAlarmExtendedBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = ActivityLockScreenAlarmExtendedBinding.inflate(inflater, container, false)

        return binding.root

    }
}



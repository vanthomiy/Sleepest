package com.doitstudio.sleepest_master.ui.sleep

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.databinding.FragmentSleepBinding

class SleepFragment : Fragment() {


    private lateinit var viewModel: SleepViewModel
    private lateinit var binding: FragmentSleepBinding


    companion object {
        fun newInstance() = SleepFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSleepBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(SleepViewModel::class.java)

        binding.sleepViewModel = viewModel

        return binding.root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(SleepViewModel::class.java)
        // TODO: Use the ViewModel

    }
}
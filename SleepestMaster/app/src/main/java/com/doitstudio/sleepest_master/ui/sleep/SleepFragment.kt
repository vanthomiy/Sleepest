package com.doitstudio.sleepest_master.ui.sleep

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.SleepViewModel

class SleepFragment : Fragment() {

    companion object {
        fun newInstance() = SleepFragment()
    }

    private lateinit var viewModel: SleepViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sleep, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SleepViewModel::class.java)
        // TODO: Use the ViewModel
    }
}
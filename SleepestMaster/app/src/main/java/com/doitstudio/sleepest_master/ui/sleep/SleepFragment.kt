package com.doitstudio.sleepest_master.ui.sleep

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.doitstudio.sleepest_master.databinding.FragmentSleepBinding
import com.doitstudio.sleepest_master.ui.charts.SparkLineStyle
import com.github.mikephil.charting.data.LineData
import javax.inject.Inject

class SleepFragment : Fragment() {


    private val viewModel by lazy { ViewModelProvider(this).get(SleepViewModel::class.java)}
    private lateinit var binding: FragmentSleepBinding

    @Inject
    lateinit var chartStyle: SparkLineStyle

    //val chartStyle by lazy { context?.let { SparkLineStyle(it) } }

    companion object {
        fun newInstance() = SleepFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSleepBinding.inflate(inflater, container, false)
        viewModel.transitionsContainer = (binding.linearAnimationlayout)
        viewModel.transitionsContainerAll = (binding.transistionAll)
        viewModel.animatedTopView = binding.animatedTopView

        binding.sleepViewModel = viewModel

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


}
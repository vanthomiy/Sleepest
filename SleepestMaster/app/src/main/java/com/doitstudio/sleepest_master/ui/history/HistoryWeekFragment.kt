package com.doitstudio.sleepest_master.ui.history

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.doitstudio.sleepest_master.databinding.FragmentHistoryWeekBinding
import java.time.*

class HistoryWeekFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java) }
    private lateinit var binding: FragmentHistoryWeekBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHistoryWeekBinding.inflate(inflater, container, false)
        binding.historyWeekViewModel = viewModel

        val barChart = viewModel.setBarChart(7, getSundayOfWeek())
        binding.lLSleepAnalysisChartsWeek.addView(barChart)
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350F, resources.displayMetrics)
        barChart.layoutParams.height = height.toInt()
        barChart.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        barChart.invalidate()

        return binding.root
    }

    private fun getSundayOfWeek(): LocalDate {
        val dayOfWeek = viewModel.analysisDate.dayOfWeek

        return when (dayOfWeek.value) {
            1 -> viewModel.analysisDate.plusDays(6L) // Monday
            2 -> viewModel.analysisDate.plusDays(5L) // Tuesday
            3 -> viewModel.analysisDate.plusDays(4L) // Wednesday
            4 -> viewModel.analysisDate.plusDays(3L) // Thursday
            5 -> viewModel.analysisDate.plusDays(2L) // Friday
            6 -> viewModel.analysisDate.plusDays(1L) // Saturday
            else -> viewModel.analysisDate.plusDays(0L) // Sunday
        }
    }
}
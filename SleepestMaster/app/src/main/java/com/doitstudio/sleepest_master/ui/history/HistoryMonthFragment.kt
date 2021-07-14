package com.doitstudio.sleepest_master.ui.history

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.doitstudio.sleepest_master.databinding.FragmentHistoryMonthBinding
import java.time.LocalDate

class HistoryMonthFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java) }
    private lateinit var binding: FragmentHistoryMonthBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHistoryMonthBinding.inflate(inflater, container, false)
        binding.historyMonthViewModel = viewModel

        val barChartDates = getEndOfMonth()
        val barChart = viewModel.setBarChart(barChartDates.first, barChartDates.second)
        binding.lLSleepAnalysisChartsMonth.addView(barChart)
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350F, resources.displayMetrics)
        barChart.layoutParams.height = height.toInt()
        barChart.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        barChart.invalidate()

        return binding.root
    }

    private fun getEndOfMonth(): Pair<Int, LocalDate> {
        val date = viewModel.analysisDate
        return Pair(
            date.lengthOfMonth(),
            date.withDayOfMonth(date.lengthOfMonth())
        )
    }
}
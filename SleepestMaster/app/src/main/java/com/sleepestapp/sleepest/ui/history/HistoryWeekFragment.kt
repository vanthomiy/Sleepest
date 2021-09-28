package com.sleepestapp.sleepest.ui.history

import android.content.Context
import android.os.Bundle
import android.transition.TransitionManager
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.Observable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sleepestapp.sleepest.databinding.FragmentHistoryWeekBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import java.time.*

class HistoryWeekFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java) }

    private val viewModelWeek by lazy { ViewModelProvider(this).get(HistoryWeekViewModel::class.java) }

    private val actualContext: Context by lazy { requireActivity().applicationContext }

    private lateinit var binding: FragmentHistoryWeekBinding

    /** Contains the BarChart for the [HistoryWeekFragment]. */
    private lateinit var barChart: BarChart

    /** Contains the LineChart for the [HistoryWeekFragment]. */
    private lateinit var activityChart: LineChart


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHistoryWeekBinding.inflate(inflater, container, false)
        binding.historyWeekViewModel = viewModelWeek

        barChart = viewModel.setBarChart(BarChart(actualContext), 7, getSundayOfWeek())
        activityChart = viewModel.setActivityChart(LineChart(context), 7, getSundayOfWeek())

        binding.lLSleepAnalysisChartsWeekSleepPhases.addView(barChart)
        binding.lLActivityAnalysisChartWeek.addView(activityChart)

        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350F, resources.displayMetrics)
        barChart.layoutParams.height = height.toInt()
        barChart.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        barChart.invalidate()

        activityChart.layoutParams.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200F, resources.displayMetrics).toInt()
        activityChart.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        activityChart.invalidate()

        viewModel.analysisDate.observe(viewLifecycleOwner) {
            viewModel.updateBarChart(barChart, 7, getSundayOfWeek())
            barChart.invalidate()

            viewModel.updateActivityChart(activityChart, 7, getSundayOfWeek())
            activityChart.invalidate()
        }

        viewModelWeek.actualExpand.observe(viewLifecycleOwner) {
            TransitionManager.beginDelayedTransition(binding.lLLinearAnimationLayoutWeeklyAnalysis)
        }

        return binding.root
    }

    /** Used to find the sunday of the current weak. */
    private fun getSundayOfWeek(): LocalDate {
        viewModel.analysisDate.value?.let {
            val dayOfWeek = it.dayOfWeek

            return when (dayOfWeek.value) {
                1 -> it.plusDays(6L) // Monday
                2 -> it.plusDays(5L) // Tuesday
                3 -> it.plusDays(4L) // Wednesday
                4 -> it.plusDays(3L) // Thursday
                5 -> it.plusDays(2L) // Friday
                6 -> it.plusDays(1L) // Saturday
                else -> it.plusDays(0L) // Sunday
            }
        }

        return LocalDate.of(2000, 1, 1)
    }
}
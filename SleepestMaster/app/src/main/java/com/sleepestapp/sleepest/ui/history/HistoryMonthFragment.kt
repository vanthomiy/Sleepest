package com.sleepestapp.sleepest.ui.history

import android.content.Context
import android.os.Bundle
import android.transition.TransitionManager
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sleepestapp.sleepest.databinding.FragmentHistoryMonthBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.sleepestapp.sleepest.util.DesignUtil
import java.time.LocalDate

class HistoryMonthFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java) }

    private val viewModelMonth by lazy { ViewModelProvider(this).get(HistoryMonthViewModel::class.java) }

    private val actualContext: Context by lazy { requireActivity().applicationContext }

    private lateinit var binding: FragmentHistoryMonthBinding
    private lateinit var barChart: BarChart
    private lateinit var barChartDates :  Pair<Int, LocalDate>
    private lateinit var activityChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHistoryMonthBinding.inflate(inflater, container, false)
        binding.historyMonthViewModel = viewModelMonth
        binding.lifecycleOwner = this

        barChartDates = getEndOfMonth()
        barChart = viewModel.setBarChart(
            BarChart(actualContext),
            barChartDates.first,
            barChartDates.second,
            DesignUtil.colorDarkMode(DesignUtil.checkDarkModeActive(actualContext))
        )
        activityChart = viewModel.setActivityChart(
            LineChart(context),
            barChartDates.first,
            barChartDates.second,
            DesignUtil.colorDarkMode(DesignUtil.checkDarkModeActive(actualContext))
        )

        binding.lLSleepAnalysisChartsMonthSleepPhases.addView(barChart)
        binding.lLActivityAnalysisChartMonth.addView(activityChart)

        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200F, resources.displayMetrics)
        barChart.layoutParams.height = height.toInt()
        barChart.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        barChart.invalidate()

        activityChart.layoutParams.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150F, resources.displayMetrics).toInt()
        activityChart.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        activityChart.invalidate()

        viewModel.analysisDate.observe(viewLifecycleOwner) {
            barChartDates = getEndOfMonth()

            viewModel.updateBarChart(
                barChart,
                barChartDates.first,
                barChartDates.second,
                DesignUtil.colorDarkMode(DesignUtil.checkDarkModeActive(actualContext)
                )
            )

            barChart.invalidate()

            viewModel.updateActivityChart(
                activityChart,
                barChartDates.first,
                barChartDates.second,
                DesignUtil.colorDarkMode(DesignUtil.checkDarkModeActive(actualContext)
                )
            )

            activityChart.invalidate()
        }

        viewModelMonth.actualExpand.observe(viewLifecycleOwner) {
            TransitionManager.beginDelayedTransition(binding.lLLinearAnimationLayoutMonthlyAnalysis)
        }

        viewModel.visibilityManagerMonthDiagrams.observe(viewLifecycleOwner) {
            viewModel.visibilityManagerMonthDiagrams.value?.let { visibility ->
                maintainVisibilityMonthHistory(visibility)
            }
        }

        return binding.root
    }

    /**
     * Returns the length and the end of the current month.
     */
    private fun getEndOfMonth(): Pair<Int, LocalDate> {
        viewModel.analysisDate.value?.let {
            val date = it
            return Pair(
                date.lengthOfMonth(),
                date.withDayOfMonth(date.lengthOfMonth())
            )
        }

        return Pair(10, LocalDate.of(2000, 1, 1))
    }

    /**
     * Maintains the visibility of the diagrams in the day fragment.
     */
    private fun maintainVisibilityMonthHistory(
        setVisibility: Boolean
    ) {
        if (setVisibility) {
            binding.iVNoDataAvailable.visibility = View.GONE
            binding.tVNoDataAvailable.visibility = View.GONE
            binding.sVSleepAnalysisChartsMonth.visibility = View.VISIBLE
        }
        else {
            binding.sVSleepAnalysisChartsMonth.visibility = View.GONE
            binding.iVNoDataAvailable.visibility = View.VISIBLE
            binding.tVNoDataAvailable.visibility = View.VISIBLE
        }
    }
}
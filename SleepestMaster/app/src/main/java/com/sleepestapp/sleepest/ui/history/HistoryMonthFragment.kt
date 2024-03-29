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

    /**
     * Contains the BarChart for the [HistoryMonthFragment] sleep phases analysis.
     */
    private lateinit var barChart: BarChart

    /**
     * Contains the dates of the current analysis month.
     */
    private lateinit var barChartDates :  Pair<Int, LocalDate>

    /**
     * Contains the LineChart for the [HistoryMonthFragment] activity analysis.
     */
    private lateinit var activityChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200F, resources.displayMetrics)
        binding = FragmentHistoryMonthBinding.inflate(inflater, container, false)
        binding.historyMonthViewModel = viewModelMonth
        binding.lifecycleOwner = this

        // Initial set up for the monthly sleep analysis bar chart.
        barChartDates = getEndOfMonth()
        barChart = viewModel.setBarChart(
            BarChart(actualContext),
            barChartDates.first,
            barChartDates.second,
            DesignUtil.colorDarkMode(
                DesignUtil.checkDarkModeActive(
                    actualContext,
                    viewModel.appSettingsDarkMode,
                    viewModel.appAutoDarkMode
                )
            )
        )

        binding.lLSleepAnalysisChartsMonthSleepPhases.addView(barChart)
        barChart.layoutParams.height = height.toInt()
        barChart.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT

        // Initial set up for the monthly activity analysis bar chart.
        activityChart = viewModel.setActivityChart(
            LineChart(context),
            barChartDates.first,
            barChartDates.second,
            DesignUtil.colorDarkMode(
                DesignUtil.checkDarkModeActive(
                    actualContext,
                    viewModel.appSettingsDarkMode,
                    viewModel.appAutoDarkMode
                )
            )
        )

        binding.lLActivityAnalysisChartMonth.addView(activityChart)
        activityChart.layoutParams.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150F, resources.displayMetrics).toInt()
        activityChart.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT

        barChart.invalidate()
        activityChart.invalidate()

        // Listener for changes in the analysis date.
        viewModel.analysisDate.observe(viewLifecycleOwner) {
            updateCharts()
        }

        // Listener for the actual information button which was selected.
        viewModelMonth.actualExpand.observe(viewLifecycleOwner) {
            TransitionManager.beginDelayedTransition(binding.lLLinearAnimationLayoutMonthlyAnalysis)
        }

        // Listener for changes of the visibility manager
        viewModel.visibilityManagerMonthDiagrams.observe(viewLifecycleOwner) {
            viewModel.visibilityManagerMonthDiagrams.value?.let { visibility ->
                maintainVisibilityMonthHistory(visibility)
            }
        }

        return binding.root
    }

    /**
     * Calls all update functions for the charts in this fragment.
     */
    private fun updateCharts() {
        barChartDates = getEndOfMonth()

        viewModel.updateBarChart(
            barChart,
            barChartDates.first,
            barChartDates.second,
            DesignUtil.colorDarkMode(
                DesignUtil.checkDarkModeActive(
                    actualContext,
                    viewModel.appSettingsDarkMode,
                    viewModel.appAutoDarkMode
                )
            )
        )

        barChart.invalidate()

        viewModel.updateActivityChart(
            activityChart,
            barChartDates.first,
            barChartDates.second,
            DesignUtil.colorDarkMode(
                DesignUtil.checkDarkModeActive(
                    actualContext,
                    viewModel.appSettingsDarkMode,
                    viewModel.appAutoDarkMode
                )
            )
        )

        activityChart.invalidate()
    }

    /**
     * Determines the [LocalDate] of the last day of the passed days month.
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
     * Maintains the visibility settings of the monthly sleep analysis.
     * If no data is to be shown, the diagrams disappear and an information will appear.
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
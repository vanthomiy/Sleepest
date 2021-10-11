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
import com.sleepestapp.sleepest.databinding.FragmentHistoryWeekBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.sleepestapp.sleepest.util.DesignUtil
import java.time.*

class HistoryWeekFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java) }

    private val viewModelWeek by lazy { ViewModelProvider(this).get(HistoryWeekViewModel::class.java) }

    private val actualContext: Context by lazy { requireActivity().applicationContext }

    private lateinit var binding: FragmentHistoryWeekBinding

    /**
     * Contains the BarChart for the [HistoryWeekFragment].
     */
    private lateinit var barChart: BarChart

    /**
     * Contains the LineChart for the [HistoryWeekFragment].
     */
    private lateinit var activityChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200F, resources.displayMetrics)
        binding = FragmentHistoryWeekBinding.inflate(inflater, container, false)
        binding.historyWeekViewModel = viewModelWeek
        binding.lifecycleOwner = this

        // Initial set up for the weekly sleep analysis bar chart.
        barChart = viewModel.setBarChart(
            BarChart(actualContext),
            7,
            getSundayOfWeek(),
            DesignUtil.colorDarkMode(
                DesignUtil.checkDarkModeActive(
                    actualContext,
                    viewModel.appSettingsDarkMode,
                    viewModel.appAutoDarkMode
                )
            )
        )

        binding.lLSleepAnalysisChartsWeekSleepPhases.addView(barChart)
        barChart.layoutParams.height = height.toInt()
        barChart.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT

        // Initial set up for the weekly activity analysis bar chart.
        activityChart = viewModel.setActivityChart(
            LineChart(context),
            7,
            getSundayOfWeek(),
            DesignUtil.colorDarkMode(
                DesignUtil.checkDarkModeActive(
                    actualContext,
                    viewModel.appSettingsDarkMode,
                    viewModel.appAutoDarkMode
                )
            )
        )

        binding.lLActivityAnalysisChartWeek.addView(activityChart)
        activityChart.layoutParams.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150F, resources.displayMetrics).toInt()
        activityChart.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT

        barChart.invalidate()
        activityChart.invalidate()

        // Listener for changes in the analysis date.
        viewModel.analysisDate.observe(viewLifecycleOwner) {
            updateCharts()
        }

        // Listener for the actual information button which was selected.
        viewModelWeek.actualExpand.observe(viewLifecycleOwner) {
            TransitionManager.beginDelayedTransition(binding.lLLinearAnimationLayoutWeeklyAnalysis)
        }

        // Listener for changes of the visibility manager
        viewModel.visibilityManagerWeekDiagrams.observe(viewLifecycleOwner) {
            viewModel.visibilityManagerWeekDiagrams.value?.let { visibility ->
                maintainVisibilityWeekHistory(visibility)
            }
        }

        return binding.root
    }

    /**
     * Calls all update functions for the charts in this fragment.
     */
    private fun updateCharts() {
        viewModel.updateBarChart(
            barChart,
            7,
            getSundayOfWeek(),
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
            7,
            getSundayOfWeek(),
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
     * Determines the [LocalDate] of the Sunday of the current calendar week.
     */
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

    /**
     * Maintains the visibility settings of the weekly sleep analysis.
     * If no data is to be shown, the diagrams disappear and an information will appear.
     */
    private fun maintainVisibilityWeekHistory(
        setVisibility: Boolean
    ) {
        if (setVisibility) {
            binding.iVNoDataAvailable.visibility = View.GONE
            binding.tVNoDataAvailable.visibility = View.GONE
            binding.sVSleepAnalysisChartsWeek.visibility = View.VISIBLE
        }
        else {
            binding.sVSleepAnalysisChartsWeek.visibility = View.GONE
            binding.iVNoDataAvailable.visibility = View.VISIBLE
            binding.tVNoDataAvailable.visibility = View.VISIBLE
        }
    }
}
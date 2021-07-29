package com.doitstudio.sleepest_master.ui.history

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.Observable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.doitstudio.sleepest_master.databinding.FragmentHistoryWeekBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import java.time.*

class HistoryWeekFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java) }
    private lateinit var binding: FragmentHistoryWeekBinding
    private lateinit var barChart: BarChart
    private lateinit var activityChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHistoryWeekBinding.inflate(inflater, container, false)
        binding.historyWeekViewModel = viewModel

        barChart = viewModel.setBarChart(7, getSundayOfWeek())
        activityChart = viewModel.setActivityChart(7, getSundayOfWeek())

        binding.lLSleepAnalysisChartsWeek.addView(barChart)
        binding.lLSleepAnalysisChartsWeek.addView(activityChart)

        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350F, resources.displayMetrics)
        barChart.layoutParams.height = height.toInt()
        barChart.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        barChart.invalidate()

        activityChart.layoutParams.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200F, resources.displayMetrics).toInt()
        activityChart.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        activityChart.invalidate()

        viewModel.analysisDate.addOnPropertyChangedCallback(
            object: Observable.OnPropertyChangedCallback() {

                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {

                    viewModel.updateBarChart(barChart, 7, getSundayOfWeek())
                    barChart.invalidate()

                    viewModel.updateActivityChart(activityChart, 7, getSundayOfWeek())
                    activityChart.invalidate()
                }
            }
        )

        return binding.root
    }

    private fun getSundayOfWeek(): LocalDate {
        viewModel.analysisDate.get()?.let {
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
package com.sleepestapp.sleepest.ui.history

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.Observable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sleepestapp.sleepest.databinding.FragmentHistoryMonthBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
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
        viewModelMonth.transitionsContainer = (binding.lLLinearAnimationLayoutMonthlyAnalysis)

        barChartDates = getEndOfMonth()
        barChart = viewModel.setBarChart(BarChart(actualContext), barChartDates.first, barChartDates.second)
        activityChart = viewModel.setActivityChart(LineChart(context), barChartDates.first, barChartDates.second)

        binding.lLSleepAnalysisChartsMonthSleepPhases.addView(barChart)
        binding.lLActivityAnalysisChartMonth.addView(activityChart)

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

                    barChartDates = getEndOfMonth()
                    viewModel.updateBarChart(barChart, barChartDates.first, barChartDates.second)
                    barChart.invalidate()

                    viewModel.updateActivityChart(activityChart, barChartDates.first, barChartDates.second)
                    activityChart.invalidate()
                }
            }
        )

        return binding.root
    }

    private fun getEndOfMonth(): Pair<Int, LocalDate> {
        viewModel.analysisDate.get()?.let {
            val date = it
            return Pair(
                date.lengthOfMonth(),
                date.withDayOfMonth(date.lengthOfMonth())
            )
        }

        return Pair(10, LocalDate.of(2000, 1, 1))
    }
}
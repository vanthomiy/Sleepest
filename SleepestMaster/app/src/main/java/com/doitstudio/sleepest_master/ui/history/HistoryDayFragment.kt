package com.doitstudio.sleepest_master.ui.history

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.Observable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.databinding.FragmentHistoryDayBinding
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.lang.Math.round
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class HistoryDayFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java) }
    private lateinit var binding: FragmentHistoryDayBinding
    private lateinit var sleepValues : Triple<List<SleepApiRawDataEntity>, Int, UserSleepSessionEntity>
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHistoryDayBinding.inflate(inflater, container, false)
        binding.historyDayViewModel = viewModel


        lineChart = setLineChart()
        updateLineChart(lineChart)
        binding.lLSleepAnalysisChartsDay.addView(lineChart)
        lineChart.layoutParams.height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 200F, resources.displayMetrics
        ).toInt()
        lineChart.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        lineChart.invalidate()

        pieChart = setPieChart()
        binding.lLSleepAnalysisChartsDay.addView(pieChart)
        pieChart.layoutParams.height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 200F, resources.displayMetrics
        ).toInt()
        pieChart.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        pieChart.invalidate()


        viewModel.analysisDate.addOnPropertyChangedCallback(
            object: Observable.OnPropertyChangedCallback() {

                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    getDataValues()
                    updateLineChart(lineChart)
                    lineChart.invalidate()

                    updatePieChart(pieChart)
                    pieChart.invalidate()
                }
            })

        getDataValues()

        return binding.root
    }

    private fun getDataValues() {
        viewModel.analysisDate.get()?.let {
            if (viewModel.checkId(it)) {
                sleepValues = viewModel.sleepSessionData[UserSleepSessionEntity.getIdByDateTime(it)]!!
            }
        }
    }

    private fun generateDataLineChart() : ArrayList<Entry> {
        val entries = ArrayList<Entry>()

        viewModel.analysisDate.get()?.let {
            if (viewModel.checkId(it)) {
                var xValue = 0

                for (rawData in sleepValues.first) {
                    for (minute in 0..((sleepValues.second / 60).toDouble()).roundToInt()) {
                        entries.add(Entry(xValue.toFloat(), rawData.sleepState.ordinal.toFloat()))
                        xValue += 1
                    }
                }
            } else {
                entries.add(Entry(0F,0F))
            }
        }

        return entries
    }

    private fun setLineChart() : LineChart {
        val chart = LineChart(context)
        val lineDataSet = LineDataSet(generateDataLineChart(), "Sleep state")
        visualSetUpLineChart(chart, lineDataSet)
        chart.data = LineData(lineDataSet)
        return chart
    }

    fun updateLineChart(chart: LineChart) {
        val lineDataSet = LineDataSet(generateDataLineChart(), "Sleep state")
        visualSetUpLineChart(chart, lineDataSet)
        chart.data = LineData(lineDataSet)
    }

    private fun visualSetUpLineChart(chart: LineChart, lineDataSet: LineDataSet) {
        lineDataSet.setDrawValues(false)
        lineDataSet.setDrawFilled(true)
        lineDataSet.setDrawCircles(false)
        lineDataSet.lineWidth = 2f
        lineDataSet.fillColor = ContextCompat.getColor(viewModel.context, R.color.sleep_sleep_color)
        lineDataSet.fillAlpha = 255
        lineDataSet.color = ContextCompat.getColor(viewModel.context, R.color.awake_sleep_color)

        val yAxisValues = ArrayList<String>()
        yAxisValues.add("Awake")
        yAxisValues.add("Light")
        yAxisValues.add("Deep")
        yAxisValues.add("REM")
        //yAxisValues.add("Zero")

        chart.axisLeft.valueFormatter = IndexAxisValueFormatter(yAxisValues)
        //chart.axisLeft.labelCount = 4
        chart.axisLeft.labelCount = 3
        chart.axisLeft.axisMinimum = 0f
        //chart.axisLeft.axisMaximum = 4f
        chart.axisLeft.axisMaximum = 3f
        chart.axisLeft.setDrawGridLines(false)
        chart.axisLeft.textColor = viewModel.checkDarkMode()
        chart.legend.textColor = viewModel.checkDarkMode()

        chart.axisRight.setDrawLabels(false)
        chart.axisRight.setDrawGridLines(false)

        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.setDrawLabels(false)
        chart.xAxis.textColor = viewModel.checkDarkMode()

        chart.description.isEnabled = false

        chart.animateX(1000)
    }

    private fun generateDataPieChart() : ArrayList<PieEntry> {
        val entries = ArrayList<PieEntry>()

        viewModel.analysisDate.get()?.let {
            if (viewModel.checkId(it)) {
                val awake = sleepValues.third.sleepTimes.awakeTime
                val sleep = sleepValues.third.sleepTimes.sleepDuration
                val lightSleep = sleepValues.third.sleepTimes.lightSleepDuration
                val deepSleep = sleepValues.third.sleepTimes.deepSleepDuration

                if (lightSleep == 0 && deepSleep == 0) {
                    entries.add(PieEntry(awake.toFloat(), "Awake"))
                    entries.add(PieEntry(sleep.toFloat(), "Sleep"))
                }
                else {
                    entries.add(PieEntry(lightSleep.toFloat(), "Light"))
                    entries.add(PieEntry(deepSleep.toFloat(), "Deep"))
                    entries.add(PieEntry(awake.toFloat(), "Awake"))
                }
            }
        }

        return entries
    }

    private fun setPieChart() : PieChart {
        val chart = PieChart(context)
        val pieDataSet = PieDataSet(generateDataPieChart(), "")
        visualSetUpPieChart(chart, pieDataSet)
        chart.data = PieData(pieDataSet)
        return chart
    }

    private fun updatePieChart(chart: PieChart) {
        val pieDataSet = PieDataSet(generateDataPieChart(), "")
        visualSetUpPieChart(chart, pieDataSet)
        chart.data = PieData(pieDataSet)
    }

    private fun visualSetUpPieChart(chart: PieChart, pieDataSet: PieDataSet) {
        val listColors = ArrayList<Int>()
        if (pieDataSet.entryCount == 2) {
            listColors.add(ContextCompat.getColor(viewModel.context, R.color.awake_sleep_color))
            listColors.add(ContextCompat.getColor(viewModel.context, R.color.sleep_sleep_color))
        }
        else {
            listColors.add(ContextCompat.getColor(viewModel.context, R.color.light_sleep_color))
            listColors.add(ContextCompat.getColor(viewModel.context, R.color.deep_sleep_color))
            listColors.add(ContextCompat.getColor(viewModel.context, R.color.awake_sleep_color))
        }

        pieDataSet.colors = listColors
        pieDataSet.setDrawValues(false)
        pieDataSet.label

        chart.setCenterTextColor(Color.WHITE)
        chart.setHoleColor(Color.BLACK)
        chart.setEntryLabelColor(Color.WHITE)

        chart.isDrawHoleEnabled = true
        chart.description.isEnabled = false
        chart.legend.textColor = viewModel.checkDarkMode()
        chart.animateY(1000, Easing.EaseInOutQuad)
    }
}


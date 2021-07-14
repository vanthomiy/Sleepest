package com.doitstudio.sleepest_master.ui.history

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class HistoryDayFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java) }
    private lateinit var binding: FragmentHistoryDayBinding
    private lateinit var sleepValues : Triple<List<SleepApiRawDataEntity>, Int, UserSleepSessionEntity>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHistoryDayBinding.inflate(inflater, container, false)
        binding.historyDayViewModel = viewModel

        getDataValues()

        val lineChart = setLineChart()
        binding.lLSleepAnalysisChartsDay.addView(lineChart)
        val heightLineChart = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350F, resources.displayMetrics)
        lineChart.layoutParams.height = heightLineChart.toInt()
        lineChart.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        lineChart.invalidate()

        val pieChart = setPieChart()
        binding.lLSleepAnalysisChartsDay.addView(pieChart)
        val heightPieChart = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350F, resources.displayMetrics)
        pieChart.layoutParams.height = heightPieChart.toInt()
        pieChart.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        pieChart.invalidate()

        return binding.root
    }

    private fun getDataValues() {
        if (viewModel.checkId(viewModel.analysisDate)) {
            sleepValues = viewModel.sleepSessionData[UserSleepSessionEntity.getIdByDateTime(viewModel.analysisDate)]!!
        }
    }

    private fun generateDataLineChart() : ArrayList<Entry> {
        val entries = ArrayList<Entry>()

        if (viewModel.checkId(viewModel.analysisDate)) {
            var xValue = 0

            for (rawData in sleepValues.first) {
                for (minute in 0..sleepValues.second) {
                    entries.add(Entry(xValue.toFloat(), rawData.sleepState.ordinal.toFloat()))
                    xValue += 1
                }
            }
        } else {
            entries.add(Entry(1F,1F))
        }

        return entries
    }

    /**
     * Sets the line chart. Calls generateDataLineChart for diagram data.
     */
    private fun setLineChart() : LineChart {
        val chart = LineChart(context)
        val vl = LineDataSet(generateDataLineChart(), "Sleep state")
        vl.setDrawValues(false)
        vl.setDrawFilled(true)
        vl.setDrawCircles(false)
        vl.lineWidth = 2f
        vl.fillColor = R.color.tertiary_text_color
        vl.fillAlpha = 255
        vl.color = R.color.colorPrimary

        val yAxisValues = ArrayList<String>()
        yAxisValues.add("Awake")
        yAxisValues.add("Light")
        yAxisValues.add("Deep")
        yAxisValues.add("REM")
        yAxisValues.add("Zero")

        chart.axisLeft.valueFormatter = IndexAxisValueFormatter(yAxisValues)
        chart.axisLeft.labelCount = 4
        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.axisMaximum = 4f

        chart.axisRight.setDrawLabels(false)
        chart.axisRight.setDrawGridLines(false)

        chart.description.isEnabled = false

        chart.data = LineData(vl)

        chart.animateX(1000)

        return chart
    }

    private fun generateDataPieChart() : ArrayList<PieEntry> {
        val entries = ArrayList<PieEntry>()

        if (viewModel.checkId(viewModel.analysisDate)) {
            val awake = sleepValues.third.sleepTimes.awakeTime
            val sleep = sleepValues.third.sleepTimes.sleepDuration
            val lightSleep = sleepValues.third.sleepTimes.lightSleepDuration
            val deepSleep = sleepValues.third.sleepTimes.deepSleepDuration

            if (lightSleep == 0 && deepSleep == 0) {
                entries.add(PieEntry(awake.toFloat(), "Awake"))
                entries.add(PieEntry(sleep.toFloat(), "Sleep"))
                entries.add(PieEntry(0.toFloat(), "Light"))
                entries.add(PieEntry(0.toFloat(), "Deep"))
            }
            else {
                entries.add(PieEntry(awake.toFloat(), "Awake"))
                entries.add(PieEntry(0.toFloat(), "Sleep"))
                entries.add(PieEntry(lightSleep.toFloat(), "Light"))
                entries.add(PieEntry(deepSleep.toFloat(), "Deep"))
            }
        }

        return entries
    }

    /**
     * Sets the pie chart. Calls generateDataPieChart for diagram data.
     */
    private fun setPieChart() : PieChart {
        val chart = PieChart(context)
        val listColors = ArrayList<Int>()
        listColors.add(R.color.light_sleep_color)
        listColors.add(R.color.deep_sleep_color)
        listColors.add(R.color.awake_sleep_color)
        listColors.add(R.color.sleep_sleep_color)

        val pieDataSet = PieDataSet(generateDataPieChart(), "Sleep states")
        pieDataSet.colors = listColors

        val pieData = PieData(pieDataSet)
        chart.data = pieData

        chart.setUsePercentValues(true)
        chart.isDrawHoleEnabled = false
        chart.description.isEnabled = false
        //chart.setEntryLabelColor(R.color.black)
        chart.animateY(1000, Easing.EaseInOutQuad)

        return chart
    }
}
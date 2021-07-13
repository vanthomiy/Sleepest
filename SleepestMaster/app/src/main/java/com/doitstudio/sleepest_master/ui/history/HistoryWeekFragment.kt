package com.doitstudio.sleepest_master.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.databinding.FragmentHistoryWeekBinding
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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

        setBarChartWeekly()

        return binding.root
    }

    private fun generateDataBarChartWeekly(): Triple<ArrayList<BarEntry>, List<Int>, Int> { //ArrayList<BarEntry> {
        val entries = ArrayList<BarEntry>()
        val xAxisLabels = mutableListOf<Int>()
        var xIndex = 0.5f
        var maxSleepTime = 0

        val ids = mutableSetOf<Int>()
        for (i in -6..0) {
            ids.add(
                UserSleepSessionEntity.getIdByDateTime(
                    LocalDate.of(
                        viewModel.analysisDate.plusDays(i.toLong()).year,
                        viewModel.analysisDate.plusDays(i.toLong()).month,
                        viewModel.analysisDate.plusDays(i.toLong()).dayOfMonth
                    )
                )
            )
        }

        ids.reversed()
        for (id in ids) {
            if (viewModel.checkId(viewModel.analysisDate)) {
                val values = viewModel.sleepSessionData[id]!!

                val awake = values.third.sleepTimes.awakeTime
                val sleep = values.third.sleepTimes.sleepDuration
                val lightSleep = values.third.sleepTimes.lightSleepDuration
                val deepSleep = values.third.sleepTimes.deepSleepDuration

                if ((sleep + awake) > maxSleepTime) { maxSleepTime = (sleep + awake) }  //Later delete awake from here

                if (lightSleep != 0 && deepSleep != 0) {
                    entries.add(
                        BarEntry(
                            xIndex, floatArrayOf(
                                lightSleep.toFloat(),
                                deepSleep.toFloat(),
                                awake.toFloat(),
                                0.toFloat()
                            )
                        )
                    )

                } else {
                    entries.add(
                        BarEntry(
                            xIndex, floatArrayOf(
                                lightSleep.toFloat(),
                                deepSleep.toFloat(),
                                awake.toFloat(),
                                0.toFloat()
                            )
                        )
                    )
                }

            } else { entries.add(BarEntry(xIndex, floatArrayOf(0F, 0F, 0F, 0F))) }
            xAxisLabels.add(id)
            xIndex += 1
        }

        return Triple(entries, xAxisLabels, maxSleepTime)
    }

    /**
     * Sets the bar chart. Calls generateDataBarChart for diagram data.
     * TODO Combine weekly and monthly charts
     */
    private fun setBarChartWeekly() { //http://developine.com/android-grouped-stacked-bar-chart-using-mpchart-kotlin/
        val diagramData = generateDataBarChartWeekly()

        val barDataSet1 = BarDataSet(diagramData.first, "")
        barDataSet1.setColors(R.color.light_sleep_color, R.color.deep_sleep_color, R.color.awake_sleep_color, R.color.sleep_sleep_color)
        barDataSet1.setDrawValues(false)

        val xAxisValues = ArrayList<String>()
        for (i in diagramData.second.indices) {
            val date = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(diagramData.second[i].toLong() * 1000),
                ZoneOffset.systemDefault())

            val month = when (date.month) {
                Month.JANUARY -> "Jan"
                Month.FEBRUARY -> "Feb"
                Month.MARCH -> "Mar"
                Month.APRIL -> "Apr"
                Month.MAY -> "May"
                Month.JUNE -> "Jun"
                Month.JULY -> "Jul"
                Month.AUGUST -> "Aug"
                Month.SEPTEMBER -> "Sep"
                Month.OCTOBER -> "Oct"
                Month.NOVEMBER -> "Nov"
                Month.DECEMBER -> "Dec"
                else -> "Fail"
            }
            xAxisValues.add(date.dayOfMonth.toString() + ". " + month)
        }

        val barData = BarData(barDataSet1)
        binding.barChartSleepAnalysisWeek.description.isEnabled = false
        binding.barChartSleepAnalysisWeek.data = barData
        binding.barChartSleepAnalysisWeek.barData.barWidth = 0.75f
        binding.barChartSleepAnalysisWeek.xAxis.axisMinimum = 0f
        binding.barChartSleepAnalysisWeek.xAxis.axisMaximum = 7f
        binding.barChartSleepAnalysisWeek.data.isHighlightEnabled = false
        binding.barChartSleepAnalysisWeek.invalidate()

        // set bar label
        val legend =         binding.barChartSleepAnalysisWeek.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)

        val legendEntries = arrayListOf<LegendEntry>()
        legendEntries.add((LegendEntry("Light", Legend.LegendForm.SQUARE, 8f, 8f, null ,
            R.color.light_sleep_color)))
        legendEntries.add((LegendEntry("Deep", Legend.LegendForm.SQUARE, 8f, 8f, null ,
            R.color.deep_sleep_color)))
        legendEntries.add((LegendEntry("Awake", Legend.LegendForm.SQUARE, 8f, 8f, null ,
            R.color.awake_sleep_color)))
        legendEntries.add((LegendEntry("Sleep", Legend.LegendForm.SQUARE, 8f, 8f, null ,
            R.color.sleep_sleep_color)))
        legend.setCustom(legendEntries)
        legend.textSize = 12f


        val xAxis =         binding.barChartSleepAnalysisWeek.xAxis
        xAxis.setDrawGridLines(true)

        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValues)
        xAxis.setCenterAxisLabels(true)


        binding.barChartSleepAnalysisWeek.isDragEnabled = true

        //Y-axis
        binding.barChartSleepAnalysisWeek.axisRight.isEnabled = true
        binding.barChartSleepAnalysisWeek.axisRight.axisMinimum = 0f
        binding.barChartSleepAnalysisWeek.axisRight.labelCount = 10

        binding.barChartSleepAnalysisWeek.axisLeft.spaceTop = 60f
        binding.barChartSleepAnalysisWeek.axisLeft.axisMinimum = 0f
        binding.barChartSleepAnalysisWeek.axisLeft.labelCount = 20


        if ((diagramData.third > 600) && (diagramData.third < 720)) {
            binding.barChartSleepAnalysisWeek.axisRight.axisMaximum = 12f
            binding.barChartSleepAnalysisWeek.axisLeft.axisMaximum = 720f
        }
        else if ((diagramData.third > 720) && (diagramData.third < 840)) {
            binding.barChartSleepAnalysisWeek.axisRight.axisMaximum = 14f
            binding.barChartSleepAnalysisWeek.axisLeft.axisMaximum = 840f
        }
        else if ((diagramData.third > 840) && (diagramData.third < 960)) {
            binding.barChartSleepAnalysisWeek.axisRight.axisMaximum = 16f
            binding.barChartSleepAnalysisWeek.axisLeft.axisMaximum = 960f
        }
        else if (diagramData.third > 960) { // between 12h and 14h
            binding.barChartSleepAnalysisWeek.axisRight.axisMaximum = 24f
            binding.barChartSleepAnalysisWeek.axisLeft.axisMaximum = 1440f
        }
        else {
            binding.barChartSleepAnalysisWeek.axisRight.axisMaximum = 10f
            binding.barChartSleepAnalysisWeek.axisLeft.axisMaximum = 600f
        }
    }
}
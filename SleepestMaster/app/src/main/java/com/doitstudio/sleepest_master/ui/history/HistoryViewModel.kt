package com.doitstudio.sleepest_master.ui.history

import android.app.Application
import android.app.ApplicationErrorReport
import android.graphics.Color
import android.view.View
import androidx.databinding.*
import androidx.lifecycle.AndroidViewModel
import androidx.room.Database
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.doitstudio.sleepest_master.storage.db.SleepDatabase
import com.doitstudio.sleepest_master.storage.db.SleepDatabase_Impl
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.*

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val scope: CoroutineScope = MainScope()
    private val context by lazy { getApplication<Application>().applicationContext }
    private val dataBaseRepository: DatabaseRepository by lazy {
        (context as MainApplication).dataBaseRepository
    }

    var analysisDate = ObservableField(LocalDate.now())

    /** <Int: Sleep session id, Triple<List<[SleepApiRawDataEntity]>, Int: Sleep duration, [UserSleepSessionEntity]>> */
    //val sleepSessionData = ObservableArrayMap<Int, Triple<List<SleepApiRawDataEntity>, Int, UserSleepSessionEntity>>()

    val idsListener = ObservableInt()

    /** <Int: Sleep session id, Triple<List<[SleepApiRawDataEntity]>, Int: Sleep duration, [UserSleepSessionEntity]>> */
    val sleepSessionData = mutableMapOf<Int, Triple<List<SleepApiRawDataEntity>, Int, UserSleepSessionEntity>>()

    init {
        getSleepData()
    }

    fun onPreviousDateClick(range: Int) {
        when (range) {
            0 -> analysisDate.set(analysisDate.get()?.minusDays(1L))
            1 -> analysisDate.set(analysisDate.get()?.minusWeeks(1L))
            2 -> analysisDate.set(analysisDate.get()?.minusMonths(1L))
        }
    }

    fun onNextDateClick(range: Int) {
        when (range) {
            0 -> analysisDate.set(analysisDate.get()?.plusDays(1L))
            1 -> analysisDate.set(analysisDate.get()?.plusWeeks(1L))
            2 -> analysisDate.set(analysisDate.get()?.plusMonths(1L))
        }
    }

    fun getSleepData() {
        val ids = mutableSetOf<Int>()
        analysisDate.get()?.let {
            val startDayToGet = it.minusMonths(1L).withDayOfMonth(1)
            val endDayToGet = it.withDayOfMonth(it.lengthOfMonth())
            val dayDifference = (endDayToGet.toEpochDay() - startDayToGet.toEpochDay()).toInt()

            for (day in 0..dayDifference) {
                ids.add(
                    UserSleepSessionEntity.getIdByDateTime(
                        LocalDate.of(
                            startDayToGet.plusDays(day.toLong()).year,
                            startDayToGet.plusDays(day.toLong()).month,
                            startDayToGet.plusDays(day.toLong()).dayOfMonth,
                        )
                    )
                )
            }
        }

        scope.launch {
            for (id in ids) {
                val session = dataBaseRepository.getSleepSessionById(id).first().firstOrNull()
                session?.let {
                    sleepSessionData[id] = Triple(
                        dataBaseRepository.getSleepApiRawDataBetweenTimestamps(
                            session.sleepTimes.sleepTimeStart,
                            session.sleepTimes.sleepTimeEnd).first().sortedBy { x -> x.timestampSeconds },
                        session.sleepTimes.sleepDuration,
                        session
                    )
                }

                idsListener.set(id)
            }
        }
    }

    fun checkId(time: LocalDate) : Boolean {
        return sleepSessionData.containsKey(UserSleepSessionEntity.getIdByDateTime(time))
    }

    fun generateDataBarChart(range: Int, endDateOfDiagram: LocalDate): Triple<ArrayList<BarEntry>, List<Int>, Int> { //ArrayList<BarEntry> {
        val entries = ArrayList<BarEntry>()
        val xAxisLabels = mutableListOf<Int>()
        var xIndex = 0.5f
        var maxSleepTime = 0

        val ids = mutableSetOf<Int>()
        for (i in -(range-2)..1) {
            ids.add(
                UserSleepSessionEntity.getIdByDateTime(
                    LocalDate.ofEpochDay(endDateOfDiagram.toEpochDay().plus(i.toLong()))
                )
            )
        }

        ids.reversed()
        for (id in ids) {
            if (sleepSessionData.containsKey(id)) {
                val values = sleepSessionData[id]!!

                val awake = values.third.sleepTimes.awakeTime / 60
                val sleep = values.third.sleepTimes.sleepDuration / 60
                val lightSleep = values.third.sleepTimes.lightSleepDuration / 60
                val deepSleep = values.third.sleepTimes.deepSleepDuration / 60

                if ((sleep + awake) > maxSleepTime) { maxSleepTime = (sleep + awake) }

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

    fun setBarChart(range: Int, endDateOfDiagram: LocalDate) : BarChart {
        //http://developine.com/android-grouped-stacked-bar-chart-using-mpchart-kotlin/
        val barChart = BarChart(context)
        val diagramData = generateDataBarChart(range, endDateOfDiagram)

        val barDataSet1 = BarDataSet(diagramData.first, "")
        barDataSet1.setColors(R.color.light_sleep_color, R.color.deep_sleep_color, R.color.awake_sleep_color, R.color.sleep_sleep_color)
        barDataSet1.setDrawValues(false)

        val barData = BarData(barDataSet1)
        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.data.isHighlightEnabled = false

        val xAxisValues = ArrayList<String>()
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        if (range > 21) {
            for (i in diagramData.second.indices) {
                val date = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(diagramData.second[i].toLong() * 1000),
                    ZoneOffset.systemDefault())

                if (i == 0 || i == 10 || i == 20  || i == (diagramData.second.size - 1)) {
                    xAxisValues.add(date.dayOfMonth.toString())
                }
                else { xAxisValues.add("") }
            }

            barChart.barData.barWidth = 0.5f
            barChart.xAxis.axisMinimum = 0f
            barChart.xAxis.axisMaximum = (diagramData.second.size).toFloat()
            barChart.xAxis.labelCount = (diagramData.second.size)
            barChart.xAxis.setCenterAxisLabels(true)
        }
        else {
            for (i in diagramData.second.indices) {
                xAxisValues.add("Mo")
                xAxisValues.add("Tu")
                xAxisValues.add("We")
                xAxisValues.add("Th")
                xAxisValues.add("Fr")
                xAxisValues.add("Sa")
                xAxisValues.add("Su")
            }

            barChart.barData.barWidth = 0.75f
            barChart.xAxis.axisMinimum = 0f
            barChart.xAxis.axisMaximum = 7f
            barChart.xAxis.labelCount = 7
            barChart.xAxis.setCenterAxisLabels(true)
        }

        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValues)
        barChart.setFitBars(true)

        // set bar label
        val legend = barChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)

        val legendEntries = arrayListOf<LegendEntry>()
        legendEntries.add((LegendEntry("Light", Legend.LegendForm.SQUARE, 8f, 8f, null ,
            Color.RED)))
        legendEntries.add((LegendEntry("Deep", Legend.LegendForm.SQUARE, 8f, 8f, null ,
            Color.BLUE))) //R.color.deep_sleep_color
        legendEntries.add((LegendEntry("Awake", Legend.LegendForm.SQUARE, 8f, 8f, null ,
            Color.CYAN)))
        legendEntries.add((LegendEntry("Sleep", Legend.LegendForm.SQUARE, 8f, 8f, null ,
            Color.MAGENTA)))
        legend.setCustom(legendEntries)
        legend.textSize = 12f

        barChart.isDragEnabled = false

        //Y-axis
        //Y-axis
        barChart.axisRight.isEnabled = true
        barChart.axisRight.axisMinimum = 0f
        barChart.axisRight.labelCount = 0
        barChart.axisRight.setDrawGridLines(false)
        barChart.axisRight.setDrawLabels(false)

        barChart.axisLeft.spaceTop = 60f
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.labelCount = 10
        barChart.axisLeft.setDrawGridLines(false)

        if ((diagramData.third > 540) && (diagramData.third < 660)) {
            barChart.axisRight.axisMaximum = 12f
            barChart.axisLeft.axisMaximum = 12f
            barChart.axisLeft.labelCount = 12
        }
        else if ((diagramData.third > 660) && (diagramData.third < 780)) {
            barChart.axisRight.axisMaximum = 14f
            barChart.axisLeft.axisMaximum = 14f
            barChart.axisLeft.labelCount = 14
        }
        else if ((diagramData.third > 780) && (diagramData.third < 900)) {
            barChart.axisRight.axisMaximum = 16f
            barChart.axisLeft.axisMaximum = 16f
            barChart.axisLeft.labelCount = 14
        }
        else if (diagramData.third > 900) { // between 12h and 14h
            barChart.axisRight.axisMaximum = 24f
            barChart.axisLeft.axisMaximum = 24f
            barChart.axisLeft.labelCount = 24
        }
        else {
            barChart.axisRight.axisMaximum = 10f
            barChart.axisLeft.axisMaximum = 10f
            barChart.axisLeft.labelCount = 10
        }

        return barChart
    }

    fun updateBarChart(barChart: BarChart, range: Int, endDateOfDiagram: LocalDate) {
        //http://developine.com/android-grouped-stacked-bar-chart-using-mpchart-kotlin/
        val diagramData = generateDataBarChart(range, endDateOfDiagram)

        val barDataSet1 = BarDataSet(diagramData.first, "")
        barDataSet1.setColors(Color.RED, Color.BLUE, Color.CYAN, Color.MAGENTA)
        barDataSet1.setDrawValues(false)

        val barData = BarData(barDataSet1)
        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.data.isHighlightEnabled = false

        val xAxisValues = ArrayList<String>()
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        if (range > 21) {
            for (i in diagramData.second.indices) {
                val date = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(diagramData.second[i].toLong() * 1000),
                    ZoneOffset.systemDefault())

                if (i == 0 || i == 10 || i == 20  || i == (diagramData.second.size - 1)) {
                    xAxisValues.add(date.dayOfMonth.toString())
                }
                else { xAxisValues.add("") }
            }

            barChart.barData.barWidth = 0.5f
            barChart.xAxis.axisMinimum = 0f
            barChart.xAxis.axisMaximum = (diagramData.second.size).toFloat()
            barChart.xAxis.labelCount = (diagramData.second.size)
            barChart.xAxis.setCenterAxisLabels(true)
        }
        else {
            for (i in diagramData.second.indices) {
                xAxisValues.add("Mo")
                xAxisValues.add("Tu")
                xAxisValues.add("We")
                xAxisValues.add("Th")
                xAxisValues.add("Fr")
                xAxisValues.add("Sa")
                xAxisValues.add("Su")
            }

            barChart.barData.barWidth = 0.75f
            barChart.xAxis.axisMinimum = 0f
            barChart.xAxis.axisMaximum = 7f
            barChart.xAxis.labelCount = 7
            barChart.xAxis.setCenterAxisLabels(true)
        }

        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValues)
        barChart.setFitBars(true)

        // set bar label
        val legend = barChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)

        val legendEntries = arrayListOf<LegendEntry>()
        legendEntries.add((LegendEntry("Light", Legend.LegendForm.SQUARE, 8f, 8f, null ,
            Color.RED)))
        legendEntries.add((LegendEntry("Deep", Legend.LegendForm.SQUARE, 8f, 8f, null ,
            Color.BLUE))) //R.color.deep_sleep_color
        legendEntries.add((LegendEntry("Awake", Legend.LegendForm.SQUARE, 8f, 8f, null ,
            Color.CYAN)))
        legendEntries.add((LegendEntry("Sleep", Legend.LegendForm.SQUARE, 8f, 8f, null ,
            Color.MAGENTA)))
        legend.setCustom(legendEntries)
        legend.textSize = 12f

        barChart.isDragEnabled = true

        //Y-axis
        barChart.axisRight.isEnabled = true
        barChart.axisRight.axisMinimum = 0f
        barChart.axisRight.labelCount = 0
        barChart.axisRight.setDrawGridLines(false)
        barChart.axisRight.setDrawLabels(false)

        barChart.axisLeft.spaceTop = 60f
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.labelCount = 10
        barChart.axisLeft.setDrawGridLines(false)

        if ((diagramData.third > 540) && (diagramData.third < 660)) {
            barChart.axisRight.axisMaximum = 12f
            barChart.axisLeft.axisMaximum = 12f
            barChart.axisLeft.labelCount = 12
        }
        else if ((diagramData.third > 660) && (diagramData.third < 780)) {
            barChart.axisRight.axisMaximum = 14f
            barChart.axisLeft.axisMaximum = 14f
            barChart.axisLeft.labelCount = 14
        }
        else if ((diagramData.third > 780) && (diagramData.third < 900)) {
            barChart.axisRight.axisMaximum = 16f
            barChart.axisLeft.axisMaximum = 16f
            barChart.axisLeft.labelCount = 14
        }
        else if (diagramData.third > 900) { // between 12h and 14h
            barChart.axisRight.axisMaximum = 24f
            barChart.axisLeft.axisMaximum = 24f
            barChart.axisLeft.labelCount = 24
        }
        else {
            barChart.axisRight.axisMaximum = 10f
            barChart.axisLeft.axisMaximum = 10f
            barChart.axisLeft.labelCount = 10
        }

        barChart.invalidate()
    }
}
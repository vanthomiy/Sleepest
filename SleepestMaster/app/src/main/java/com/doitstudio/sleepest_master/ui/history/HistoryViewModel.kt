package com.doitstudio.sleepest_master.ui.history

import android.app.Application
import android.app.ApplicationErrorReport
import android.view.View
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.room.Database
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.databinding.FragmentHistoryWeekBinding
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

    val analysisDate: LocalDate = LocalDate.now()

    /** <Int: Sleep session id, Triple<List<[SleepApiRawDataEntity]>, Int: Sleep duration, [UserSleepSessionEntity]>> */
    val sleepSessionData = mutableMapOf<Int, Triple<List<SleepApiRawDataEntity>, Int, UserSleepSessionEntity>>()


    init {
        getSleepData()
    }

    private fun getSleepData() {
        val ids = mutableSetOf<Int>()
        val startDayToGet = analysisDate.minusMonths(1L).withDayOfMonth(1)
        val endDayToGet = analysisDate.withDayOfMonth(analysisDate.lengthOfMonth())
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
        val xAxis = barChart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM

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
            barChart.xAxis.axisMaximum = endDateOfDiagram.lengthOfMonth().toFloat()
            xAxis.setCenterAxisLabels(false)
        }
        else {
            for (i in diagramData.second.indices) {
                /*
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
                 */

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
            xAxis.setCenterAxisLabels(true)
        }

        xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValues)
        barChart.invalidate()

        // set bar label
        val legend = barChart.legend
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

        barChart.isDragEnabled = true

        //Y-axis
        barChart.axisRight.isEnabled = true
        barChart.axisRight.axisMinimum = 0f
        barChart.axisRight.labelCount = 10

        barChart.axisLeft.spaceTop = 60f
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.labelCount = 20

        if ((diagramData.third > 540) && (diagramData.third < 660)) {
            barChart.axisRight.axisMaximum = 12f
            barChart.axisLeft.axisMaximum = 720f
        }
        else if ((diagramData.third > 660) && (diagramData.third < 780)) {
            barChart.axisRight.axisMaximum = 14f
            barChart.axisLeft.axisMaximum = 840f
        }
        else if ((diagramData.third > 780) && (diagramData.third < 900)) {
            barChart.axisRight.axisMaximum = 16f
            barChart.axisLeft.axisMaximum = 960f
        }
        else if (diagramData.third > 900) { // between 12h and 14h
            barChart.axisRight.axisMaximum = 24f
            barChart.axisLeft.axisMaximum = 1440f
        }
        else {
            barChart.axisRight.axisMaximum = 10f
            barChart.axisLeft.axisMaximum = 600f
        }

        return barChart
    }
}
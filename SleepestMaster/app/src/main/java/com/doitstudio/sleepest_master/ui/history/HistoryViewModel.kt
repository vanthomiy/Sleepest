package com.doitstudio.sleepest_master.ui.history

import android.app.Application
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.databinding.*
import androidx.lifecycle.AndroidViewModel
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.model.data.Constants
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity
import com.doitstudio.sleepest_master.util.SmileySelectorUtil
import com.doitstudio.sleepest_master.util.StringUtil
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val scope: CoroutineScope = MainScope()
    val context: Context by lazy { getApplication<Application>().applicationContext }

    /**  */
    val dataBaseRepository: DatabaseRepository by lazy { (context as MainApplication).dataBaseRepository }

    /**  */
    private val dataStoreRepository: DataStoreRepository by lazy { (context as MainApplication).dataStoreRepository }

    /**  */
    var analysisDate = ObservableField(LocalDate.now())

    /**  */
    var darkMode = false

    /**  */
    var autoDarkMode = false

    /**  */
    var onWork = false

    /**  */
    private val xAxisValues = ArrayList<String>()

    /**  */
    private val idsListener = ObservableInt()

    val dataReceived = ObservableBoolean(false)

    /** <Int: Sleep session id, Triple<List<[SleepApiRawDataEntity]>, Int: Sleep duration, [UserSleepSessionEntity]>> */
    val sleepSessionData = mutableMapOf<Int, Triple<List<SleepApiRawDataEntity>, Int, UserSleepSessionEntity>>()

    init {
        //getSleepData()
        scope.launch {
            darkMode = dataStoreRepository.settingsDataFlow.first().designDarkMode
            autoDarkMode = dataStoreRepository.settingsDataFlow.first().designAutoDarkMode
        }


    }

    /**  */
    fun onPreviousDateClick(range: Int) {
        analysisDate.let {
            when (range) {
                0 -> it.set(it.get()?.minusDays(1L))
                1 -> it.set(it.get()?.minusWeeks(1L))
                2 -> it.set(it.get()?.minusMonths(1L))
            }
        }
    }

    /**  */
    fun onNextDateClick(range: Int) {
        analysisDate.let {
            when (range) {
                0 -> {
                    if (LocalDate.now().dayOfYear >= it.get()?.plusDays(1L)?.dayOfYear!!) {
                        it.set(it.get()?.plusDays(1L))
                    }
                }
                1 -> {
                    if (LocalDate.now().dayOfYear >= it.get()?.plusWeeks(1L)?.dayOfYear!!) {
                        it.set(it.get()?.plusWeeks(1L))
                    }
                }
                2 -> {
                    if (LocalDate.now().dayOfYear >= it.get()?.plusMonths(1L)?.dayOfYear!!) {
                        it.set(it.get()?.plusMonths(1L))
                    }
                }
            }
        }

    }

    /**  */
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
                            session.sleepTimes.sleepTimeEnd).first()?.sortedBy { x -> x.timestampSeconds },
                        session.sleepTimes.sleepDuration,
                        session
                    ) as Triple<List<SleepApiRawDataEntity>, Int, UserSleepSessionEntity>
                }

                idsListener.set(id)
            }
            checkSessionIntegrity()
            dataReceived.set(true)
        }
    }

    /**  */
    private fun checkSessionIntegrity() {
        onWork = true
        for (key in sleepSessionData.keys) {
            val session = sleepSessionData[key]?.first
            session?.let {
                val mobilePosition = sleepSessionData[key]?.third?.mobilePosition
                val isSleeping = it.any { x -> x.sleepState == SleepState.SLEEPING }
                val isUnidentified = it.any { x -> x.sleepState == SleepState.NONE }

                if ((isUnidentified)) {
                    scope.launch {
                        SleepCalculationHandler.getHandler(context).checkIsUserSleeping(
                            LocalDateTime.ofInstant(
                                Instant.ofEpochMilli((sleepSessionData[key]?.third?.sleepTimes?.sleepTimeStart?.toLong())!! * 1000),
                                ZoneOffset.systemDefault()
                            ),
                            true
                        )
                    }
                }

                if ((mobilePosition == MobilePosition.INBED && isSleeping)) { // || isUnidentified) {
                    scope.launch {
                        SleepCalculationHandler.getHandler(context).defineUserWakeup(
                            LocalDateTime.ofInstant(
                                Instant.ofEpochMilli((sleepSessionData[key]?.third?.sleepTimes?.sleepTimeStart?.toLong())!! * 1000),
                                ZoneOffset.systemDefault()
                            ),
                            false
                        )
                    }
                }
            }
        }
        onWork = false
    }

    /**  */
    fun checkId(time: LocalDate) : Boolean {
        return sleepSessionData.containsKey(UserSleepSessionEntity.getIdByDateTime(time))
    }

    /**  */
    fun checkDarkMode() : Int {
        var color = Color.BLACK
        if (autoDarkMode) {
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                color = Color.WHITE
            }
        } else if (darkMode) {
            color = Color.WHITE
        }
        return color
    }

    /**  */
    fun generateDataBarChart(range: Int, endDateOfDiagram: LocalDate): Triple<ArrayList<BarEntry>, List<Int>, Int> {
        val entries = ArrayList<BarEntry>()
        val xAxisLabels = mutableListOf<Int>()
        var xIndex = 0.5f
        var maxSleepTime = 0f

        val ids = mutableSetOf<Int>()
        for (i in -(range-2)..1) {
            ids.add(
                UserSleepSessionEntity.getIdByDateTime(
                    LocalDate.ofEpochDay(
                        endDateOfDiagram.toEpochDay().plus((i - 1).toLong())
                    )
                )
            )
        }

        ids.reversed()
        for (id in ids) {
            if (sleepSessionData.containsKey(id)) {
                val values = sleepSessionData[id]!!

                val awake = values.third.sleepTimes.awakeTime / 60f
                val sleep = values.third.sleepTimes.sleepDuration / 60f
                val lightSleep = values.third.sleepTimes.lightSleepDuration / 60f
                val deepSleep = values.third.sleepTimes.deepSleepDuration / 60f

                if (((sleep + awake) * 60f) > maxSleepTime) {
                    maxSleepTime = (sleep + awake) * 60f
                }

                if (lightSleep != 0f && deepSleep != 0f) {
                    entries.add(
                        BarEntry(
                            xIndex, floatArrayOf(
                                lightSleep,
                                deepSleep,
                                0.toFloat(),
                                awake
                            )
                        )
                    )

                } else {
                    entries.add(
                        BarEntry(
                            xIndex, floatArrayOf(
                                0.toFloat(),
                                0.toFloat(),
                                sleep,
                                awake
                            )
                        )
                    )
                }

            } else { entries.add(BarEntry(xIndex, floatArrayOf(0F, 0F, 0F, 0F))) }
            xAxisLabels.add(id)
            xIndex += 1
        }

        return Triple(entries, xAxisLabels, maxSleepTime.toInt())
    }

    /**  */
    private fun generateBarDataSet(barEntries: ArrayList<BarEntry>) : BarDataSet {
        val barDataSet = BarDataSet(barEntries, "")
        barDataSet.setColors(
            ContextCompat.getColor(context, R.color.light_sleep_color),
            ContextCompat.getColor(context, R.color.deep_sleep_color),
            ContextCompat.getColor(context, R.color.sleep_sleep_color),
            ContextCompat.getColor(context, R.color.awake_sleep_color)
        )
        barDataSet.setDrawValues(false)

        return barDataSet
    }

    /**  */
    private fun getBarChartYAxisProportion(sleepAmount: Int) : Float {
        return if ((sleepAmount >= 540) && (sleepAmount < 660)) {
            12F
        } else if ((sleepAmount >= 660) && (sleepAmount < 780)) {
            14F
        } else if ((sleepAmount >= 780) && (sleepAmount < 900)) {
            16F
        } else if (sleepAmount >= 900) {
            24F
        } else {
            10F
        }
    }

    /**  */
    fun setBarChart(range: Int, endDateOfDiagram: LocalDate) : BarChart {
        //http://developine.com/android-grouped-stacked-bar-chart-using-mpchart-kotlin/
        val barChart = BarChart(context)
        val diagramData = generateDataBarChart(range, endDateOfDiagram)
        val barData = BarData(generateBarDataSet(diagramData.first))
        barChart.data = barData
        visualSetUpBarChart(barChart, diagramData, range)
        return barChart
    }

    /**  */
    fun updateBarChart(barChart: BarChart, range: Int, endDateOfDiagram: LocalDate) {
        val diagramData = generateDataBarChart(range, endDateOfDiagram)
        val barData = BarData(generateBarDataSet(diagramData.first))
        barChart.data = barData
        visualSetUpBarChart(barChart, diagramData, range)
        barChart.invalidate()
    }

    /**  */
    private fun visualSetUpBarChart(barChart: BarChart,
                                    diagramData: Triple<ArrayList<BarEntry>, List<Int>, Int>,
                                    range: Int) {
        barChart.description.isEnabled = false
        barChart.data.isHighlightEnabled = false

        //val xAxisValues = ArrayList<String>()
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        xAxisValues.clear()

        if (range > 21) {
            for (i in diagramData.second.indices) {
                val date = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli((diagramData.second[i].toLong() + Constants.DAY_IN_SECONDS) * 1000),
                    ZoneOffset.systemDefault())

                if (i == 0 || i == 10 || i == 20  || i == (diagramData.second.size - 1)) {
                    xAxisValues.add(date.dayOfMonth.toString())
                }
                else { xAxisValues.add("") }
            }

            barChart.barData.barWidth = 0.5f
            barChart.xAxis.axisMaximum = (diagramData.second.size).toFloat()
            barChart.xAxis.labelCount = (diagramData.second.size)
        }
        else {
            xAxisValues.add(
                StringUtil.getStringXml(
                    R.string.alarm_entity_day_mo,
                    getApplication()
                )
            )
            xAxisValues.add(
                StringUtil.getStringXml(
                    R.string.alarm_entity_day_tu,
                    getApplication()
                )
            )
            xAxisValues.add(
                StringUtil.getStringXml(
                    R.string.alarm_entity_day_we,
                    getApplication()
                )
            )
            xAxisValues.add(
                StringUtil.getStringXml(
                    R.string.alarm_entity_day_th,
                    getApplication()
                )
            )
            xAxisValues.add(
                StringUtil.getStringXml(
                    R.string.alarm_entity_day_fr,
                    getApplication()
                )
            )
            xAxisValues.add(
                StringUtil.getStringXml(
                    R.string.alarm_entity_day_sa,
                    getApplication()
                )
            )
            xAxisValues.add(
                StringUtil.getStringXml(
                    R.string.alarm_entity_day_su,
                    getApplication()
                )
            )

            barChart.barData.barWidth = 0.75f
            barChart.xAxis.axisMaximum = 7f
            barChart.xAxis.labelCount = 7
        }

        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValues)
        barChart.setFitBars(true)

        barChart.xAxis.axisMinimum = 0f
        barChart.xAxis.setCenterAxisLabels(true)
        barChart.xAxis.textColor = checkDarkMode()


        // set bar label
        barChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        barChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        barChart.legend.orientation = Legend.LegendOrientation.HORIZONTAL
        barChart.legend.setDrawInside(false)
        barChart.legend.textSize = 12f
        barChart.legend.textColor = checkDarkMode()

        barChart.legend.setCustom(
            listOf(
                LegendEntry(
                    StringUtil.getStringXml(R.string.history_day_timeInPhase_lightSleep, getApplication()),
                    Legend.LegendForm.SQUARE,
                    8f,
                    8f,
                    null,
                    ContextCompat.getColor(context, R.color.light_sleep_color)
                ),
                LegendEntry(
                    StringUtil.getStringXml(R.string.history_day_timeInPhase_deepSleep, getApplication()),
                    Legend.LegendForm.SQUARE,
                    8f,
                    8f,
                    null,
                    ContextCompat.getColor(context, R.color.deep_sleep_color)
                ),
                LegendEntry(
                    StringUtil.getStringXml(R.string.history_day_timeInPhase_sleepSum, getApplication()),
                    Legend.LegendForm.SQUARE,
                    8f,
                    8f,
                    null,
                    ContextCompat.getColor(context, R.color.sleep_sleep_color)
                ),
                LegendEntry(
                    StringUtil.getStringXml(R.string.history_day_timeInPhase_awake, getApplication()),
                    Legend.LegendForm.SQUARE,
                    8f,
                    8f,
                    null,
                    ContextCompat.getColor(context, R.color.awake_sleep_color)
                )
            )
        )

        barChart.isDragEnabled = false

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
        barChart.axisLeft.textColor = checkDarkMode()

        val proportion = getBarChartYAxisProportion(diagramData.third)
        barChart.axisRight.axisMaximum = proportion
        barChart.axisLeft.axisMaximum = proportion
        barChart.axisLeft.labelCount = proportion.toInt()

        barChart.setScaleEnabled(false)
        barChart.setTouchEnabled(false)
        barChart.setPinchZoom(false)
        barChart.isDoubleTapToZoomEnabled = false
    }

    /**  */
    private fun generateDataActivityChart(range: Int, endDateOfDiagram: LocalDate): ArrayList<Entry> {
        val entries = ArrayList<Entry>()
        var xValue = 0

        val ids = mutableSetOf<Int>()
        for (i in -(range-2)..1) {
            ids.add(
                UserSleepSessionEntity.getIdByDateTime(
                    LocalDate.ofEpochDay(
                        endDateOfDiagram.toEpochDay().plus((i - 1).toLong())
                    )
                )
            )
        }

        ids.reversed()
        for (id in ids) {
            if (sleepSessionData.containsKey(id)) {
                val values = sleepSessionData[id]?.third!!

                entries.add(Entry(xValue.toFloat(), values.userSleepRating.activityOnDay.ordinal.toFloat()))
            }
            else {
                entries.add(Entry(xValue.toFloat(), 0f))
            }
            xValue += 1
        }
        return entries
    }

    /**  */
    fun setActivityChart(range: Int, endDateOfDiagram: LocalDate) : LineChart {
        val chart = LineChart(context)
        val lineDataSet = LineDataSet(generateDataActivityChart(range, endDateOfDiagram), "")
        visualSetUpActivityChart(chart, lineDataSet, range)
        chart.data = LineData(lineDataSet)
        return chart
    }

    /**  */
    fun updateActivityChart(chart: LineChart, range: Int, endDateOfDiagram: LocalDate) {
        val lineDataSet = LineDataSet(generateDataActivityChart(range, endDateOfDiagram), "")
        visualSetUpActivityChart(chart, lineDataSet, range)
        chart.data = LineData(lineDataSet)
    }

    /**  */
    private fun visualSetUpActivityChart(chart: LineChart, lineDataSet: LineDataSet, range: Int) {
        lineDataSet.setDrawValues(false)
        lineDataSet.setDrawFilled(true)
        lineDataSet.setDrawCircles(false)
        lineDataSet.lineWidth = 2f
        lineDataSet.fillColor = ContextCompat.getColor(context, R.color.sleep_sleep_color)
        lineDataSet.fillAlpha = 255
        lineDataSet.color = ContextCompat.getColor(context, R.color.awake_sleep_color)
        lineDataSet.fillDrawable = ContextCompat.getDrawable(context, R.drawable.bg_spark_line)

        val yAxisValues = ArrayList<String>()
        yAxisValues.add("")
        yAxisValues.add(SmileySelectorUtil.getSmileyActivity(1))
        yAxisValues.add("")
        yAxisValues.add(SmileySelectorUtil.getSmileyActivity(2))
        yAxisValues.add("")
        yAxisValues.add(SmileySelectorUtil.getSmileyActivity(3))

        chart.axisLeft.labelCount = 5
        chart.axisLeft.axisMaximum = 5.05f

        chart.axisLeft.valueFormatter = IndexAxisValueFormatter(yAxisValues)
        chart.axisLeft.axisMinimum = -0.05f
        chart.axisLeft.setDrawGridLines(false)
        chart.axisLeft.textColor = checkDarkMode()
        chart.axisLeft.textSize = 16f
        chart.legend.isEnabled= false

        chart.axisRight.setDrawLabels(false)
        chart.axisRight.setDrawGridLines(false)

        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.setDrawLabels(true)

        chart.xAxis.textColor = checkDarkMode()
        chart.xAxis.setCenterAxisLabels(false)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        if (xAxisValues.size > 21) {
            chart.xAxis.labelCount = xAxisValues.size
            lineDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        }
        else {
            chart.xAxis.labelCount = 6
            lineDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        }

        chart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValues)

        chart.description.isEnabled = false
        chart.setScaleEnabled(false)
        chart.setTouchEnabled(false)
        chart.setPinchZoom(false)
        chart.isDoubleTapToZoomEnabled = false

        chart.animateX(500)
    }
}
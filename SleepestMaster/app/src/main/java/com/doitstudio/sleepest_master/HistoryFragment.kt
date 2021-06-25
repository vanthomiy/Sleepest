package com.doitstudio.sleepest_master

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.storage.db.*
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.*
import java.time.Instant.ofEpochMilli
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList

class HistoryFragment(val applicationContext: Context) : Fragment() {

    private lateinit var sleepDbRepository: DatabaseRepository
    private val scope: CoroutineScope = MainScope()
    private val dbDatabase by lazy { SleepDatabase.getDatabase(applicationContext) }

    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var tVDisplayedDay: TextView
    private lateinit var tVNoDataAvailable: TextView
    private lateinit var btnSleepAnalysisDay : Button
    private lateinit var btnSleepAnalysisWeek : Button
    private lateinit var btnSleepAnalysisMonth : Button
    private lateinit var btnSleepAnalysisPreviousDay : Button
    private lateinit var btnSleepAnalysisNextDay : Button
    private lateinit var sVSleepAnalysisDay : ScrollView
    private lateinit var sVSleepAnalysisWeek : ScrollView
    private lateinit var sVSleepAnalysisMonth : ScrollView
    private lateinit var sleepSessionIDs : MutableSet<Int>
    private lateinit var sleepSessionsData : MutableMap<Int, List<SleepApiRawDataEntity>>
    private var dateOfDiagram  = LocalDate.now() //of(2021, 3, 13)
    private var currentAnalysisRange = 0 // Day = 0, Week = 1, Month = 2
    private var diagrammVisibility = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barChart = view.findViewById(R.id.barChart_sleepAnalysisWeek)
        lineChart = view.findViewById(R.id.lineChart_sleepAnalysisDay)
        pieChart = view.findViewById(R.id.pieChart_sleepAnalysisDay)
        tVDisplayedDay = view.findViewById(R.id.tV_ActualDay)
        tVNoDataAvailable = view.findViewById(R.id.tV_noDataAvailable)
        btnSleepAnalysisDay = view.findViewById(R.id.btn_SleepAnalysisDay)
        btnSleepAnalysisWeek = view.findViewById(R.id.btn_SleepAnalysisWeek)
        btnSleepAnalysisMonth = view.findViewById(R.id.btn_SleepAnalysisMonth)
        btnSleepAnalysisPreviousDay = view.findViewById(R.id.btn_SleepAnalysisPreviousDay)
        btnSleepAnalysisNextDay = view.findViewById(R.id.btn_SleepAnalysisNextDay)
        sVSleepAnalysisDay = view.findViewById(R.id.sV_sleepAnalysisChartsDays)
        sVSleepAnalysisWeek = view.findViewById(R.id.sV_sleepAnalysisChartsWeek)
        sVSleepAnalysisMonth = view.findViewById(R.id.sV_sleepAnalysisChartsMonth)

        sleepSessionIDs = mutableSetOf()
        sleepSessionsData = mutableMapOf()

        tVDisplayedDay.text = dateOfDiagram.format(DateTimeFormatter.ISO_DATE)

        tVDisplayedDay.setOnClickListener {
            dateOfDiagram = LocalDate.now()
            tVDisplayedDay.text = dateOfDiagram.format(DateTimeFormatter.ISO_DATE)
            setDiagrams()
        }

        btnSleepAnalysisDay.setOnClickListener {
            currentAnalysisRange = 0
            setDiagrams()
        }

        btnSleepAnalysisWeek.setOnClickListener {
            currentAnalysisRange = 1
            setDiagrams()
        }

        btnSleepAnalysisMonth.setOnClickListener {
            currentAnalysisRange = 2
            setDiagrams()
        }

        btnSleepAnalysisPreviousDay.setOnClickListener {
            when (currentAnalysisRange) { // Move back in time
                0 -> generateDateData(false, 0) // Day
                1 -> generateDateData(false, 1) // Week
                2 -> generateDateData(false, 2) // Month
            }
        }

        btnSleepAnalysisNextDay.setOnClickListener {
            when (currentAnalysisRange) { // Move ahead in time
                0 -> generateDateData(true, 0) // Day
                1 -> generateDateData(true, 1) // Week
                2 -> generateDateData(true, 2) // Month
            }
        }

        sleepDbRepository = DatabaseRepository.getRepo(
            dbDatabase.sleepApiRawDataDao(),
            dbDatabase.userSleepSessionDao(),
            dbDatabase.alarmDao(),
            dbDatabase.activityApiRawDataDao()
        )

        getSleepData()
        setDiagrams()
    }

    private fun getSleepData() {
        val ids = mutableSetOf<Int>()
        val beginOfMonth = dateOfDiagram.minusDays((dateOfDiagram.dayOfMonth - 1).toLong())
        var numberDaysPerMonth = dateOfDiagram.lengthOfMonth()
        var date = 1
        for (day in 1..numberDaysPerMonth) {
            ids.add(UserSleepSessionEntity.getIdByDateTime(LocalDate.of(dateOfDiagram.year, dateOfDiagram.month, date)))
            date += 1
        }

        // Add another month backwards in case the bar chart crosses a month border
        numberDaysPerMonth = dateOfDiagram.minusMonths(1.toLong()).lengthOfMonth()
        date = 1
        for (day in 1..numberDaysPerMonth) {
            ids.add(UserSleepSessionEntity.getIdByDateTime(LocalDate.of(
                dateOfDiagram.minusMonths(1.toLong()).year,
                dateOfDiagram.minusMonths(1.toLong()).month,
                date)))
            date += 1
        }

        scope.launch {
            for (id in ids) {
                val session = sleepDbRepository.getSleepSessionById(id).first().firstOrNull()
                session?.let {
                    //sleepSessionIDs.add(id) //Use ID as Key for the day since it can be accessed via .getIdByDateTime(LocalDate.of("Day of interest"))
                    sleepSessionsData[id] = sleepDbRepository.getSleepApiRawDataBetweenTimestamps(
                        session.sleepTimes.sleepTimeStart,
                        session.sleepTimes.sleepTimeEnd
                    ).first().sortedBy { x -> x.timestampSeconds }
                }
            }
        }
    }

    private fun generateDateData(direction: Boolean, range: Int) {
        val currentMonth = dateOfDiagram.month
        if (direction) {
            when (range) {
                0 -> { dateOfDiagram = dateOfDiagram.plusDays(1) }
                1 -> { dateOfDiagram = dateOfDiagram.plusWeeks(1) }
                2 -> { dateOfDiagram = dateOfDiagram.plusMonths(1) }
            }
        }
        else {
            when (range) {
                0 -> { dateOfDiagram = dateOfDiagram.minusDays(1) }
                1 -> { dateOfDiagram = dateOfDiagram.minusWeeks(1) }
                2 -> { dateOfDiagram = dateOfDiagram.minusMonths(1) }
            }
        }
        tVDisplayedDay.text = dateOfDiagram.format(DateTimeFormatter.ISO_DATE)
        if (currentMonth != dateOfDiagram.month) {
            getSleepData()
        }
        setDiagrams()
    }

    private fun setDiagrams() {
        setLineChart()
        setPieChart()
        setBarChart()

        when (currentAnalysisRange) {
            0 -> {
                if (diagrammVisibility) {
                    sVSleepAnalysisDay.isVisible = true
                    sVSleepAnalysisWeek.isVisible = false
                    sVSleepAnalysisMonth.isVisible = false
                    tVNoDataAvailable.isVisible = false
                } else {
                    sVSleepAnalysisDay.isVisible = false
                    sVSleepAnalysisWeek.isVisible = false
                    sVSleepAnalysisMonth.isVisible = false
                    tVNoDataAvailable.isVisible = true
                }
            }
            1 -> {
                if (diagrammVisibility) {
                    sVSleepAnalysisDay.isVisible = false
                    sVSleepAnalysisWeek.isVisible = true
                    sVSleepAnalysisMonth.isVisible = false
                    tVNoDataAvailable.isVisible = false
                } else {
                    sVSleepAnalysisDay.isVisible = false
                    sVSleepAnalysisWeek.isVisible = true
                    sVSleepAnalysisMonth.isVisible = false
                    tVNoDataAvailable.isVisible = false
                }
            }
            2 -> {
                if (diagrammVisibility) {
                    sVSleepAnalysisDay.isVisible = false
                    sVSleepAnalysisWeek.isVisible = false
                    sVSleepAnalysisMonth.isVisible = true
                    tVNoDataAvailable.isVisible = false
                } else {
                    sVSleepAnalysisDay.isVisible = false
                    sVSleepAnalysisWeek.isVisible = false
                    sVSleepAnalysisMonth.isVisible = false
                    tVNoDataAvailable.isVisible = true
                }
            }
        }
    }

    private fun generateDataLineChart() : ArrayList<Entry> {
        val entries = ArrayList<Entry>()
        var xValue = 0
        val values: List<SleepApiRawDataEntity>

        if (sleepSessionsData.containsKey(UserSleepSessionEntity.getIdByDateTime(dateOfDiagram))) {
            values = sleepSessionsData[UserSleepSessionEntity.getIdByDateTime(dateOfDiagram)]!!

            for (i in values) {
                entries.add(Entry(xValue.toFloat(), i.sleepState.ordinal.toFloat()))
                xValue += 1
            }
            diagrammVisibility = true //Check if all daily diagrams should be visible
        }
        else {
            entries.add(Entry(1F,1F))
            diagrammVisibility = false //Check if all daily diagrams should be visible
        }

        return entries
    }

    private fun setLineChart() {
        val vl = LineDataSet(generateDataLineChart(), "Sleep state")
        vl.setDrawValues(false)
        vl.setDrawFilled(true)
        vl.setDrawCircles(false)
        vl.lineWidth = 0f
        vl.fillColor = R.color.gray
        vl.fillAlpha = R.color.red

        lineChart.data = LineData(vl)
        lineChart.axisLeft.setStartAtZero(true)
        lineChart.axisLeft.setAxisMaxValue(5f)

        lineChart.animateX(500)
    }

    private fun generateDataPieChart() : ArrayList<PieEntry> {
        var awake = 0.01f
        var sleep = 0.01f
        var lightSleep = 0.01f
        var deepSleep = 0.01f
        var remSleep = 0.01f
        val entries = ArrayList<PieEntry>()
        var absolute = 0.05f

        val values: List<SleepApiRawDataEntity>

        if (sleepSessionsData.containsKey(UserSleepSessionEntity.getIdByDateTime(dateOfDiagram))) {
            values = sleepSessionsData[UserSleepSessionEntity.getIdByDateTime(dateOfDiagram)]!!

            for (i in values ) { //sleepSessionsData[UserSleepSessionEntity.getIdByDateTime(dateOfDiagram)]!!) {
                when (i.sleepState.ordinal) {
                    0 -> { awake += 1f }
                    1 -> { lightSleep += 1f }
                    2 -> { deepSleep += 1f }
                    3 -> { remSleep += 1f }
                    4 -> { sleep += 1f }
                }
                absolute += 1
            }
        }

        if (awake > 0) { entries.add(PieEntry((awake / absolute), "awake")) }
        if (lightSleep > 0) { entries.add(PieEntry((lightSleep / absolute), "light")) }
        if (deepSleep > 0) { entries.add(PieEntry((deepSleep / absolute), "deep")) }
        if (remSleep > 0) { entries.add(PieEntry((remSleep / absolute), "rem")) }
        if (sleep > 0) { entries.add(PieEntry((sleep / absolute), "sleep")) }

        if (absolute == 0.05F) { entries.add(PieEntry(1F, "no data")) }

        return entries
    }

    private fun setPieChart() {
        val listColors = ArrayList<Int>()
        listColors.add(resources.getColor(R.color.black))
        listColors.add(resources.getColor(R.color.green))
        listColors.add(resources.getColor(R.color.red))
        listColors.add(resources.getColor(R.color.blue))
        listColors.add(resources.getColor(R.color.dark_green))

        val pieDataSet = PieDataSet(generateDataPieChart(), "Sleep states")
        pieDataSet.colors = listColors

        val pieData = PieData(pieDataSet)
        pieChart.data = pieData

        pieChart.setUsePercentValues(true)
        pieChart.isDrawHoleEnabled = false
        pieChart.description.isEnabled = false
        pieChart.setEntryLabelColor(R.color.black)
        pieChart.animateY(1000, Easing.EaseInOutQuad)
    }

    private fun generateDataBarChart(): Pair<ArrayList<BarEntry>, List<Int>> { //ArrayList<BarEntry> {
        var xIndex = 0
        var awake = 0f
        var sleep = 0f
        var ligthSleep = 0f
        var deepSleep = 0f
        var remSleep = 0f
        val entries = ArrayList<BarEntry>()
        var absolute = 0

        val xAxisLabels = mutableListOf<Int>()

        val ids = mutableSetOf<Int>()
        for (i in -6..0) {
            ids.add(UserSleepSessionEntity.getIdByDateTime(LocalDate.of(
                dateOfDiagram.plusDays(i.toLong()).year,
                dateOfDiagram.plusDays(i.toLong()).month,
                dateOfDiagram.plusDays(i.toLong()).dayOfMonth
            )))
        }

        var values: List<SleepApiRawDataEntity>

        for (id in ids) {
            if (sleepSessionsData.containsKey(id)) {
                values = sleepSessionsData[id]!!

                for (i in values) {
                    when (i.sleepState.ordinal) {
                        0 -> { awake += 1f }
                        1 -> { ligthSleep += 1f }
                        2 -> { deepSleep += 1f }
                        3 -> { remSleep += 1f }
                        4 -> { sleep += 1f }
                    }
                    absolute += 1
                }
                if (awake > 0) { awake = awake / absolute * 100 }
                if (ligthSleep > 0) { ligthSleep = ligthSleep / absolute * 100 }
                if (deepSleep > 0) { deepSleep = deepSleep / absolute * 100 }
                if (remSleep > 0) { remSleep = remSleep / absolute * 100 }
                if (sleep > 0) { sleep = sleep / absolute * 100 }

                entries.add(BarEntry(xIndex.toFloat(), floatArrayOf(awake, ligthSleep, deepSleep, remSleep, sleep)))
                xAxisLabels.add(id)

                xIndex += 1
                awake = 0f
                sleep = 0f
                ligthSleep = 0f
                deepSleep = 0f
                remSleep = 0f
                absolute = 0
            } else {
                entries.add(BarEntry(0F, 0F))
                xAxisLabels.add(id)
            }
        }

        return Pair(entries, xAxisLabels)
    }

    private fun setBarChart() { //http://developine.com/android-grouped-stacked-bar-chart-using-mpchart-kotlin/
        val barWidth = 0.3f

        val diagramData = generateDataBarChart()

        val barDataSet1 = BarDataSet(diagramData.first, "")
        barDataSet1.setColors(Color.RED, Color.MAGENTA, Color.BLUE, Color.BLACK, Color.YELLOW)
        barDataSet1.label = "States"
        barDataSet1.setDrawIcons(false)
        barDataSet1.setDrawValues(false)

        val xAxisValues = ArrayList<String>()
        for (i in diagramData.second.indices) {
            val date = LocalDateTime.ofInstant(
                ofEpochMilli(diagramData.second[i].toLong() * 1000),
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

        barChart.description.isEnabled = false
        barChart.description.textSize = 0f
        barData.setValueFormatter(LargeValueFormatter())
        barChart.data = barData
        barChart.barData.barWidth = barWidth
        barChart.xAxis.axisMinimum = 0f
        barChart.xAxis.axisMaximum = 7f
        barChart.data.isHighlightEnabled = false
        barChart.invalidate()

        // set bar label
        val legend = barChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)

        val legenedEntries = arrayListOf<LegendEntry>()
        legenedEntries.add((LegendEntry("Awake", Legend.LegendForm.SQUARE, 8f, 8f, null ,Color.RED)))
        legenedEntries.add((LegendEntry("Light", Legend.LegendForm.SQUARE, 8f, 8f, null ,Color.MAGENTA)))
        legenedEntries.add((LegendEntry("Deep", Legend.LegendForm.SQUARE, 8f, 8f, null ,Color.BLUE)))
        legenedEntries.add((LegendEntry("REM", Legend.LegendForm.SQUARE, 8f, 8f, null ,Color.BLACK)))
        legenedEntries.add((LegendEntry("Sleep", Legend.LegendForm.SQUARE, 8f, 8f, null ,Color.YELLOW)))
        legend.setCustom(legenedEntries)

        //legend.yOffset = 2f
        //legend.xOffset = 2f
        legend.yEntrySpace = 0f
        legend.textSize = 12f

        val xAxis = barChart.xAxis
        //xAxis.granularity = 1f
        //xAxis.isGranularityEnabled = true
        //xAxis.setCenterAxisLabels(true)
        //xAxis.setDrawGridLines(false)
        xAxis.textSize = 12f

        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValues)

        xAxis.labelCount = 7
        xAxis.mAxisMaximum = 7f
        //xAxis.setCenterAxisLabels(true)
        //xAxis.setAvoidFirstLastClipping(true)
        //xAxis.spaceMin = 2f
        //xAxis.spaceMax = 4f

        barChart.setVisibleXRangeMaximum(7f)
        barChart.setVisibleXRangeMinimum(7f)
        barChart.isDragEnabled = true

        //Y-axis
        barChart.axisRight.isEnabled = false
        barChart.setScaleEnabled(true)

        val leftAxis = barChart.axisLeft
        leftAxis.valueFormatter = LargeValueFormatter()
        leftAxis.setDrawGridLines(false)
        leftAxis.spaceTop = 1f
        leftAxis.axisMinimum = 0f


        barChart.data = barData
        barChart.setVisibleXRange(0f, 7f)
    }
}
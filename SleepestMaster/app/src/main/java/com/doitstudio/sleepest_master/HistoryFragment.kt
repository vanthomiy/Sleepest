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
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationDbRepository
import com.doitstudio.sleepest_master.sleepcalculation.db.SleepCalculationDatabase
import com.doitstudio.sleepest_master.sleepcalculation.db.UserSleepSessionEntity
import com.doitstudio.sleepest_master.storage.DbRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.doitstudio.sleepest_master.storage.db.SleepDatabase
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
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class HistoryFragment(val applicationContext: Context) : Fragment() {

    private lateinit var sleepDbRepository: DbRepository
    private lateinit var sleep1DbRepository: SleepCalculationDbRepository
    private val scope: CoroutineScope = MainScope()
    private val dbDatabase by lazy { SleepDatabase.getDatabase(applicationContext) }
    private val db1Database by lazy { SleepCalculationDatabase.getDatabase(applicationContext) }

    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var tVdisplayedDay: TextView
    private lateinit var btnSleepAnalysisDay : Button
    private lateinit var btnSleepAnalysisWeek : Button
    private lateinit var btnSleepAnalysisMonth : Button
    private lateinit var btnSleepAnalysisPreviousDay : Button
    private lateinit var btnSleepAnalysisNextDay : Button
    private lateinit var sVSleepAnalysisDay : ScrollView
    private lateinit var sVSleepAnalysisWeek : ScrollView
    private lateinit var sVSleepAnalysisMonth : ScrollView
    private lateinit var sleepSessions : MutableList<UserSleepSessionEntity>
    private lateinit var sleepSessionsData : MutableMap<UserSleepSessionEntity, List<SleepApiRawDataEntity>>
    
    private var dateOfDiagram  = LocalDate.now()
    private var startTimeDay = Date(dateOfDiagram.year, dateOfDiagram.month, dateOfDiagram.date, 0, 0, 1) //May result in bug due to 2 sec glitch
    private var endTimeDay = Date(dateOfDiagram.year, dateOfDiagram.month, dateOfDiagram.date, 23, 59, 59)
    private var dateFormatOfDiagram = SimpleDateFormat("dd/MM/yyyy").format(dateOfDiagram)

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
        tVdisplayedDay = view.findViewById(R.id.tV_ActualDay)
        btnSleepAnalysisDay = view.findViewById(R.id.btn_SleepAnalysisDay)
        btnSleepAnalysisWeek = view.findViewById(R.id.btn_SleepAnalysisWeek)
        btnSleepAnalysisMonth = view.findViewById(R.id.btn_SleepAnalysisMonth)
        btnSleepAnalysisPreviousDay = view.findViewById(R.id.btn_SleepAnalysisPreviousDay)
        btnSleepAnalysisNextDay = view.findViewById(R.id.btn_SleepAnalysisNextDay)
        sVSleepAnalysisDay = view.findViewById(R.id.sV_sleepAnalysisChartsDays)
        sVSleepAnalysisWeek = view.findViewById(R.id.sV_sleepAnalysisChartsWeek)
        //sVSleepAnalysisMonth = view.findViewById(R.id.sV_sleepAnalysisChartsMonth)

        tVdisplayedDay.text = dateFormatOfDiagram

        sleepSessions = mutableListOf()
        sleepSessionsData = mutableMapOf()

        btnSleepAnalysisDay.setOnClickListener {
            sVSleepAnalysisDay.isVisible = true
            sVSleepAnalysisWeek.isVisible = false
            //sVSleepAnalysisMonth.isVisible = false
        }

        btnSleepAnalysisWeek.setOnClickListener {
            sVSleepAnalysisDay.isVisible = false
            sVSleepAnalysisWeek.isVisible = true
            //sVSleepAnalysisMonth.isVisible = false
        }

        btnSleepAnalysisMonth.setOnClickListener {
            sVSleepAnalysisDay.isVisible = false
            sVSleepAnalysisWeek.isVisible = false
            //sVSleepAnalysisMonth.isVisible = true
        }

        btnSleepAnalysisPreviousDay.setOnClickListener {
            if (sVSleepAnalysisDay.isVisible) { // Move back in time
                generateDateData(false, 1) // Day
            }
            else if (sVSleepAnalysisWeek.isVisible) {
                generateDateData(false, 2) // Week
            }
            else if (sVSleepAnalysisMonth.isVisible) {
                generateDateData(false, 3) // Month
            }
        }

        btnSleepAnalysisNextDay.setOnClickListener {
            if (sVSleepAnalysisDay.isVisible) { // Move ahead in time
                generateDateData(true, 1) // Day
            }
            else if (sVSleepAnalysisWeek.isVisible) {
                generateDateData(true, 2) // Week
            }
            else if (sVSleepAnalysisMonth.isVisible) {
                generateDateData(true, 3) // Month
            }
        }

        sleepDbRepository = DbRepository.getRepo(
            dbDatabase.sleepDataDao(),
            dbDatabase.userSleepSessionDao(),
            dbDatabase.alarmDao()
        )

        sleep1DbRepository = SleepCalculationDbRepository.getRepo(
            db1Database.sleepApiRawDataDao()
        )


        scope.launch {
            sleepSessions.addAll(sleepDbRepository.allUserSleepSessions.first())
            sleepSessions.sortBy { x -> x.id }

            for (i in sleepSessions.indices) {
                val startTime = sleepSessions[i].sleepTimes.sleepTimeStart
                val endTime = sleepSessions[i].sleepTimes.sleepTimeEnd
                sleepSessionsData[sleepSessions[i]] =
                    sleep1DbRepository.getSleepApiRawDataBetweenTimestamps(startTime, endTime).first().sortedBy { x -> x.timestampSeconds }
            }
            setLineChart()
            setPieChart()
            setBarChart()
        }
    }

    private fun generateDateData(direction: Boolean, range: Int) {
        if (direction) {
            when (range) {
                1 -> dateOfDiagram = Date(dateOfDiagram.time + 86400000) //Millis per day
                2 -> dateOfDiagram = Date(dateOfDiagram.time + 86400000) //Millis per week
                3 -> dateOfDiagram = Date(dateOfDiagram.time + 86400000) //Millis per month
            }

            dateOfDiagram = Date(dateOfDiagram.time + 86400000) //Millis per day
        }
        else {
            dateOfDiagram = Date(dateOfDiagram.time - 86400000) //Millis per day
        }
        startTimeDay = Date(dateOfDiagram.year, dateOfDiagram.month, dateOfDiagram.date, 0, 0, 1) //May result in bug due to 2 sec glitch
        endTimeDay = Date(dateOfDiagram.year, dateOfDiagram.month, dateOfDiagram.date, 23, 59, 59)
        dateFormatOfDiagram = SimpleDateFormat("dd/MM/yyyy").format(dateOfDiagram)

        tVdisplayedDay.text = dateFormatOfDiagram
    }

    private fun makeDiagramms() {
        setLineChart()
        setPieChart()
        setBarChart()
    }

    private fun generateDataLineChart() : ArrayList<Entry> {
        val entries = ArrayList<Entry>()
        var xValue = 0
        for (i in sleepSessionsData[sleepSessions[0]]!!) {
            entries.add(Entry(xValue.toFloat(), i.sleepState.ordinal.toFloat()))
            xValue += 1
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

        lineChart.animateX(1000)
    }

    private fun generateDataPieChart() : ArrayList<PieEntry> {
        var awake = 0f
        var sleep = 0f
        var ligthSleep = 0f
        var deepSleep = 0f
        var remSleep = 0f
        val entries = ArrayList<PieEntry>()
        var absolute = 0f

        for (i in sleepSessionsData[sleepSessions[0]]!!) {
            when (i.sleepState.ordinal) {
                0 -> { awake += 1f }
                1 -> { ligthSleep += 1f }
                2 -> { deepSleep += 1f }
                3 -> { remSleep += 1f }
                4 -> { sleep += 1f }
            }
            absolute += 1
        }

        if (awake > 0) { entries.add(PieEntry((awake / absolute), "awake")) }
        if (ligthSleep > 0) { entries.add(PieEntry((ligthSleep / absolute), "light")) }
        if (deepSleep > 0) { entries.add(PieEntry((deepSleep / absolute), "deep")) }
        if (remSleep > 0) { entries.add(PieEntry((remSleep / absolute), "rem")) }
        if (sleep > 0) { entries.add(PieEntry((sleep / absolute), "sleep")) }

        return entries
    }

    private fun setPieChart() {
        val listColors = ArrayList<Int>()
        listColors.add(resources.getColor(R.color.colorPrimary))
        listColors.add(resources.getColor(R.color.green))
        listColors.add(resources.getColor(R.color.red))
        listColors.add(resources.getColor(R.color.blue))

        val pieDataSet = PieDataSet(generateDataPieChart(), "")
        pieDataSet.colors = listColors

        val pieData = PieData(pieDataSet)
        pieChart.data = pieData

        pieChart.setUsePercentValues(true)
        pieChart.isDrawHoleEnabled = false
        pieChart.description.isEnabled = false
        pieChart.setEntryLabelColor(R.color.black)
        pieChart.animateY(1000, Easing.EaseInOutQuad)
    }

    private fun generateDataBarChart(): ArrayList<BarEntry> {
        var xIndex = 0
        var awake = 0f
        var sleep = 0f
        var ligthSleep = 0f
        var deepSleep = 0f
        var remSleep = 0f
        val entries = ArrayList<BarEntry>()
        var absolute = 0

        for (i in sleepSessions) {
            for (j in sleepSessionsData[i]!!) {
                when (j.sleepState.ordinal) {
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

            xIndex += 1
            awake = 0f
            sleep = 0f
            ligthSleep = 0f
            deepSleep = 0f
            remSleep = 0f
            absolute = 0
        }
        return entries
    }

    private fun setBarChart() { //http://developine.com/android-grouped-stacked-bar-chart-using-mpchart-kotlin/
        val barWidth = 0.15f

        val xAxisValues = ArrayList<String>()
        xAxisValues.add("0")
        xAxisValues.add("1")
        xAxisValues.add("2")
        xAxisValues.add("3")
        xAxisValues.add("4")
        xAxisValues.add("5")
        xAxisValues.add("6")
        xAxisValues.add("7")
        xAxisValues.add("8")
        xAxisValues.add("9")
        xAxisValues.add("10")

        val barDataSet1 = BarDataSet(generateDataBarChart(), "")
        barDataSet1.setColors(Color.BLUE, Color.RED, Color.CYAN, Color.GREEN, Color.YELLOW)
        barDataSet1.label = "States"
        barDataSet1.setDrawIcons(false)
        barDataSet1.setDrawValues(false)

        val barData = BarData(barDataSet1)

        barChart.description.isEnabled = false
        barChart.description.textSize = 0f
        barData.setValueFormatter(LargeValueFormatter())
        barChart.data = barData
        barChart.barData.barWidth = barWidth
        barChart.xAxis.axisMinimum = 0f
        barChart.xAxis.axisMaximum = 12f
        barChart.data.isHighlightEnabled = false
        barChart.invalidate()

        // set bar label
        val legend = barChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)

        val legenedEntries = arrayListOf<LegendEntry>()

        legenedEntries.add(LegendEntry("States", Legend.LegendForm.SQUARE, 8f, 8f, null, Color.RED))

        legend.setCustom(legenedEntries)

        legend.yOffset = 2f
        legend.xOffset = 2f
        legend.yEntrySpace = 0f
        legend.textSize = 5f

        val xAxis = barChart.xAxis
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        xAxis.setCenterAxisLabels(true)
        xAxis.setDrawGridLines(false)
        xAxis.textSize = 9f

        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValues)

        xAxis.labelCount = 12
        xAxis.mAxisMaximum = 12f
        xAxis.setCenterAxisLabels(true)
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.spaceMin = 2f
        xAxis.spaceMax = 4f

        barChart.setVisibleXRangeMaximum(12f)
        barChart.setVisibleXRangeMinimum(12f)
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
        barChart.setVisibleXRange(0f, 12f)
    }
}
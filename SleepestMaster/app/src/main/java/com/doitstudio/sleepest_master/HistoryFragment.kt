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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.*
import java.time.Instant.ofEpochMilli
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList

class HistoryFragment(val applicationContext: Context) : Fragment() {

    // region variable declarations
    /** TODO Description */
    private lateinit var sleepDbRepository: DatabaseRepository

    /** Used for access suspend functions and database. */
    private val scope: CoroutineScope = MainScope()

    /** TODO Description */
    private val dbDatabase by lazy { SleepDatabase.getDatabase(applicationContext) }

    /** Weekly sleep analysis (Sleep phases) */
    private lateinit var barChart: BarChart

    /** Daily sleep analysis (Sleep phases) */
    private lateinit var lineChart: LineChart

    /** Daily sleep analysis */
    private lateinit var pieChart: PieChart

    /** Displays the current analysis day */
    private lateinit var tVDisplayedDay: TextView

    /** Pops up if no sleep analysis data is available */
    private lateinit var tVNoDataAvailable: TextView

    /** Switch to day analysis */
    private lateinit var btnSleepAnalysisDay : Button

    /** Switch to week analysis */
    private lateinit var btnSleepAnalysisWeek : Button

    /** Switch to month analysis */
    private lateinit var btnSleepAnalysisMonth : Button

    /** Move into the past in time (analysis) */
    private lateinit var btnSleepAnalysisPrevious : Button

    /** Move ahead in time (analysis) */
    private lateinit var btnSleepAnalysisNext : Button

    /** Contains the daily analysis */
    private lateinit var sVSleepAnalysisDay : ScrollView

    /** Contains the weekly analysis */
    private lateinit var sVSleepAnalysisWeek : ScrollView

    /** Contains the monthly analysis */
    private lateinit var sVSleepAnalysisMonth : ScrollView

    /** Contains all sleep session ids which are loaded */
    private lateinit var sleepSessionIDs : MutableSet<Int>

    /** Int: Sleep session id, Pair0: List of the sessions raw api data for each id, Pair1: Sleep duration of the session */
    // TODO Check if the Pair[1]: Int ist still in use
    private lateinit var sleepSessionsRawData : MutableMap<Int, Pair<List<SleepApiRawDataEntity>, Int>>

    /** Int: Sleep session id, UserSleepSessionEntity: Sleep session corresponding to id */
    // TODO Check if sleepSessionsRawData and sleepSessionIDs can be combined
    private lateinit var sleepSessionData : MutableMap<Int, UserSleepSessionEntity>

    /** Analysis date */
    private var dateOfDiagram  = LocalDate.now() //of(2021, 3, 30) //now()

    /** Analysis range */
    private var currentAnalysisRange = 0 // Day = 0, Week = 1, Month = 2

    /** Maintains the visibility of the diagrams */
    private var diagramVisibility = false
    //endregion

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //region variable initializations
        barChart = view.findViewById(R.id.barChart_sleepAnalysisWeek)
        lineChart = view.findViewById(R.id.lineChart_sleepAnalysisDay)
        pieChart = view.findViewById(R.id.pieChart_sleepAnalysisDay)
        tVDisplayedDay = view.findViewById(R.id.tV_ActualDay)
        tVNoDataAvailable = view.findViewById(R.id.tV_noDataAvailable)
        btnSleepAnalysisDay = view.findViewById(R.id.btn_SleepAnalysisDay)
        btnSleepAnalysisWeek = view.findViewById(R.id.btn_SleepAnalysisWeek)
        btnSleepAnalysisMonth = view.findViewById(R.id.btn_SleepAnalysisMonth)
        btnSleepAnalysisPrevious = view.findViewById(R.id.btn_SleepAnalysisPreviousDay)
        btnSleepAnalysisNext = view.findViewById(R.id.btn_SleepAnalysisNextDay)
        sVSleepAnalysisDay = view.findViewById(R.id.sV_sleepAnalysisChartsDays)
        sVSleepAnalysisWeek = view.findViewById(R.id.sV_sleepAnalysisChartsWeek)
        sVSleepAnalysisMonth = view.findViewById(R.id.sV_sleepAnalysisChartsMonth)
        tVDisplayedDay.text = dateOfDiagram.format(DateTimeFormatter.ISO_DATE)

        sleepSessionIDs = mutableSetOf()
        sleepSessionsRawData = mutableMapOf()
        sleepSessionData = mutableMapOf()
        //endregion

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

        btnSleepAnalysisPrevious.setOnClickListener {
            when (currentAnalysisRange) { // Move back in time
                0 -> generateDateData(false, 0) // Day
                1 -> generateDateData(false, 1) // Week
                2 -> generateDateData(false, 2) // Month
            }
        }

        btnSleepAnalysisNext.setOnClickListener {
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

    /** Accesses the database and gets the required sleep session data for the current analysis range */
    private fun getSleepData() {
        val ids = mutableSetOf<Int>()
        var numberDaysPerMonth = dateOfDiagram.lengthOfMonth()
        var date = 1

        // TODO Refactor and combine both for loops
        // Get all ids for the current dateOfDiagram month
        for (day in 1..numberDaysPerMonth) {
            ids.add(UserSleepSessionEntity.getIdByDateTime(LocalDate.of(
                dateOfDiagram.year,
                dateOfDiagram.month,
                date)))
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

        // Get raw api data for ever sleep session id
        scope.launch {
            for (id in ids) {
                val session = sleepDbRepository.getSleepSessionById(id).first().firstOrNull()
                session?.let {
                    sleepSessionsRawData[id] = Pair(
                        sleepDbRepository.getSleepApiRawDataBetweenTimestamps(
                            session.sleepTimes.sleepTimeStart,
                            session.sleepTimes.sleepTimeEnd).first().sortedBy { x -> x.timestampSeconds },
                        session.sleepTimes.sleepDuration
                    )

                    // Add the current sleep session to the map of sleep sessions
                    sleepSessionData[id] = session
                }
            }
        }
    }

    /**
     * Alter the dateOfMonth based on the button click "next" or "previous".
     * This means move back or ahead in the analysis time.
     */
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

        // TODO Check if this call is still necessary and if it´s valid
        if (currentMonth != dateOfDiagram.month) {
            getSleepData()
        }
        setDiagrams()
    }

    /**
     * Sets and changes visibility of the analysis diagrams.
     * TODO Check if this can be improved or refactored.
     */
    private fun setDiagrams() {
        setLineChart()
        setPieChart()
        setBarChart()

        when (currentAnalysisRange) {
            0 -> {
                if (diagramVisibility) {
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
                if (diagramVisibility) {
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
                if (diagramVisibility) {
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

    /**
     * Generates the analysis data for the daily analysis with the line chart.
     */
    private fun generateDataLineChart() : ArrayList<Entry> {
        val entries = ArrayList<Entry>()
        var xValue = 0
        val values: Pair<List<SleepApiRawDataEntity>, Int>

        if (sleepSessionsRawData.containsKey(UserSleepSessionEntity.getIdByDateTime(dateOfDiagram))) {
            values = sleepSessionsRawData[UserSleepSessionEntity.getIdByDateTime(dateOfDiagram)]!!

            for (i in values.first) {
                entries.add(Entry(xValue.toFloat(), i.sleepState.ordinal.toFloat()))
                xValue += 1
            }
            diagramVisibility = true //Check if all daily diagrams should be visible
        }
        else {
            entries.add(Entry(1F,1F))
            diagramVisibility = false //Check if all daily diagrams should be visible
        }

        return entries
    }

    /**
     * Sets the line chart. Calls generateDataLineChart for diagram data.
     */
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

    /**
     * Generates the analysis data for the daily analysis with the pie chart.
     */
    private fun generateDataPieChart() : ArrayList<PieEntry> {
        val entries = ArrayList<PieEntry>()

        if (sleepSessionData.containsKey(UserSleepSessionEntity.getIdByDateTime(dateOfDiagram))) {
            val values = sleepSessionData[UserSleepSessionEntity.getIdByDateTime(dateOfDiagram)]!!

            val awake = values.sleepTimes.awakeTime
            val sleep = values.sleepTimes.sleepDuration
            val lightSleep = values.sleepTimes.lightSleepDuration
            val deepSleep = values.sleepTimes.deepSleepDuration
            val remSleep = values.sleepTimes.remSleepDuration

            if (sleep == 0) {
                entries.add(PieEntry(sleep.toFloat(), "sleep", R.color.dark_green))
            }
            else {
                entries.add(PieEntry(lightSleep.toFloat(), "light"))
                entries.add(PieEntry(deepSleep.toFloat(), "deep"))
                entries.add(PieEntry(remSleep.toFloat(), "rem"))
            }
            entries.add(PieEntry(awake.toFloat(), "awake"))
        }

        return entries
    }

    /**
     * Sets the pie chart. Calls generateDataPieChart for diagram data.
     */
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

    /**
     * Generates the analysis data for the weekly analysis with the bar chart.
     */
    private fun generateDataBarChart(): Triple<ArrayList<BarEntry>, List<Int>, Int> { //ArrayList<BarEntry> {
        val entries = ArrayList<BarEntry>()
        val xAxisLabels = mutableListOf<Int>()
        var xIndex = 0.5f
        var maxSleepTime = 0

        val ids = mutableSetOf<Int>()
        for (i in -6..0) {
            ids.add(UserSleepSessionEntity.getIdByDateTime(LocalDate.of(
                dateOfDiagram.plusDays(i.toLong()).year,
                dateOfDiagram.plusDays(i.toLong()).month,
                dateOfDiagram.plusDays(i.toLong()).dayOfMonth
            )))
        }

        ids.reversed()
        for (id in ids) {
            if (sleepSessionData.containsKey(id)) {
                val values = sleepSessionData[id]!!

                //val awake = values.sleepTimes.awakeTime
                val sleep = values.sleepTimes.sleepDuration
                val lightSleep = values.sleepTimes.lightSleepDuration
                val deepSleep = values.sleepTimes.deepSleepDuration
                val remSleep = values.sleepTimes.remSleepDuration

                //if ((sleep + awake) > maxSleepTime) { maxSleepTime = (sleep + awake) }  //Later delete awake from here
                if (sleep > maxSleepTime) { maxSleepTime = sleep }

                entries.add(
                    BarEntry(
                        xIndex, floatArrayOf(
                            //awake.toFloat(),
                            lightSleep.toFloat(),
                            deepSleep.toFloat(),
                            remSleep.toFloat(),
                            //sleep.toFloat()
                        )
                    )
                )
            } else { entries.add(BarEntry(xIndex, floatArrayOf(0F, 0F, 0F))) }
            xAxisLabels.add(id)
            xIndex += 1
        }

        return Triple(entries, xAxisLabels, maxSleepTime)
    }

    /**
     * Sets the bar chart. Calls generateDataBarChart for diagram data.
     */
    private fun setBarChart() { //http://developine.com/android-grouped-stacked-bar-chart-using-mpchart-kotlin/
        val diagramData = generateDataBarChart()

        val barDataSet1 = BarDataSet(diagramData.first, "")
        //barDataSet1.setColors(Color.YELLOW, Color.MAGENTA, Color.BLUE, Color.BLACK, Color.RED)
        barDataSet1.setColors(Color.MAGENTA, Color.BLUE, Color.BLACK)
        //barDataSet1.label = "States"
        //barDataSet1.setDrawIcons(false)
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
        //barChart.description.textSize = 0f
        //barData.setValueFormatter(LargeValueFormatter())
        barChart.data = barData
        barChart.barData.barWidth = 0.75f
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

        val legendEntries = arrayListOf<LegendEntry>()
        //legendEntries.add((LegendEntry("Awake", Legend.LegendForm.SQUARE, 8f, 8f, null ,Color.YELLOW)))
        legendEntries.add((LegendEntry("Light", Legend.LegendForm.SQUARE, 8f, 8f, null ,Color.MAGENTA)))
        legendEntries.add((LegendEntry("Deep", Legend.LegendForm.SQUARE, 8f, 8f, null ,Color.BLUE)))
        legendEntries.add((LegendEntry("REM", Legend.LegendForm.SQUARE, 8f, 8f, null ,Color.BLACK)))
        //legendEntries.add((LegendEntry("Sleep", Legend.LegendForm.SQUARE, 8f, 8f, null ,Color.RED)))
        legend.setCustom(legendEntries)

        //legend.yOffset = 2f
        //legend.xOffset = 2f
        //legend.yEntrySpace = 0f
        legend.textSize = 12f


        val xAxis = barChart.xAxis
        //xAxis.granularity = 1f
        //xAxis.isGranularityEnabled = true
        //xAxis.setCenterAxisLabels(true)
        xAxis.setDrawGridLines(true)
        //xAxis.textSize = 12f

        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValues)

        //xAxis.labelCount = 7
        //xAxis.mAxisMaximum = 6f
        xAxis.setCenterAxisLabels(true)
        //xAxis.setAvoidFirstLastClipping(true)
        //xAxis.spaceMin = 2f
        //xAxis.spaceMax = 4f


        //barChart.setVisibleXRangeMaximum(7f)
        barChart.isDragEnabled = true

        //Y-axis
        barChart.axisRight.isEnabled = true
        barChart.axisRight.axisMinimum = 0f
        barChart.axisRight.labelCount = 10
        //barChart.setScaleEnabled(true)

        //val leftAxis = barChart.axisLeft
        //leftAxis.valueFormatter = LargeValueFormatter()
        //leftAxis.setDrawGridLines(false)
        barChart.axisLeft.spaceTop = 60f
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.labelCount = 20


        if ((diagramData.third > 600) && (diagramData.third < 720)) {
            barChart.axisRight.axisMaximum = 12f
            barChart.axisLeft.axisMaximum = 720f
        }
        else if ((diagramData.third > 720) && (diagramData.third < 840)) {
            barChart.axisRight.axisMaximum = 14f
            barChart.axisLeft.axisMaximum = 840f
        }
        else if ((diagramData.third > 840) && (diagramData.third < 960)) {
            barChart.axisRight.axisMaximum = 16f
            barChart.axisLeft.axisMaximum = 960f
        }
        else if (diagramData.third > 960) { // between 12h and 14h
            barChart.axisRight.axisMaximum = 24f
            barChart.axisLeft.axisMaximum = 1440f
        }
        else {
            barChart.axisRight.axisMaximum = 10f
            barChart.axisLeft.axisMaximum = 600f
        }

        //barChart.setVisibleXRange(0f, 7f)
    }
}
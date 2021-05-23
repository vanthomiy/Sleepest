package com.doitstudio.sleepest_master

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout
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

class HistoryFragment(val applicationContext: Context) : Fragment() {

    private lateinit var sleepDbRepository: DbRepository
    private lateinit var sleep1DbRepository: SleepCalculationDbRepository
    private val scope: CoroutineScope = MainScope()
    private val dbDatabase by lazy { SleepDatabase.getDatabase(applicationContext) }
    private val db1Database by lazy { SleepCalculationDatabase.getDatabase(applicationContext) }

    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
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
        btnSleepAnalysisDay = view.findViewById(R.id.btn_SleepAnalysisDay)
        btnSleepAnalysisWeek = view.findViewById(R.id.btn_SleepAnalysisWeek)
        btnSleepAnalysisMonth = view.findViewById(R.id.btn_SleepAnalysisMonth)
        btnSleepAnalysisPreviousDay = view.findViewById(R.id.btn_SleepAnalysisPreviousDay)
        btnSleepAnalysisNextDay = view.findViewById(R.id.btn_SleepAnalysisNextDay)
        sVSleepAnalysisDay = view.findViewById(R.id.sV_sleepAnalysisChartsDays)
        sVSleepAnalysisWeek = view.findViewById(R.id.sV_sleepAnalysisChartsWeek)
        //sVSleepAnalysisMonth = view.findViewById(R.id.sV_sleepAnalysisChartsMonth)

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

        }

        btnSleepAnalysisNextDay.setOnClickListener {

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
            generateDataBarChart()
        }
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
        var awake = 0
        var sleep = 0
        var ligthSleep = 0
        var deepSleep = 0
        var remSleep = 0
        val entries = ArrayList<PieEntry>()
        var absolute = 0

        for (i in sleepSessionsData[sleepSessions[0]]!!) {
            when (i.sleepState.ordinal) {
                0 -> { awake += 1 }
                1 -> { ligthSleep += 1 }
                2 -> { deepSleep += 1 }
                3 -> { remSleep += 1 }
                4 -> { sleep += 1 }
            }
            absolute += 1
        }

        if (awake > 0) { entries.add(PieEntry((absolute / awake).toFloat(), "awake")) }
        if (ligthSleep > 0) { entries.add(PieEntry((absolute / ligthSleep).toFloat(), "light")) }
        if (deepSleep > 0) { entries.add(PieEntry((absolute / deepSleep).toFloat(), "deep")) }
        if (remSleep > 0) { entries.add(PieEntry((absolute / remSleep).toFloat(), "rem")) }
        if (sleep > 0) { entries.add(PieEntry((absolute / sleep).toFloat(), "sleep")) }

        return entries
    }

    private fun setPieChart() {
        val listColors = ArrayList<Int>()
        listColors.add(resources.getColor(R.color.colorPrimary))
        listColors.add(resources.getColor(R.color.green))
        listColors.add(resources.getColor(R.color.red))

        val pieDataSet = PieDataSet(generateDataPieChart(), "")
        //pieDataSet.colors = listColors

        val pieData = PieData(pieDataSet)
        pieChart.data = pieData

        pieChart.setUsePercentValues(true)
        pieChart.isDrawHoleEnabled = false
        pieChart.description.isEnabled = false
        pieChart.setEntryLabelColor(R.color.black)
        pieChart.animateY(1000, Easing.EaseInOutQuad)
    }

    private fun generateDataBarChart() {
        val barWidth: Float
        val barSpace: Float
        val groupSpace: Float
        val groupCount = 12

        barWidth = 0.15f
        barSpace = 0.07f
        groupSpace = 0.56f

        var xAxisValues = ArrayList<String>()
        xAxisValues.add("Jan")
        xAxisValues.add("Feb")
        xAxisValues.add("Mar")
        xAxisValues.add("Apr")
        xAxisValues.add("May")
        xAxisValues.add("June")
        xAxisValues.add("Jul")
        xAxisValues.add("Aug")
        xAxisValues.add("Sep")
        xAxisValues.add("Oct")
        xAxisValues.add("Nov")
        xAxisValues.add("Dec")

        var yValueGroup1 = ArrayList<BarEntry>()
        var yValueGroup2 = ArrayList<BarEntry>()

        // draw the graph
        var barDataSet1: BarDataSet
        var barDataSet2: BarDataSet


        yValueGroup1.add(BarEntry(1f, floatArrayOf(9.toFloat(), 3.toFloat())))
        yValueGroup2.add(BarEntry(1f, floatArrayOf(2.toFloat(), 7.toFloat())))


        yValueGroup1.add(BarEntry(2f, floatArrayOf(3.toFloat(), 3.toFloat())))
        yValueGroup2.add(BarEntry(2f, floatArrayOf(4.toFloat(), 15.toFloat())))

        yValueGroup1.add(BarEntry(3f, floatArrayOf(3.toFloat(), 3.toFloat())))
        yValueGroup2.add(BarEntry(3f, floatArrayOf(4.toFloat(), 15.toFloat())))

        yValueGroup1.add(BarEntry(4f, floatArrayOf(3.toFloat(), 3.toFloat())))
        yValueGroup2.add(BarEntry(4f, floatArrayOf(4.toFloat(), 15.toFloat())))


        yValueGroup1.add(BarEntry(5f, floatArrayOf(9.toFloat(), 3.toFloat())))
        yValueGroup2.add(BarEntry(5f, floatArrayOf(10.toFloat(), 6.toFloat())))

        yValueGroup1.add(BarEntry(6f, floatArrayOf(11.toFloat(), 1.toFloat())))
        yValueGroup2.add(BarEntry(6f, floatArrayOf(12.toFloat(), 2.toFloat())))


        yValueGroup1.add(BarEntry(7f, floatArrayOf(11.toFloat(), 7.toFloat())))
        yValueGroup2.add(BarEntry(7f, floatArrayOf(12.toFloat(), 12.toFloat())))


        yValueGroup1.add(BarEntry(8f, floatArrayOf(11.toFloat(), 9.toFloat())))
        yValueGroup2.add(BarEntry(8f, floatArrayOf(12.toFloat(), 8.toFloat())))


        yValueGroup1.add(BarEntry(9f, floatArrayOf(11.toFloat(), 13.toFloat())))
        yValueGroup2.add(BarEntry(9f, floatArrayOf(12.toFloat(), 12.toFloat())))

        yValueGroup1.add(BarEntry(10f, floatArrayOf(11.toFloat(), 2.toFloat())))
        yValueGroup2.add(BarEntry(10f, floatArrayOf(12.toFloat(), 7.toFloat())))

        yValueGroup1.add(BarEntry(11f, floatArrayOf(11.toFloat(), 6.toFloat())))
        yValueGroup2.add(BarEntry(11f, floatArrayOf(12.toFloat(), 5.toFloat())))

        yValueGroup1.add(BarEntry(12f, floatArrayOf(11.toFloat(), 2.toFloat())))
        yValueGroup2.add(BarEntry(12f, floatArrayOf(12.toFloat(), 3.toFloat())))


        barDataSet1 = BarDataSet(yValueGroup1, "")
        barDataSet1.setColors(Color.BLUE, Color.RED)
        barDataSet1.label = "2016"
        barDataSet1.setDrawIcons(false)
        barDataSet1.setDrawValues(false)



        barDataSet2 = BarDataSet(yValueGroup2, "")

        barDataSet2.label = "2017"
        barDataSet2.setColors(Color.YELLOW, Color.RED)

        barDataSet2.setDrawIcons(false)
        barDataSet2.setDrawValues(false)

        var barData = BarData(barDataSet1, barDataSet2)

        barChart.description.isEnabled = false
        barChart.description.textSize = 0f
        barData.setValueFormatter(LargeValueFormatter())
        barChart.setData(barData)
        barChart.getBarData().setBarWidth(barWidth)
        barChart.getXAxis().setAxisMinimum(0f)
        barChart.getXAxis().setAxisMaximum(12f)
        barChart.groupBars(0f, groupSpace, barSpace)
        //   barChartView.setFitBars(true)
        barChart.getData().setHighlightEnabled(false)
        barChart.invalidate()

        // set bar label
        var legend = barChart.legend
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM)
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT)
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL)
        legend.setDrawInside(false)

        var legenedEntries = arrayListOf<LegendEntry>()

        legenedEntries.add(LegendEntry("2016", Legend.LegendForm.SQUARE, 8f, 8f, null, Color.RED))
        legenedEntries.add(LegendEntry("2017", Legend.LegendForm.SQUARE, 8f, 8f, null, Color.YELLOW))

        legend.setCustom(legenedEntries)

        legend.setYOffset(2f)
        legend.setXOffset(2f)
        legend.setYEntrySpace(0f)
        legend.setTextSize(5f)

        val xAxis = barChart.getXAxis()
        xAxis.setGranularity(1f)
        xAxis.setGranularityEnabled(true)
        xAxis.setCenterAxisLabels(true)
        xAxis.setDrawGridLines(false)
        xAxis.textSize = 9f

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
        xAxis.setValueFormatter(IndexAxisValueFormatter(xAxisValues))

        xAxis.setLabelCount(12)
        xAxis.mAxisMaximum = 12f
        xAxis.setCenterAxisLabels(true)
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.spaceMin = 4f
        xAxis.spaceMax = 4f

        barChart.setVisibleXRangeMaximum(12f)
        barChart.setVisibleXRangeMinimum(12f)
        barChart.setDragEnabled(true)

        //Y-axis
        barChart.getAxisRight().setEnabled(false)
        barChart.setScaleEnabled(true)

        val leftAxis = barChart.getAxisLeft()
        leftAxis.setValueFormatter(LargeValueFormatter())
        leftAxis.setDrawGridLines(false)
        leftAxis.setSpaceTop(1f)
        leftAxis.setAxisMinimum(0f)


        barChart.data = barData
        barChart.setVisibleXRange(1f, 12f)
    }
}
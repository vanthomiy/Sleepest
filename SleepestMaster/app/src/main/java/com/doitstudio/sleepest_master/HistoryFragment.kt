package com.doitstudio.sleepest_master

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
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
import com.github.mikephil.charting.data.*
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
    private lateinit var sVSleepAnalysisChartsDay : ScrollView
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

        barChart = view.findViewById(R.id.barChart)
        lineChart = view.findViewById(R.id.lineChart_sleepAnalysisDay)
        pieChart = view.findViewById(R.id.pieChart_sleepAnalysisDay)
        btnSleepAnalysisDay = view.findViewById(R.id.btn_SleepAnalysisDay)
        btnSleepAnalysisWeek = view.findViewById(R.id.btn_SleepAnalysisWeek)
        btnSleepAnalysisMonth = view.findViewById(R.id.btn_SleepAnalysisMonth)
        btnSleepAnalysisPreviousDay = view.findViewById(R.id.btn_SleepAnalysisPreviousDay)
        btnSleepAnalysisNextDay = view.findViewById(R.id.btn_SleepAnalysisNextDay)

        sleepSessions = mutableListOf()
        sleepSessionsData = mutableMapOf()

        /*
        btnSleepAnalysisDay.setOnClickListener {
            viewExtendedAlarmSettings.isVisible = !viewExtendedAlarmSettings.isVisible
        }

        btnSleepAnalysisWeek.setOnClickListener {
            viewExtendedAlarmSettings.isVisible = !viewExtendedAlarmSettings.isVisible
        }

        btnSleepAnalysisMonth.setOnClickListener {
            viewExtendedAlarmSettings.isVisible = !viewExtendedAlarmSettings.isVisible
        }

        btnSleepAnalysisPreviousDay.setOnClickListener {

        }

        btnSleepAnalysisNextDay.setOnClickListener {

        }

         */

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
            var b = sleepSessions
            setLineChart()
            setPieChart()
        }

        //setBarChart()

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

    private fun setBarChart() {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(8f, 0f))
        entries.add(BarEntry(2f, 1f))
        entries.add(BarEntry(5f, 2f))
        entries.add(BarEntry(20f, 3f))
        entries.add(BarEntry(15f, 4f))
        entries.add(BarEntry(19f, 5f))

        val barDataSet = BarDataSet(entries, "Cells")

        val data = BarData(barDataSet)
        barChart.data = data // set the data and list of lables into char

        barDataSet.color = resources.getColor(R.color.colorAccent)

        barChart.animateY(100)
    }
}
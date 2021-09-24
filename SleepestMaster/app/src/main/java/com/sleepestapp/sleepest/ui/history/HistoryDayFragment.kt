package com.sleepestapp.sleepest.ui.history

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.Observable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.databinding.FragmentHistoryDayBinding
import com.sleepestapp.sleepest.model.data.ActivityOnDay
import com.sleepestapp.sleepest.model.data.MobilePosition
import com.sleepestapp.sleepest.model.data.SleepState
import com.sleepestapp.sleepest.sleepcalculation.model.UserSleepRating
import com.sleepestapp.sleepest.storage.db.SleepApiRawDataEntity
import com.sleepestapp.sleepest.storage.db.UserSleepSessionEntity
import com.sleepestapp.sleepest.util.SmileySelectorUtil
import com.sleepestapp.sleepest.util.StringUtil
import com.sleepestapp.sleepest.util.TimeConverterUtil
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.sleepestapp.sleepest.sleepcalculation.SleepCalculationHandler
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil.Is24HourFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

/**  */
class HistoryDayFragment : Fragment() {

    private val scope: CoroutineScope = MainScope()

    private val actualContext: Context by lazy { requireActivity().applicationContext }

    /** ViewModel for the main History Fragment. Contains calculations for the weekly and monthly charts. */
    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java) }

    /** ViewModel for the daily calculations. */
    private val viewModelDay by lazy { ViewModelProvider(requireActivity(), factory).get(HistoryDayViewModel::class.java) }

    var factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return  HistoryDayViewModel(
                SleepCalculationHandler.getHandler(actualContext)
            ) as T
        }
    }
    /** Binding for daily history analysis and the corresponding fragment_history_day.xml. */
    private lateinit var binding: FragmentHistoryDayBinding

    /** Contains all relevant values for one sleep session:
     * A list of all [SleepApiRawDataEntity] of the session.
     * The sleepDuration of for the sessions night.
     * The whole session [UserSleepSessionEntity]. */
    private var sleepValues : Triple<List<SleepApiRawDataEntity>, Int, UserSleepSessionEntity>? = null

    /** [PieChart] for the daily sleep analysis. */
    private lateinit var pieChartSleepAnalysis: PieChart

    /** [BarChart] for the daily sleep analysis. */
    private lateinit var barChartSleepAnalysis: BarChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHistoryDayBinding.inflate(inflater, container, false)
        binding.historyDayViewModel = viewModelDay
        viewModelDay.transitionsContainer = binding.lLLinearAnimationLayoutDailyAnalysis

        viewModelDay.is24HourFormat = Is24HourFormat(actualContext)

        // Initial set up for the daily sleep analysis bar chart.
        barChartSleepAnalysis = setBarChart()
        updateBarChart(barChartSleepAnalysis)
        binding.lLSleepAnalysisChartsDaySleepPhases.addView(barChartSleepAnalysis)
        barChartSleepAnalysis.layoutParams.height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 200F, resources.displayMetrics
        ).toInt()
        barChartSleepAnalysis.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        barChartSleepAnalysis.invalidate()


        // Initial set up for the daily sleep analysis pie chart.
        pieChartSleepAnalysis = setPieChart()
        binding.lLSleepAnalysisChartsDaySleepPhasesAmount.addView(pieChartSleepAnalysis)
        pieChartSleepAnalysis.layoutParams.height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 200F, resources.displayMetrics
        ).toInt()
        pieChartSleepAnalysis.layoutParams.width = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 200F, resources.displayMetrics
        ).toInt()
        pieChartSleepAnalysis.invalidate()

        // Listener for changes in the analysis date. If user changes the day for the diagrams.
        viewModel.analysisDate.addOnPropertyChangedCallback(
            object: Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    getDataValues()
                    updateCharts()
                }
            })

        viewModelDay.sleepMoodSmiley.addOnPropertyChangedCallback(
            object: Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    saveSleepRatingDaily()
                }
            }
        )

        viewModel.dataReceived.addOnPropertyChangedCallback(
            object: Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    if (viewModel.dataReceived.get() && !viewModelDay.sleepRatingUpdate) {
                        getDataValues()
                        updateCharts()
                        viewModel.dataReceived.set(false)
                    }
                    viewModelDay.sleepRatingUpdate = false
                }
            }
        )

        return binding.root
    }

    /** Updates all existing charts on the fragment. */
    private fun updateCharts() {
        updateBarChart(barChartSleepAnalysis)
        barChartSleepAnalysis.invalidate()

        updatePieChart(pieChartSleepAnalysis)
        pieChartSleepAnalysis.invalidate()

        updateActivitySmiley()
    }

    private fun maintainVisibilityDayHistory(setVisibility: Boolean) {
        if (setVisibility) {
            binding.iVNoDataAvailable.visibility = View.GONE
            binding.tVNoDataAvailable.visibility = View.GONE
            binding.tVActivitySmileyNoSleepDataAvailable.visibility = View.GONE
            binding.sVSleepAnalysisChartsDays.visibility = View.VISIBLE
        }
        else {
            binding.sVSleepAnalysisChartsDays.visibility = View.GONE
            binding.iVNoDataAvailable.visibility = View.VISIBLE
            binding.tVNoDataAvailable.visibility = View.VISIBLE
            binding.tVActivitySmileyNoSleepDataAvailable.visibility = View.VISIBLE
        }
    }

    /** Save users input of the [UserSleepRating.moodAfterSleep] into database. */
    private fun saveSleepRatingDaily() {
        scope.launch {
            viewModelDay.sleepMoodSmiley.get()?.let {
                sleepValues?.let { sVal ->
                    viewModel.dataBaseRepository.updateMoodAfterSleep(
                        it, sVal.third.id)
                }
            }
        }
    }

    /** Get [sleepValues] for the currently selected analysisDate. */
    private fun getDataValues() {
        viewModel.analysisDate.get()?.let {
            if (viewModel.checkId(it)) {
                sleepValues = viewModel.sleepSessionData[UserSleepSessionEntity.getIdByDateTime(it)]!!
            }
        }
    }

    /** Formats sleep duration times for [setTimeStamps]. */
    private fun generateSleepValueInformation(time: Int): String {
        return kotlin.math.floor((time.toFloat() / 60f).toDouble()).toInt().toString() +
                "h " +
                (time % 60).toString() +
                "min"
    }

    /** Tells the user the exact duration for the selected sleep session. */
    private fun setTimeStamps() {

        // Initial setting necessary in case asynchronous demand of the sleep session (sleepValues) isn`t ready.
        var time = LocalDateTime.of(1970, 1, 1, 0, 0, 0).format(DateTimeFormatter.ISO_TIME)
        viewModelDay.beginOfSleep.set(time)
        viewModelDay.endOfSeep.set(time)

        viewModelDay.awakeTime.set(
            actualContext.getString(R.string.history_day_timeInPhase_awake) + " " + generateSleepValueInformation(0)
        )

        viewModelDay.lightSleepTime.set(
            actualContext.getString(R.string.history_day_timeInPhase_lightSleep) + " " + generateSleepValueInformation(0)
        )

        viewModelDay.deepSleepTime.set(
            actualContext.getString(R.string.history_day_timeInPhase_deepSleep) + " " + generateSleepValueInformation(0)
        )

        viewModelDay.remSleepTime.set(
            actualContext.getString(R.string.history_day_timeInPhase_remSleep) + " " + generateSleepValueInformation(0)
        )

        viewModelDay.sleepTime.set(
            actualContext.getString(R.string.history_day_timeInPhase_sleepSum) + " " + generateSleepValueInformation(0)
        )

        // In case the session is available, set values.
        sleepValues?.let {
            var tempTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli((it.third.sleepTimes.sleepTimeStart.toLong()) * 1000),
                ZoneOffset.systemDefault()
            )

            time = TimeConverterUtil.toTimeFormat(tempTime.hour, tempTime.minute) //tempTime.hour.toString() + ":" + tempTime.minute.toString()
            viewModelDay.beginOfSleep.set(time)
            viewModelDay.sessionId = it.third.id
            viewModelDay.beginOfSleepEpoch.set(it.third.sleepTimes.sleepTimeStart.toLong() * 1000)

            tempTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli((it.third.sleepTimes.sleepTimeEnd.toLong()) * 1000),
                ZoneOffset.systemDefault()
            )

            time = TimeConverterUtil.toTimeFormat(tempTime.hour, tempTime.minute) //tempTime.hour.toString() + ":" + tempTime.minute.toString()
            viewModelDay.endOfSeep.set(time)
            viewModelDay.endOfSleepEpoch.set(it.third.sleepTimes.sleepTimeEnd.toLong() * 1000)

            viewModelDay.awakeTime.set(
                actualContext.getString(R.string.history_day_timeInPhase_awake) + " " + generateSleepValueInformation(it.third.sleepTimes.awakeTime)
            )

            viewModelDay.lightSleepTime.set(
                actualContext.getString(R.string.history_day_timeInPhase_lightSleep) + " " + generateSleepValueInformation(it.third.sleepTimes.lightSleepDuration)
            )

            viewModelDay.deepSleepTime.set(
                actualContext.getString(R.string.history_day_timeInPhase_deepSleep) + " " + generateSleepValueInformation(it.third.sleepTimes.deepSleepDuration)
            )

            viewModelDay.remSleepTime.set(
                actualContext.getString(R.string.history_day_timeInPhase_remSleep) + " " + generateSleepValueInformation(it.third.sleepTimes.remSleepDuration)
            )

            viewModelDay.sleepTime.set(
                actualContext.getString(R.string.history_day_timeInPhase_sleepSum) + " " + generateSleepValueInformation(it.third.sleepTimes.sleepDuration)
            )
        }
    }

    /**  */
    fun generateDataBarChart(): ArrayList<BarEntry> {
        val entries = ArrayList<BarEntry>()
        var xIndex = 0.5f

        viewModel.analysisDate.get()?.let {
            if (viewModel.checkId(it)) {

                setTimeStamps()
                maintainVisibilityDayHistory(true)

                sleepValues?.let {
                    for (rawData in it.first) {
                        for (minute in 0..((it.second / 60).toDouble()).roundToInt()) {
                            when (rawData.sleepState) {
                                SleepState.AWAKE -> {
                                    entries.add(BarEntry(xIndex, 1f))
                                }
                                SleepState.LIGHT -> {
                                    entries.add(BarEntry(xIndex, 2f))
                                }
                                SleepState.DEEP -> {
                                    entries.add(BarEntry(xIndex, 3f))
                                }
                                SleepState.REM -> {
                                    entries.add(BarEntry(xIndex, 4f))
                                }
                                SleepState.SLEEPING -> {
                                    entries.add(BarEntry(xIndex, 5f))
                                }
                                else -> entries.add(BarEntry(xIndex, 6f))
                            }
                            xIndex += 1f
                        }
                    }

                    if (it.third.sleepTimes.sleepTimeStart == 0)
                        maintainVisibilityDayHistory(false)
                } ?: kotlin.run {
                    maintainVisibilityDayHistory(false)
                }
            }
            else {
                maintainVisibilityDayHistory(false)
            }
        }

        return entries
    }

    /**  */
    private fun generateBarDataSet(barEntries: ArrayList<BarEntry>) : BarDataSet {
        val barDataSet = BarDataSet(barEntries, "")

        val colorList = mutableListOf<Int>()
        for (ent in barEntries) {
            when (ent.y) {
                1f -> colorList.add(ContextCompat.getColor(actualContext, R.color.awake_sleep_color))
                2f -> colorList.add(ContextCompat.getColor(actualContext, R.color.light_sleep_color))
                3f -> colorList.add(ContextCompat.getColor(actualContext, R.color.deep_sleep_color))
                4f -> colorList.add(ContextCompat.getColor(actualContext, R.color.rem_sleep_color))
                5f -> colorList.add(ContextCompat.getColor(actualContext, R.color.sleep_sleep_color))
                else -> colorList.add(ContextCompat.getColor(actualContext, R.color.warning_color))
            }
        }

        barDataSet.colors = colorList
        barDataSet.setDrawValues(false)

        return barDataSet
    }

    /**  */
    private fun getBarChartYAxisProportion(entries: ArrayList<BarEntry>) : Float {
        var size = 0f
        for (ent in entries) {
            if (size < ent.y) {
                size = ent.y
            }
        }
        return  size
    }

    /**  */
    fun setBarChart() : BarChart {
        val barChart = BarChart(context)
        val diagramData = generateDataBarChart()
        val barData = BarData(generateBarDataSet(diagramData))
        barChart.data = barData
        visualSetUpBarChart(barChart, diagramData)
        return barChart
    }

    /**  */
    private fun updateBarChart(barChart: BarChart) {
        val diagramData = generateDataBarChart()
        val barData = BarData(generateBarDataSet(diagramData))
        barChart.data = barData
        visualSetUpBarChart(barChart, diagramData)
        barChart.invalidate()
    }

    /**  */
    private fun visualSetUpBarChart(barChart: BarChart, diagramData: ArrayList<BarEntry>) {
        barChart.description.isEnabled = false
        barChart.data.isHighlightEnabled = false

        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        barChart.barData.barWidth = 1.1f
        barChart.xAxis.axisMaximum = diagramData.size.toFloat()

        barChart.setFitBars(true)

        barChart.xAxis.axisMinimum = 0f
        barChart.xAxis.setDrawLabels(false)

        // set bar label
        barChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        barChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        barChart.legend.orientation = Legend.LegendOrientation.HORIZONTAL
        barChart.legend.setDrawInside(false)
        barChart.legend.textSize = 12f
        barChart.legend.textColor = viewModel.checkDarkMode()

        val legendEntryList = mutableListOf<LegendEntry>()

        val sleepStates = SleepState.getListOfSleepStates()
        sleepStates.forEach {
            legendEntryList.add(
                LegendEntry(
                    viewModel.sleepStateString[it],
                    Legend.LegendForm.SQUARE,
                    8f,
                    8f,
                    null,
                    viewModel.sleepStateColor[it]?: 1
                )
            )
        }

        barChart.legend.setCustom(legendEntryList)

        /*
        barChart.legend.setCustom(
            listOf(
                LegendEntry(
                    StringUtil.getStringXml(R.string.history_day_timeInPhase_lightSleep, application),
                    Legend.LegendForm.SQUARE,
                    8f,
                    8f,
                    null,
                    ContextCompat.getColor(actualContext, R.color.light_sleep_color)
                ),
                LegendEntry(
                    StringUtil.getStringXml(R.string.history_day_timeInPhase_deepSleep, application),
                    Legend.LegendForm.SQUARE,
                    8f,
                    8f,
                    null,
                    ContextCompat.getColor(actualContext, R.color.deep_sleep_color)
                ),
                LegendEntry(
                    StringUtil.getStringXml(R.string.history_day_timeInPhase_remSleep, application),
                    Legend.LegendForm.SQUARE,
                    8f,
                    8f,
                    null,
                    ContextCompat.getColor(actualContext, R.color.rem_sleep_color)
                ),
                LegendEntry(
                    StringUtil.getStringXml(R.string.history_day_timeInPhase_awake, application),
                    Legend.LegendForm.SQUARE,
                    8f,
                    8f,
                    null,
                    ContextCompat.getColor(actualContext, R.color.awake_sleep_color)
                ),
                LegendEntry(
                    StringUtil.getStringXml(R.string.history_day_timeInPhase_sleepSum, application),
                    Legend.LegendForm.SQUARE,
                    8f,
                    8f,
                    null,
                    ContextCompat.getColor(actualContext, R.color.sleep_sleep_color)
                )
            )
        ) */

        barChart.isDragEnabled = false

        //Y-axis
        barChart.axisRight.isEnabled = true
        barChart.axisRight.axisMinimum = 0f
        barChart.axisRight.labelCount = 0
        barChart.axisRight.setDrawGridLines(false)
        barChart.axisRight.setDrawLabels(false)

        barChart.axisLeft.spaceTop = 1f
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.labelCount = 0
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisLeft.setDrawLabels(false)

        val proportion = getBarChartYAxisProportion(diagramData)
        barChart.axisRight.axisMaximum = proportion
        barChart.axisLeft.axisMaximum = proportion
        barChart.axisLeft.labelCount = proportion.toInt()

        barChart.setScaleEnabled(false)
        barChart.setTouchEnabled(false)
        barChart.setPinchZoom(false)
        barChart.isDoubleTapToZoomEnabled = false
    }

    /** Generates the data needed for the [PieChart]. */
    private fun generateDataPieChart() : Pair<ArrayList<PieEntry>, BooleanArray> {
        val entries = ArrayList<PieEntry>()
        val sleepTypes = booleanArrayOf(false, false, false, false, false)  //awake, sleep, light, deep, rem

        viewModel.analysisDate.get()?.let {
            if (viewModel.checkId(it)) {
                sleepValues?.let { sVal ->
                    val awake = sVal.third.sleepTimes.awakeTime
                    val sleep = sVal.third.sleepTimes.sleepDuration
                    val lightSleep = sVal.third.sleepTimes.lightSleepDuration
                    val deepSleep = sVal.third.sleepTimes.deepSleepDuration
                    val remSleep = sVal.third.sleepTimes.remSleepDuration

                    if (sVal.third.mobilePosition == MobilePosition.ONTABLE) {
                        if (awake > 0)
                            entries.add(PieEntry(awake.toFloat(), actualContext.getString(R.string.history_day_timeInPhase_awake)))
                        sleepTypes[0] = true
                        if (sleep > 0)
                            entries.add(PieEntry(sleep.toFloat(), actualContext.getString(R.string.history_day_timeInPhase_sleepSum)))
                        sleepTypes[1] = true
                    }
                    else if (sVal.third.mobilePosition == MobilePosition.INBED) {
                        if (awake > 0)
                            entries.add(PieEntry(awake.toFloat(), actualContext.getString(R.string.history_day_timeInPhase_awake)))
                        sleepTypes[0] = true
                        if (lightSleep > 0)
                            entries.add(PieEntry(lightSleep.toFloat(), actualContext.getString(R.string.history_day_timeInPhase_lightSleep)))
                        sleepTypes[2] = true
                        if (deepSleep > 0)
                            entries.add(PieEntry(deepSleep.toFloat(), actualContext.getString(R.string.history_day_timeInPhase_deepSleep)))
                        sleepTypes[3] = true
                        if (remSleep > 0)
                            entries.add(PieEntry(remSleep.toFloat(), actualContext.getString(R.string.history_day_timeInPhase_remSleep)))
                        sleepTypes[4] = true
                    }
                }?: kotlin.run {
                    // If no data is here when the Fragment launches, call again.
                    //viewModel.getSleepData()
                }
            }
        }

        return Pair(entries, sleepTypes)
    }

    /**  */
    private fun setPieChart() : PieChart {
        val chart = PieChart(actualContext)
        val data = generateDataPieChart()
        val pieDataSet = PieDataSet(data.first, "")
        visualSetUpPieChart(chart, pieDataSet, data.second)
        chart.data = PieData(pieDataSet)
        return chart
    }

    /**  */
    private fun updatePieChart(chart: PieChart) {
        val data = generateDataPieChart()
        val pieDataSet = PieDataSet(data.first, "")
        visualSetUpPieChart(chart, pieDataSet, data.second)
        chart.data = PieData(pieDataSet)
    }

    /**  */
    private fun visualSetUpPieChart(chart: PieChart, pieDataSet: PieDataSet, sleepTypes: BooleanArray) {
        val listColors = ArrayList<Int>()
        //sleepTypes[0] = awake, sleepTypes[1] = sleep, sleepTypes[2] = light, sleepTypes[3] = deep, sleepTypes[4] = rem

        if (sleepTypes[0])
            listColors.add(ContextCompat.getColor(actualContext, R.color.awake_sleep_color))
        if (sleepTypes[1])
            listColors.add(ContextCompat.getColor(actualContext, R.color.sleep_sleep_color))
        if (sleepTypes[2])
            listColors.add(ContextCompat.getColor(actualContext, R.color.light_sleep_color))
        if (sleepTypes[3])
            listColors.add(ContextCompat.getColor(actualContext, R.color.deep_sleep_color))
        if (sleepTypes[4])
            listColors.add(ContextCompat.getColor(actualContext, R.color.rem_sleep_color))

        pieDataSet.colors = listColors
        pieDataSet.setDrawValues(false)
        pieDataSet.label

        chart.setCenterTextColor(viewModel.checkDarkMode())
        chart.setHoleColor(viewModel.checkDarkModeInverse())
        chart.setEntryLabelColor(viewModel.checkDarkMode())

        chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        chart.legend.isEnabled = false
        chart.legend.textColor = viewModel.checkDarkMode()

        chart.isDrawHoleEnabled = true
        chart.description.isEnabled = false
        chart.setTouchEnabled(false)
        chart.animateY(500, Easing.EaseInOutQuad)
    }

    /**  */
    private fun updateActivitySmiley() {
        var activityOnDay = 0

        viewModel.analysisDate.get()?.let { it_time ->
            if (viewModel.checkId(it_time)) {
                sleepValues?.let {
                    activityOnDay = when (it.third.userSleepRating.activityOnDay) {
                        ActivityOnDay.NOACTIVITY -> 1
                        ActivityOnDay.SMALLACTIVITY -> 1
                        ActivityOnDay.NORMALACTIVITY -> 2
                        ActivityOnDay.MUCHACTIVITY -> 2
                        ActivityOnDay.EXTREMACTIVITY -> 3
                        else -> 0
                    }
                }
                sleepValues?.let { viewModelDay.sleepMoodSmileyTag.set(it.third.userSleepRating.moodAfterSleep.ordinal) }
            }
        }
        viewModelDay.activitySmiley.set(SmileySelectorUtil.getSmileyActivity(activityOnDay))
    }
}


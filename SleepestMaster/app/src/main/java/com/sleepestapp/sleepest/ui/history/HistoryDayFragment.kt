package com.sleepestapp.sleepest.ui.history

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.transition.TransitionManager
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.databinding.FragmentHistoryDayBinding
import com.sleepestapp.sleepest.model.data.ActivityOnDay
import com.sleepestapp.sleepest.model.data.MobilePosition
import com.sleepestapp.sleepest.model.data.SleepState
import com.sleepestapp.sleepest.sleepcalculation.model.UserSleepRating
import com.sleepestapp.sleepest.util.SmileySelectorUtil
import com.sleepestapp.sleepest.util.TimeConverterUtil
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.sleepestapp.sleepest.sleepcalculation.SleepCalculationHandler
import com.sleepestapp.sleepest.util.DesignUtil
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil.is24HourFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class HistoryDayFragment : Fragment() {

    private val scope: CoroutineScope = MainScope()

    private val actualContext: Context by lazy { requireActivity().applicationContext }

    /**
     * Base ViewModel which contains relevant information for the whole history fragment.
     */
    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java) }

    /**
     * ViewModel for the daily sleep analysis.
     */
    private val viewModelDay by lazy { ViewModelProvider(requireActivity(), factory).get(HistoryDayViewModel::class.java) }

    var factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return  HistoryDayViewModel(
                SleepCalculationHandler(actualContext)
            ) as T
        }
    }

    /**
     * Binding for daily history analysis fragment.
     */
    private lateinit var binding: FragmentHistoryDayBinding

    /**
     * [PieChart] for the daily sleep analysis.
     */
    private lateinit var pieChartSleepAnalysis: PieChart

    /**
     * [BarChart] for the daily sleep analysis.
     */
    private lateinit var barChartSleepAnalysis: BarChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Listener for changes in the analysis date.
        viewModel.analysisDate.observe(viewLifecycleOwner) {
            viewModelDay.getSleepSessionId(it)
            updateCharts()
        }

        binding = FragmentHistoryDayBinding.inflate(inflater, container, false)
        binding.historyDayViewModel = viewModelDay
        binding.lifecycleOwner = this

        viewModelDay.is24HourFormat = is24HourFormat(actualContext)

        // Initial set up for the daily sleep analysis bar chart.
        barChartSleepAnalysis = setBarChart(
            DesignUtil.colorDarkMode(
                DesignUtil.checkDarkModeActive(
                    actualContext,
                    viewModel.appSettingsDarkMode,
                    viewModel.appAutoDarkMode
                )
            )
        )
        updateBarChart(
            barChartSleepAnalysis,
            DesignUtil.colorDarkMode(
                DesignUtil.checkDarkModeActive(
                    actualContext,
                    viewModel.appSettingsDarkMode,
                    viewModel.appAutoDarkMode
                )
            )
        )
        binding.lLSleepAnalysisChartsDaySleepPhases.addView(barChartSleepAnalysis)
        barChartSleepAnalysis.layoutParams.height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            150F,
            resources.displayMetrics
        ).toInt()
        barChartSleepAnalysis.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        barChartSleepAnalysis.invalidate()


        // Initial set up for the daily sleep analysis pie chart.
        pieChartSleepAnalysis = setPieChart(
            DesignUtil.determineHoleColorPieChart(
                DesignUtil.checkDarkModeActive(
                    actualContext,
                    viewModel.appSettingsDarkMode,
                    viewModel.appAutoDarkMode
                )
            )
        )
        binding.lLSleepAnalysisChartsDaySleepPhasesAmount.addView(pieChartSleepAnalysis)
        pieChartSleepAnalysis.layoutParams.height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            175F,
            resources.displayMetrics
        ).toInt()
        pieChartSleepAnalysis.layoutParams.width = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            175F,
            resources.displayMetrics
        ).toInt()
        pieChartSleepAnalysis.invalidate()

        // Listener for changes in the sleepMoodSmiley
        viewModelDay.sleepMoodSmiley.observe(viewLifecycleOwner) {
            saveSleepRatingDaily()
        }

        // Listener for new data which was extracted from the database.
        viewModel.dataReceived.observe(viewLifecycleOwner) {
            if (viewModel.dataReceived.value == true && !viewModelDay.sleepRatingUpdate) {
                updateCharts()
                viewModel.dataReceived.value = false
            }
            viewModelDay.sleepRatingUpdate = false
        }

        // Listener for the actual information button which was selected.
        viewModelDay.actualExpand.observe(viewLifecycleOwner) {
            TransitionManager.beginDelayedTransition(binding.lLLinearAnimationLayoutDailyAnalysis)
        }

        return binding.root
    }

    /**
     * Calls all update functions for the charts in this fragment.
     * [updateBarChart], [updatePieChart],  [updateActivitySmiley].
     */
    private fun updateCharts() {
        updateBarChart(
            barChartSleepAnalysis,
            DesignUtil.colorDarkMode(
                DesignUtil.checkDarkModeActive(
                    actualContext,
                    viewModel.appSettingsDarkMode,
                    viewModel.appAutoDarkMode
                )
            )
        )
        barChartSleepAnalysis.invalidate()

        updatePieChart(
            pieChartSleepAnalysis,
            DesignUtil.determineHoleColorPieChart(
                DesignUtil.checkDarkModeActive(
                    actualContext,
                    viewModel.appSettingsDarkMode,
                    viewModel.appAutoDarkMode
                )
            )
        )
        pieChartSleepAnalysis.invalidate()

        updateActivitySmiley()
    }

    /**
     * Maintains the visibility settings of the daily sleep analysis.
     * If no data is to be shown, the diagrams disappear and an information will appear.
     */
    private fun maintainVisibilityDayHistory(
        setVisibility: Boolean
    ) {
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

    /**
     * Save user input for the [UserSleepRating.moodAfterSleep] into the database.
     * Alters the current value of this day in the [HistoryViewModel.sleepAnalysisData].
     */
    private fun saveSleepRatingDaily() {
        // Save to database.
        scope.launch {
            viewModelDay.sleepMoodSmiley.value?.let {
                viewModel.dataBaseRepository.updateMoodAfterSleep(
                    it,
                    viewModelDay.sessionId
                )
            }
        }

        // Alter value in the currently used sleepAnalysisData.
        viewModel.sleepAnalysisData.firstOrNull {
            x -> x.sleepSessionId == viewModelDay.sessionId
        }?.let { session ->
            viewModelDay.sleepMoodSmiley.value?.let {rating ->
                session.userSleepSessionEntity.userSleepRating.moodAfterSleep = rating
            }
        }
    }

    /**
     * Auxiliary function for [setTimeStamps] which formats the strings for time information.
     */
    private fun generateSleepValueInformation(
        time: Int
    ): String {
        return kotlin.math.floor((time.toFloat() / 60f).toDouble()).toInt().toString() +
                "h " +
                (time % 60).toString() +
                "min"
    }

    /**
     * Tells the user the exact fall asleep time and wakeup time which is to be shown on top of the [barChartSleepAnalysis].
     */
    private fun setTimeStamps() {

        // In case the session is available, set values.
        viewModel.sleepAnalysisData.firstOrNull {
                x -> x.sleepSessionId == viewModelDay.sessionId
        }?.let {
            val sleepTimeStart = LocalDateTime.ofInstant(
                Instant.ofEpochMilli((it.userSleepSessionEntity.sleepTimes.sleepTimeStart.toLong()) * 1000),
                ZoneOffset.systemDefault()
            )

            val sleepTimeEnd = LocalDateTime.ofInstant(
                Instant.ofEpochMilli((it.userSleepSessionEntity.sleepTimes.sleepTimeEnd.toLong()) * 1000),
                ZoneOffset.systemDefault()
            )

            viewModelDay.beginOfSleep.value = (TimeConverterUtil.toTimeFormat(sleepTimeStart.hour, sleepTimeStart.minute))
            viewModelDay.beginOfSleepEpoch.value = (it.userSleepSessionEntity.sleepTimes.sleepTimeStart.toLong() * 1000)

            viewModelDay.endOfSeep.value = (TimeConverterUtil.toTimeFormat(sleepTimeEnd.hour, sleepTimeEnd.minute))
            viewModelDay.endOfSleepEpoch.value = (it.userSleepSessionEntity.sleepTimes.sleepTimeEnd.toLong() * 1000)

            viewModelDay.awakeTime.value = (
                    actualContext.getString(R.string.history_day_timeInPhase_awake) + " " + generateSleepValueInformation(it.userSleepSessionEntity.sleepTimes.awakeTime)
                    )

            viewModelDay.lightSleepTime.value = (
                    actualContext.getString(R.string.history_day_timeInPhase_lightSleep) + " " + generateSleepValueInformation(it.userSleepSessionEntity.sleepTimes.lightSleepDuration)
                    )

            viewModelDay.deepSleepTime.value = (
                    actualContext.getString(R.string.history_day_timeInPhase_deepSleep) + " " + generateSleepValueInformation(it.userSleepSessionEntity.sleepTimes.deepSleepDuration)
                    )

            viewModelDay.remSleepTime.value = (
                    actualContext.getString(R.string.history_day_timeInPhase_remSleep) + " " + generateSleepValueInformation(it.userSleepSessionEntity.sleepTimes.remSleepDuration)
                    )

            viewModelDay.sleepTime.value = (
                    actualContext.getString(R.string.history_day_timeInPhase_sleepSum) + " " + generateSleepValueInformation(it.userSleepSessionEntity.sleepTimes.sleepDuration)
                    )

            // Manage visibility of the text information based on the mobile position
            if (it.userSleepSessionEntity.mobilePosition == MobilePosition.INBED) {
                viewModelDay.timeInSleepPhaseTextField.value = (View.VISIBLE)
            }
            else {
                viewModelDay.timeInSleepPhaseTextField.value = (View.INVISIBLE)
            }
        } ?: run {
            val time = LocalDateTime.of(1970, 1, 1, 0, 0, 0).format(DateTimeFormatter.ISO_TIME)

            viewModelDay.beginOfSleep.value = time
            viewModelDay.endOfSeep.value = time

            viewModelDay.awakeTime.value = (
                    actualContext.getString(R.string.history_day_timeInPhase_awake) + " " + generateSleepValueInformation(0)
                    )

            viewModelDay.lightSleepTime.value = (
                    actualContext.getString(R.string.history_day_timeInPhase_lightSleep) + " " + generateSleepValueInformation(0)
                    )

            viewModelDay.deepSleepTime.value = (
                    actualContext.getString(R.string.history_day_timeInPhase_deepSleep) + " " + generateSleepValueInformation(0)
                    )

            viewModelDay.remSleepTime.value = (
                    actualContext.getString(R.string.history_day_timeInPhase_remSleep) + " " + generateSleepValueInformation(0)
                    )

            viewModelDay.sleepTime.value = (
                    actualContext.getString(R.string.history_day_timeInPhase_sleepSum) + " " + generateSleepValueInformation(0)
                    )
        }
    }

    /**
     * Auxiliary function for generating entries for the [barChartSleepAnalysis].
     * Analysis the [HistoryViewModel.sleepAnalysisData] for the currently selected date and creates a [BarEntry] in a timely fitting manner.
     */
    fun generateDataBarChart(): ArrayList<BarEntry> {
        val entries = ArrayList<BarEntry>()
        var xIndex = 0.5f

        viewModel.sleepAnalysisData.firstOrNull {
                x -> x.sleepSessionId == viewModelDay.sessionId
        }?.let {

            setTimeStamps()
            maintainVisibilityDayHistory(true)

            it.sleepApiRawDataEntity.forEach { rawData ->
                for (minute in 0..((it.userSleepSessionEntity.sleepTimes.sleepDuration / 60).toDouble()).roundToInt()) {
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

                    if (it.userSleepSessionEntity.sleepTimes.sleepTimeStart == 0) {
                        maintainVisibilityDayHistory(false)
                    }
                }
            }
        } ?: kotlin.run {
            maintainVisibilityDayHistory(false)
        }

        return entries
    }

    /**
     * Auxiliary function for generating a [BarDataSet] for the [barChartSleepAnalysis].
     * Adds fitting colors for every [BarEntry] of the diagram.
     */
    private fun generateBarDataSet(
        barEntries: ArrayList<BarEntry>
    ) : BarDataSet {
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

    /**
     * Auxiliary function to determine the height of the [barChartSleepAnalysis].
     * */
    private fun getBarChartYAxisProportion(
        entries: ArrayList<BarEntry>
    ) : Float {
        var size = 0f
        for (ent in entries) {
            if (size < ent.y) {
                size = ent.y
            }
        }
        return  size
    }

    /**
     * Function for creating a new [BarChart] entity.
     * */
    fun setBarChart(
        colorDarkMode: Int
    ): BarChart {
        val barChart = BarChart(context)
        val diagramData = generateDataBarChart()
        val barData = BarData(
            generateBarDataSet(
                diagramData
            )
        )

        barChart.data = barData

        visualSetUpBarChart(
            barChart,
            diagramData,
            colorDarkMode
        )

        return barChart
    }

    /**
     * Function for updating an existing [BarChart] entity.
     * */
    private fun updateBarChart(
        barChart: BarChart,
        colorDarkMode: Int
    ) {
        val diagramData = generateDataBarChart()
        val barData = BarData(
            generateBarDataSet(
                diagramData
            )
        )

        barChart.data = barData

        barChart.notifyDataSetChanged()

        visualSetUpBarChart(
            barChart,
            diagramData,
            colorDarkMode
        )
    }

    /**
     * Auxiliary function for setting up or updating the visual settings of a [BarChart].
     */
    private fun visualSetUpBarChart(
        barChart: BarChart,
        diagramData: ArrayList<BarEntry>,
        colorDarkMode: Int
    ) {
        val proportion = getBarChartYAxisProportion(diagramData)
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

        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.axisMinimum = 0f
        barChart.xAxis.axisMaximum = diagramData.size.toFloat()
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.setDrawLabels(false)

        barChart.legend.textSize = 12f
        barChart.legend.textColor = colorDarkMode
        barChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        barChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        barChart.legend.orientation = Legend.LegendOrientation.HORIZONTAL
        barChart.legend.setDrawInside(false)
        barChart.legend.setCustom(legendEntryList)

        barChart.axisRight.isEnabled = true
        barChart.axisRight.spaceTop = 1f
        barChart.axisRight.axisMinimum = 0f
        barChart.axisRight.axisMaximum = proportion
        barChart.axisRight.labelCount = 0
        barChart.axisRight.setDrawGridLines(false)
        barChart.axisRight.setDrawLabels(false)

        barChart.axisLeft.isEnabled = true
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.axisMaximum = proportion
        barChart.axisLeft.labelCount = proportion.toInt()
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisLeft.setDrawLabels(false)

        barChart.description.isEnabled = false
        barChart.data.isHighlightEnabled = false
        barChart.barData.barWidth = 1.1f
        barChart.isDragEnabled = false
        barChart.isDoubleTapToZoomEnabled = false
        barChart.setFitBars(true)
        barChart.setScaleEnabled(false)
        barChart.setTouchEnabled(false)
        barChart.setPinchZoom(false)

    }

    /**
     * Auxiliary function for generating entries for the [pieChartSleepAnalysis].
     * Analysis the [HistoryViewModel.sleepAnalysisData] for the currently selected date and creates a [PieEntry] for each sleep phase.
     * */
    private fun generateDataPieChart() : Pair<ArrayList<PieEntry>, BooleanArray> {
        val entries = ArrayList<PieEntry>()
        val sleepTypes = booleanArrayOf(false, false, false, false, false)  //awake, sleep, light, deep, rem

        viewModel.sleepAnalysisData.firstOrNull {
                x -> x.sleepSessionId == viewModelDay.sessionId
        }?.let {
            val awake = it.userSleepSessionEntity.sleepTimes.awakeTime
            val sleep = it.userSleepSessionEntity.sleepTimes.sleepDuration
            val lightSleep = it.userSleepSessionEntity.sleepTimes.lightSleepDuration
            val deepSleep = it.userSleepSessionEntity.sleepTimes.deepSleepDuration
            val remSleep = it.userSleepSessionEntity.sleepTimes.remSleepDuration

            if (it.userSleepSessionEntity.mobilePosition == MobilePosition.ONTABLE) {
                if (awake > 0) {
                    entries.add(PieEntry(awake.toFloat(), actualContext.getString(R.string.history_day_timeInPhase_awake)))
                    sleepTypes[0] = true
                }
                if (sleep > 0) {
                    entries.add(PieEntry(sleep.toFloat(), actualContext.getString(R.string.history_day_timeInPhase_sleepSum)))
                    sleepTypes[1] = true
                }
            }
            else if (it.userSleepSessionEntity.mobilePosition == MobilePosition.INBED) {
                if (awake > 0) {
                    entries.add(PieEntry(awake.toFloat(), actualContext.getString(R.string.history_day_timeInPhase_awake)))
                    sleepTypes[0] = true
                }
                if (lightSleep > 0) {
                    entries.add(PieEntry(lightSleep.toFloat(), actualContext.getString(R.string.history_day_timeInPhase_lightSleep)))
                    sleepTypes[2] = true
                }
                if (deepSleep > 0) {
                    entries.add(PieEntry(deepSleep.toFloat(), actualContext.getString(R.string.history_day_timeInPhase_deepSleep)))
                    sleepTypes[3] = true
                }
                if (remSleep > 0) {
                    entries.add(PieEntry(remSleep.toFloat(), actualContext.getString(R.string.history_day_timeInPhase_remSleep)))
                    sleepTypes[4] = true
                }
            }
        }

        return Pair(entries, sleepTypes)
    }

    /**
     * Function for creating a new [PieChart] entity.
     */
    private fun setPieChart(
        holeColorPieChart: Int
    ): PieChart {
        val chart = PieChart(actualContext)
        val data = generateDataPieChart()
        val pieDataSet = PieDataSet(data.first, "")
        visualSetUpPieChart(chart, pieDataSet, data.second, holeColorPieChart)
        chart.data = PieData(pieDataSet)
        return chart
    }

    /**
     * Function for updating an existing [PieChart] entity.
     */
    private fun updatePieChart(
        chart: PieChart,
        holeColorPieChart: Int
    ) {
        val data = generateDataPieChart()
        val pieDataSet = PieDataSet(data.first, "")
        visualSetUpPieChart(chart, pieDataSet, data.second, holeColorPieChart)
        chart.data = PieData(pieDataSet)
        chart.notifyDataSetChanged()
    }

    /**
     * Auxiliary function for setting up or updating the visual settings of a [PieChart].
     */
    private fun visualSetUpPieChart(
        chart: PieChart,
        pieDataSet: PieDataSet,
        sleepTypes: BooleanArray,
        holeColorPieChart: Int
    ) {
        val listColors = ArrayList<Int>()
        //sleepTypes[0] = awake, sleepTypes[1] = sleep, sleepTypes[2] = light, sleepTypes[3] = deep, sleepTypes[4] = rem

        if (sleepTypes[0]) {
            listColors.add(ContextCompat.getColor(actualContext, R.color.awake_sleep_color))
        }
        if (sleepTypes[1]) {
            listColors.add(ContextCompat.getColor(actualContext, R.color.sleep_sleep_color))
        }
        if (sleepTypes[2]) {
            listColors.add(ContextCompat.getColor(actualContext, R.color.light_sleep_color))
        }
        if (sleepTypes[3]) {
            listColors.add(ContextCompat.getColor(actualContext, R.color.deep_sleep_color))
        }
        if (sleepTypes[4]) {
            listColors.add(ContextCompat.getColor(actualContext, R.color.rem_sleep_color))
        }

        pieDataSet.colors = listColors
        pieDataSet.setDrawValues(false)

        //chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        chart.legend.isEnabled = false
        //chart.legend.textColor = viewModel.checkDarkMode()

        chart.description.isEnabled = false
        chart.setEntryLabelColor(Color.WHITE)
        chart.isDrawHoleEnabled = true
        chart.setHoleColor(holeColorPieChart)
        chart.setTouchEnabled(false)
        chart.animateY(500, Easing.EaseInOutQuad)
        //chart.setEntryLabelColor(viewModel.checkDarkMode())
    }

    /**
     * Accesses the value of the [ActivityOnDay] from [HistoryViewModel.sleepAnalysisData].
     * Accesses the value of the [UserSleepRating.moodAfterSleep] from [HistoryViewModel.sleepAnalysisData].
     */
    private fun updateActivitySmiley() {
        var activityOnDay = 0

        viewModel.sleepAnalysisData.firstOrNull {
                x -> x.sleepSessionId == viewModelDay.sessionId
        }?.let {
            activityOnDay = when (it.userSleepSessionEntity.userSleepRating.activityOnDay) {
                ActivityOnDay.NOACTIVITY -> 1
                ActivityOnDay.SMALLACTIVITY -> 1
                ActivityOnDay.NORMALACTIVITY -> 2
                ActivityOnDay.MUCHACTIVITY -> 2
                ActivityOnDay.EXTREMACTIVITY -> 3
                else -> 0
            }
            viewModelDay.sleepMoodSmileyTag.value = it.userSleepSessionEntity.userSleepRating.moodAfterSleep.ordinal
        }

        viewModelDay.activitySmileyTag.value = (SmileySelectorUtil.getSmileyActivity(activityOnDay))
    }
}


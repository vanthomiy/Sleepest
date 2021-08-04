package com.doitstudio.sleepest_master.ui.history

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.Observable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.databinding.FragmentHistoryDayBinding
import com.doitstudio.sleepest_master.model.data.ActivityOnDay
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.MoodType
import com.doitstudio.sleepest_master.sleepcalculation.model.UserSleepRating
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity
import com.doitstudio.sleepest_master.util.SmileySelectorUtil
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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

    /** ViewModel for the main History Fragment. Contains calculations for the weekly and monthly charts. */
    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java) }

    /** ViewModel for the daily calculations. */
    private val viewModelDay by lazy { ViewModelProvider(this).get(HistoryDayViewModel::class.java) }

    /** Binding for daily history analysis and the corresponding fragment_history_day.xml. */
    private lateinit var binding: FragmentHistoryDayBinding

    /** Contains all relevant values for one sleep session:
     * A list of all [SleepApiRawDataEntity] of the session.
     * The sleepDuration of for the sessions night.
     * The whole session [UserSleepSessionEntity]. */
    private lateinit var sleepValues : Triple<List<SleepApiRawDataEntity>, Int, UserSleepSessionEntity>

    /** [LineChart] for the daily sleep analysis. */
    private lateinit var lineChartSleepAnalysis: LineChart

    /** [PieChart] for the daily sleep analysis. */
    private lateinit var pieChartSleepAnalysis: PieChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHistoryDayBinding.inflate(inflater, container, false)
        binding.historyDayViewModel = viewModelDay
        viewModelDay.transitionsContainer = binding.lLLinearAnimationLayoutDailyAnalysis


        // Initial set up for the daily sleep analysis line chart.
        lineChartSleepAnalysis = setLineChart()
        updateLineChart(lineChartSleepAnalysis)
        binding.lLSleepAnalysisChartsDaySleepPhases.addView(lineChartSleepAnalysis)
        lineChartSleepAnalysis.layoutParams.height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 200F, resources.displayMetrics
        ).toInt()
        lineChartSleepAnalysis.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        lineChartSleepAnalysis.invalidate()

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

        // Listener for changes in the analysis date. If user changes the day for the diagramms.
        viewModel.analysisDate.addOnPropertyChangedCallback(
            object: Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    getDataValues()
                    updateLineChart(lineChartSleepAnalysis)
                    lineChartSleepAnalysis.invalidate()

                    updatePieChart(pieChartSleepAnalysis)
                    pieChartSleepAnalysis.invalidate()

                    updateActivitySmiley()
                }
            })

        viewModelDay.sleepMoodSmiley.addOnPropertyChangedCallback(
            object: Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    saveSleepRatingDaily()
                }
            }
        )

        getDataValues()

        return binding.root
    }

    /** Save users input of the [UserSleepRating.moodAfterSleep] into database. */
    private fun saveSleepRatingDaily() {
        scope.launch {
            viewModelDay.sleepMoodSmiley.get()?.let {
                viewModel.dataBaseRepository.updateMoodAfterSleep(
                    it, sleepValues.third.id)
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
        var time = LocalDateTime.of(1970, 1, 1, 0, 0, 0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        viewModelDay.beginOfSleep.set(time)
        viewModelDay.endOfSeep.set(time)

        viewModelDay.awakeTime.set(
            "Awake: " + generateSleepValueInformation(0)
        )

        viewModelDay.lightSleepTime.set(
            "Light: " + generateSleepValueInformation(0)
        )

        viewModelDay.deepSleepTime.set(
            "Deep: " + generateSleepValueInformation(0)
        )

        viewModelDay.sleepTime.set(
            "Sleep: " + generateSleepValueInformation(0)
        )

        // In case the session is available, set values.
        sleepValues.let {
            var tempTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli((it.third.sleepTimes.sleepTimeStart.toLong()) * 1000),
                ZoneOffset.systemDefault()
            )

            time = tempTime.dayOfMonth.toString() + "." + tempTime.monthValue + " " + tempTime.hour + ":" + tempTime.minute
            viewModelDay.beginOfSleep.set(time)

            tempTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli((it.third.sleepTimes.sleepTimeEnd.toLong()) * 1000),
                ZoneOffset.systemDefault()
            )
            
            time = tempTime.dayOfMonth.toString() + "." + tempTime.monthValue + " " + tempTime.hour + ":" + tempTime.minute
            viewModelDay.endOfSeep.set(time)

            viewModelDay.awakeTime.set(
                "Awake: " + generateSleepValueInformation(it.third.sleepTimes.awakeTime)
            )

            viewModelDay.lightSleepTime.set(
                "Light: " + generateSleepValueInformation(it.third.sleepTimes.lightSleepDuration)
            )

            viewModelDay.deepSleepTime.set(
                "Deep: " + generateSleepValueInformation(it.third.sleepTimes.deepSleepDuration)
            )

            viewModelDay.sleepTime.set(
                "Sleep: " + generateSleepValueInformation(it.third.sleepTimes.sleepDuration)
            )
        }
    }

    /** Generates the data needed for the [LineChart].
     * Manages the visibility of the whole view for the daily analysis. */
    private fun generateDataLineChart() : ArrayList<Entry> {
        val entries = ArrayList<Entry>()

        viewModel.analysisDate.get()?.let {
            if (viewModel.checkId(it)) {
                var xValue = 0

                for (rawData in sleepValues.first) {
                    for (minute in 0..((sleepValues.second / 60).toDouble()).roundToInt()) {
                        entries.add(Entry(xValue.toFloat(), rawData.sleepState.ordinal.toFloat()))
                        xValue += 1
                    }
                }

                setTimeStamps()

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

        return entries
    }

    /** Set up the [LineChart] for daily analysis. */
    private fun setLineChart() : LineChart {
        val chart = LineChart(viewModel.context)
        val lineDataSet = LineDataSet(generateDataLineChart(), "")
        visualSetUpLineChart(chart, lineDataSet)
        chart.data = LineData(lineDataSet)
        return chart
    }

    /** Update the [LineChart] in case the user alters the analysis date. */
    fun updateLineChart(chart: LineChart) {
        val lineDataSet = LineDataSet(generateDataLineChart(), "")
        visualSetUpLineChart(chart, lineDataSet)
        chart.data = LineData(lineDataSet)
    }

    /** Visual set up for the [LineChart]. Only matches proportions for the daily sleep analysis purposes. */
    private fun visualSetUpLineChart(chart: LineChart, lineDataSet: LineDataSet) {
        lineDataSet.setDrawValues(false)
        lineDataSet.setDrawFilled(true)
        lineDataSet.setDrawCircles(false)
        lineDataSet.lineWidth = 2f
        lineDataSet.fillColor = ContextCompat.getColor(viewModel.context, R.color.sleep_sleep_color)
        lineDataSet.fillAlpha = 255
        lineDataSet.color = ContextCompat.getColor(viewModel.context, R.color.awake_sleep_color)
        lineDataSet.fillDrawable = ContextCompat.getDrawable(viewModel.context, R.drawable.bg_spark_line)

        val yAxisValues = ArrayList<String>()

        if (lineDataSet.yMax == 4f) { // TODO Maybe change this to Phone.INBED // Phone.NOTINBED
            // Only sleep and awake is detected. Phone not in bed.
            yAxisValues.add("Awake")
            yAxisValues.add("")
            yAxisValues.add("")
            yAxisValues.add("")
            yAxisValues.add("Sleep")
            yAxisValues.add("")
            chart.axisLeft.labelCount = 5
            chart.axisLeft.axisMaximum = 5f
        }
        else {
            // Normal night with all sleep phases detected. Phone in bed.
            yAxisValues.add("Awake")
            yAxisValues.add("Light")
            yAxisValues.add("Deep")
            chart.axisLeft.labelCount = 2
            chart.axisLeft.axisMaximum = 2f
        }

        chart.axisLeft.valueFormatter = IndexAxisValueFormatter(yAxisValues)
        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.setDrawGridLines(false)
        chart.axisLeft.textColor = viewModel.checkDarkMode()
        chart.legend.isEnabled= false

        chart.axisRight.setDrawLabels(false)
        chart.axisRight.setDrawGridLines(false)

        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.setDrawLabels(false)

        /*
        chart.xAxis.labelCount = 6
        chart.xAxis.textColor = viewModel.checkDarkMode()
        chart.xAxis.setCenterAxisLabels(false)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        val xAxisValues = ArrayList<String>()
        xAxisValues.add("23:00")
        xAxisValues.add("6:00")
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValues)
         */

        chart.description.isEnabled = false
        chart.setScaleEnabled(false)
        chart.setTouchEnabled(false)
        chart.setPinchZoom(false)
        chart.isDoubleTapToZoomEnabled = false

        chart.animateX(500)
    }

    /** Generates the data needed for the [PieChart]. */
    private fun generateDataPieChart() : Pair<ArrayList<PieEntry>, BooleanArray> {
        val entries = ArrayList<PieEntry>()
        val sleepTypes = booleanArrayOf(false, false, false, false)

        viewModel.analysisDate.get()?.let {
            if (viewModel.checkId(it)) {
                val awake = sleepValues.third.sleepTimes.awakeTime
                val sleep = sleepValues.third.sleepTimes.sleepDuration
                val lightSleep = sleepValues.third.sleepTimes.lightSleepDuration
                val deepSleep = sleepValues.third.sleepTimes.deepSleepDuration

                if (sleepValues.third.mobilePosition == MobilePosition.ONTABLE) {
                    entries.add(PieEntry(awake.toFloat(), "Awake"))
                    sleepTypes[0] = true
                    entries.add(PieEntry(sleep.toFloat(), "Sleep"))
                    sleepTypes[1] = true
                }
                else if (sleepValues.third.mobilePosition == MobilePosition.INBED) {
                    if (lightSleep != 0 && deepSleep != 0 && awake == 0) {
                        entries.add(PieEntry(lightSleep.toFloat(), "Light"))
                        sleepTypes[2] = true
                        entries.add(PieEntry(deepSleep.toFloat(), "Deep"))
                        sleepTypes[3] = true
                    }
                    else if (lightSleep != 0 && deepSleep == 0 && awake != 0) {
                        entries.add(PieEntry(lightSleep.toFloat(), "Light"))
                        sleepTypes[2] = true
                        entries.add(PieEntry(awake.toFloat(), "Awake"))
                        sleepTypes[0] = true
                    }
                    else {
                        entries.add(PieEntry(lightSleep.toFloat(), "Light"))
                        sleepTypes[2] = true
                        entries.add(PieEntry(deepSleep.toFloat(), "Deep"))
                        sleepTypes[3] = true
                        entries.add(PieEntry(awake.toFloat(), "Awake"))
                        sleepTypes[0] = true
                    }
                }
            }
        }

        return Pair(entries, sleepTypes)
    }

    /**  */
    private fun setPieChart() : PieChart {
        val chart = PieChart(viewModel.context)
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
        //sleepTypes[0] = awake, sleepTypes[1] = sleep, sleepTypes[2] = light, sleepTypes[3] = deep

        if (sleepTypes[0] && sleepTypes[1]) {
            listColors.add(ContextCompat.getColor(viewModel.context, R.color.awake_sleep_color))
            listColors.add(ContextCompat.getColor(viewModel.context, R.color.sleep_sleep_color))
        }
        else if (sleepTypes[2] && sleepTypes[3] && !sleepTypes[0]) {
            listColors.add(ContextCompat.getColor(viewModel.context, R.color.light_sleep_color))
            listColors.add(ContextCompat.getColor(viewModel.context, R.color.deep_sleep_color))
        }
        else {
            listColors.add(ContextCompat.getColor(viewModel.context, R.color.light_sleep_color))
            listColors.add(ContextCompat.getColor(viewModel.context, R.color.deep_sleep_color))
            listColors.add(ContextCompat.getColor(viewModel.context, R.color.awake_sleep_color))
        }

        pieDataSet.colors = listColors
        pieDataSet.setDrawValues(false)
        pieDataSet.label

        chart.setCenterTextColor(Color.WHITE)
        chart.setHoleColor(Color.BLACK)
        chart.setEntryLabelColor(Color.WHITE)
        chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER

        chart.isDrawHoleEnabled = true
        chart.description.isEnabled = false
        chart.legend.textColor = viewModel.checkDarkMode()
        chart.setTouchEnabled(false)
        chart.animateY(500, Easing.EaseInOutQuad)
    }

    /**  */
    private fun updateActivitySmiley() {
        var activityOnDay = 0

        viewModel.analysisDate.get()?.let { it_time ->
            if (viewModel.checkId(it_time)) {
                sleepValues.let {
                    activityOnDay = when (it.third.userSleepRating.activityOnDay) {
                        ActivityOnDay.NOACTIVITY -> 1
                        ActivityOnDay.SMALLACTIVITY -> 1
                        ActivityOnDay.NORMALACTIVITY -> 2
                        ActivityOnDay.MUCHACTIVITY -> 2
                        ActivityOnDay.EXTREMACTIVITY -> 3
                        else -> 0
                    }
                }
                // Update sleepRating Bar
                //binding.rBSleepRatingBarDaily.rating = sleepValues.third.userSleepRating.moodAfterSleep.ordinal.toFloat()
                viewModelDay.sleepMoodSmileyTag.set(sleepValues.third.userSleepRating.moodAfterSleep.ordinal)
            }
        }
        viewModelDay.activitySmiley.set(SmileySelectorUtil.getSmileyActivity(activityOnDay))
    }
}


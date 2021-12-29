package com.sleepestapp.sleepest.ui.history

import android.graphics.drawable.Drawable
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepestapp.sleepest.model.data.MobilePosition
import com.sleepestapp.sleepest.model.data.SleepState
import com.sleepestapp.sleepest.sleepcalculation.SleepCalculationHandler
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.storage.db.UserSleepSessionEntity
import com.sleepestapp.sleepest.util.SmileySelectorUtil
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.sleepestapp.sleepest.model.data.SleepDataAnalysis
import com.sleepestapp.sleepest.storage.db.UserSleepSessionEntity.Companion.getIdByDateTimeWithTimeZone
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.*
import java.util.*
import kotlin.collections.ArrayList

class HistoryViewModel(
    val dataStoreRepository: DataStoreRepository,
    val dataBaseRepository: DatabaseRepository,
    val sleepCalculationHandler: SleepCalculationHandler
    ) : ViewModel() {

    /**
     * Currently selected analysis date which will be displayed in the history.
     */
    var analysisDate = MutableLiveData(LocalDate.now())

    /**
     * Currently selected analysis date range which will be displayed in the history.
     */
    var analysisRangeString = MutableLiveData("")

    /**
     * The year of the currently selected analysis date range which will be displayed in the history.
     */
    var analysisRangeYearString = MutableLiveData("")

    /**
     * The first date for which sleep data is available.
     */
    var firstDayWithData = 0L

    /**
     * Indicates that [checkSessionIntegrity] is currently working.
     */
    var onWork = false

    /**
     * Container for the x-axis values of the bar charts.
     */
    var xAxisValues = ArrayList<String>()

    /**
     * Container for the labels of the weekly charts.
     */
    var xAxisValuesWeek = ArrayList<String>()

    /**
     * Container for the strings of the [SleepState] for the legend of the diagrams.
     */
    var sleepStateString = mutableMapOf<SleepState, String>()

    /**
     * Container for the colors of the [SleepState] for the legend of the diagrams.
     */
    var sleepStateColor = mutableMapOf<SleepState, Int>()

    /**
     * Maintains the visibility of the diagrams in the weekly analysis based on the available information.
     */
    val visibilityManagerWeekDiagrams = MutableLiveData(false)

    /**
     * Maintains the visibility of the diagrams in the monthly analysis based on the available information.
     */
    val visibilityManagerMonthDiagrams = MutableLiveData(false)

    /**
     * Container for the background drawable of the activity analysis [LineChart].
     */
    var activityBackgroundDrawable : Drawable? = null

    /**
     * Indicates that [getSleepData] has finished and new data was pulled from the database.
     */
    val dataReceived = MutableLiveData(false)

    /**
     * List of the currently loaded [UserSleepSessionEntity] data from the database.
     */
    val sleepAnalysisData = mutableListOf<SleepDataAnalysis>()

    /**
     * Contains the dark mode settings of the app.
     */
    var appSettingsDarkMode = false

    /**
     * Contains the auto dark mode settings of the app.
     */
    var appAutoDarkMode = false

    /**
     * Maintains the visibility of the information buttons and its text fields.
     */
    val actualExpand = MutableLiveData(-1)

    val goneState = MutableLiveData(View.GONE)

    val visibleState = MutableLiveData(View.VISIBLE)

    fun onInfoClicked(
        view: View
    ){
        val value = view.tag.toString()
        actualExpand.value = if(actualExpand.value == value.toIntOrNull()) -1 else value.toIntOrNull()
    }

    /**
     * Handler for altering the [analysisDate] based on the currently selected analysis range.
     */
    fun onPreviousDateClick(
        range: Int
    ) {
        analysisDate.let {
            when (range) {
                0 -> {
                    val fistDayWithData = LocalDateTime.ofEpochSecond(
                        firstDayWithData,
                        0,
                        ZoneOffset.UTC
                    ).toLocalDate().toEpochDay()

                    if (it.value?.minusDays(1L)?.toEpochDay()!! >= fistDayWithData) {
                        it.value = it.value?.minusDays(1L)
                    }
                }
                1 -> {
                    val fistDayWithData = LocalDateTime.ofEpochSecond(
                        firstDayWithData,
                        0,
                        ZoneOffset.UTC
                    ).toLocalDate()

                    val firstDayOfWeekWithData : Long = when (fistDayWithData.dayOfWeek.value) {
                        1 -> fistDayWithData.plusDays(0L).toEpochDay() // Monday
                        2 -> fistDayWithData.minusDays(1L).toEpochDay() // Tuesday
                        3 -> fistDayWithData.minusDays(2L).toEpochDay() // Wednesday
                        4 -> fistDayWithData.minusDays(3L).toEpochDay() // Thursday
                        5 -> fistDayWithData.minusDays(4L).toEpochDay() // Friday
                        6 -> fistDayWithData.minusDays(5L).toEpochDay() // Saturday
                        else -> fistDayWithData.minusDays(6L).toEpochDay() // Sunday
                    }

                    val analysisDay = it.value?.minusWeeks(1L)?.toEpochDay()!!

                    if (analysisDay >= firstDayOfWeekWithData) {
                        it.value = it.value?.minusWeeks(1L)
                    }
                }
                2 -> {
                    val fistMonthWithData = LocalDateTime.ofEpochSecond(
                        firstDayWithData.toLong(),
                        0,
                        ZoneOffset.UTC
                    ).toLocalDate().withDayOfMonth(1).toEpochDay()

                    if (it.value?.minusMonths(1L)?.withDayOfMonth(1)?.toEpochDay()!! >= fistMonthWithData) {
                        it.value = it.value?.minusMonths(1L)
                    }
                }
            }
        }
    }

    /**
     * Handler for altering the [analysisDate] based on the currently selected analysis range.
     */
    fun onNextDateClick(
        range: Int
    ) {
        analysisDate.let {

            when (range) {
                0 -> {
                    if (LocalDate.now().toEpochDay() >= it.value?.plusDays(1L)?.toEpochDay()!!) {
                        it.value = it.value?.plusDays(1L)
                    }
                }

                1 -> {
                    val actualDate = LocalDate.now()
                    val actualEndOfWeekDate : Long = when (actualDate.dayOfWeek.value) {
                        1 -> actualDate.plusDays(6L).toEpochDay() // Monday
                        2 -> actualDate.plusDays(5L).toEpochDay() // Tuesday
                        3 -> actualDate.plusDays(4L).toEpochDay() // Wednesday
                        4 -> actualDate.plusDays(3L).toEpochDay() // Thursday
                        5 -> actualDate.plusDays(2L).toEpochDay() // Friday
                        6 -> actualDate.plusDays(1L).toEpochDay() // Saturday
                        else -> actualDate.plusDays(0L).toEpochDay() // Sunday
                    }

                    val analysisDay = it.value?.plusWeeks(1L)?.toEpochDay()!!

                    if (actualEndOfWeekDate >= analysisDay) {
                        if (analysisDay > LocalDate.now().toEpochDay()) {
                            it.value = LocalDate.now()
                        } else {
                            it.value = it.value?.plusWeeks(1L)
                        }
                    }
                }
                
                2 -> {
                    val actualEndOfMonthDate = LocalDate.now().withDayOfMonth(
                        LocalDate.now().lengthOfMonth()
                    ).toEpochDay()

                    val analysisDay = it.value?.plusMonths(1L)?.toEpochDay()!!

                    if (actualEndOfMonthDate >= analysisDay) {
                        if (analysisDay > LocalDate.now().toEpochDay()) {
                            it.value = LocalDate.now()
                        } else {
                            it.value = it.value?.plusMonths(1L)
                        }
                    }
                }
            }
        }
    }

    /**
     * Pulls the required data from the database and fills the [sleepAnalysisData] with [SleepDataAnalysis] instances.
     */
    fun getSleepData() {
        val ids = mutableSetOf<Int>()
        analysisDate.value?.let {
            val startDayToGet = it.minusMonths(1L).withDayOfMonth(1)
            val endDayToGet = it.withDayOfMonth(it.lengthOfMonth())
            val dayDifference = (endDayToGet.toEpochDay() - startDayToGet.toEpochDay()).toInt()

            for (day in 0..dayDifference) {
                ids.add(
                    getIdByDateTimeWithTimeZone(
                        LocalDate.of(
                            startDayToGet.plusDays(day.toLong()).year,
                            startDayToGet.plusDays(day.toLong()).month,
                            startDayToGet.plusDays(day.toLong()).dayOfMonth,
                        )
                    )
                )
            }
        }

        viewModelScope.launch {
            appSettingsDarkMode = dataStoreRepository.settingsDataFlow.first().designDarkMode
            appAutoDarkMode = dataStoreRepository.settingsDataFlow.first().designAutoDarkMode

            for (id in ids) {
                val session = dataBaseRepository.getSleepSessionById(id).first().firstOrNull()

                session?.let {
                    sleepAnalysisData.add(
                        SleepDataAnalysis(
                            id,
                            dataBaseRepository.getSleepApiRawDataBetweenTimestamps(
                                session.sleepTimes.sleepTimeStart,
                                session.sleepTimes.sleepTimeEnd).first()?.sortedBy { x -> x.timestampSeconds }?: listOf(),
                            session
                        )
                    )
                }
            }
            firstDayWithData = (dataBaseRepository.getOldestId().first()?: getIdByDateTimeWithTimeZone(LocalDate.now())).toLong()
            checkSessionIntegrity()
        }
    }

    /**
     * Checks if the currently used data set from the database contains any unusual values.
     * If so, the [SleepCalculationHandler] is called to correct them.
     */
    private suspend fun checkSessionIntegrity() {
        onWork = true
        var data = false

        sleepAnalysisData.forEach {
            val mobilePosition = it.userSleepSessionEntity.mobilePosition
            val isSleepStateSleeping = it.sleepApiRawDataEntity.any { x -> x.sleepState == SleepState.SLEEPING }
            val isSleepStateUnidentified = it.sleepApiRawDataEntity.any { x -> x.sleepState == SleepState.NONE }

            if (isSleepStateUnidentified) {
                sleepCalculationHandler.checkIsUserSleeping(
                    LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(it.sleepSessionId.toLong() * 1000),
                        ZoneOffset.systemDefault(),

                    ),
                    false,
                    true
                )
                data = true
            }

            if (mobilePosition == MobilePosition.INBED && isSleepStateSleeping) {
                sleepCalculationHandler.defineUserWakeup(
                    LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(it.sleepSessionId.toLong() * 1000),
                        ZoneOffset.systemDefault()
                    ),
                    false
                )
                data = true

            }

            if (mobilePosition == MobilePosition.UNIDENTIFIED) {
                sleepCalculationHandler.defineUserWakeup(
                    LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(it.sleepSessionId.toLong() * 1000),
                        ZoneOffset.systemDefault()
                    ),
                    false,
                    recalculateMobilePosition = true
                )
                data = true
            }

            if (it.userSleepSessionEntity.sleepTimes.sleepTimeStart == 0) {
                dataBaseRepository.deleteUserSleepSession(it.userSleepSessionEntity)
                data = true
            }
        }

        if (data) {
            sleepAnalysisData.clear()
            getSleepData()
        } else {
            dataReceived.value = true
            onWork = false
        }
    }

    /**
     * Auxiliary function for generating entries for the weekly and monthly sleep analysis [BarChart].
     * Analysis the [sleepAnalysisData] for the currently selected range and creates a [BarEntry] for every day within the range.
     */
    fun generateDataBarChart(
        range: Int,
        endDateOfDiagram: LocalDate
    ): Triple<ArrayList<BarEntry>, List<Int>, Int> {
        val entries = ArrayList<BarEntry>()
        val xAxisLabels = mutableListOf<Int>()
        var xIndex = 0.5f
        var maxSleepTime = 0f
        var visibilityManager = false

        val ids = mutableSetOf<Int>()
        for (i in -(range-2)..1) {
            ids.add(
                getIdByDateTimeWithTimeZone(
                    LocalDate.ofEpochDay(
                        endDateOfDiagram.toEpochDay().plus((i - 1).toLong())
                    )
                )
            )
        }

        ids.reversed()
        for (id in ids) {

            sleepAnalysisData.firstOrNull {
                    x -> x.sleepSessionId == id
            }?.let {
                if (it.userSleepSessionEntity.sleepTimes.sleepDuration > 0) {
                    visibilityManager = true
                }
                val awake = it.userSleepSessionEntity.sleepTimes.awakeTime / 60f
                val sleep = it.userSleepSessionEntity.sleepTimes.sleepDuration / 60f
                val lightSleep = it.userSleepSessionEntity.sleepTimes.lightSleepDuration / 60f
                val deepSleep = it.userSleepSessionEntity.sleepTimes.deepSleepDuration / 60f
                val remSleep = it.userSleepSessionEntity.sleepTimes.remSleepDuration / 60f

                if (((sleep + awake) * 60f) > maxSleepTime) {
                    maxSleepTime = (sleep + awake) * 60f
                }

                if (it.userSleepSessionEntity.mobilePosition == MobilePosition.INBED) {
                    entries.add(
                        BarEntry(
                            xIndex, floatArrayOf(
                                lightSleep,
                                deepSleep,
                                remSleep,
                                0F,
                                awake
                            )
                        )
                    )

                }
                else {
                    entries.add(
                        BarEntry(
                            xIndex, floatArrayOf(
                                0F,
                                0F,
                                0F,
                                sleep,
                                awake
                            )
                        )
                    )
                }
            } ?: kotlin.run {
                entries.add(
                    BarEntry(
                        xIndex, floatArrayOf(
                            0F,
                            0F,
                            0F,
                            0F,
                            0F
                        )
                    )
                )
            }

            xAxisLabels.add(id)
            xIndex += 1
        }

        if (range < 21) {
            visibilityManagerWeekDiagrams.value = visibilityManager
        }
        else if (range > 21) {
            visibilityManagerMonthDiagrams.value = visibilityManager
        }

        return Triple(entries, xAxisLabels, maxSleepTime.toInt())
    }

    /**
     * Auxiliary function for generating a [BarDataSet] for the weekly and monthly sleep analysis [BarChart].
     * Adds fitting colors for every [BarEntry] of the diagram.
     */
    private fun generateBarDataSet(
        barEntries: ArrayList<BarEntry>
    ) : BarDataSet {
        val barDataSet = BarDataSet(barEntries, "")
        val barDataColors = mutableListOf<Int>()
        val sleepStates = SleepState.getListOfSleepStates()

        sleepStates.forEach {
            barDataColors.add(
                sleepStateColor[it] ?: 0
            )
        }

        barDataSet.colors = barDataColors
        barDataSet.setDrawValues(false)

        return barDataSet
    }

    /**
     * Auxiliary function to determine the height of the weekly and monthly sleep analysis [BarChart].
     * */
    private fun getBarChartYAxisProportion(
        sleepAmount: Int
    ) : Float {
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

    /**
     * Function for creating a new [BarChart] entity.
     * */
    fun setBarChart(
        barChart: BarChart,
        range: Int,
        endDateOfDiagram: LocalDate,
        colorDarkMode: Int
    ) : BarChart {
        val diagramData = generateDataBarChart(
            range,
            endDateOfDiagram
        )

        val barData = BarData(
            generateBarDataSet(
                diagramData.first
            )
        )

        barChart.data = barData

        visualSetUpBarChart(
            barChart,
            diagramData,
            range,
            colorDarkMode
        )

        return barChart
    }

    /**
     * Function for updating an existing [BarChart] entity.
     * */
    fun updateBarChart(
        barChart: BarChart,
        range: Int,
        endDateOfDiagram: LocalDate,
        colorDarkMode: Int
    ) {
        val diagramData = generateDataBarChart(
            range,
            endDateOfDiagram
        )

        val barData = BarData(
            generateBarDataSet(
                diagramData.first
            )
        )

        barChart.data = barData

        visualSetUpBarChart(
            barChart,
            diagramData,
            range,
            colorDarkMode
        )
        barChart.invalidate()
    }

    /**
     * Auxiliary function for setting up or updating the visual settings of a [BarChart].
     */
    private fun visualSetUpBarChart(
        barChart: BarChart,
        diagramData: Triple<ArrayList<BarEntry>, List<Int>, Int>,
        range: Int,
        colorDarkMode: Int
    ) {
        val proportion = getBarChartYAxisProportion(diagramData.third)
        val legendEntryList = mutableListOf<LegendEntry>()
        val sleepStates = SleepState.getListOfSleepStates()
        val barWidth: Any
        val axisMaximum: Any
        val labelCount: Any
        xAxisValues.clear()

        if (range > 21) {
            for (i in diagramData.second.indices) {
                val date = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(
                        (diagramData.second[i].toLong()) * 1000), //Constants.DAY_IN_SECONDS
                    ZoneOffset.systemDefault()
                )

                if (i == 0 || i == 10 || i == 20  || i == (diagramData.second.size - 1)) {
                    xAxisValues.add(date.dayOfMonth.toString())
                }
                else {
                    xAxisValues.add("")
                }
            }

            barWidth = 0.5f
            axisMaximum = (diagramData.second.size).toFloat()
            labelCount = diagramData.second.size
        }
        else {
            // Set up the chart for weekly analysis
            xAxisValues.addAll(xAxisValuesWeek)
            barWidth = 0.75f
            axisMaximum = 7f
            labelCount = 7
        }

        sleepStates.forEach {
            legendEntryList.add(
                LegendEntry(
                    sleepStateString[it],
                    Legend.LegendForm.SQUARE,
                    8f,
                    8f,
                    null,
                    sleepStateColor[it]?: 1
                )
            )
        }

        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValues)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.axisMinimum = 0f
        barChart.xAxis.axisMaximum = axisMaximum
        barChart.xAxis.labelCount = labelCount
        barChart.xAxis.textColor = colorDarkMode
        barChart.xAxis.setCenterAxisLabels(true)
        barChart.xAxis.setDrawGridLines(false)

        barChart.legend.textSize = 12f
        barChart.legend.textColor = colorDarkMode
        barChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        barChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        barChart.legend.orientation = Legend.LegendOrientation.HORIZONTAL
        barChart.legend.setDrawInside(false)
        barChart.legend.setCustom(legendEntryList)

        barChart.axisRight.isEnabled = true
        barChart.axisRight.spaceTop = 0F
        barChart.axisRight.axisMinimum = 0F
        barChart.axisRight.axisMaximum = 0F
        barChart.axisRight.labelCount = 0
        barChart.axisRight.setDrawGridLines(false)
        barChart.axisRight.setDrawLabels(false)

        barChart.axisLeft.isEnabled = true
        barChart.axisLeft.spaceTop = 0f
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.axisMaximum = proportion
        barChart.axisLeft.textColor = colorDarkMode
        barChart.axisLeft.labelCount = proportion.toInt()
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.setDrawLabels(true)

        barChart.description.isEnabled = false
        barChart.data.isHighlightEnabled = false
        barChart.barData.barWidth = barWidth
        barChart.isDragEnabled = false
        barChart.isDoubleTapToZoomEnabled = false
        barChart.setFitBars(true)
        barChart.setScaleEnabled(false)
        barChart.setTouchEnabled(false)
        barChart.setPinchZoom(false)
    }

    /**
     * Auxiliary function for generating entries for the weekly and monthly activity analysis chart.
     * Analysis the [sleepAnalysisData] for the currently selected range and creates a [Entry] for each day.
     */
    private fun generateDataActivityChart(
        range: Int,
        endDateOfDiagram: LocalDate
    ): ArrayList<Entry> {
        val entries = ArrayList<Entry>()
        var xValue = 0

        val ids = mutableSetOf<Int>()
        for (i in -(range - 2)..1) {
            ids.add(
                getIdByDateTimeWithTimeZone(
                    LocalDate.ofEpochDay(
                        endDateOfDiagram.toEpochDay().plus((i - 1).toLong())
                    )
                )
            )
        }

        ids.reversed()
        for (id in ids) {

            sleepAnalysisData.firstOrNull {
                    x -> x.sleepSessionId == id
            }?.let {
                entries.add(
                    Entry(
                        xValue.toFloat(),
                        it.userSleepSessionEntity.userSleepRating.activityOnDay.ordinal.toFloat()
                    )
                )
            } ?: kotlin.run {
                entries.add(Entry(xValue.toFloat(), 0f))
            }

            xValue += 1
        }
        return entries
    }

    /**
     * Function for creating a new [LineChart] entity.
     */
    fun setActivityChart(
        chart: LineChart,
        range: Int,
        endDateOfDiagram: LocalDate,
        colorDarkMode: Int
    ) : LineChart {
        val lineDataSet = LineDataSet(
            generateDataActivityChart(
                range,
                endDateOfDiagram
            ),
            ""
        )

        visualSetUpActivityChart(
            chart,
            lineDataSet,
            colorDarkMode
        )

        chart.data = LineData(lineDataSet)
        return chart
    }

    /**
     * Function for updating an existing [LineChart] entity.
     */
    fun updateActivityChart(
        chart: LineChart,
        range: Int,
        endDateOfDiagram: LocalDate,
        colorDarkMode: Int
    ) {
        val lineDataSet = LineDataSet(
            generateDataActivityChart(
                range,
                endDateOfDiagram
            ),
            ""
        )

        visualSetUpActivityChart(
            chart,
            lineDataSet,
            colorDarkMode
        )

        chart.data = LineData(lineDataSet)
    }

    /**
     * Auxiliary function for setting up or updating the visual settings of a [LineChart].
     */
    private fun visualSetUpActivityChart(
        chart: LineChart,
        lineDataSet: LineDataSet,
        colorDarkMode: Int
    ) {
        val yAxisValues = arrayListOf(
            "",
            SmileySelectorUtil.getSmileyActivity(1),
            "",
            SmileySelectorUtil.getSmileyActivity(2),
            "",
            SmileySelectorUtil.getSmileyActivity(3)
        )

        val labelCount = if (xAxisValues.size > 21) {
            xAxisValues.size
        } else {
            6
        }

        lineDataSet.color = sleepStateColor[SleepState.SLEEPING] ?: 1
        lineDataSet.fillColor = sleepStateColor[SleepState.SLEEPING] ?: 1
        lineDataSet.fillDrawable = activityBackgroundDrawable
        lineDataSet.fillAlpha = 255
        lineDataSet.lineWidth = 2f
        lineDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        lineDataSet.setDrawValues(false)
        lineDataSet.setDrawFilled(true)
        lineDataSet.setDrawCircles(false)

        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.textColor = colorDarkMode
        chart.xAxis.labelCount = labelCount
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValues)
        chart.xAxis.setCenterAxisLabels(false)
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.setDrawLabels(true)

        chart.legend.isEnabled= false

        chart.axisRight.setDrawGridLines(false)
        chart.axisRight.setDrawLabels(false)

        chart.axisLeft.textSize = 16f
        chart.axisLeft.textColor = colorDarkMode
        chart.axisLeft.labelCount = 5
        chart.axisLeft.axisMinimum = -0.05f
        chart.axisLeft.axisMaximum = 5.05f
        chart.axisLeft.valueFormatter = IndexAxisValueFormatter(yAxisValues)
        chart.axisLeft.setDrawGridLines(false)

        chart.description.isEnabled = false
        chart.isDoubleTapToZoomEnabled = false
        chart.setScaleEnabled(false)
        chart.setTouchEnabled(false)
        chart.setPinchZoom(false)
        chart.animateX(500)
    }
}
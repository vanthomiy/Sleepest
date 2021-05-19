package com.doitstudio.sleepest_master.ui.charts

import android.content.Context
import androidx.core.content.ContextCompat
import com.doitstudio.sleepest_master.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SparkLineStyle @Inject constructor(private val context: Context) {

    companion object {
        private val timeFormatter = DateTimeFormatter.ofPattern("H:mm")
    }

    fun styleChartWeek(lineChart: LineChart) = lineChart.apply {
        axisRight.isEnabled = false

        axisLeft.apply {
            isEnabled = false
            axisMinimum = 0f
            axisMaximum = 12f
        }

        xAxis.apply {
            axisMinimum = 0f
            axisMaximum = 6f
            isGranularityEnabled = true
            granularity = 1f
            setDrawGridLines(false)
            setDrawAxisLine(false)
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(getWeekXAxisValues())
            //typeface = ResourcesCompat.getFont(context, R.font.barlow_semi_condensed_regular)
            textColor = ContextCompat.getColor(context, R.color.black)
        }

        setTouchEnabled(true)
        isDragEnabled = true
        setScaleEnabled(false)
        setPinchZoom(false)

        description = null
        legend.isEnabled = false
    }

    fun styleLineDataSet(lineDataSet: LineDataSet) = lineDataSet.apply {
        color = ContextCompat.getColor(context, R.color.green)
        valueTextColor = ContextCompat.getColor(context, R.color.gray)
        setDrawValues(false)
        lineWidth = 3f
        isHighlightEnabled = true
        setDrawHighlightIndicators(false)
        setDrawCircles(false)
        mode = LineDataSet.Mode.CUBIC_BEZIER

        setDrawFilled(true)
        fillDrawable = ContextCompat.getDrawable(context, R.drawable.bg_spark_line)
    }

    private fun getWeekXAxisValues(): ArrayList<String> {
        val labels = ArrayList<String>()

        var arrList = DayOfWeek.values()
        var start = LocalDateTime.now().dayOfWeek.ordinal+1

        for (i in 0..7){

            labels.add(arrList[start].toString().subSequence(0,2).toString())
            start++

            if(start >= 7){
                start = 0
            }
        }

        return labels
    }

    private fun getMonthXAxisValues(step:Int): ArrayList<String> {
        val labels = ArrayList<String>()

        for (i in 1..30 step step){

            labels.add((i).toString())
        }

        return labels
    }


    private class TimeValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val localTime = LocalTime.of(value.toInt(), 0)
            return timeFormatter.format(localTime)
        }
    }


}

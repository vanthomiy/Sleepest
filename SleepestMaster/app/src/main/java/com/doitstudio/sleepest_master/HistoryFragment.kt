package com.doitstudio.sleepest_master

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.appyvet.rangebar.Bar
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*

class HistoryFragment : Fragment() {
    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart

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
        lineChart = view.findViewById(R.id.lineChart)

        setBarChart()
        setLineChart()
    }
    private fun generateDataLineChart(data: List<Int>) {
        val entries = ArrayList<Entry>()
        entries.add(Entry(1f, 0f))
        entries.add(Entry(2f, 1f))
        entries.add(Entry(3f, 2f))
        entries.add(Entry(4f, 3f))
        entries.add(Entry(5f, 4f))
    }

    private fun setLineChart() {
        val entries = ArrayList<Entry>()

        entries.add(Entry(1f, 0f))
        entries.add(Entry(2f, 1f))
        entries.add(Entry(3f, 2f))
        entries.add(Entry(4f, 3f))
        entries.add(Entry(5f, 4f))

        val vl = LineDataSet(entries, "My Type")
        vl.setDrawValues(false)
        vl.setDrawFilled(true)
        vl.lineWidth = 3f
        vl.fillColor = R.color.gray
        vl.fillAlpha = R.color.red

        lineChart.xAxis.labelRotationAngle = 0f

        lineChart.data = LineData(vl)

        lineChart.axisRight.isEnabled = false

        lineChart.xAxis.mAxisMaximum = 0.1f

        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)

        lineChart.contentDescription = "Days"
        lineChart.setNoDataText("No forex yet!")

        lineChart.animateX(1800)
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

        barChart.animateY(5000)
    }
}
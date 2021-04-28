package com.doitstudio.sleepest_master

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.asLiveData
import com.doitstudio.sleepest_master.storage.db.AlarmEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmSettings : FragmentActivity() {

    private val repository by lazy { (this.applicationContext as MainApplication).dbRepository }
    private val scope: CoroutineScope = MainScope()
    private val alarmEntityLiveData by lazy { repository.alarmFlow.asLiveData()}

    lateinit var btnAddAlarmEntity: Button
    var parentLinearLayout: ConstraintLayout? = null
    var linearLayoutTemp: LinearLayout? = null
    lateinit var allAlarms : MutableList<AlarmEntity>
    lateinit var alarmEntity : AlarmEntity

    fun setupAlarms() {
        allAlarms = mutableListOf()
        val context = this.applicationContext
        scope.launch {
            val alarmList = repository.alarmFlow.first()
            for (i in alarmList.indices) {
                allAlarms.add(alarmList[i])

                val alarmFragment = AlarmInstance(context, i + 1)
                alarmFragment.arguments = intent.extras
                val transaction = supportFragmentManager.beginTransaction()
                transaction.add(R.id.lL_temp, alarmFragment)
                transaction.commit()
            }
        }
        /*
        alarmEntityLiveData.observe(this)
        {
                alarmList ->
            for (i in alarmList.indices) {
                allAlarms.add(alarmList[i])

                val alarmFragment = AlarmInstance(this.applicationContext, i + 1)
                alarmFragment.arguments = intent.extras
                val transaction = supportFragmentManager.beginTransaction()
                transaction.add(R.id.lL_temp, alarmFragment)
                transaction.commit()
            }
        }
         */
    }

    fun onAddAlarm(view: View) {
        scope.launch {
            alarmEntity = AlarmEntity(1)
            repository.insertAlarm(alarmEntity)
        }

        val alarmFragment = AlarmInstance(this.applicationContext, 1)
        alarmFragment.arguments = intent.extras
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.lL_temp, alarmFragment)
        transaction.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_settings)
        allAlarms = mutableListOf()
        setupAlarms()


        parentLinearLayout = findViewById(R.id.lL_parent)
        btnAddAlarmEntity = findViewById(R.id.btn_addAlarmEntity)
        linearLayoutTemp = findViewById(R.id.lL_temp)
    }
}

/*
private fun provideSlices(): ArrayList<Slice> {
    return arrayListOf(
            Slice(
                    Random.nextInt(1000, 3000).toFloat(),
                    R.color.purple,
                    "Non-REM 1"
            ),
            Slice(
                    Random.nextInt(1000, 2000).toFloat(),
                    R.color.purple_200,
                    "Non-REM 2"
            ),
            Slice(
                    Random.nextInt(1000, 5000).toFloat(),
                    R.color.purple_500,
                    "REM"
            ),
            Slice(
                    Random.nextInt(1000, 10000).toFloat(),
                    R.color.purple_700,
                    "Non-Sleep"
            ),
    )
}

fun func(index: Float) {
    when (index) {
        0.0.toFloat() -> text = "Non-REM 1"
        1.0.toFloat() -> text = "Non-REM 2"
        2.0.toFloat() -> text = "REM"
        else -> {
            text = "Wach"
        }
    }
}

        /*
        // Piechart https://github.com/furkanaskin/ClickablePieChart
        val pieChartDSL = buildChart {
            slices { provideSlices() }
            sliceWidth { 80f }
            sliceStartPoint { 0f }
            clickListener { angle, index ->
                //func(index)
            }
        }
        chart.setPieChart(pieChartDSL)
        chart.showLegend(legendLayout)
         */
 */
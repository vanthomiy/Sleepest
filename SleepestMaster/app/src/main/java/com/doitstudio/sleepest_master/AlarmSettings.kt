package com.doitstudio.sleepest_master

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.FragmentActivity
import com.doitstudio.sleepest_master.storage.db.AlarmEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmSettings : FragmentActivity() {

    private val repository by lazy { (this.applicationContext as MainApplication).dbRepository }
    private val scope: CoroutineScope = MainScope()
    //private val alarmEntityLiveData by lazy { repository.alarmFlow.asLiveData()}

    lateinit var btnAddAlarmEntity: Button
    var lLAlarmEntities: LinearLayout? = null
    lateinit var allAlarms : MutableList<AlarmEntity>
    lateinit var usedIds: MutableList<Int>

    private fun setupAlarms() {
        allAlarms = mutableListOf()
        val context = this.applicationContext
        scope.launch {
            val alarmList = repository.alarmFlow.first()
            for (i in alarmList.indices) {
                usedIds.add(alarmList[i].id)
                allAlarms.add(alarmList[i])
                addAlarmEntity(context, i)
            }
        }
    }

    fun onAddAlarm(view: View) {
        var id = 0
        for (i in usedIds.indices) {
            if (!usedIds.contains(i)){
                id = i
                usedIds.add(id)
            }
            else {
                id = i + 1
                usedIds.add(id)
            }
        }
        scope.launch {
            repository.insertAlarm(AlarmEntity(id))
        }
        addAlarmEntity(this.applicationContext, id)
    }

    private fun addAlarmEntity(context: Context, alarmId: Int) {
        val alarmFragment = AlarmInstance(context, alarmId)
        alarmFragment.arguments = intent.extras
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.lL_alarmEntities, alarmFragment)
        transaction.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_settings)

        btnAddAlarmEntity = findViewById(R.id.btn_addAlarmEntity)
        lLAlarmEntities = findViewById(R.id.lL_alarmEntities)
        usedIds = mutableListOf()

        setupAlarms()
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
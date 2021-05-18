package com.doitstudio.sleepest_master

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.doitstudio.sleepest_master.storage.db.AlarmEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmSettings : FragmentActivity() {

    private val repository by lazy { (this.applicationContext as MainApplication).dbRepository }
    private val scope: CoroutineScope = MainScope()

    lateinit var btnAddAlarmEntity: Button
    var lLAlarmEntities: LinearLayout? = null
    lateinit var allAlarms : MutableList<AlarmEntity>
    lateinit var usedIds : MutableSet<Int>
    lateinit var transactions: MutableMap<Int, FragmentTransaction>
    lateinit var fragments: MutableMap<Int, AlarmInstance>

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: AlarmSettings? = null

        fun getAlarmSettings(): AlarmSettings {
            return INSTANCE ?: synchronized(this) {
                val instance = AlarmSettings()
                INSTANCE = instance
                instance
            }
        }
    }

    private fun setupAlarms() {
        allAlarms = mutableListOf()
        val context = this.applicationContext
        scope.launch {
            val alarmList = repository.alarmFlow.first().reversed()
            for (i in alarmList.indices) {
                usedIds.add(alarmList[i].id)
                allAlarms.add(alarmList[i])
                addAlarmEntity(context, alarmList[i].id)
            }
        }
    }

    fun onAddAlarm(view: View) {
        var newId = 0
        for (id in usedIds.indices) {
            if (usedIds.contains(newId)) {
                newId += 1
            }
        }
        scope.launch {
            repository.insertAlarm(AlarmEntity(newId))
        }
        addAlarmEntity(this.applicationContext, newId)
        usedIds.add(newId)
    }

    private fun addAlarmEntity(context: Context, alarmId: Int) {
        transactions[alarmId] = supportFragmentManager.beginTransaction()
        fragments[alarmId] = AlarmInstance(context, alarmId)
        fragments[alarmId]?.arguments = intent.extras
        transactions[alarmId]?.add(R.id.lL_alarmEntities, fragments[alarmId]!!)?.commit()
    }

    fun removeAlarmEntity(alarmId: Int) {
        supportFragmentManager.beginTransaction().remove(fragments[alarmId]!!).commit()
        transactions.remove(alarmId)
        fragments.remove(alarmId)
        usedIds.remove(alarmId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_settings)

        INSTANCE = this
        btnAddAlarmEntity = findViewById(R.id.btn_addAlarmEntity)
        lLAlarmEntities = findViewById(R.id.lL_alarmEntities)
        usedIds = mutableSetOf()
        transactions = mutableMapOf()
        fragments = mutableMapOf()

        setupAlarms()
    }
}
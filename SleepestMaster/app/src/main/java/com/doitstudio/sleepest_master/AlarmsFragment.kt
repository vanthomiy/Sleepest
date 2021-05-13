package com.doitstudio.sleepest_master

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.FragmentTransaction
import com.doitstudio.sleepest_master.databinding.ActivityMainBinding
import com.doitstudio.sleepest_master.databinding.FragmentAlarmsBinding
import com.doitstudio.sleepest_master.storage.db.AlarmEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 */
class AlarmsFragment() : Fragment() {

    //private lateinit var binding: FragmentAlarmsBinding

    private val repository by lazy { (actualContext as MainApplication).dbRepository }
    private val scope: CoroutineScope = MainScope()

    private val actualContext: Context by lazy {requireActivity().applicationContext}

    lateinit var btnAddAlarmEntity: Button
    var lLAlarmEntities: LinearLayout? = null
    lateinit var allAlarms : MutableList<AlarmEntity>
    lateinit var usedIds : MutableSet<Int>
    lateinit var transactions: MutableMap<Int, FragmentTransaction>
    lateinit var fragments: MutableMap<Int, AlarmInstance>

    private fun setupAlarms() {
        allAlarms = mutableListOf()
        scope.launch {
            val alarmList = repository.alarmFlow.first().reversed()
            for (i in alarmList.indices) {
                usedIds.add(alarmList[i].id)
                allAlarms.add(alarmList[i])
                addAlarmEntity(actualContext, alarmList[i].id)
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
        addAlarmEntity(actualContext, newId)
        usedIds.add(newId)
    }

    private fun addAlarmEntity(context: Context, alarmId: Int) {

        transactions[alarmId] = childFragmentManager.beginTransaction()
        fragments[alarmId] = AlarmInstance(context, alarmId)
        //fragments[alarmId]?.arguments = intent.extras
        transactions[alarmId]?.add(R.id.lL_alarmEntities, fragments[alarmId]!!)?.commit()
    }

    fun removeAlarmEntity(alarmId: Int) {
        childFragmentManager.beginTransaction().remove(fragments[alarmId]!!).commit()
        transactions.remove(alarmId)
        fragments.remove(alarmId)
        usedIds.remove(alarmId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        INSTANCE = this
        btnAddAlarmEntity = view.findViewById(R.id.btn_addAlarmEntity)
        lLAlarmEntities = view.findViewById(R.id.lL_alarmEntities)

        btnAddAlarmEntity.setOnClickListener{
            view -> onAddAlarm(view)
        }


        usedIds = mutableSetOf()
        transactions = mutableMapOf()
        fragments = mutableMapOf()

        setupAlarms()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alarms, container, false)
    }


    companion object {

        // For Singleton instantiation
        @Volatile
        private var INSTANCE: AlarmsFragment? = null

        fun getAlarmFragment(): AlarmsFragment {
            return INSTANCE ?: synchronized(this) {
                val instance = AlarmsFragment()
                INSTANCE = instance
                instance
            }
        }
    }
}
package com.doitstudio.sleepest_master

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
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
    var lLinsideScrollView: LinearLayout? = null
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
                addAlarmEntity(actualContext, i)
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
        transactions[alarmId]?.add(R.id.lL_insideScrollView, fragments[alarmId]!!)?.commit()
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
        //lLAlarmEntities = view.findViewById(R.id.lL_alarmEntities)
        lLAlarmEntities = view.findViewById(R.id.lL_insideScrollView)
        usedIds = mutableSetOf()
        transactions = mutableMapOf()
        fragments = mutableMapOf()

        btnAddAlarmEntity.setOnClickListener{
            view -> onAddAlarm(view)
        }

        /*
        val scrollView = ScrollView(actualContext)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        scrollView.layoutParams = layoutParams
        //lLTest?.addView(scrollView)

        val linearLayout = LinearLayout(actualContext)
        val linearParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.layoutParams = linearParams
        scrollView.addView(linearLayout)

        val imageView1 = ImageView(actualContext)
        val params1 =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        params1.setMargins(0, 30, 0, 30)
        params1.gravity = Gravity.CENTER
        imageView1.setLayoutParams(params1)
        imageView1.setImageResource(R.drawable.person_icon)
        linearLayout.addView(imageView1)

        val instance = ImageView(actualContext)
        instance.setLayoutParams(params1)
        //instance.setImageResource(R.layout.alarm_entity)
        linearLayout.addView(instance)

         */

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
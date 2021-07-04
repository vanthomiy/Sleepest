package com.doitstudio.sleepest_master

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.doitstudio.sleepest_master.storage.db.AlarmEntity
import com.doitstudio.sleepest_master.ui.profile.ProfileFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 */
class AlarmsFragment() : Fragment() {

    //private lateinit var binding: FragmentAlarmsBinding
    private val repository by lazy { (actualContext as MainApplication).dataBaseRepository }
    private val scope: CoroutineScope = MainScope()
    private val actualContext: Context by lazy {requireActivity().applicationContext}

    private lateinit var btnAddAlarmEntity: Button
    private var lLContainerAlarmEntities: LinearLayout? = null
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

    private fun onAddAlarm(view: View) {
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
        transactions[alarmId]?.add(R.id.lL_containerAlarmEntities, fragments[alarmId]!!)?.commit()
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
        lLContainerAlarmEntities = view.findViewById(R.id.lL_containerAlarmEntities)
        usedIds = mutableSetOf()
        transactions = mutableMapOf()
        fragments = mutableMapOf()

        btnAddAlarmEntity.setOnClickListener{
            //view ->  onAddAlarm(view)
            if (checkPermissions()) {
                onAddAlarm(view)
            } else {
                Toast.makeText(actualContext, "Please grant all permissions", Toast.LENGTH_LONG).show()

                /*val transaction = getParentFragmentManager().beginTransaction()
                transaction.replace(R.id.navigationFrame, ProfileFragment()) // give your fragment container id in first parameter

                transaction.addToBackStack(null) // if written, this transaction will be added to backstack

                transaction.commit()*/

                (activity as MainActivity).changeFragment()

            }
        }

        setupAlarms()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alarms, container, false)
    }

    fun checkPermissions() : Boolean {
        val notificationManager = actualContext.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted){
            return false
        } else if(!Settings.canDrawOverlays(actualContext)) {
            return false
        } else if(PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(actualContext, Manifest.permission.ACTIVITY_RECOGNITION)) {
            return false
        }

        return true
    }

    companion object {
        // For Singleton instantiation
        @SuppressLint("StaticFieldLeak")
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
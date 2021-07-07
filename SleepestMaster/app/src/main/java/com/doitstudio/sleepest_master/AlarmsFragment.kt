package com.doitstudio.sleepest_master

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.doitstudio.sleepest_master.storage.db.AlarmEntity
import com.kevalpatel.ringtonepicker.RingtonePickerDialog

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
    private val dataStoreRepository by lazy { (actualContext as MainApplication).dataStoreRepository }
    private val scope: CoroutineScope = MainScope()
    private val actualContext: Context by lazy { requireActivity().applicationContext }

    private lateinit var btnAddAlarmEntity: Button
    private var lLContainerAlarmEntities: LinearLayout? = null
    lateinit var lLAlarmSoundSettings: LinearLayout
    lateinit var btnExpandAlarmSoundSettings: ImageButton
    lateinit var btnExpandAlarmSoundInformation: ImageButton
    lateinit var fLAlarmSoundInformation: FrameLayout
    lateinit var swAutoCancelAlarm: Switch
    lateinit var btnChangeAlarmSound: Button
    lateinit var btnTemporaryDisableAlarm: Button

    lateinit var allAlarms: MutableList<AlarmEntity>
    lateinit var usedIds: MutableSet<Int>
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

    private fun onAlarmSoundChange(view: View) {
        //check if audio volume is 0

        val audioManager = actualContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) <= 0) {
            Toast.makeText(actualContext, "Increase volume to hear sounds", Toast.LENGTH_LONG)
                .show()
        }

        var savedRingtoneUri = Uri.parse(dataStoreRepository.getAlarmToneJob())

        if (dataStoreRepository.getAlarmToneJob() == "null") {
            savedRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }

        val ringtonePickerBuilder =
            RingtonePickerDialog.Builder(actualContext, parentFragmentManager)
                .setTitle("Select your ringtone")
                .displayDefaultRingtone(true)
                .setCurrentRingtoneUri(savedRingtoneUri)
                .setPositiveButtonText("Set")
                .setCancelButtonText("Cancel")
                .setPlaySampleWhileSelection(true)
                .setListener { ringtoneName, ringtoneUri ->
                    scope.launch {
                        dataStoreRepository.updateAlarmTone(
                            ringtoneUri.toString()
                        )
                    }
                }

        ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_ALARM)
        ringtonePickerBuilder.show()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        INSTANCE = this
        lLAlarmSoundSettings = view.findViewById(R.id.lL_alarmSoundSettings)
        btnExpandAlarmSoundSettings = view.findViewById(R.id.btn_expandSoundSettings)
        btnAddAlarmEntity = view.findViewById(R.id.btn_addAlarmEntity)
        lLContainerAlarmEntities = view.findViewById(R.id.lL_containerAlarmEntities)
        btnExpandAlarmSoundInformation = view.findViewById(R.id.btn_expandAlarmSoundInformation)
        fLAlarmSoundInformation = view.findViewById(R.id.fL_alarmSoundInformation)
        swAutoCancelAlarm = view.findViewById(R.id.sw_autoCancelAlarm)
        btnChangeAlarmSound = view.findViewById(R.id.btn_changeAlarmSound)
        btnTemporaryDisableAlarm = view.findViewById(R.id.btn_temporaryDisableAlarm)

        usedIds = mutableSetOf()
        transactions = mutableMapOf()
        fragments = mutableMapOf()


        btnAddAlarmEntity.setOnClickListener {
            //view ->  onAddAlarm(view)

            (activity as MainActivity).switchToMenu(R.id.profile, changeType = 3)

            if (checkPermissions()) {
                onAddAlarm(view)
            } else {

                Toast.makeText(actualContext, "Please grant all permissions", Toast.LENGTH_LONG)
                    .show()
            }
        }

        scope.launch {
            if (repository.getNextActiveAlarm() != null) {
                if (repository.getNextActiveAlarm()!!.tempDisabled) {
                    btnTemporaryDisableAlarm.text = "Reactivate next alarm"
                } else {
                    btnTemporaryDisableAlarm.text = "Disable next alarm"
                }
            } else {
                btnTemporaryDisableAlarm.isVisible = false
            }
        }




            btnExpandAlarmSoundSettings.setOnClickListener {
                lLAlarmSoundSettings.isVisible = !lLAlarmSoundSettings.isVisible
            }

            btnExpandAlarmSoundInformation.setOnClickListener {
                fLAlarmSoundInformation.isVisible = !fLAlarmSoundInformation.isVisible
            }

            swAutoCancelAlarm.setOnClickListener {
                scope.launch {
                    dataStoreRepository.updateEndAlarmAfterFired(swAutoCancelAlarm.isChecked)
                }
            }

            btnChangeAlarmSound.setOnClickListener {
                onAlarmSoundChange(view)
            }


        btnTemporaryDisableAlarm.setOnClickListener {
            scope.launch {
                if (repository.getNextActiveAlarm() != null) {
                    if (repository.getNextActiveAlarm()!!.tempDisabled) {
                        repository.updateAlarmTempDisabled(false, repository.getNextActiveAlarm()!!.id)
                        btnTemporaryDisableAlarm.text = "Reactivate next alarm"
                    }
                    else  {
                        repository.updateAlarmTempDisabled(true ,repository.getNextActiveAlarm()!!.id)
                        btnTemporaryDisableAlarm.text = "Disable next alarm"
                    }
                }

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

        fun checkPermissions(): Boolean {
            val notificationManager =
                actualContext.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            if (!notificationManager.isNotificationPolicyAccessGranted) {
                return false
            } else if (!Settings.canDrawOverlays(actualContext)) {
                return false
            } else if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(
                    actualContext,
                    Manifest.permission.ACTIVITY_RECOGNITION
                )
            ) {
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


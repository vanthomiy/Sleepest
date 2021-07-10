package com.doitstudio.sleepest_master.ui.alarm

import android.Manifest
import android.annotation.SuppressLint
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
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.storage.db.AlarmEntity
import com.kevalpatel.ringtonepicker.RingtonePickerDialog

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * A fragment which contains the alarm instances.
 */
class AlarmsFragment : Fragment() {

    /**  */
    private val repository by lazy { (actualContext as MainApplication).dataBaseRepository }

    /**  */
    private val dataStoreRepository by lazy { (actualContext as MainApplication).dataStoreRepository }

    /**  */
    private val scope: CoroutineScope = MainScope()

    /**  */
    private val actualContext: Context by lazy { requireActivity().applicationContext }

    /** Button which lets the user create a new [AlarmInstance]. */
    private lateinit var btnAddAlarmEntity: Button

    /**  */
    private var lLContainerAlarmEntities: LinearLayout? = null

    /** [LinearLayout] which contains the alarm sound settings for all alarm entities. */
    private lateinit var lLAlarmSoundSettings: LinearLayout

    /** [ImageButton] which maintains the expansion of the alarm sound settings. */
    private lateinit var btnExpandAlarmSoundSettings: ImageButton

    /** [ImageButton] which maintains the expansion of the further information about the alarm sound settings. */
    private lateinit var btnExpandAlarmSoundInformation: ImageButton

    /**  */
    private lateinit var fLAlarmSoundInformation: FrameLayout

    /**  */
    private lateinit var swAutoCancelAlarm: Switch

    /** [Button] which lets the user alter the alarm sound. */
    private lateinit var btnChangeAlarmSound: Button

    /**  */
    private lateinit var btnTemporaryDisableAlarm: Button

    /**  */
    private lateinit var allAlarms: MutableList<AlarmEntity>

    /**  */
    private lateinit var usedIds: MutableSet<Int>

    /**  */
    private lateinit var transactions: MutableMap<Int, FragmentTransaction>

    /**  */
    private lateinit var fragments: MutableMap<Int, AlarmInstance>

    /**
     *
     */
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

    /**
     *
     */
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

    /**
     *
     */
    private fun addAlarmEntity(context: Context, alarmId: Int) {
        transactions[alarmId] = childFragmentManager.beginTransaction()
        fragments[alarmId] = AlarmInstance(context, alarmId)
        transactions[alarmId]?.add(R.id.lL_containerAlarmEntities, fragments[alarmId]!!)?.commit()
    }

    /**
     *
     */
    fun removeAlarmEntity(alarmId: Int) {
        childFragmentManager.beginTransaction().remove(fragments[alarmId]!!).commit()
        transactions.remove(alarmId)
        fragments.remove(alarmId)
        usedIds.remove(alarmId)
    }

    /**
     *
     */
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

    /**
     *
     */
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
            if (checkPermissions()) {
                onAddAlarm(view)
            } else {
                Toast.makeText(actualContext, "Please grant all permissions", Toast.LENGTH_LONG)
                    .show()

            }
        }

        /*
        var disableNextAlarm = false
        scope.launch {
            //disableNextAlarm = repository.getNextActiveAlarm().tempDisabled //liefert Ture oder False
            if (disableNextAlarm) {
                btnTemporaryDisableAlarm.text = "Reactivate next alarm"
            }
            else if (!disableNextAlarm) {
                btnTemporaryDisableAlarm.text = "Disable next alarm"
            }
            else {
                btnTemporaryDisableAlarm.isVisible = false
            }
        }
        */

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

        /*
        btnTemporaryDisableAlarm.setOnClickListener {
            scope.launch {
                if (disableNextAlarm) {
                    repository.updateAlarmTempDisabled()
                    btnTemporaryDisableAlarm.text = "Disable next alarm"
                }
                else  {
                    repository.updateAlarmTempDisabled()
                    btnTemporaryDisableAlarm.text = "Reactivate next alarm"
                }
            }
        }
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

    /**
     *
     */
    private fun checkPermissions(): Boolean {
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

    /**
     *
     */
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


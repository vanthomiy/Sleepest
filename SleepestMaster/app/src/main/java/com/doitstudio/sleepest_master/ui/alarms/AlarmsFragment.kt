package com.doitstudio.sleepest_master.ui.alarms

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
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.doitstudio.sleepest_master.MainActivity
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.databinding.FragmentAlarmsBinding
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
    private val activeAlarmsLiveData by lazy {  repository.activeAlarmsFlow().asLiveData() }

    private lateinit var binding: FragmentAlarmsBinding
    private val viewModel by lazy { ViewModelProvider(this).get(AlarmsViewModel::class.java) }

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
        TransitionManager.beginDelayedTransition(viewModel.transitionsContainer);

        transactions[alarmId] = childFragmentManager.beginTransaction()
        fragments[alarmId] = AlarmInstance(context, alarmId)
        transactions[alarmId]?.add(R.id.lL_containerAlarmEntities, fragments[alarmId]!!)?.commit()
    }

    fun openCloseAlarm(){
        TransitionManager.beginDelayedTransition(viewModel.transitionsContainer)
    }

    fun removeAlarmEntity(alarmId: Int) {

        TransitionManager.beginDelayedTransition(viewModel.transitionsContainer);

        childFragmentManager.beginTransaction().remove(fragments[alarmId]!!).commit()
        transactions.remove(alarmId)
        fragments.remove(alarmId)
        usedIds.remove(alarmId)
    }



    private fun onAlarmSoundChange() {
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
                        dataStoreRepository.updateAlarmName(
                            ringtoneName
                        )
                    }

                    viewModel.alarmSoundName.set(ringtoneName)

                }

        ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_ALARM)
        ringtonePickerBuilder.show()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlarmsBinding.inflate(inflater, container, false)
        viewModel.transitionsContainer = (binding.cLParent)
        binding.alarmsViewModel = viewModel

        INSTANCE = this

        usedIds = mutableSetOf()
        transactions = mutableMapOf()
        fragments = mutableMapOf()

        activeAlarmsLiveData.observe(requireActivity()){
            scope.launch {
                if (repository.getNextActiveAlarm() != null) {
                    if (repository.getNextActiveAlarm()!!.tempDisabled) {
                        binding.btnTemporaryDisableAlarm.text = getString(R.string.alarm_fragment_btn_disable_alarm_disable)
                        /**TODO: Change color**/
                    } else {
                        binding.btnTemporaryDisableAlarm.text = getString(R.string.alarm_fragment_btn_disable_alarm_reactivate)
                        /**TODO: Change color**/
                    }
                } else {
                    binding.btnTemporaryDisableAlarm.isVisible = false
                }

            }
        }

        binding.btnAddAlarmEntity.setOnClickListener {

            if (checkPermissions()) {
                onAddAlarm(it)
            } else {

                (activity as MainActivity).switchToMenu(R.id.profile, changeType = 3)

                Toast.makeText(actualContext, "Please grant all permissions", Toast.LENGTH_LONG)
                    .show()
            }
        }

        binding.sVAlarmEntities.setOnClickListener {
            viewModel.actualExpand.set(View.GONE)
        }

        binding.lLAlarmSoundSettings.onFocusChangeListener =
            View.OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    viewModel.actualExpand.set(View.GONE)
                    viewModel.rotateState.set(0)
                } else {
                }
            }

        scope.launch {
            if (repository.getNextActiveAlarm() != null) {
                if (repository.getNextActiveAlarm()!!.tempDisabled) {
                    //BackgroundAlarmTimeHandler.getHandler(actualContext).disableAlarmTemporaryInApp(true, false)
                } else {
                    //BackgroundAlarmTimeHandler.getHandler(actualContext).disableAlarmTemporaryInApp(true, true)
                }
            } else {
                binding.btnTemporaryDisableAlarm.isVisible = false
            }
        }

        binding.soundChange.setOnClickListener{
            onAlarmSoundChange()
        }


        binding.btnTemporaryDisableAlarm.setOnClickListener {
            scope.launch {
                if (repository.getNextActiveAlarm() != null) {
                    if (repository.getNextActiveAlarm()!!.tempDisabled) {
                        repository.updateAlarmTempDisabled(
                            false,
                            repository.getNextActiveAlarm()!!.id
                        )
                        binding.btnTemporaryDisableAlarm.text = "Reactivate next alarm"
                    }
                    else  {
                        repository.updateAlarmTempDisabled(
                            true,
                            repository.getNextActiveAlarm()!!.id
                        )
                        binding.btnTemporaryDisableAlarm.text = "Disable next alarm"
                    }
                }

            }
        }


        setupAlarms()

        return binding.root
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


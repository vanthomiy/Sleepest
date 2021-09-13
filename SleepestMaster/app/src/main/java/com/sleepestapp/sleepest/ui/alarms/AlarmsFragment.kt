package com.sleepestapp.sleepest.ui.alarms

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.sleepestapp.sleepest.MainActivity
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.background.BackgroundAlarmTimeHandler
import com.sleepestapp.sleepest.databinding.FragmentAlarmsBinding
import com.sleepestapp.sleepest.storage.db.AlarmEntity
import com.sleepestapp.sleepest.util.IconAnimatorUtil
import com.sleepestapp.sleepest.util.PermissionsUtil
import com.kevalpatel.ringtonepicker.RingtonePickerDialog
import com.sleepestapp.sleepest.ui.settings.SettingsFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


/**
 * A fragment representing a list of Items.
 */
class AlarmsFragment() : Fragment() {

    // region init

    /**
     * Scope is used to call datastore async
     */
    private val scope: CoroutineScope = MainScope()

    /**
     * Get actual context
     */
    private val actualContext: Context by lazy { requireActivity().applicationContext }


    /**
     * Binding XML Code to Fragment
     */
    private lateinit var binding: FragmentAlarmsBinding

    /**
     * View model of the [AlarmsFragment]
     */
    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(AlarmsViewModel::class.java) }

    /**
     * All actual setup alarms
     */
    lateinit var allAlarms: MutableList<AlarmEntity>
    /**
     * All ids of the setup alarms
     */
    lateinit var usedIds: MutableSet<Int>
    /**
     * All transactions for each id of the setup alarms
     */
    lateinit var transactions: MutableMap<Int, FragmentTransaction>
    /**
     * All fragments for each id of the setup alarms
     */
    lateinit var fragments: MutableMap<Int, AlarmInstanceFragment>

    // endregion

    // region alarms setup

    /**
     * Load all alarms and then add them to the lists
     */
    private fun setupAlarms() {
        allAlarms = mutableListOf()
        scope.launch {
            val alarmList = viewModel.databaseRepository.alarmFlow.first().reversed()

            for (i in alarmList.indices) {
                usedIds.add(alarmList[i].id)
                allAlarms.add(alarmList[i])
                addAlarmEntity(actualContext, alarmList[i].id)
            }

            viewModel.noAlarmsView.set(if(usedIds.count() > 0) View.GONE else View.VISIBLE)

        }
    }

    /**
     * When a new alarm is added by the user
     */
    private fun onAddAlarm(view: View) {
        var newId = 0
        for (id in usedIds.indices) {
            if (usedIds.contains(newId)) {
                newId += 1
            }
        }
        scope.launch {
            var sleepTime = viewModel.dataStoreRepository.getSleepDuration()
            viewModel.databaseRepository.insertAlarm(AlarmEntity(newId, sleepDuration = sleepTime))
        }
        addAlarmEntity(actualContext, newId)
        usedIds.add(newId)

        viewModel.noAlarmsView.set(View.GONE)

        IconAnimatorUtil.animateView(view as ImageView)
    }

    /**
     * Add the alarm to the view and the stored lists
     */
    private fun addAlarmEntity(context: Context, alarmId: Int) {

        transactions[alarmId] = childFragmentManager.beginTransaction()
        transactions[alarmId]?.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        fragments[alarmId] = AlarmInstanceFragment(context, alarmId)
        transactions[alarmId]?.add(R.id.lL_containerAlarmEntities, fragments[alarmId]!!)?.commit()
    }

    /**
     * Remove an alarm by id and then remove it from the lists
     */
    fun removeAlarmEntity(alarmId: Int) {

        TransitionManager.beginDelayedTransition(viewModel.transitionsContainer);

        transactions[alarmId]?.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);

        childFragmentManager.beginTransaction().remove(fragments[alarmId]!!).commit()
        transactions.remove(alarmId)
        fragments.remove(alarmId)
        usedIds.remove(alarmId)
        scope.launch {
            viewModel.databaseRepository.deleteAlarmById(alarmId)
        }

        viewModel.noAlarmsView.set(if(usedIds.count() > 0) View.GONE else View.VISIBLE)
    }

    /**
     * When alarm sound changed is clicked in the settings view
     */
    private fun onAlarmSoundChange() {
        //check if audio volume is 0

        val audioManager = actualContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) <= 0) {
            Toast.makeText(actualContext, "Increase volume to hear sounds", Toast.LENGTH_LONG)
                .show()
        }

        var savedRingtoneUri = Uri.parse(viewModel.dataStoreRepository.getAlarmToneJob())

        if (viewModel.dataStoreRepository.getAlarmToneJob() == "null") {
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
                        viewModel.dataStoreRepository.updateAlarmTone(
                            ringtoneUri.toString()
                        )
                        viewModel.dataStoreRepository.updateAlarmName(
                            ringtoneName
                        )
                    }

                    viewModel.alarmSoundName.set(ringtoneName)

                }

        ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_ALARM)
        ringtonePickerBuilder.show()

    }

    //endregion

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

        // Update the disable next alarm button by checking the settings of all alarms
        viewModel.activeAlarmsLiveData.observe(requireActivity()){
            activeAlarms ->
            if (activeAlarms.isNotEmpty()){
                val nextAlarm = activeAlarms.minByOrNull { x-> x.wakeupEarly }
                if(nextAlarm?.tempDisabled == true){
                    binding.btnTemporaryDisableAlarm.text = getString(R.string.alarm_fragment_btn_disable_alarm_reactivate)
                    binding.btnTemporaryDisableAlarm.isVisible = true
                    /**TODO: Change color**/
                }
                else{
                    binding.btnTemporaryDisableAlarm.text = getString(R.string.alarm_fragment_btn_disable_alarm_disable)
                    binding.btnTemporaryDisableAlarm.isVisible = true
                    /**TODO: Change color**/
                }

                if (nextAlarm?.wasFired == true) {
                    binding.btnTemporaryDisableAlarm.isVisible = false
                }
            }
            else{
                binding.btnTemporaryDisableAlarm.isVisible = false
            }
        }

        // new alarm is added
        binding.btnAddAlarmEntity.setOnClickListener {

            if (PermissionsUtil.checkAllNeccessaryPermissions(actualContext)) {
                onAddAlarm(it)
            } else {

                (activity as MainActivity).switchToMenu(R.id.profile, changeType = 3)

                Toast.makeText(actualContext, "Please grant all permissions", Toast.LENGTH_LONG)
                    .show()
            }
        }

        // new click on alarm entity
        binding.sVAlarmEntities.setOnClickListener {
            viewModel.actualExpand.set(View.GONE)
        }

        // new click on alarm sound settings
        binding.lLAlarmSoundSettings.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.actualExpand.set(View.GONE)
                    viewModel.rotateState.set(0)
                } else {
                }
            }

        // sound was changed click
        binding.soundChange.setOnClickListener{
            onAlarmSoundChange()
        }

        // temp disable alarm was clicked
        binding.btnTemporaryDisableAlarm.setOnClickListener {
            scope.launch {
                if (viewModel.databaseRepository.getNextActiveAlarm() != null) {
                    if (viewModel.databaseRepository.getNextActiveAlarm()!!.tempDisabled && !viewModel.dataStoreRepository.backgroundServiceFlow.first().isForegroundActive) {
                        BackgroundAlarmTimeHandler.getHandler(actualContext).disableAlarmTemporaryInApp(true, true)
                    }
                    else if (viewModel.databaseRepository.getNextActiveAlarm()!!.tempDisabled && viewModel.dataStoreRepository.backgroundServiceFlow.first().isForegroundActive) {
                        BackgroundAlarmTimeHandler.getHandler(actualContext).disableAlarmTemporaryInApp(false, true)
                    }
                    else  {
                        BackgroundAlarmTimeHandler.getHandler(actualContext).disableAlarmTemporaryInApp(true, false)
                    }
                }

            }
        }

        // setup the alarms
        setupAlarms()

        return binding.root
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



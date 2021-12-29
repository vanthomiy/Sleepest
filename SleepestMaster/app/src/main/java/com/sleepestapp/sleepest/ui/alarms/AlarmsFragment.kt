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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.sleepestapp.sleepest.MainActivity
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.background.BackgroundAlarmTimeHandler
import com.sleepestapp.sleepest.databinding.FragmentAlarmsBinding
import com.sleepestapp.sleepest.storage.db.AlarmEntity
import com.sleepestapp.sleepest.util.IconAnimatorUtil
import com.sleepestapp.sleepest.util.PermissionsUtil
import com.kevalpatel.ringtonepicker.RingtonePickerDialog
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil.getActiveAlarms
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil.getTimeBetweenSecondsOfDay
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil.subtractMinutesFromSecondsOfDay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


/**
 * A fragment representing a list of Items.
 */
class AlarmsFragment : Fragment() {

    // region init

    var factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            // Workaround because we know that we can cast to T
            return  AlarmsViewModel(
                (actualContext as MainApplication).dataStoreRepository,
                (actualContext as MainApplication).dataBaseRepository
            ) as T
        }
    }

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
    private val viewModel by lazy { ViewModelProvider(requireActivity(), factory).get(AlarmsViewModel::class.java) }


    // endregion

    // region alarms setup

    /**
     * Load all alarms and then add them to the lists
     */
    private fun setupAlarms() {
        viewModel.allAlarms = mutableListOf()
        lifecycleScope.launch {
            val alarmList = viewModel.dataBaseRepository.alarmFlow.first().reversed()

            for (i in alarmList.indices) {
                viewModel.usedIds.add(alarmList[i].id)
                viewModel.allAlarms.add(alarmList[i])
                addAlarmEntity(actualContext, alarmList[i].id)
            }

            viewModel.noAlarmsView.value = (if(viewModel.usedIds.count() > 0) View.GONE else View.VISIBLE)

        }
    }

    /**
     * When a new alarm is added by the user
     */
    private fun onAddAlarm(view: View) {
        var newId = 0
        for (id in viewModel.usedIds.indices) {
            if (viewModel.usedIds.contains(newId)) {
                newId += 1
            }
        }
        lifecycleScope.launch {
            val sleepParams = viewModel.dataStoreRepository.sleepParameterFlow.first()
            val sleepTime = sleepParams.sleepDuration

            val maxTimeMinutes = getTimeBetweenSecondsOfDay(
                sleepParams.sleepTimeEnd,
                sleepParams.sleepTimeStart) / 60
            val offset = 240
            val timeOffset = if(maxTimeMinutes < offset)
                (maxTimeMinutes / 2) else offset

            val wakeUpEarly = subtractMinutesFromSecondsOfDay(
                sleepParams.sleepTimeEnd,
                timeOffset)
            val wakeUpLate = subtractMinutesFromSecondsOfDay(
                sleepParams.sleepTimeEnd,
                timeOffset / 2)

            viewModel.dataBaseRepository.insertAlarm(
                AlarmEntity(
                id = newId,
                sleepDuration = sleepTime
                , wakeupEarly = wakeUpEarly
                , wakeupLate = wakeUpLate)
            )
        }
        addAlarmEntity(actualContext, newId)
        viewModel.usedIds.add(newId)

        viewModel.noAlarmsView.value = (View.GONE)

        IconAnimatorUtil.animateView(view as ImageView)
    }

    /**
     * Add the alarm to the view and the stored lists
     */
    private fun addAlarmEntity(context: Context, alarmId: Int) {

        viewModel.transactions[alarmId] = childFragmentManager.beginTransaction()
        viewModel.transactions[alarmId]?.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
        viewModel.fragments[alarmId] = AlarmInstanceFragment(context, alarmId)
        viewModel.transactions[alarmId]?.add(R.id.lL_containerAlarmEntities, viewModel.fragments[alarmId]!!)?.commit()
    }

    /**
     * Remove an alarm by id and then remove it from the lists
     */
    fun removeAlarmEntity(alarmId: Int) {

        TransitionManager.beginDelayedTransition(binding.cLParent)

        viewModel.transactions[alarmId]?.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)

        childFragmentManager.beginTransaction().remove(viewModel.fragments[alarmId]!!).commit()
        viewModel.transactions.remove(alarmId)
        viewModel.fragments.remove(alarmId)
        viewModel.usedIds.remove(alarmId)
        lifecycleScope.launch {
            viewModel.dataBaseRepository.deleteAlarmById(alarmId)
        }

        viewModel.noAlarmsView.value = (if(viewModel.usedIds.count() > 0) View.GONE else View.VISIBLE)
    }

    /**
     * When alarm sound changed is clicked in the settings view
     */
    private fun onAlarmSoundChange() {
        //check if audio volume is 0

        val audioManager = actualContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) <= 0) {
            Toast.makeText(actualContext, getString(R.string.alarms_ringtone_information), Toast.LENGTH_LONG)
                .show()
        }

        var savedRingtoneUri = Uri.parse(viewModel.dataStoreRepository.getAlarmToneJob())

        if (viewModel.dataStoreRepository.getAlarmToneJob() == "null") {
            savedRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }

        val ringtonePickerBuilder =
            RingtonePickerDialog.Builder(actualContext, parentFragmentManager)
                .setTitle(getString(R.string.alarms_ringtone_header))
                .displayDefaultRingtone(true)
                .setCurrentRingtoneUri(savedRingtoneUri)
                .setPositiveButtonText(getString(R.string.alarms_ringtone_set))
                .setCancelButtonText(getString(R.string.alarms_ringtone_cancel))
                .setPlaySampleWhileSelection(true)
                .setListener { ringtoneName, ringtoneUri ->
                    lifecycleScope.launch {
                        viewModel.dataStoreRepository.updateAlarmTone(
                            ringtoneUri.toString()
                        )
                        viewModel.dataStoreRepository.updateAlarmName(
                            ringtoneName
                        )
                    }

                    viewModel.alarmSoundName.value = (ringtoneName)

                }

        ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_ALARM)
        ringtonePickerBuilder.show()

    }

    //endregion

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAlarmsBinding.inflate(inflater, container, false)

        binding.alarmsViewModel = viewModel
        binding.lifecycleOwner = this

        INSTANCE = this

        viewModel.usedIds = mutableSetOf()
        viewModel.transactions = mutableMapOf()
        viewModel.fragments = mutableMapOf()

        // Update the disable next alarm button by checking the settings of all alarms
        viewModel.alarmsLiveData.observe(viewLifecycleOwner) {
            alarms ->
            lifecycleScope.launch{
                val activeAlarms = getActiveAlarms(alarms, dataStoreRepository = viewModel.dataStoreRepository)
                TransitionManager.beginDelayedTransition(binding.cLParent)

                if (activeAlarms.isNotEmpty()){
                    val nextAlarm = activeAlarms.minByOrNull { x-> x.wakeupEarly }
                    viewModel.tempDisabledVisible.value = nextAlarm?.wasFired == false
                    viewModel.isTempDisabled.value = nextAlarm?.tempDisabled
                }
                else{
                    viewModel.tempDisabledVisible.value = false
                }
            }
        }

        // Update the disable next alarm button by checking the settings of all alarms
        viewModel.sleepParameterLiveData.observe(viewLifecycleOwner){
            lifecycleScope.launch {
                val nextAlarm = viewModel.dataBaseRepository.getNextActiveAlarm(viewModel.dataStoreRepository)
                if (nextAlarm != null){
                    viewModel.tempDisabledVisible.value = nextAlarm.wasFired == false
                    viewModel.isTempDisabled.value = nextAlarm.tempDisabled
                }
                else{
                    viewModel.tempDisabledVisible.value = false
                }
            }
        }

        // new alarm is added
        binding.btnAddAlarmEntity.setOnClickListener {

            if (PermissionsUtil.checkAllNecessaryPermissions(actualContext)) {
                onAddAlarm(it)
            } else {

                (activity as MainActivity).switchToMenu(R.id.profile, changeType = 3)

                Toast.makeText(actualContext, actualContext.getString(R.string.onboarding_grant_all_permissions), Toast.LENGTH_LONG)
                    .show()
            }
        }

        // new click on alarm entity
        binding.sVAlarmEntities.setOnClickListener {
            viewModel.actualExpand.value = (View.GONE)
        }


        // new click on alarm sound settings
        binding.lLAlarmSoundSettings.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.actualExpand.value = (View.GONE)
                    viewModel.rotateState.value = (0)
                }
            }

        // sound was changed click
        binding.soundChange.setOnClickListener{
            onAlarmSoundChange()
        }

        // temp disable alarm was clicked
        binding.btnTemporaryDisableAlarm.setOnClickListener {
            lifecycleScope.launch {
                val nextAlarm = viewModel.dataBaseRepository.getNextActiveAlarm(viewModel.dataStoreRepository)
                if (nextAlarm != null) {
                    if (nextAlarm.tempDisabled && !viewModel.dataStoreRepository.backgroundServiceFlow.first().isForegroundActive) {
                        BackgroundAlarmTimeHandler.getHandler(actualContext).disableAlarmTemporaryInApp(fromApp = true, reactivate = true)
                        viewModel.isTempDisabled.value = false
                    }
                    else if (nextAlarm.tempDisabled && viewModel.dataStoreRepository.backgroundServiceFlow.first().isForegroundActive) {
                        BackgroundAlarmTimeHandler.getHandler(actualContext).disableAlarmTemporaryInApp(fromApp = false, reactivate = true)
                        viewModel.isTempDisabled.value = false
                    }
                    else  {
                        BackgroundAlarmTimeHandler.getHandler(actualContext).disableAlarmTemporaryInApp(fromApp = true, reactivate = false)
                        viewModel.isTempDisabled.value = true
                    }
                }
            }
        }

        // setup the alarms
        setupAlarms()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.alarmArtSelections.value = (mutableListOf((actualContext.getString(R.string.alarms_type_selection_only_alarm)), (actualContext.getString(R.string.alarms_type_selection_alarm_vibration)), (actualContext.getString(R.string.alarms_type_selection_only_vibration))))
        viewModel.alarmSoundName.value = (actualContext.getString(R.string.alarms_type_selection_default))

        viewModel.expandToggled.observe(viewLifecycleOwner){
            TransitionManager.beginDelayedTransition(binding.cLParent)

            /*if(viewModel.actualExpand.value == View.GONE)
                binding.lottieSettings.playAnimation()
            else
                binding.lottieSettings.pauseAnimation()*/
        }
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



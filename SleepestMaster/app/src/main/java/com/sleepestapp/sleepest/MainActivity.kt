package com.sleepestapp.sleepest

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.sleepestapp.sleepest.alarmclock.AlarmClockReceiver
import com.sleepestapp.sleepest.alarmclock.LockScreenAlarmActivity
import com.sleepestapp.sleepest.background.AlarmCycleState
import com.sleepestapp.sleepest.background.AlarmReceiver
import com.sleepestapp.sleepest.background.BackgroundAlarmTimeHandler
import com.sleepestapp.sleepest.databinding.ActivityMainBinding
import com.sleepestapp.sleepest.model.data.AlarmReceiverUsage
import com.sleepestapp.sleepest.model.data.SleepSleepChangeFrom
import com.sleepestapp.sleepest.model.data.export.ImportUtil
import com.sleepestapp.sleepest.ui.alarms.AlarmsFragment
import com.sleepestapp.sleepest.ui.history.HistoryTabView
import com.sleepestapp.sleepest.ui.settings.SettingsFragment
import com.sleepestapp.sleepest.ui.sleep.SleepFragment
import com.sleepestapp.sleepest.util.PermissionsUtil
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil
import com.sleepestapp.sleepest.util.TimeConverterUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import com.sleepestapp.sleepest.googleapi.ActivityTransitionHandler
import com.sleepestapp.sleepest.model.data.AlarmClockReceiverUsage
import com.sleepestapp.sleepest.model.data.Constants
import com.sleepestapp.sleepest.onboarding.OnBoardingActivity


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val actualContext: Context by lazy{ application.applicationContext }

    var factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            // Workaround because we know that we can cast to T
            return  (MainActivityViewModel(
                (actualContext as MainApplication).dataStoreRepository,
                (actualContext as MainApplication).dataBaseRepository
            ) as T)
        }
    }

    /**
     * View model of the [SleepFragment]
     */
    private val viewModel by lazy { ViewModelProvider(this, factory).get(MainActivityViewModel::class.java)}

    // region fragments

    lateinit var alarmsFragment : AlarmsFragment
    lateinit var historyFragment : HistoryTabView
    lateinit var sleepFragment : SleepFragment
    lateinit var settingsFragment : SettingsFragment

    private fun setupFragments(isStart:Boolean){
        lifecycleScope.launch {

            val settings = viewModel.dataStoreRepository.settingsDataFlow.first()

            alarmsFragment = AlarmsFragment()
            historyFragment = HistoryTabView()
            sleepFragment = SleepFragment()
            settingsFragment = SettingsFragment()

            if(isStart || !settings.designDarkModeAckn){
                supportFragmentManager.beginTransaction().add(R.id.navigationFrame, alarmsFragment).commit()
            }
            else{
                if(settings.designDarkModeAckn)
                    viewModel.dataStoreRepository.updateAutoDarkModeAcknowledge(false)

                supportFragmentManager.beginTransaction().replace(
                    R.id.navigationFrame,
                    settingsFragment
                ).commit()

                binding.bottomBar.selectedItemId = R.id.profile

                if(settings.afterRestartApp){
                    settingsFragment.setCaseOfEntry(4)
                    viewModel.dataStoreRepository.updateAfterRestartApp(false)
                }
                else{

                    settingsFragment.setCaseOfEntry(0)
                }
            }

            binding.bottomBar.setOnItemSelectedListener { item->

                val ft = supportFragmentManager.beginTransaction()

                when (item.itemId) {
                    R.id.home -> {
                        if (alarmsFragment.isAdded) {
                            ft.show(alarmsFragment)
                        } else {
                            ft.add(R.id.navigationFrame, alarmsFragment)
                        }
                    }
                    R.id.history -> {
                        if (historyFragment.isAdded) {
                            ft.show(historyFragment)
                        } else {
                            ft.add(R.id.navigationFrame, historyFragment)
                        }
                    }
                    R.id.sleep -> {
                        if (sleepFragment.isAdded) {
                            ft.show(sleepFragment)
                        } else {
                            ft.add(R.id.navigationFrame, sleepFragment)
                        }
                    }
                    else -> {
                        if (settingsFragment.isAdded) {
                            ft.show(settingsFragment)
                        } else {
                            ft.add(R.id.navigationFrame, settingsFragment)
                        }
                    }
                }

                if (item.itemId != R.id.home && alarmsFragment.isAdded) {
                    ft.hide(alarmsFragment)
                }
                if (item.itemId != R.id.history && historyFragment.isAdded) {
                    ft.hide(historyFragment)
                }
                if (item.itemId != R.id.sleep && sleepFragment.isAdded) {
                    ft.hide(sleepFragment)
                }
                if (item.itemId != R.id.profile && settingsFragment.isAdded) {
                    ft.hide(settingsFragment)
                }

                ft.commit()

                true
            }

        }
    }

    fun switchToMenu(itemId: Int, changeType:Int = -1) {
        settingsFragment.setCaseOfEntry(changeType)
        binding.bottomBar.selectedItemId = itemId
    }

    // endregion

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        // null workaround... else pass the [savedInstanceState]
        super.onCreate(null)
        binding = ActivityMainBinding.inflate(layoutInflater)

        lifecycleScope.launch {

            if (!viewModel.dataStoreRepository.tutorialStatusFlow.first().tutorialCompleted) {
                startTutorial()
            } else {
                checkPermissions()
            }

            val settings = viewModel.dataStoreRepository.settingsDataFlow.first()

            if (!settings.designAutoDarkMode && (AppCompatDelegate.getDefaultNightMode() != if (settings.designDarkMode) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO)
            ) {
                AppCompatDelegate
                    .setDefaultNightMode(
                        if (settings.designDarkMode) AppCompatDelegate.MODE_NIGHT_YES
                        else AppCompatDelegate.MODE_NIGHT_NO
                    )
                recreate()
            } else {
                setupFragments(savedInstanceState == null)
                setContentView(binding.root)
            }

            // start/restart activity tracking from main
            if (viewModel.dataStoreRepository.sleepParameterFlow.first().userActivityTracking) {
                ActivityTransitionHandler(actualContext).startActivityHandler()
            }
        }

        supportActionBar?.hide()

        viewModel.alarmsLiveData.observe(this) { alarms ->
            // check the list if empty or not
            lifecycleScope.launch {
                val activeAlarms = SleepTimeValidationUtil.getActiveAlarms(
                    alarms,
                    dataStoreRepository = viewModel.dataStoreRepository
                )

                BackgroundAlarmTimeHandler.getHandler(applicationContext)
                    .changeOfAlarmEntity(activeAlarms.isEmpty())
            }
        }

        // observe sleep time changes
        viewModel.sleepParametersLiveData.observe(this) {
            BackgroundAlarmTimeHandler.getHandler(applicationContext).changeSleepTime()
        }

        viewModel.settingsLiveData.observe(this) { settings ->

            if (settings.restartApp && settings.afterRestartApp) {
                lifecycleScope.launch {
                    viewModel.dataStoreRepository.updateRestartApp(false)
                    recreate()
                }
            }
        }




        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if ("application/json" == intent.type) {
                    lifecycleScope.launch {
                        ImportUtil.getLoadFileFromIntent(
                            intent,
                            applicationContext,
                            viewModel.dataBaseRepository
                        )
                    }
                }
            }
            Intent.ACTION_VIEW -> {
                if ("application/json" == intent.type) {
                    lifecycleScope.launch {
                        ImportUtil.getLoadFileFromIntent(
                            intent,
                            applicationContext,
                            viewModel.dataBaseRepository
                        )
                    }
                }
            }
        }

        val bundle: Bundle? = intent.extras

        if(bundle != null && bundle.getBoolean("from_onboarding"))
        {
            lifecycleScope.launch {
                //Get default settings of tutorial and save it in datastore
                if (viewModel.dataStoreRepository.tutorialStatusFlow.first().tutorialCompleted && !viewModel.dataStoreRepository.tutorialStatusFlow.first().energyOptionsShown) {
                    //DontKillMyAppFragment.show(this@MainActivity)
                }

                val calendar = TimeConverterUtil.getAlarmDate(viewModel.dataStoreRepository.getSleepTimeStart())
                AlarmReceiver.startAlarmManager(
                    calendar[Calendar.DAY_OF_WEEK],
                    calendar[Calendar.HOUR_OF_DAY],
                    calendar[Calendar.MINUTE],
                    applicationContext, AlarmReceiverUsage.START_FOREGROUND)
            }
        }
    }

    private fun startTutorial() {
        val intent = Intent(this, OnBoardingActivity::class.java)
        startActivity(intent)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissions() {
        // check permission

        if(!PermissionsUtil.checkAllNecessaryPermissions(this))
            startTutorial()

        /*if (!PermissionsUtil.isActivityRecognitionPermissionGranted(applicationContext)) {
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        if(!PermissionsUtil.isOverlayPermissionGranted(applicationContext)) {
            PermissionsUtil.setOverlayPermission(this@MainActivity)
        }

        if (!PermissionsUtil.isNotificationPolicyAccessGranted(applicationContext)) {
            PermissionsUtil.setOverlayPermission(this@MainActivity)
        }*/
    }


    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                //Permission denied on Android platform that supports runtime permissions.
            } else {
                // Permission was granted (either by approval or Android version below Q).

                //DontKillMyAppFragment.show(this@MainActivity)

                lifecycleScope.launch {
                    val calendar = TimeConverterUtil.getAlarmDate(viewModel.dataStoreRepository.getSleepTimeBegin())

                    //Start a alarm for the new foreground service start time
                    AlarmReceiver.startAlarmManager(
                        calendar[Calendar.DAY_OF_WEEK],
                        calendar[Calendar.HOUR_OF_DAY],
                        calendar[Calendar.MINUTE],
                        applicationContext, AlarmReceiverUsage.START_FOREGROUND
                    )

                }
            }
        }

}


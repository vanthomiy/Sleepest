package com.sleepestapp.sleepest

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager

import android.os.Build

import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData

import com.sleepestapp.sleepest.background.AlarmCycleState

import com.sleepestapp.sleepest.background.AlarmReceiver
import com.sleepestapp.sleepest.background.BackgroundAlarmTimeHandler
import com.sleepestapp.sleepest.databinding.ActivityMainBinding
import com.sleepestapp.sleepest.model.data.AlarmReceiverUsage
import com.sleepestapp.sleepest.model.data.SleepSleepChangeFrom
import com.sleepestapp.sleepest.model.data.export.ImportUtil
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.ui.alarms.AlarmsFragment
import com.sleepestapp.sleepest.ui.history.HistoryTabView
import com.sleepestapp.sleepest.ui.settings.SettingsFragment
import com.sleepestapp.sleepest.ui.sleep.SleepFragment
import com.sleepestapp.sleepest.util.PermissionsUtil
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil
import com.sleepestapp.sleepest.util.TimeConverterUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.sleepestapp.sleepest.ui.sleep.SleepViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /**
     * View model of the [SleepFragment]
     */
    private val viewModel by lazy { ViewModelProvider(this).get(MainActivityViewModel::class.java)}

    // region fragments

    lateinit var alarmsFragment : AlarmsFragment
    lateinit var historyFragment : HistoryTabView
    lateinit var sleepFragment : SleepFragment
    lateinit var settingsFragment : SettingsFragment

    private fun setupFragments(isStart:Boolean){
        viewModel.scope.launch {

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
                    viewModel.dataStoreRepository.updateAutoDarkModeAckn(false)

                supportFragmentManager.beginTransaction().replace(
                    R.id.navigationFrame,
                    settingsFragment
                ).commit()

                binding.bottomBar.selectedItemId = R.id.profile

                if(settings.afterRestartApp){
                    settingsFragment.setCaseOfEntrie(4)
                    viewModel.dataStoreRepository.updateAfterRestartApp(false)
                }
                else{

                    settingsFragment.setCaseOfEntrie(0)
                }
            }

            binding.bottomBar.setOnNavigationItemSelectedListener { item->

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
        settingsFragment.setCaseOfEntrie(changeType)
        binding.bottomBar.selectedItemId = itemId;
    }

    // endregion

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        // null workaround... else pass the [savedInstanceState]
        super.onCreate(null)
        binding = ActivityMainBinding.inflate(layoutInflater)

        viewModel.scope.launch {
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
            }
            else {
                setupFragments(savedInstanceState == null)
                setContentView(binding.root)
            }
        }

        supportActionBar?.hide()

        // observe alarm changes
        viewModel.activeAlarmsLiveData.observe(this){ list ->
            // check the list if empty or not
            BackgroundAlarmTimeHandler.getHandler(applicationContext).changeOfAlarmEntity(list.isEmpty())
        }

        // observe sleeptime changes
        viewModel.sleepParametersLiveData.observe(this) {
            BackgroundAlarmTimeHandler.getHandler(applicationContext).changeSleepTime()
        }

        viewModel.settingsLiveData.observe(this) { settings ->

            if(settings.restartApp && settings.afterRestartApp)
            {
                viewModel.scope.launch {
                    viewModel.dataStoreRepository.updateRestartApp(false)
                    recreate()
                }
            }
        }


        // check permission
        if (!PermissionsUtil.isActivityRecognitionPermissionGranted(applicationContext)) {
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        if(!PermissionsUtil.isOverlayPermissionGranted(applicationContext)) {
            PermissionsUtil.setOverlayPermission(this@MainActivity)
        }

        if (!PermissionsUtil.isNotificationPolicyAccessGranted(applicationContext)) {
            PermissionsUtil.setOverlayPermission(this@MainActivity)
        }

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if ("application/json" == intent.type) {
                    viewModel.scope.launch {
                        ImportUtil.getLoadFileFromIntent(intent, applicationContext, viewModel.dataBaseRepository)
                    }
                }
            }
            Intent.ACTION_VIEW -> {
                if ("application/json" == intent.type) {
                    viewModel.scope.launch {
                        ImportUtil.getLoadFileFromIntent(intent, applicationContext, viewModel.dataBaseRepository)
                    }
                }
            }
        }

        val bundle :Bundle ?=intent.extras

        //Get default settings of tutorial and save it in datastore
        //TODO("Shared prefs!")
        if (bundle != null && bundle.getBoolean(getString(R.string.onboarding_intent_data_available))) {
            viewModel.scope.launch {
                if (viewModel.dataStoreRepository.tutorialStatusFlow.first().tutorialCompleted && !viewModel.dataStoreRepository.tutorialStatusFlow.first().energyOptionsShown) {
                    DontKillMyAppFragment.show(this@MainActivity)
                }
                //Start a alarm for the new foregroundservice start time
                val calendar = TimeConverterUtil.getAlarmDate(bundle.getInt(getString(R.string.onboarding_intent_starttime)))
                AlarmReceiver.startAlarmManager(
                    calendar[Calendar.DAY_OF_WEEK],
                    calendar[Calendar.HOUR_OF_DAY],
                    calendar[Calendar.MINUTE],
                    applicationContext, AlarmReceiverUsage.START_FOREGROUND)

                SleepTimeValidationUtil.checkSleepActionIsAllowedAndDoAction(
                    viewModel.dataStoreRepository,
                    viewModel.dataBaseRepository,
                    bundle.getInt(getString(R.string.onboarding_intent_starttime)),
                    bundle.getInt(getString(R.string.onboarding_intent_endtime)),
                    bundle.getInt(getString(R.string.onboarding_intent_duration)),
                    false,
                    SleepSleepChangeFrom.DURATION
                )

                SleepTimeValidationUtil.checkSleepActionIsAllowedAndDoAction(
                    viewModel.dataStoreRepository,
                    viewModel.dataBaseRepository,
                    bundle.getInt(getString(R.string.onboarding_intent_starttime)),
                    bundle.getInt(getString(R.string.onboarding_intent_endtime)),
                    bundle.getInt(getString(R.string.onboarding_intent_duration)),
                    false,
                    SleepSleepChangeFrom.SLEEPTIMEEND
                )

                SleepTimeValidationUtil.checkSleepActionIsAllowedAndDoAction(
                    viewModel.dataStoreRepository,
                    viewModel.dataBaseRepository,
                    bundle.getInt(getString(R.string.onboarding_intent_starttime)),
                    bundle.getInt(getString(R.string.onboarding_intent_endtime)),
                    bundle.getInt(getString(R.string.onboarding_intent_duration)),
                    false,
                    SleepSleepChangeFrom.SLEEPTIMESTART
                )

            }

        }

        val alarmCycleState = AlarmCycleState(applicationContext)
        val pref: SharedPreferences = getSharedPreferences("State", 0)
        val ed = pref.edit()
        ed.putString("state", alarmCycleState.getState().toString())
        ed.apply()
    }


    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                //mainViewModel.updatePermissionActive(false)
                //Permission denied on Android platform that supports runtime permissions.
                //displayPermissionSettingsSnackBar()
            } else {
                //mainViewModel.updatePermissionActive(true)
                // Permission was granted (either by approval or Android version below Q).

                com.sleepestapp.sleepest.DontKillMyAppFragment.show(this@MainActivity)

                viewModel.scope.launch {
                    val calendar = TimeConverterUtil.getAlarmDate(viewModel.dataStoreRepository.getSleepTimeBegin())

                    //Start a alarm for the new foregroundservice start time
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


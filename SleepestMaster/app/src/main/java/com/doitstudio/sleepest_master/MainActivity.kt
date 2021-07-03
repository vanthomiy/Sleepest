package com.doitstudio.sleepest_master

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat.recreate
import androidx.core.content.ContextCompat
import androidx.lifecycle.asLiveData
import com.doitstudio.sleepest_master.background.AlarmReceiver
import com.doitstudio.sleepest_master.background.ForegroundService
import com.doitstudio.sleepest_master.databinding.ActivityMainBinding
import com.doitstudio.sleepest_master.model.data.Actions
import com.doitstudio.sleepest_master.model.data.AlarmReceiverUsage
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.ui.profile.ProfileFragment
import com.doitstudio.sleepest_master.ui.sleep.SleepFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomBar: BottomNavigationView

    // region Databases

    private val scope: CoroutineScope = MainScope()

    private val dataBaseRepository: DatabaseRepository by lazy {
        (applicationContext as MainApplication).dataBaseRepository
    }

    private val dataStoreRepository: DataStoreRepository by lazy {
        (applicationContext as MainApplication).dataStoreRepository
    }

    private val activeAlarmsLiveData by lazy {  dataBaseRepository.activeAlarmsFlow().asLiveData() }
    private val sleepParametersLiveData by lazy {  dataStoreRepository.sleepParameterFlow.asLiveData() }
    private val settingsLiveData by lazy {  dataStoreRepository.settingsDataFlow.asLiveData() }


    private var sleepTimeBeginTemp = 0
    private var earliestWakeupTemp = 0

    // endregion

    // region fragments

    lateinit var alarmsFragment : AlarmsFragment
    lateinit var historyFragment : HistoryFragment
    lateinit var sleepFragment : SleepFragment
    lateinit var profileFragment : ProfileFragment

    fun setupFragments(isStart:Boolean){
        scope.launch {

            val settings = dataStoreRepository.settingsDataFlow.first()

            bottomBar = binding.bottomBar
            alarmsFragment = AlarmsFragment()
            historyFragment = HistoryFragment(applicationContext)
            sleepFragment = SleepFragment()
            profileFragment = ProfileFragment()

            if(isStart){
                supportFragmentManager.beginTransaction().add(R.id.navigationFrame, alarmsFragment).commit()
            }
            else{
                supportFragmentManager.beginTransaction().replace(R.id.navigationFrame, profileFragment).commit()
                if(settings.afterRestartApp){
                    profileFragment.caseOfEntrie = 2
                    dataStoreRepository.updateAfterRestartApp(false)
                }
                else{
                    profileFragment.caseOfEntrie = if(isStart) 0 else 1
                }
            }



            bottomBar.setOnNavigationItemSelectedListener { item->

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
                        if(profileFragment.isAdded){
                            ft.show(profileFragment)
                        }else{
                            ft.add(R.id.navigationFrame, profileFragment)
                        }
                    }
                }

                // Hide fragment B
                if (item.title != "home" && alarmsFragment.isAdded) { ft.hide(alarmsFragment) }
                if (item.title != "history" && historyFragment.isAdded) { ft.hide(historyFragment) }
                if (item.title != "sleep" && sleepFragment.isAdded) { ft.hide(sleepFragment) }
                if (item.title != "profile" && profileFragment.isAdded) { ft.hide(profileFragment) }

                ft.commit()

                true
            }

        }


    }

    // endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        scope.launch {
            // check system dark mode if necessary and safe in
            val settings = dataStoreRepository.settingsDataFlow.first()
            if(settings.designAutoDarkMode){
                AppCompatDelegate
                        .setDefaultNightMode(
                                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
            /*else {
                AppCompatDelegate
                        .setDefaultNightMode(if(settings.designDarkMode)
                            AppCompatDelegate.MODE_NIGHT_YES else
                            AppCompatDelegate.MODE_NIGHT_NO);
            }*/
        }


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFragments(savedInstanceState == null)

        sleepTimeBeginTemp = dataStoreRepository.getSleepTimeBeginJob();

        scope.launch {

            if (dataBaseRepository.getNextActiveAlarm() != null) {
                earliestWakeupTemp = dataBaseRepository.getNextActiveAlarm()!!.wakeupEarly
            } else {
                earliestWakeupTemp = 0
            }


        }


        // observe alarm changes
        activeAlarmsLiveData.observe(this){ list ->
            // check the list if empty or not

            scope.launch {
                // is in sleep time ?
                if (dataStoreRepository.isInSleepTime() && dataBaseRepository.getNextActiveAlarm() != null) {
                    if(list.isEmpty())
                    {
                        // empty..
                        // We need to check if foreground is active or not... if not active we have to start it from here
                        // if already inside sleeptime
                        if(dataStoreRepository.backgroundServiceFlow.first().isForegroundActive){
                            ForegroundService.startOrStopForegroundService(
                                Actions.STOP,
                                applicationContext
                            )
                        }
                    }
                    else if(!dataStoreRepository.backgroundServiceFlow.first().isForegroundActive && !dataBaseRepository.getNextActiveAlarm()!!.wasFired
                            && ((LocalTime.now().toSecondOfDay() < dataBaseRepository.getNextActiveAlarm()!!.actualWakeup) || (dataStoreRepository.getSleepTimeBegin() < LocalTime.now().toSecondOfDay()))){
                        // Is empty..
                        // We need to check if foreground is active or not... if active we have to stop it from here
                        // if already inside sleeptime
                        ForegroundService.startOrStopForegroundService(
                            Actions.START,
                            applicationContext
                        )
                    }
                }

            }
        }

        // observe sleeptime changes
        sleepParametersLiveData.observe(this) { livedata ->

            scope.launch {

                // in sleep time
                if (dataStoreRepository.isInSleepTime() && (dataBaseRepository.getNextActiveAlarm() != null)) {
                    // alarm should be active else set active
                    if(!dataStoreRepository.backgroundServiceFlow.first().isForegroundActive){

                        if (!dataBaseRepository.getNextActiveAlarm()!!.wasFired ||
                                ((LocalTime.now().toSecondOfDay() > dataBaseRepository.getNextActiveAlarm()!!.actualWakeup) &&
                                        (dataStoreRepository.getSleepTimeBegin() < LocalTime.now().toSecondOfDay()))) {
                            ForegroundService.startOrStopForegroundService(
                                Actions.START,
                                applicationContext
                            )
                        }
                    } else if (earliestWakeupTemp != dataBaseRepository.getNextActiveAlarm()!!.wakeupEarly) {

                        earliestWakeupTemp = dataBaseRepository.getNextActiveAlarm()!!.wakeupEarly

                        val calendarFirstCalc = AlarmReceiver.getAlarmDate(dataBaseRepository.getNextActiveAlarm()!!.wakeupEarly - 1800)
                        AlarmReceiver.startAlarmManager(calendarFirstCalc[Calendar.DAY_OF_WEEK], calendarFirstCalc[Calendar.HOUR_OF_DAY], calendarFirstCalc[Calendar.MINUTE], applicationContext,AlarmReceiverUsage.START_WORKMANAGER_CALCULATION)
                    }
                } else // not in sleep time
                {
                    // alarm should be not active else disable and set to a new time...
                    if(dataStoreRepository.backgroundServiceFlow.first().isForegroundActive){
                        ForegroundService.startOrStopForegroundService(
                            Actions.STOP,
                            applicationContext
                        )
                    }

                    if (sleepTimeBeginTemp != livedata.sleepTimeStart) {
                        sleepTimeBeginTemp = livedata.sleepTimeStart

                        val calendarAlarm = Calendar.getInstance()
                        calendarAlarm[Calendar.HOUR_OF_DAY] = 0
                        calendarAlarm[Calendar.MINUTE] = 0
                        calendarAlarm[Calendar.SECOND] = 0
                        calendarAlarm.add(Calendar.SECOND, livedata.sleepTimeStart)

                        //Start a alarm for the new foregroundservice start time
                        AlarmReceiver.startAlarmManager(calendarAlarm[Calendar.DAY_OF_WEEK], calendarAlarm[Calendar.HOUR_OF_DAY], calendarAlarm[Calendar.MINUTE], applicationContext, AlarmReceiverUsage.START_FOREGROUND)

                        val pref = getSharedPreferences("AlarmReceiver1", 0)
                        val ed = pref.edit()
                        ed.putString("usage", "MainActivity")
                        ed.putInt("day", calendarAlarm[Calendar.DAY_OF_WEEK])
                        ed.putInt("hour", calendarAlarm[Calendar.HOUR_OF_DAY])
                        ed.putInt("minute", calendarAlarm[Calendar.MINUTE])
                        ed.apply()

                    }


                }
            }
        }


        settingsLiveData.observe(this) { livedata ->

            if(livedata.restartApp && livedata.afterRestartApp)
            {
                scope.launch {
                    dataStoreRepository.updateRestartApp(false)
                    recreate()
                }
            }
        }

        // check permission
        if (!activityRecognitionPermissionApproved()) {
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        checkDrawOverlayPermission()
        checkDoNotDisturbPermission()
    }

    override fun onResume() {
        super.onResume()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (!notificationManager.isNotificationPolicyAccessGranted){
            //Toast.makeText(this,"Alarm could be silence without this permission", Toast.LENGTH_SHORT).show()
            val snack = Snackbar.make(findViewById(android.R.id.content),"This is a simple Snackbar",Snackbar.LENGTH_INDEFINITE)
            snack.show()
        } else if(!Settings.canDrawOverlays(this)) {
            //Toast.makeText(this,"Sorry. Can't draw overlays without permission...", Toast.LENGTH_SHORT).show()
            val snack = Snackbar.make(findViewById(android.R.id.content),"This is a simple Snackbar",Snackbar.LENGTH_INDEFINITE)
            snack.show()
        } else if(!activityRecognitionPermissionApproved()) {
            val snack = Snackbar.make(findViewById(android.R.id.content),"This is a simple Snackbar",Snackbar.LENGTH_INDEFINITE)
            snack.show()
        }

    }

    private fun checkDoNotDisturbPermission() {
       val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (!notificationManager.isNotificationPolicyAccessGranted){
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }

    private fun checkDrawOverlayPermission() {

        // Checks if app already has permission to draw overlays
        if (!Settings.canDrawOverlays(this)) {

            // If not, start Intent to launch the permission request
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        }
    }
    
    // region get permission for sleep api at first start etc.
    private fun activityRecognitionPermissionApproved(): Boolean {
        // Because this app targets 29 and above (recommendation for using the Sleep APIs), we
        // don't need to check if this is on a device before runtime permissions, that is, a device
        // prior to 29 / Q.
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
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

                DontKillMyAppFragment.show(this@MainActivity)

                scope.launch {
                    val calendar = AlarmReceiver.getAlarmDate(dataStoreRepository.getSleepTimeBegin())
                    //AlarmReceiver.cancelAlarm(applicationContext, 6)
                    AlarmReceiver.startAlarmManager(
                        calendar.get(Calendar.DAY_OF_WEEK), calendar.get(
                            Calendar.HOUR_OF_DAY
                        ), calendar.get(Calendar.MINUTE), applicationContext, AlarmReceiverUsage.START_WORKMANAGER
                    )

                    val calendarAlarm = Calendar.getInstance()
                    calendarAlarm[Calendar.HOUR_OF_DAY] = 0
                    calendarAlarm[Calendar.MINUTE] = 0
                    calendarAlarm[Calendar.SECOND] = 0
                    calendarAlarm.add(Calendar.SECOND, dataStoreRepository.getSleepTimeBegin())

                    //Start a alarm for the new foregroundservice start time
                    AlarmReceiver.startAlarmManager(
                        calendarAlarm[Calendar.DAY_OF_WEEK],
                        calendarAlarm[Calendar.HOUR_OF_DAY],
                        calendarAlarm[Calendar.MINUTE],
                        applicationContext, AlarmReceiverUsage.START_FOREGROUND
                    )

                }
            }
        }

}


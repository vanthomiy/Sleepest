package com.doitstudio.sleepest_master

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.recreate
import androidx.core.content.ContextCompat
import androidx.lifecycle.asLiveData
import com.doitstudio.sleepest_master.background.AlarmReceiver
import com.doitstudio.sleepest_master.background.ForegroundService
import com.doitstudio.sleepest_master.databinding.ActivityMainBinding
import com.doitstudio.sleepest_master.model.data.Actions
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.ui.profile.ProfileFragment
import com.doitstudio.sleepest_master.ui.sleep.SleepFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
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

    fun setupFragments(){

        bottomBar = binding.bottomBar
        alarmsFragment = AlarmsFragment()
        historyFragment = HistoryFragment(applicationContext)
        sleepFragment = SleepFragment()
        profileFragment = ProfileFragment()

        supportFragmentManager.beginTransaction().add(R.id.navigationFrame, alarmsFragment).commit()

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

    // endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFragments()

        sleepTimeBeginTemp = dataStoreRepository.getSleepTimeBeginJob();
        scope.launch {

            //dataStoreRepository.updateRestartApp(false)

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
                        ForegroundService.startOrStopForegroundService(Actions.START, applicationContext)
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
                            ForegroundService.startOrStopForegroundService(Actions.START, applicationContext)
                        }
                    } else if (earliestWakeupTemp != dataBaseRepository.getNextActiveAlarm()!!.wakeupEarly) {

                        earliestWakeupTemp = dataBaseRepository.getNextActiveAlarm()!!.wakeupEarly
                        //AlarmReceiver.cancelAlarm(applicationContext, 5)

                        //Create a new instance of calendar for the new foregroundservice start time

                        /*if (AlarmReceiver.isAlarmManagerActive(applicationContext, 5)) {
                            AlarmReceiver.cancelAlarm(applicationContext, 5)
                        }*/

                        val calendarFirstCalc = AlarmReceiver.getAlarmDate(dataBaseRepository.getNextActiveAlarm()!!.wakeupEarly - 1800)
                        AlarmReceiver.startAlarmManager(calendarFirstCalc[Calendar.DAY_OF_WEEK], calendarFirstCalc[Calendar.HOUR_OF_DAY], calendarFirstCalc[Calendar.MINUTE], applicationContext, 5)
                    }
                } else // not in sleep time
                {
                    // alarm should be not active else disable and set to a new time...
                    if(dataStoreRepository.backgroundServiceFlow.first().isForegroundActive){
                        ForegroundService.startOrStopForegroundService(Actions.STOP, applicationContext)
                    }

                    if (sleepTimeBeginTemp != livedata.sleepTimeStart) {
                        sleepTimeBeginTemp = livedata.sleepTimeStart

                        /*AlarmReceiver.cancelAlarm(applicationContext, 1)

                        //Create a new instance of calendar for the new foregroundservice start time

                        if (AlarmReceiver.isAlarmManagerActive(applicationContext, 1)) {
                            AlarmReceiver.cancelAlarm(applicationContext, 1)
                        }

                        if (!AlarmReceiver.isAlarmManagerActive(applicationContext, 1)) {*/
                            val calendarAlarm = Calendar.getInstance()
                            calendarAlarm[Calendar.HOUR_OF_DAY] = 0
                            calendarAlarm[Calendar.MINUTE] = 0
                            calendarAlarm[Calendar.SECOND] = 0
                            calendarAlarm.add(Calendar.SECOND, livedata.sleepTimeStart)

                            //Start a alarm for the new foregroundservice start time
                            AlarmReceiver.startAlarmManager(
                                    calendarAlarm[Calendar.DAY_OF_WEEK],
                                    calendarAlarm[Calendar.HOUR_OF_DAY],
                                    calendarAlarm[Calendar.MINUTE],
                                    applicationContext, 1
                            )
                        //}
                    }


                }
            }
        }

        settingsLiveData.observe(this) { livedata ->

            if(livedata.restartApp)
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
    }

    private fun checkDrawOverlayPermission() {

        // Checks if app already has permission to draw overlays
        if (!Settings.canDrawOverlays(this)) {

            // If not, form up an Intent to launch the permission request
            val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse(
                    "package:$packageName"
            )
            )

            // Launch Intent, with the supplied request code
            startActivityForResult(intent, 1234)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check if a request code is received that matches that which we provided for the overlay draw request
        if (requestCode == 1234) {

            // Double-check that the user granted it, and didn't just dismiss the request
            if (Settings.canDrawOverlays(this)) {

                // Launch the service

            } else {
                Toast.makeText(
                        this,
                        "Sorry. Can't draw overlays without permission...",
                        Toast.LENGTH_SHORT
                ).show()
            }
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
                    AlarmReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), applicationContext, 6)

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
                            applicationContext, 1
                    )

                }
            }
        }
}


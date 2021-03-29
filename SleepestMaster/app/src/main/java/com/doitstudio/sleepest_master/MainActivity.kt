package com.doitstudio.sleepest_master

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.viewbinding.BuildConfig
import com.doitstudio.sleepest_master.sleepapi.SleepHandler
import com.doitstudio.sleepest_master.Background.ForegroundService
import com.doitstudio.sleepest_master.Background.Workmanager
import com.doitstudio.sleepest_master.databinding.ActivityMainBinding
import com.doitstudio.sleepest_master.model.data.Actions
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Status of subscription to sleep data. This is stored in [SleepSubscriptionStatus] which saves
    // the data in a [DataStore] in case the user navigates away from the app.
    private var alarmActive = false
        set(newAlarmActive) {
            field = newAlarmActive
            if (newAlarmActive) {
                binding.buttonAlarmToogle.text = getString(R.string.alarm_active)
            } else {
                binding.buttonAlarmToogle.text = getString(R.string.alarm_disabled)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel.alarmLiveData.observe(this) { alarmData ->
            if (alarmActive != alarmData?.isActive) {
                alarmActive = alarmData?.isActive == true
            }

            binding.sleepSegmentsText.text = alarmData.alarmName
        }

        mainViewModel.sleepApiLiveData.observe(this) { sleepApiData ->
            var text:String = sleepApiData.isSubscribed.toString() + "\n"
            text += sleepApiData.subscribeFailed.toString()
            binding.sleepApiDataStatus.text = text

            isTimerRunning = sleepApiData.isSubscribed
        }

        // check permission
        if (!activityRecognitionPermissionApproved()) {
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    private val mainViewModel: MainViewModel by lazy {
        MainViewModel(
            (application as MainApplication).dbRepository,
            (application as MainApplication).dataStoreRepository
        )
    }

    private val sch:SleepCalculationHandler by lazy {
        SleepCalculationHandler.getHandler(this)
    }

    fun buttonClick1(view: View){
        sch.calculateSleepData()
    }

    private val sleepHandler : SleepHandler by lazy {SleepHandler.getHandler(this)}

    fun buttonClick2(view: View){
        ForegroundService.startOrStopForegroundService(Actions.START, this)
        Workmanager.startPeriodicWorkmanager(15);
    }

    var isTimerRunning = false

    fun buttonClick3(view: View) {
        requestData()

    }

    private fun requestData(){
        if (activityRecognitionPermissionApproved()) {
            if (isTimerRunning) {
                sleepHandler.stopSleepHandler()
            } else {
                sleepHandler.startSleepHandler()
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
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
                    mainViewModel.updatePermissionActive(false)
                    // Permission denied on Android platform that supports runtime permissions.
                    displayPermissionSettingsSnackBar()
                } else {
                    mainViewModel.updatePermissionActive(true)
                    // Permission was granted (either by approval or Android version below Q).
                }
            }

    private fun displayPermissionSettingsSnackBar() {
        Snackbar.make(
                binding.mainActivity,
                "hiHo",
                Snackbar.LENGTH_LONG
        )
                .setAction("Settings") {
                    // Build intent that displays the App settings screen.
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts(
                            "package",
                            //BuildConfig.APPLICATION_ID,
                            BuildConfig.VERSION_NAME,
                            null
                    )
                    intent.data = uri
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                .show()
    }
    // endregion

}
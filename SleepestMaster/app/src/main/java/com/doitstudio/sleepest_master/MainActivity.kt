package com.doitstudio.sleepest_master

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewbinding.BuildConfig
import com.doitstudio.sleepest_master.background.AlarmReceiver
import com.doitstudio.sleepest_master.background.ForegroundService
import com.doitstudio.sleepest_master.databinding.ActivityMainBinding
import com.doitstudio.sleepest_master.model.data.Actions
import com.doitstudio.sleepest_master.sleepapi.SleepHandler
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Status of subscription to sleep data. This is stored in [SleepSubscriptionStatus] which saves
    // the data in a [DataStore] in case the user navigates away from the app.


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //val fs = ForegroundObserver(this, this)


        mainViewModel.liveUserSleepActivityLiveData.observe(this) { data ->

            var text = "User Sleeping: " + data.isUserSleeping + "\n"
            text += "Is Data Available: " + data.isDataAvailable + "\n"


            binding.status2.text = text
        }

        /*mainViewModel.userSleepSessionLiveData.observe(this) { data ->
            val lastdata = data.firstOrNull() ?: return@observe

            var text = "Sleep Time: " + lastdata?.sleepTimes.sleepDuration + "\n"
            text += "Sleep Type Phone: " + lastdata?.sleepUserType?.mobilePosition + "\n"

            binding.status1.text = text
        }*/

        val pref = getSharedPreferences("Alarm", MODE_PRIVATE)
        val logText = pref.getString("Log", "No Data")
        status1.text = logText

        mainViewModel.sleepApiLiveData.observe(this) { data ->

            var text = "Permission Active: " + data.isPermissionActive + "\n"
            text += "Subscribed: " + data.isSubscribed + "\n"
            text += "Sleep Values: " + data.sleepApiValuesAmount + "\n"

            binding.status0.text = text
        }

        // check permission
        if (!activityRecognitionPermissionApproved()) {
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        requestData()
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
        sch.calculateLiveuserSleepActivity()
    }

    private val sleepHandler : SleepHandler by lazy {SleepHandler.getHandler(this)}

    fun buttonClick2(view: View){

        /**
         * TEST
         */

        AlarmReceiver.startAlarmManager(5, 21, 27, applicationContext, 1)

        //ForegroundService.startOrStopForegroundService(Actions.START, applicationContext)

        sch.calculateUserWakup()

    }

    var isTimerRunning = false

    fun buttonClick3(view: View) {
        sch.recalculateUserSleep()
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
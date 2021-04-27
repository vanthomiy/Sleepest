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
import com.doitstudio.sleepest_master.alarmclock.AlarmClockReceiver
import com.doitstudio.sleepest_master.background.AlarmReceiver
import com.doitstudio.sleepest_master.background.ForegroundService
import com.doitstudio.sleepest_master.background.Workmanager
import com.doitstudio.sleepest_master.databinding.ActivityMainBinding
import com.doitstudio.sleepest_master.model.data.Actions
import com.doitstudio.sleepest_master.model.data.SleepStatePattern
import com.doitstudio.sleepest_master.model.data.UserFactorPattern
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler
import com.doitstudio.sleepest_master.sleepcalculation.db.SleepStateParameterEntity
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepStateParameter
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val scope: CoroutineScope = MainScope()

    // Status of subscription to sleep data. This is stored in [SleepSubscriptionStatus] which saves
    // the data in a [DataStore] in case the user navigates away from the app.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel.allSleepStateParameters.observe(this){ data->
            binding.status1.text = data.size.toString()
        }

        mainViewModel.allSleepTimeModels.observe(this){ data->
            binding.status2.text = data.size.toString()
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
                (application as MainApplication).sleepCalculationRepository,
                (application as MainApplication).sleepCalculationDbRepository
        )
    }

    private val sch: SleepCalculationHandler by lazy {
        SleepCalculationHandler.getHandler(this)
    }

    fun buttonClick1(view: View){
        ForegroundService.startOrStopForegroundService(Actions.START, applicationContext)
        //xyz()
    }

    fun xyz() {
        var calenderAlarm = AlarmReceiver.getAlarmDate(Calendar.getInstance()[Calendar.DAY_OF_WEEK], 20, 26)
        AlarmReceiver.startAlarmManager(calenderAlarm[Calendar.DAY_OF_WEEK], calenderAlarm[Calendar.HOUR_OF_DAY], calenderAlarm[Calendar.MINUTE], applicationContext, 2)

        /*calenderAlarm = AlarmReceiver.getAlarmDate(Calendar.getInstance()[Calendar.DAY_OF_WEEK], 9, 52)
        AlarmReceiver.startAlarmManager(calenderAlarm[Calendar.DAY_OF_WEEK], calenderAlarm[Calendar.HOUR_OF_DAY], calenderAlarm[Calendar.MINUTE], applicationContext, 4)


        calenderAlarm = AlarmReceiver.getAlarmDate(Calendar.getInstance()[Calendar.DAY_OF_WEEK], 9, 54)
        AlarmReceiver.startAlarmManager(calenderAlarm[Calendar.DAY_OF_WEEK], calenderAlarm[Calendar.HOUR_OF_DAY], calenderAlarm[Calendar.MINUTE], applicationContext, 2)
        AlarmClockReceiver.startAlarmManager(calenderAlarm[Calendar.DAY_OF_WEEK], calenderAlarm[Calendar.HOUR_OF_DAY], calenderAlarm[Calendar.MINUTE], applicationContext, 1)

        AlarmReceiver.cancelAlarm(applicationContext, 1)*/

        /*calenderAlarm = AlarmReceiver.getAlarmDate(Calendar.getInstance()[Calendar.DAY_OF_WEEK], 8, 28)
        AlarmReceiver.startAlarmManager(calenderAlarm[Calendar.DAY_OF_WEEK], calenderAlarm[Calendar.HOUR_OF_DAY], calenderAlarm[Calendar.MINUTE], applicationContext, 1)

        AlarmClockReceiver.startAlarmManager(calenderAlarm[Calendar.DAY_OF_WEEK], calenderAlarm[Calendar.HOUR_OF_DAY], calenderAlarm[Calendar.MINUTE], applicationContext)
        calenderAlarm = AlarmReceiver.getAlarmDate(Calendar.getInstance()[Calendar.DAY_OF_WEEK], 8, 32)
        AlarmReceiver.startAlarmManager(calenderAlarm[Calendar.DAY_OF_WEEK], calenderAlarm[Calendar.HOUR_OF_DAY], calenderAlarm[Calendar.MINUTE], applicationContext, 2)*/

    }

    var index = 9
    fun buttonClick2(view: View){
        /*AlarmReceiver.cancelAlarm(applicationContext, 2)
        AlarmClockReceiver.cancelAlarm(applicationContext, 1)
        val calendar = AlarmReceiver.getAlarmDate(Calendar.getInstance()[Calendar.DAY_OF_WEEK], 10, 39)
        AlarmReceiver.startAlarmManager(calendar[Calendar.DAY_OF_WEEK], calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE], applicationContext, 2)
        AlarmClockReceiver.startAlarmManager(calendar[Calendar.DAY_OF_WEEK], calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE], applicationContext, 1)
        */
    }

    var isTimerRunning = false

    fun buttonClick3(view: View) {

        // Testing

        val stpe = SleepStateParameterEntity(
                "12", UserFactorPattern.NORMAL, SleepStatePattern.TOLESSREM, SleepStateParameter(
                1f,
                1f,
                1f,
                1f,
                1f,
                1f,
                1f,
                1f,
                1f,
                1f,
                1f,
                20
        )
        )
        val jsonString = Gson().toJson(stpe)

    }

    private fun requestData(){
        if (activityRecognitionPermissionApproved()) {

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
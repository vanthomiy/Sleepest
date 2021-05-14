package com.doitstudio.sleepest_master

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.doitstudio.sleepest_master.background.AlarmReceiver
import com.doitstudio.sleepest_master.background.ForegroundService
import com.doitstudio.sleepest_master.databinding.ActivityMainBinding
import com.doitstudio.sleepest_master.model.data.Actions
import com.doitstudio.sleepest_master.sleepapi.SleepHandler
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationStoreRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val scope: CoroutineScope = MainScope()
    private val sleepCalculationStoreRepository by lazy {  SleepCalculationStoreRepository.getRepo(applicationContext)}


    // Status of subscription to sleep data. This is stored in [SleepSubscriptionStatus] which saves
    // the data in a [DataStore] in case the user navigates away from the app.


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        /**Start Test*/

        var pref = getSharedPreferences("AlarmChanged", 0)
        val textAlarm = """
            Last Alarm changed: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)},${pref.getInt("actualWakeup", 0)},${pref.getInt("alarmUse", 0)}
            
            """.trimIndent()
        pref = getSharedPreferences("StartService", 0)
        val textStartService = """
            Last service start: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)}
            
            """.trimIndent()
        pref = getSharedPreferences("StopService", 0)
        val textStopService = """
            Last service stop: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)}
            
            """.trimIndent()
        pref = getSharedPreferences("Workmanager", 0)
        val textLastWorkmanager = """
            Last workmanager call: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)}
            
            """.trimIndent()
        pref = getSharedPreferences("WorkmanagerCalculation", 0)
        val textLastWorkmanagerCalculation = """
            Last workmanagerCalc call: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)}
            
            """.trimIndent()
        pref = getSharedPreferences("SleepCalc1", 0)
        val textCalc1 = """
            Calc1: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)}
            
            """.trimIndent()
        pref = getSharedPreferences("SleepCalc2", 0)
        val textCalc2 = """
            Calc2: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)},${pref.getInt("value1", 0)},${pref.getInt(
                "value2",
                0
        )}
            
            """.trimIndent()
        pref = getSharedPreferences("AlarmReceiver", 0)
        val textAlarmReceiver = """
            AlarmReceiver: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)},${pref.getInt(
                "intent",
                0
        )}
            
            """.trimIndent()
        pref = getSharedPreferences("StopException", 0)
        val textStopException = """
            Exc.: ${pref.getString("exception", "XX")}
            
            """.trimIndent()



        val textGesamt = textAlarm + textStartService + textStopService + textLastWorkmanager + textLastWorkmanagerCalculation + textCalc1 + textCalc2 + textAlarmReceiver + textStopException

        binding.status0.text = textGesamt

        /**EndTest*/

        /*
        mainViewModel.allSleepStateParameters.observe(this){ data->
            binding.status1.text = data.size.toString()
        }
         */


        //val fs = ForegroundObserver(this, this)


        // check permission
        if (!activityRecognitionPermissionApproved()) {
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        requestData()

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

    fun buttonClick1(view: View){
        ForegroundService.startOrStopForegroundService(Actions.START, applicationContext)
        //xyz()
    }

    fun xyz() {
        var calenderAlarm = AlarmReceiver.getAlarmDate(Calendar.getInstance()[Calendar.DAY_OF_WEEK], 14, 38)
        AlarmReceiver.startAlarmManager(calenderAlarm[Calendar.DAY_OF_WEEK], calenderAlarm[Calendar.HOUR_OF_DAY], calenderAlarm[Calendar.MINUTE], applicationContext, 1)

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

    private val sleepHandler : SleepHandler by lazy {SleepHandler.getHandler(this)}

    fun buttonClick2(view: View){
        /*AlarmReceiver.cancelAlarm(applicationContext, 2)
        AlarmClockReceiver.cancelAlarm(applicationContext, 1)
        val calendar = AlarmReceiver.getAlarmDate(Calendar.getInstance()[Calendar.DAY_OF_WEEK], 10, 39)
        AlarmReceiver.startAlarmManager(calendar[Calendar.DAY_OF_WEEK], calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE], applicationContext, 2)
        AlarmClockReceiver.startAlarmManager(calendar[Calendar.DAY_OF_WEEK], calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE], applicationContext, 1)
        */
        //setSleepTime()
    }


    private fun setSleepTime(){
        scope.launch {
            sleepCalculationStoreRepository.updateIsUserSleeping(true)
            sleepCalculationStoreRepository.updateUserSleepTime(27001)

        }

    }

    var isTimerRunning = false

    fun buttonClick3(view: View) {
        //sch.recalculateUserSleep()
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
                    //mainViewModel.updatePermissionActive(false)
                    // Permission denied on Android platform that supports runtime permissions.
                    //displayPermissionSettingsSnackBar()
                } else {
                    //mainViewModel.updatePermissionActive(true)
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
                    /*
                    val uri = Uri.fromParts(
                            "package",
                            //BuildConfig.APPLICATION_ID,
                            BuildConfig.VERSION_NAME,
                            null
                    )
                    intent.data = uri
                     */
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                .show()
    }
    // endregion

    fun switchToAlarmSettings(view: View) {
        val intent = Intent(this, AlarmSettings::class.java)
        startActivity(intent)
    }
}
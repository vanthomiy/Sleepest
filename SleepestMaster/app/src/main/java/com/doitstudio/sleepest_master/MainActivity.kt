package com.doitstudio.sleepest_master


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.doitstudio.sleepest_master.background.ForegroundService
import com.doitstudio.sleepest_master.databinding.ActivityMainBinding
import com.doitstudio.sleepest_master.model.data.Actions
import com.doitstudio.sleepest_master.sleepcalculation.ml.SleepClassifier
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.io.BufferedReader


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

   /* private val sch: SleepCalculationHandler by lazy {
        SleepCalculationHandler.getHandler(this)
    }*/

    fun buttonClick1(view: View){
        ForegroundService.startOrStopForegroundService(Actions.START, applicationContext)
    }

    var index = 9
    fun buttonClick2(view: View){

        val sleepClassifier = SleepClassifier.getHandler(this)


        var gson = Gson()
        val jsonFile = this
                .assets
                .open("databases/testdata/SleepValues.json")
                .bufferedReader()
                .use(BufferedReader::readText)


        val sleepTimes = gson.fromJson(jsonFile, Array<Array<SleepApiRawDataEntity>>::class.java).asList()

        var sleepData = (sleepTimes[3].filter{it.timestampSeconds < sleepTimes[3][(sleepTimes[3].size / 2).toInt()].timestampSeconds}.toList().reversed())
        var processedSleepData = sleepClassifier.createFeatures(sleepData)
        var sleepState = sleepClassifier.isUserSleeping(processedSleepData)

        var b = sleepState
    }

    var isTimerRunning = false

    fun buttonClick3(view: View) {

        // Testing

        val sleepClassifier = SleepClassifier(this)

        var data4 = intArrayOf(
                1,1,1,
                3,1,1,
                4,1,1,
                5,1,1,
                1,2,1,
                1,1,1,
                1,1,2,
                1,2,1,
                1,1,2,
                1,1,1)

        var result4 = sleepClassifier.isUserSleeping(data4)

        data4 = intArrayOf(
                91,1,1,
                93,1,1,
                91,1,1,
                92,1,1,
                1,2,1,
                1,1,1,
                1,1,2,
                1,2,1,
                1,1,2,
                1,1,1)

        result4 = sleepClassifier.isUserSleeping(data4)

        val a = result4.toString()
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
                } else {
                    mainViewModel.updatePermissionActive(true)
                    // Permission was granted (either by approval or Android version below Q).
                }
            }

    // endregion

}
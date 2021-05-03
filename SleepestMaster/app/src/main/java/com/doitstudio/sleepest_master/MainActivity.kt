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
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.sleepcalculation.ml.SleepClassifier
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataRealEntity
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

        val jsonFileReal = this
                .assets
                .open("databases/testdata/SleepValuesTrue.json")
                .bufferedReader()
                .use(BufferedReader::readText)

        val sleepTimes = gson.fromJson(jsonFile, Array<Array<SleepApiRawDataEntity>>::class.java).asList()
        val sleepTimesReal = gson.fromJson(jsonFileReal, Array<Array<SleepApiRawDataRealEntity>>::class.java).asList()



        for (j in 0 until sleepTimes[5].count()) {
            var done = 0
            var sleeppred = 0
            var awakepred = 0
            var sleepr = 0
            var awaker = 0
            var none = 0

            for (i in 0 until sleepTimes[j].count()) {
                var sleepData = (sleepTimes[j].filter { it.timestampSeconds < sleepTimes[j][i].timestampSeconds }.toList())
                var sleepDataReal = (sleepTimesReal[j].filter { it.timestampSeconds < sleepTimesReal[j][i].timestampSeconds }.toList()).sortedByDescending { it.timestampSeconds }

                var processedSleepData = sleepClassifier.createFeatures(sleepData)
                var sleepState = sleepClassifier.isUserSleeping(processedSleepData)


                var realSleepState = "NONE"
                if (sleepDataReal != null && sleepDataReal.count() > 1) {
                    realSleepState = sleepDataReal.first().real
                }

                if (sleepState == SleepState.NONE) {
                    none += 1
                } else if (sleepState == SleepState.AWAKE) {
                    awakepred += 1
                } else if (sleepState == SleepState.SLEEPING) {
                    sleeppred += 1
                }

                if (realSleepState == "awake" || realSleepState == "NONE") {
                    awaker += 1
                } else {
                    sleepr += 1
                }


            }

            var a = sleepr

        }





    }

    var isTimerRunning = false

    fun buttonClick3(view: View) {

        // Testing

        val sleepClassifier = SleepClassifier(this)

        var data4 = floatArrayOf(
                95f,1f,1f,
                95f,1f,1f,
                95f,1f,1f,
                95f,1f,1f,
                1f,1f,1f,
                1f,1f,1f,
                1f,1f,1f,
                1f,1f,1f,
                1f,1f,1f,
                1f,1f,1f)

        var result4 = sleepClassifier.isUserSleeping(data4)

        data4 = floatArrayOf(
                91f,1f,1f,
                60f,1f,1f,
                70f,1f,1f,
                80f,1f,1f,
                20f,1f,1f,
                30f,1f,1f,
                20f,1f,1f,
                30f,1f,1f,
                20f,1f,1f,
                60f,1f,1f)


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
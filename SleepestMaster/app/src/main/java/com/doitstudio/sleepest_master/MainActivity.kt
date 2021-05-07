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
import com.doitstudio.sleepest_master.model.data.ModelProcess
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationDbRepository
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationStoreRepository
import com.doitstudio.sleepest_master.sleepcalculation.ml.SleepClassifier
import com.doitstudio.sleepest_master.storage.DbRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataRealEntity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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


        val sleepCalculationHandler = SleepCalculationHandler.getHandler(this)

        var oawakepred  = 0
        var osleeppred = 0// += sleeppred
        var oawaker  = 0//+= awaker
        var osleepr  = 0//+= sleepr

        var olight = 0
        var odepp = 0
        var onone = 0

        for (j in 0 until sleepTimes.count()) {

            var light = 0
            var depp = 0
            var none = 0

            var awakepred  = 0
            var sleeppred = 0// += sleeppred
            var awaker  = 0//+= awaker
            var sleepr  = 0//+= sleepr



            for (i in 0 until sleepTimes[j].count()) {
                var sleepData = (sleepTimes[j].filter { it.timestampSeconds <= sleepTimes[j][i].timestampSeconds }.toList())
                var sleepDataReal = (sleepTimesReal[j].filter { it.timestampSeconds <= sleepTimesReal[j][i].timestampSeconds }.toList()).sortedByDescending { it.timestampSeconds }

                val sleepState = sleepCalculationHandler.checkIsUserSleepingTest(sleepData)

                sleepTimes[j][i].sleepState = sleepState

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

            oawakepred += awakepred
            osleeppred += sleeppred
            oawaker+= awaker
            osleepr+= sleepr

            /*
            for (i in 0 until sleepTimes[j].count()) {

                if(sleepTimes[j][i].sleepState != SleepState.SLEEPING)
                {continue}

                val sleepState = sleepCalculationHandler.defineSleepStates(sleepTimes[j][i].timestampSeconds, sleepTimes[j].sortedByDescending { x->x.timestampSeconds }.toList())

                if (sleepState == SleepState.LIGHT) {
                    light += 1
                } else if (sleepState == SleepState.DEEP) {
                    depp += 1
                } else
                {
                    none += 1
                }

            }

            olight +=light
            odepp += depp
            onone += none
            */


            val sleepTime = sleepCalculationHandler.defineUserWakeupTest(sleepTimes[j].toList())
            print(sleepTime)

        }

        var a = onone

    }

    var isTimerRunning = false

    var context = this

    private val sleepDbRepository: SleepCalculationDbRepository by lazy {
        (context.applicationContext as MainApplication).sleepCalculationDbRepository
    }

    private val normalDbRepository: DbRepository by lazy {
        (context.applicationContext as MainApplication).dbRepository
    }

    private val sleepCalculationRepository: SleepCalculationStoreRepository by lazy {
        (context.applicationContext as MainApplication).sleepCalculationRepository
    }


    fun buttonClick3(view: View) = runBlocking {

        // test des kompletten ablaufs !

        // sleep api data löschen

        // als erstes muss ein schlaf geholt werden.
        // dieser wird nacheinander in sleep api data ein gespeichert.
        // inzwischen wird checkIsUserSleeping aufgerufen und berechnet

        // komplett durchlaufen // bzw bis 3/4 durch sind!!

        // nach 3/4 wird es erweitert

        // durchlaufen, einspeicher, checkIsUserSleeping, und dann defineUserWakeup


        // region load data
        var gson = Gson()
        val jsonFile = context
                .assets
                .open("databases/testdata/SleepValues.json")
                .bufferedReader()
                .use(BufferedReader::readText)

        val jsonFileReal = context
                .assets
                .open("databases/testdata/SleepValuesTrue.json")
                .bufferedReader()
                .use(BufferedReader::readText)

        val sleepTimes = gson.fromJson(jsonFile, Array<Array<SleepApiRawDataEntity>>::class.java).asList()
        val sleepTimesReal = gson.fromJson(jsonFileReal, Array<Array<SleepApiRawDataRealEntity>>::class.java).asList()


        val sleepCalculationHandler = SleepCalculationHandler.getHandler(context)

        // endregion

        // delete sleep api data
        mainViewModel.deleteApi()


        val index = 10
        // die ersten 2/3
        for (i in 0 until sleepTimes[index].count()) {

            // insert data
            val data = sleepTimes[index][i]
            data.sleepState = SleepState.NONE
            val sleepApiRawDataEntityList = listOf<SleepApiRawDataEntity>(data)
            sleepDbRepository.insertSleepApiRawData(sleepApiRawDataEntityList)

            // call handler
            sleepCalculationHandler.checkIsUserSleeping(data.timestampSeconds-86400)



            val livesess = sleepCalculationRepository.liveUserSleepActivityFlow.first()
            val a = livesess

            // wenn 2/3 erreicht
            if (i > sleepTimes[index].count() * (2f / 3f)) {

                val ab = livesess

                sleepCalculationHandler.defineUserWakeup(data.timestampSeconds-86400)

                var session = normalDbRepository.allUserSleepSessions.first().first()
                val b = session

            }


        }
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
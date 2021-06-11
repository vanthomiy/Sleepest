/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.example.sleepsamplekotlin

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewbinding.BuildConfig
import com.android.example.sleepsamplekotlin.databinding.ActivityMainBinding
import com.android.example.sleepsamplekotlin.receiver.SleepReceiver
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.SleepSegmentRequest
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Demos Android's Sleep APIs; subscribe/unsubscribe to sleep data, save that data, and display it.
 */
class MainActivity : AppCompatActivity() {

    private var PRIVATE_MODE = 0
    private val PREF_NAME = "phone_Placement"

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by lazy {
        MainViewModel((application as MainApplication).repository)
    }

    // Used to construct the output from multiple tables (very basic implementation just to show
    // the live data coming in).

    private var sleepOutClassifyOutput: String = ""
    private var sleepOutClassifyOutputExport: String = ""

    // Status of subscription to sleep data. This is stored in [SleepSubscriptionStatus] which saves
    // the data in a [DataStore] in case the user navigates away from the app.
    private var subscribedToSleepData = false
        set(newSubscribedToSleepData) {
            field = newSubscribedToSleepData
            if (newSubscribedToSleepData) {
                binding.buttonSleep.text = getString(R.string.sleep_button_unsubscribe_text)
            } else {
                binding.buttonSleep.text = getString(R.string.sleep_button_subscribe_text)
            }
            updateOutput()
        }
    private var phoneOnBed = false

    private lateinit var sleepPendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel.subscribedToSleepDataLiveData.observe(this) { newSubscribedToSleepData ->
            if (subscribedToSleepData != newSubscribedToSleepData) {
                subscribedToSleepData = newSubscribedToSleepData
            }
        }

        mainViewModel.allSleepClassifyEventEntities.observe(this) {
                sleepClassifyEventEntities ->
            Log.d(TAG, "sleepClassifyEventEntities: $sleepClassifyEventEntities")

            if (sleepClassifyEventEntities.isNotEmpty())
            {
                sleepOutClassifyOutput = "We found ${sleepClassifyEventEntities.size} items  \n"

                sleepClassifyEventEntities.forEach {

                    val time = it.timestampSeconds.toLong() * 1000; // wokraround to change format

                    val instantNow = Instant.now()
                    val date = millisToDateTime(time)
                    sleepOutClassifyOutputExport += "${date.toLocalDate()};${date.hour}:${date.minute};${it.confidence};${(it.light)};${(it.motion)};0\n"
                    // Just display values that are shorter than 24Hours away
                    if (instantNow.minusSeconds(86400).epochSecond > it.timestampSeconds)
                        return@forEach

                    sleepOutClassifyOutput += "Status: ${it.confidence} " +
                            "Light ${(it.light)} " +
                            "Motion ${(it.motion)} \n" +
                            "time ${millisToStringDateTime(time)}\n"
                }

                updateOutput()
            }
        }

        sleepPendingIntent =
            SleepReceiver.createSleepReceiverPendingIntent(context = applicationContext)

        requestData()

    }

    fun onClickRequestSleepData(view: View) {
        requestData()
    }

    private fun requestData(){
        if (activityRecognitionPermissionApproved()) {
            if (subscribedToSleepData) {
                unsubscribeToSleepSegmentUpdates(applicationContext, sleepPendingIntent)
            } else {
                subscribeToSleepSegmentUpdates(applicationContext, sleepPendingIntent)
            }
        } else {
            requestPermissionLauncher.launch(permission.ACTIVITY_RECOGNITION)
        }
    }

    fun onClickExportSleepData(view: View) {


        var switchExportFile =  "Datum;Uhrzeit;Schlaf;Licht;Bewegung;Wahre Zeiten"

        val splitOut  = sleepOutClassifyOutputExport.split("\n")
        //val splitBed  = sleepBedClassifyOutputExport.split("\n")

        /*splitBed.reversed().forEach {
            switchExportFile += "${it}\n";
        }*/

        switchExportFile +=  "\n\n\nOut\nDatum;Uhrzeit;Schlaf;Licht;Bewegung;Wahre Zeiten"

        splitOut.reversed().forEach {
            switchExportFile += "${it}\n";
        }

        /*val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, file)
            type = "text/plain"
        }*/

        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, switchExportFile)
            type = "text/csv"
        }

        startActivity(Intent.createChooser(shareIntent, "Export data"))

        //CreateFile()
        //saveTextFile()
    }


    private fun CreateFile(){
        val HEADER = "Datum;Uhrzeit;Schlaf;Licht;Bewegung;Wahre Zeiten"

        var filename = "export.csv"

        var path = getExternalFilesDir(null)   //get file directory for this package
        //(Android/data/.../files | ... is your app package)

//create fileOut object
        var fileOut = File(path, filename)

//delete any file object with path and filename that already exists
        fileOut.delete()

//create a new file
        fileOut.createNewFile()

        //append the header and a newline
        fileOut.appendText(HEADER)


        val splitOut  = sleepOutClassifyOutputExport.split("\n")

        splitOut.reversed().forEach {
            fileOut.appendText("${it}\n")
        }


/*
        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileOut))
        sendIntent.type = "text/csv"
        startActivity(Intent.createChooser(sendIntent, "SHARE"))
*/

        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileOut))
            type = "text/csv"
        }

        startActivity(Intent.createChooser(shareIntent, "Export data"))

    }

    fun saveTextFile() {

        var text = "Datum;Uhrzeit;Schlaf;Licht;Bewegung;Wahre Zeiten\n"

        val splitOut  = sleepOutClassifyOutputExport.split("\n")

        splitOut.reversed().forEach {
            text += ("${it}\n")
        }




        try {

            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, text)
                type = "text/csv"
            }



            startActivity(Intent.createChooser(shareIntent, "Export data"))

            //display file saved message
            Toast.makeText(baseContext, "File saved successfully!", Toast.LENGTH_SHORT).show()
        }
        catch (e: Exception) {
            e.printStackTrace()
        }


    }


    fun onClickDeleteOldData(view: View){
        mainViewModel.deleteAllSleepData()
    }

    // Permission is checked before this method is called.
    @SuppressLint("MissingPermission")
    private fun subscribeToSleepSegmentUpdates(context: Context, pendingIntent: PendingIntent) {
        Log.d(TAG, "requestSleepSegmentUpdates()")
        val task = ActivityRecognition.getClient(context).requestSleepSegmentUpdates(
            pendingIntent,
            // Registers for both [SleepSegmentEvent] and [SleepClassifyEvent] data.
            SleepSegmentRequest.getDefaultSleepSegmentRequest()
        )

        task.addOnSuccessListener {
            mainViewModel.updateSubscribedToSleepData(true)
            Log.d(TAG, "Successfully subscribed to sleep data.")
        }
        task.addOnFailureListener { exception ->
            Log.d(TAG, "Exception when subscribing to sleep data: $exception")
        }
    }

    private fun unsubscribeToSleepSegmentUpdates(context: Context, pendingIntent: PendingIntent) {
        Log.d(TAG, "unsubscribeToSleepSegmentUpdates()")
        val task = ActivityRecognition.getClient(context).removeSleepSegmentUpdates(pendingIntent)

        task.addOnSuccessListener {
            mainViewModel.updateSubscribedToSleepData(false)
            Log.d(TAG, "Successfully unsubscribed to sleep data.")
            // request data again
            requestData()
        }
        task.addOnFailureListener { exception ->
            Log.d(TAG, "Exception when unsubscribing to sleep data: $exception")
        }
    }

    private fun activityRecognitionPermissionApproved(): Boolean {
        // Because this app targets 29 and above (recommendation for using the Sleep APIs), we
        // don't need to check if this is on a device before runtime permissions, that is, a device
        // prior to 29 / Q.
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
            this,
            permission.ACTIVITY_RECOGNITION
        )
    }

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                // Permission denied on Android platform that supports runtime permissions.
                displayPermissionSettingsSnackBar()
            } else {
                // Permission was granted (either by approval or Android version below Q).
                binding.outputTextView.text = getString(R.string.permission_approved)
            }
        }

    private fun displayPermissionSettingsSnackBar() {
        Snackbar.make(
            binding.mainActivity,
            R.string.permission_rational,
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.action_settings) {
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

     /**
     * Redimentary implementation of the output from multiple tables. The [LiveData] observers just
     * save their data to one of the strings (segmentOutput or classifyOutput) and triggers this
     * function.
      **/
    private fun updateOutput() {
        Log.d(TAG, "updateOutput()")

        val header = if (subscribedToSleepData) {
            val timestamp = Calendar.getInstance().time.toString()
            //val dateTime = LocalDateTime.now()

            getString(R.string.main_output_header1_subscribed_sleep_data, timestamp)
        } else {
            getString(R.string.main_output_header1_unsubscribed_sleep_data)
        }

        val sleepData = getString(
            R.string.main_output_header2_and_sleep_data,
            sleepOutClassifyOutput,
                ""
        )

        val newOutput = header + sleepData
        binding.outputTextView.text = newOutput
    }
    
    companion object {
        private const val TAG = "MainActivity"
    }

    private fun millisToStringDateTime(millis: Long) : String {
        // define once somewhere in order to reuse it
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // JVM representation of a millisecond epoch absolute instant
        val instant = Instant.ofEpochMilli(millis)

        // Adding the timezone information to be able to format it (change accordingly)
        val date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())//.plusHours(1) //reudiger workaround haha
       return formatter.format(date) // 10/12/2019 06:35:45
    }

    private fun millisToDateTime(millis: Long) : LocalDateTime {
        // define once somewhere in order to reuse it
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // JVM representation of a millisecond epoch absolute instant
        val instant = Instant.ofEpochMilli(millis)

        // Adding the timezone information to be able to format it (change accordingly)
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())//.plusHours(1) //reudiger workaround haha
    }

}

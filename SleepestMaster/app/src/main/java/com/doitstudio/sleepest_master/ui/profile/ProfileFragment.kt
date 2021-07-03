package com.doitstudio.sleepest_master.ui.profile


import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context

import android.content.Context.NOTIFICATION_SERVICE
import android.service.notification.StatusBarNotification

import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.Settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.annotation.RequiresApi

import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.alarmclock.AlarmClockReceiver
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler

import androidx.activity.result.contract.ActivityResultContracts
import com.doitstudio.sleepest_master.databinding.FragmentProfileBinding
import com.doitstudio.sleepest_master.model.data.export.UserSleepExportData
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity
import com.doitstudio.sleepest_master.ui.sleep.SleepFragment
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.*
import java.util.*


class ProfileFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(this).get(ProfileViewModel::class.java) }
    private lateinit var binding: FragmentProfileBinding
    private val actualContext: Context by lazy { requireActivity().applicationContext }
    private val scope: CoroutineScope = MainScope()
    private val dataBaseRepository: DatabaseRepository by lazy {
        (actualContext as MainApplication).dataBaseRepository
    }

    var caseOfEntrie = 0


    companion object {
        fun newInstance() = SleepFragment()
    }


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        binding = FragmentProfileBinding.inflate(inflater, container, false)
        viewModel.transitionsContainer = (binding.linearAnimationlayout)
        viewModel.animatedTopView = binding.animatedTopView
        binding.profileViewModel = viewModel

        binding.sleepActivityPermission.setOnClickListener {
            onPermissionClicked(it)
        }
        binding.dailyActivityPermission.setOnClickListener {
            onPermissionClicked(it)
        }
        binding.storagePermission.setOnClickListener {
            onPermissionClicked(it)
        }
        binding.overlayPermission.setOnClickListener {
            onPermissionClicked(it)
        }
        binding.importButton.setOnClickListener {
            onDataClicked(it)
        }
        binding.exportButton.setOnClickListener {
            onDataClicked(it)
        }

        viewModel.designExpand.set(if(caseOfEntrie == 1) View.VISIBLE else View.GONE)
        viewModel.dataExpand.set(if(caseOfEntrie == 2) View.VISIBLE else View.GONE)


        //region Test

        var pref = actualContext.getSharedPreferences("AlarmChanged", 0)
        val textAlarm = """
            Last Alarm changed: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)},${pref.getInt(
            "actualWakeup",
            0
        )},${pref.getInt("alarmUse", 0)}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("StartService", 0)
        val textStartService = """
            Last service start: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("StopService", 0)
        val textStopService = """
            Last service stop: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("Workmanager", 0)
        val textLastWorkmanager = """
            Last workmanager call: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("WorkmanagerCalculation", 0)
        val textLastWorkmanagerCalculation = """
            Last workmanagerCalc call: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("AlarmClock", 0)
        val textCalc1 = """
            Alarmclock: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("AlarmSet", 0)
        val textCalc2 = """
            AlarmSet: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)},${pref.getInt(
                "hour1",
                0
        )}:${pref.getInt("minute1", 0)},${
            pref.getInt("actualWakeup", 0)}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("AlarmReceiver", 0)
        val textAlarmReceiver = """
            AlarmReceiver: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)},${pref.getString(

                "intent",
                "XX"
        )}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("SleepTime", 0)
        val textSleepTime= """
            SleepTime: ${pref.getInt("sleeptime", 0)}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("StopException", 0)
        val textStopException = """
            Exc.: ${pref.getString("exception", "XX")}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("AlarmReceiver1", 0)
        val textAlarmReceiver1 = """
            AlarmReceiver1: ${pref.getString("usage", "XX")},${pref.getInt("day", 0)},${pref.getInt(
                "hour",
                0
        )},${pref.getInt("minute", 0)}
            
            """.trimIndent()

        var textGesamt = textAlarm + textStartService + textStopService + textLastWorkmanager + textLastWorkmanagerCalculation + textCalc1 + textCalc2 + textAlarmReceiver + textSleepTime + textStopException + textAlarmReceiver1


        binding.testText.text = textGesamt


        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }



    fun onDataClicked(view: View) {
        when (view.tag.toString()) {
            "export" -> {

                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/*"
                    putExtra(Intent.EXTRA_TITLE, "Schlafdaten.json")
                }

                startActivityForResult(intent, 1010)

            }
            "import" -> {

                // Choose a directory using the system's file picker.
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                    // Optionally, specify a URI for the directory that should be opened in
                    // the system file picker when it loads.
                }


                startActivityForResult(intent, 1012)
            }
        }
    }

    fun onPermissionClicked(view: View) {
        when (view.tag.toString()) {
            "dailyActivity" -> if (viewModel.dailyPermission.get() != true) requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION) else viewModel.showPermissionInfo("dailyActivity")
            "sleepActivity" -> if (viewModel.activityPermission.get() != true) requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION) else viewModel.showPermissionInfo("sleepActivity")
            "storage" -> if (viewModel.storagePermission.get() != true) requestPermissionLauncher.launch(Manifest.permission.ANSWER_PHONE_CALLS) else viewModel.showPermissionInfo("storage")
            "overlay" -> if (viewModel.overlayPermission.get() != true) {
                // If not, form up an Intent to launch the permission request
                val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse(
                        "package:${actualContext.packageName}"
                )
                )

                // Launch Intent, with the supplied request code
                startActivityForResult(intent, 1234)

            } else viewModel.showPermissionInfo("overlay")
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        // Check if a request code is received that matches that which we provided for the overlay draw request
        if (requestCode == 1234) {
            viewModel.checkPermissions()
        }
        else if (requestCode == 1012) {

            val uri = data?.data

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
                type = "text/*"
            }

            startActivityForResult(intent, 1011)
        }
        else if (requestCode == 1011) {

            val uri = data?.data

            uri?.let {
                val importJson = readTextFromUri(it)

                var data = mutableListOf<UserSleepExportData>()
                try {
                    var gson = Gson()

                    data.addAll(gson.fromJson(importJson, Array<UserSleepExportData>::class.java).asList())
                } catch (ex: Exception) {
                    Toast.makeText(actualContext, "Wrong data format", Toast.LENGTH_SHORT).show()
                    return@let
                }

                try {

                    var sessions = mutableListOf<UserSleepSessionEntity>()
                    var sleepApiRawDataEntity = mutableListOf<SleepApiRawDataEntity>()

                    data.forEach { session ->

                        sessions.add(UserSleepSessionEntity(
                                session.id,
                                session.mobilePosition,
                                session.sleepTimes,
                                session.userSleepRating,
                                session.userCalculationRating
                        ))

                        sleepApiRawDataEntity.addAll(session.sleepApiRawData)
                    }

                    scope.launch {
                        dataBaseRepository.insertSleepApiRawData(sleepApiRawDataEntity)
                        dataBaseRepository.insertUserSleepSessions(sessions)
                    }
                } catch (ex: Exception) {
                    Toast.makeText(actualContext, "Cant write to database", Toast.LENGTH_SHORT).show()
                    return@let
                } finally {
                    Toast.makeText(actualContext, "Successful imported data", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else if (requestCode == 1010) {

            try {
                scope.launch {
                    var gson = Gson()

                    val userSessions = dataBaseRepository.allUserSleepSessions.first()

                    val userExporSessions = mutableListOf<UserSleepExportData>()

                    userSessions.forEach { session ->

                        val sessionSleepData = dataBaseRepository.getSleepApiRawDataBetweenTimestamps(session.sleepTimes.sleepTimeStart, session.sleepTimes.sleepTimeEnd).first()

                        val userExporSession = UserSleepExportData(
                                session.id,
                                session.mobilePosition,
                                session.sleepTimes,
                                session.userSleepRating,
                                session.userCalculationRating,
                                sessionSleepData
                        )

                        userExporSessions.add(userExporSession)
                    }

                    val exportFile = gson.toJson(userExporSessions)

                    data?.data?.let { writeTextToUri(it, exportFile) }
                }

            } catch (e: IOException) {
                Toast.makeText(actualContext, "Export failed", Toast.LENGTH_SHORT).show()
            }
        }
    }


    val contentResolver by lazy {actualContext.contentResolver}

    private fun readTextFromUri(uri: Uri): String {
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }

    private fun writeTextToUri(uri: Uri, text:String) {
        try {
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use {
                    it.write(
                            text
                                    .toByteArray()
                    )
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        Toast.makeText(actualContext, "Export successfully", Toast.LENGTH_SHORT).show()

    }

    private val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    viewModel.checkPermissions()
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                    viewModel.checkPermissions()
                }
            }

}



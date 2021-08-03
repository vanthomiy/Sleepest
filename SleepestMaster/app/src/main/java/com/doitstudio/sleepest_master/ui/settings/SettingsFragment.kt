package com.doitstudio.sleepest_master.ui.settings


import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.doitstudio.sleepest_master.DontKillMyAppFragment
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.databinding.FragmentSettingsBinding
import com.doitstudio.sleepest_master.googleapi.SleepHandler
import com.doitstudio.sleepest_master.model.data.Constants
import com.doitstudio.sleepest_master.model.data.export.ImportUtil
import com.doitstudio.sleepest_master.model.data.export.UserSleepExportData
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.*
import java.util.*


class SettingsFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(this).get(SettingsViewModel::class.java) }
    private lateinit var binding: FragmentSettingsBinding
    private val actualContext: Context by lazy { requireActivity().applicationContext }
    private val scope: CoroutineScope = MainScope()
    private val dataBaseRepository: DatabaseRepository by lazy {
        (actualContext as MainApplication).dataBaseRepository
    }
    private val sleepHandler : SleepHandler by lazy {
        SleepHandler.getHandler(actualContext)
    }

    private var caseOfEntrie = -1

    fun setCaseOfEntrie(case: Int){
        caseOfEntrie = case
        if(this::binding.isInitialized)
            viewModel.actualExpand.set(caseOfEntrie)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSettingsBinding.inflate(inflater, container, false)
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
        binding.btnTutorial.setOnClickListener() {
            //val calendar = TimeConverterUtil.getAlarmDate(LocalTime.now().toSecondOfDay() + 60)
            //AlarmClockReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), context, AlarmClockReceiverUsage.START_ALARMCLOCK)
            //val notificationsUtil = NotificationUtil(context, NotificationUsage.NOTIFICATION_NO_API_DATA,null)
            //notificationsUtil.chooseNotification()
            sleepHandler.stopSleepHandler()
        }
        binding.btnImportantSettings.setOnClickListener() {
            DontKillMyAppFragment.show(requireActivity())
        }

        viewModel.actualExpand.set(caseOfEntrie)
        var version : String = "XX"
        try {
            val packageInfo = actualContext.packageManager.getPackageInfo(actualContext.packageName, 0)
            version = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }


        //region Test
        val textVersion = "Version: $version\n"
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
            Last workmanager call: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)},${pref.getLong("diff", 0)}
            
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
        pref = actualContext.getSharedPreferences("BootTime1", 0)
        val textBooReceiver1= """
            Last Boot: ${pref.getInt("hour", 0)},${pref.getInt("minute", 0)},${pref.getInt("usage", 0)}
            
            """.trimIndent()

        var textGesamt = textVersion + textAlarm + textStartService + textStopService + textLastWorkmanager + textLastWorkmanagerCalculation + textCalc1 + textCalc2 + textAlarmReceiver + textSleepTime + textBooReceiver1 + textStopException + textAlarmReceiver1


        binding.testText.text = textGesamt


        return binding.root

    }



    fun onDataClicked(view: View) {
        when (view.tag.toString()) {
            "export" -> {

                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    putExtra(Intent.EXTRA_TITLE, "Schlafdaten.json")
                }

                startActivityForResult(intent, Constants.EXPORT_REQUEST_CODE)


            }
            "import" -> {

                // Choose a directory using the system's file picker.
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                    // Optionally, specify a URI for the directory that should be opened in
                    // the system file picker when it loads.
                }


                startActivityForResult(intent, Constants.IMPORT_REQUEST_CODE)
            }
        }
    }

    fun onPermissionClicked(view: View) {
        when (view.tag.toString()) {
            "dailyActivity" -> if (viewModel.dailyPermission.get() != true) requestPermissionLauncher.launch(
                Manifest.permission.ACTIVITY_RECOGNITION
            ) else viewModel.showPermissionInfo("dailyActivity")
            "sleepActivity" -> if (viewModel.activityPermission.get() != true) requestPermissionLauncher.launch(
                Manifest.permission.ACTIVITY_RECOGNITION
            ) else viewModel.showPermissionInfo("sleepActivity")
            "storage" -> if (viewModel.storagePermission.get() != true) requestPermissionLauncher.launch(
                Manifest.permission.ANSWER_PHONE_CALLS
            ) else viewModel.showPermissionInfo("storage")
            "overlay" -> if (viewModel.overlayPermission.get() != true) {
                // If not, form up an Intent to launch the permission request
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse(
                        "package:${actualContext.packageName}"
                    )
                )

                // Launch Intent, with the supplied request code
                startActivityForResult(intent, Constants.ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE)

            } else viewModel.showPermissionInfo("overlay")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        // Check if a request code is received that matches that which we provided for the overlay draw request
        if (requestCode == Constants.ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE) {
            viewModel.checkPermissions()
        }
        else if (requestCode == Constants.IMPORT_REQUEST_CODE) {

            val uri = data?.data

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
                type = "application/json"
            }
            startActivityForResult(intent, Constants.LOAD_FILE_REQUEST_CODE)
        }
        else if (requestCode == Constants.LOAD_FILE_REQUEST_CODE) {
            scope.launch {
                ImportUtil.getLoadFileFromUri(data?.data, actualContext, dataBaseRepository)
            }
        }
        else if (requestCode == Constants.EXPORT_REQUEST_CODE) {

            try {
                scope.launch {
                    var gson = Gson()

                    val userSessions = dataBaseRepository.allUserSleepSessions.first()

                    val userExporSessions = mutableListOf<UserSleepExportData>()

                    userSessions.forEach { session ->

                        val sessionSleepData = dataBaseRepository.getSleepApiRawDataBetweenTimestamps(
                            session.sleepTimes.sleepTimeStart,
                            session.sleepTimes.sleepTimeEnd
                        ).first()

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


    private fun writeTextToUri(uri: Uri, text: String) {
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

        val intentShareFile = Intent(Intent.ACTION_SEND)

        intentShareFile.type = "application/json"
        intentShareFile.putExtra(Intent.EXTRA_STREAM, uri)
        intentShareFile.putExtra(
            Intent.EXTRA_SUBJECT,
            "Sharing File..."
        )
        intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...")
        startActivity(Intent.createChooser(intentShareFile, "Share File"))
    }

    private val contentResolver: ContentResolver by lazy { actualContext.contentResolver}


    private val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
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



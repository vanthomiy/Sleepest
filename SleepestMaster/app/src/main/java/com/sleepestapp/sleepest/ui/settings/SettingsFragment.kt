package com.sleepestapp.sleepest.ui.settings


import android.Manifest
import android.content.ActivityNotFoundException
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
import android.view.View.TEXT_ALIGNMENT_CENTER
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.sleepestapp.sleepest.DontKillMyAppFragment
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.databinding.FragmentSettingsBinding
import com.sleepestapp.sleepest.model.data.*
import com.sleepestapp.sleepest.model.data.credits.CreditsSites
import com.sleepestapp.sleepest.model.data.export.ImportUtil
import com.sleepestapp.sleepest.model.data.export.UserSleepExportData
import com.sleepestapp.sleepest.onboarding.OnboardingActivity
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.ui.sleep.SleepViewModel
import com.sleepestapp.sleepest.util.IconAnimatorUtil.isDarkThemeOn
import com.sleepestapp.sleepest.util.PermissionsUtil
import com.sleepestapp.sleepest.util.SmileySelectorUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.*
import java.util.*


class SettingsFragment : Fragment() {

    var factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return  SettingsViewModel(
                (actualContext as MainApplication).dataStoreRepository,
                (actualContext as MainApplication).dataBaseRepository
            ) as T
        }
    }

    /**
     * View model of the [SettingsFragment]
     */
    private val viewModel by lazy { ViewModelProvider(this, factory).get(SettingsViewModel::class.java) }
    /**
     * Binding XML Code to Fragment
     */
    private lateinit var binding: FragmentSettingsBinding
    /**
     * Get actual context
     */
    private val actualContext: Context by lazy { requireActivity().applicationContext }

    /**
     * actual case of entrie
     * e.g. after switching dark mode the case of entrie changes
     */
    private var caseOfEntrie = -1

    /**
     * Open the selected information view if [caseOfEntrie] is not -1
     */
    fun setCaseOfEntrie(case: Int){
        caseOfEntrie = case
        if(this::binding.isInitialized)
            viewModel.actualExpand.value = (caseOfEntrie)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        viewModel.transitionsContainer = (binding.linearAnimationlayout)
        binding.profileViewModel = viewModel
        binding.lifecycleOwner = this;

        //region On Click Listeners

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
            lifecycleScope.launch {
                val intent = Intent(activity, OnboardingActivity::class.java)
                intent.putExtra(getString(R.string.onboarding_intent_not_first_app_start), true)
                intent.putExtra(getString(R.string.onboarding_intent_starttime), viewModel.dataStoreRepository.getSleepTimeBegin())
                intent.putExtra(getString(R.string.onboarding_intent_endtime), viewModel.dataStoreRepository.getSleepTimeEnd())
                intent.putExtra(getString(R.string.onboarding_intent_duration), viewModel.dataStoreRepository.getSleepDuration())

                startActivity(intent)
            }

        }
        binding.btnImportantSettings.setOnClickListener() {
            DontKillMyAppFragment.show(requireActivity())
        }

        viewModel.aboutUsSelection.observe(this){
            onAboutUsClicked(it)
        }

        //endregion

        viewModel.actualExpand.value = (caseOfEntrie)


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
            Last workmanager call: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)},${pref.getInt("day", 0)}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("WorkmanagerCalculation", 0)
        val textLastWorkmanagerCalculation = """
            Last workmanagerCalc call: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)},${pref.getInt("day", 0)}
            
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
        pref = actualContext.getSharedPreferences("State", 0)
        val textActualState= """
            Actual State: ${pref.getString("state", "XX")}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("ForegroundServiceTime", 0)
        val textForegroundServiceTime= """
            Foreground Service Time: ${pref.getInt("time", 0)}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("SleepValue", 0)
        val textSleepValue= """
            Sleep value amount: ${pref.getInt("value", 0)},${pref.getBoolean("status",false)}
            
            """.trimIndent()

        var textGesamt = textVersion + textAlarm + textStartService + textStopService + textLastWorkmanager + textLastWorkmanagerCalculation + textCalc1 + textCalc2 + textAlarmReceiver + textSleepTime + textBooReceiver1 + textStopException + textAlarmReceiver1 + textActualState + textForegroundServiceTime + textSleepValue


        binding.testText.text = textGesamt


        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // check dark mode settings for lottie animation
        lifecycleScope.launch {

            val settings = viewModel.dataStoreRepository.settingsDataFlow.first()


            if ((settings.designAutoDarkMode  && actualContext.isDarkThemeOn()) || !settings.designAutoDarkMode && settings.designDarkMode)
                binding.lottieDarkMode.setMinAndMaxFrame(0, 240) //to play the first half
            else
                binding.lottieDarkMode.setMinAndMaxFrame(240, 481) //to play the second half

            binding.lottieDarkMode.playAnimation()

        }

        viewModel.removeTextNormal = actualContext.getString(R.string.settings_delete_all_data)
        viewModel.removeTextSpecific = actualContext.getString(R.string.settings_return)
        viewModel.removeButtonText.value = viewModel.removeTextNormal

        checkPermissions()
        createCredits()
    }

    /**
     * create the credits for the credtis view
     */
    private fun createCredits(){
        val creditsSites = CreditsSites.createCreditSites()

        creditsSites.forEach{ site ->

            if(site.site != Websites.PRIVACY_POLICE) {

                var creditsText = ""

                site.authors.forEach { author ->
                    creditsText += "\n      " + SmileySelectorUtil.getSmileyIteration() + " " + author.author + " " +
                            actualContext.getString(R.string.settings_from) + " " +
                            Info.getName(author.usage, actualContext)
                }


                // creating the button
                val button = Button(actualContext)
                // setting layout_width and layout_height using layout parameters
                button.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    resources.getDimension(R.dimen.valuesHeight).toInt()
                )
                var marginParams = button.layoutParams as ViewGroup.MarginLayoutParams
                marginParams.setMargins(10, 50, 10, 5)
                button.setTextColor(resources.getColor(R.color.accent_text_color))
                button.setBackgroundDrawable(resources.getDrawable(R.drawable.transparentrounded))
                button.textAlignment = TEXT_ALIGNMENT_CENTER
                button.text = site.name
                button.tag = site.site
                button.textSize = 14f
                button.setOnClickListener { onWebsiteClicked(it) }
                // add Button to LinearLayout
                binding.llCredits.addView(button)

                // creating the text
                val textView = TextView(actualContext)
                // setting layout_width and layout_height using layout parameters
                textView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                textView.setPadding(40, 5, 0, 30)
                textView.textSize = 16f
                textView.setTextColor(resources.getColor(R.color.primary_text_color))
                textView.text = creditsText
                // add text to LinearLayout
                binding.llCredits.addView(textView)
            }
        }
    }

    /**
     * A website url was clicked
     * Navigate to it with default browser
     */
    private fun onWebsiteClicked(view: View) {
        val websiteUrl = Websites.getWebsite(view.tag as Websites)

        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl)))
    }

    /**
     * About us clicked
     * Improvement, Rate, Error or Police
     */
    fun onAboutUsClicked(tag:String) {
        when (tag) {
            "improvement" -> {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto: sleepestapp@gmail.com")
                val packageInfo = actualContext.packageManager.getPackageInfo(actualContext.packageName, 0)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_EMAIL, "Improvement")
                intent.putExtra(Intent.EXTRA_SUBJECT, "Version: " + packageInfo.versionName)

                actualContext.startActivity(intent)

            }
            "rate" -> {
                val uri = Uri.parse("market://details?id=" + actualContext.packageName)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                try {
                    actualContext.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(actualContext, " unable to find market app", Toast.LENGTH_LONG).show()
                }
            }
            "error" -> {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto: sleepestapp@gmail.com")
                val packageInfo = actualContext.packageManager.getPackageInfo(actualContext.packageName, 0)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_EMAIL, "Error")
                intent.putExtra(Intent.EXTRA_SUBJECT, "Version: " + packageInfo.versionName)

                actualContext.startActivity(intent)

            }
            "PRIVACY_POLICE" -> {
                val websiteUrl = Websites.getWebsite(Websites.getWebsiteByString(tag))

                actualContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl)))
            }
        }
    }


    /**
     * Data export or import was clicked
     */
    private fun onDataClicked(view: View) {
        when (view.tag.toString()) {
            "export" -> {

                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    putExtra(Intent.EXTRA_TITLE, getString(R.string.settings_export_file_name))
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

    /**
     * Request permissions clicked
     */
    private fun onPermissionClicked(view: View) {
        when (view.tag.toString()) {
            "dailyActivity" -> if (viewModel.dailyPermission.value != true) requestPermissionLauncher.launch(
                Manifest.permission.ACTIVITY_RECOGNITION
            ) else viewModel.showPermissionInfo("dailyActivity")
            "sleepActivity" -> if (viewModel.activityPermission.value != true) requestPermissionLauncher.launch(
                Manifest.permission.ACTIVITY_RECOGNITION
            ) else viewModel.showPermissionInfo("sleepActivity")
            "storage" -> if (viewModel.storagePermission.value != true) requestPermissionLauncher.launch(
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ) else viewModel.showPermissionInfo("storage")
            "overlay" -> if (viewModel.overlayPermission.value != true) {
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

    /**
     * Load/Save file result handler and also share intent
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        // Check if a request code is received that matches that which we provided for the overlay draw request
        if (requestCode == Constants.ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE) {
            checkPermissions()
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
            lifecycleScope.launch {
                ImportUtil.getLoadFileFromUri(data?.data, actualContext, viewModel.dataBaseRepository)
            }
        }
        else if (requestCode == Constants.EXPORT_REQUEST_CODE) {

            try {
                lifecycleScope.launch {
                    var gson = Gson()

                    val userSessions = viewModel.dataBaseRepository.allUserSleepSessions.first()

                    val userExporSessions = mutableListOf<UserSleepExportData>()

                    userSessions.forEach { session ->

                        val sessionSleepData = viewModel.dataBaseRepository.getSleepApiRawDataBetweenTimestamps(
                            session.sleepTimes.sleepTimeStart,
                            session.sleepTimes.sleepTimeEnd
                        ).first()

                        val userExporSession = sessionSleepData?.let {
                            UserSleepExportData(
                                session.id,
                                session.mobilePosition,
                                session.lightConditions,
                                session.sleepTimes,
                                session.userSleepRating,
                                session.userCalculationRating,
                                it
                            )
                        }

                        userExporSession?.let { userExporSessions.add(it) }
                    }

                    val exportFile = gson.toJson(userExporSessions)

                    data?.data?.let { writeTextToUri(it, exportFile) }
                }

            } catch (e: IOException) {
                Toast.makeText(actualContext, "Export failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Text to URI Function, used to write text to a file
     */
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

        Toast.makeText(actualContext, getString(R.string.settings_export_success), Toast.LENGTH_SHORT).show()

        val intentShareFile = Intent(Intent.ACTION_SEND)

        intentShareFile.type = "application/json"
        intentShareFile.putExtra(Intent.EXTRA_STREAM, uri)
        intentShareFile.putExtra(
            Intent.EXTRA_SUBJECT,
            getString(R.string.setting_export_sharing_intent)
        )
        intentShareFile.putExtra(Intent.EXTRA_TEXT, getString(R.string.setting_export_sharing_intent))
        startActivity(Intent.createChooser(intentShareFile, getString(R.string.setting_export_sharing_intent)))
    }

    private val contentResolver: ContentResolver by lazy { actualContext.contentResolver}

    /**
     * Check if permissions are granted
     */
    fun checkPermissions(){

        viewModel.activityPermission.value = (
                PermissionsUtil.isActivityRecognitionPermissionGranted(actualContext)
                )

        viewModel.dailyPermission.value = (
                PermissionsUtil.isActivityRecognitionPermissionGranted(actualContext)
                )

        viewModel.storagePermission.value = (
                PermissionsUtil.isNotificationPolicyAccessGranted(actualContext)
                )

        viewModel.overlayPermission.value = (PermissionsUtil.isOverlayPermissionGranted(actualContext))

    }

    private val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    checkPermissions()
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                    checkPermissions()
                }
            }

}


package com.sleepestapp.sleepest.ui.settings


import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.DocumentsContract
import android.provider.Settings
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.View.TEXT_ALIGNMENT_CENTER
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.sleepestapp.sleepest.DontKillMyAppFragment
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.alarmclock.LockScreenAlarmActivity
import com.sleepestapp.sleepest.databinding.FragmentSettingsBinding
import com.sleepestapp.sleepest.model.data.*
import com.sleepestapp.sleepest.model.data.credits.CreditsSites
import com.sleepestapp.sleepest.model.data.export.ImportUtil
import com.sleepestapp.sleepest.model.data.export.UserSleepExportData
import com.sleepestapp.sleepest.onboarding.OnBoardingActivity
import com.sleepestapp.sleepest.util.IconAnimatorUtil.isDarkThemeOn
import com.sleepestapp.sleepest.util.PermissionsUtil
import com.sleepestapp.sleepest.util.SmileySelectorUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.*


class SettingsFragment : Fragment() {

    var factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            // Workaround because we know that we can cast to T
            return SettingsViewModel(
                (actualContext as MainApplication).dataStoreRepository,
                (actualContext as MainApplication).dataBaseRepository
            ) as T
        }
    }

    /**
     * View model of the [SettingsFragment]
     */
    private val viewModel by lazy {
        ViewModelProvider(
            this,
            factory
        ).get(SettingsViewModel::class.java)
    }

    /**
     * Binding XML Code to Fragment
     */
    private lateinit var binding: FragmentSettingsBinding

    /**
     * Get actual context
     */
    private val actualContext: Context by lazy { requireActivity().applicationContext }

    /**
     * actual case of entry
     * e.g. after switching dark mode the case of entry changes
     */
    private var caseOfEntry = -1

    /**
     * Open the selected information view if [caseOfEntry] is not -1
     */
    fun setCaseOfEntry(case: Int) {
        caseOfEntry = case
        if (this::binding.isInitialized)
            viewModel.actualExpand.value = (caseOfEntry)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        //viewModel.transitionsContainer = (binding.linearAnimationlayout)
        binding.profileViewModel = viewModel
        binding.lifecycleOwner = this

        //region On Click Listeners

        binding.sleepActivityPermission.setOnClickListener {
            onPermissionClicked(it)
        }
        binding.powerOptimizationPermission.setOnClickListener {
            onPermissionClicked(it)
        }
        binding.notificationPrivacyPermission.setOnClickListener {
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
        binding.btnTutorial.setOnClickListener {
            lifecycleScope.launch {
                val intent = Intent(activity, OnBoardingActivity::class.java)
                //intent.putExtra(getString(R.string.onboarding_intent_not_first_app_start), true)
                //intent.putExtra(getString(R.string.onboarding_intent_starttime), viewModel.dataStoreRepository.getSleepTimeBegin())
                //intent.putExtra(getString(R.string.onboarding_intent_endtime), viewModel.dataStoreRepository.getSleepTimeEnd())
                //intent.putExtra(getString(R.string.onboarding_intent_duration), viewModel.dataStoreRepository.getSleepDuration())

                startActivity(intent)
            }

        }
        binding.btnImportantSettings.setOnClickListener {
            DontKillMyAppFragment.show(requireActivity())
        }

        viewModel.aboutUsSelection.observe(viewLifecycleOwner) {
            onAboutUsClicked(it)
        }

        viewModel.autoDarkMode.observe(viewLifecycleOwner) {
            TransitionManager.beginDelayedTransition(binding.linearAnimationlayout)
        }

        viewModel.removeExpand.observe(viewLifecycleOwner) {
            TransitionManager.beginDelayedTransition(binding.linearAnimationlayout)
        }

        viewModel.actualExpand.observe(viewLifecycleOwner) {
            TransitionManager.beginDelayedTransition(binding.linearAnimationlayout)
        }

        viewModel.descriptionChanged.observe(viewLifecycleOwner) {
            TransitionManager.beginDelayedTransition(binding.linearAnimationlayout)
        }

        //endregion

        viewModel.actualExpand.value = (caseOfEntry)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // check dark mode settings for lottie animation
        lifecycleScope.launch {

            val settings = viewModel.dataStoreRepository.settingsDataFlow.first()


            if ((settings.designAutoDarkMode && actualContext.isDarkThemeOn()) || !settings.designAutoDarkMode && settings.designDarkMode)
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
     * create the credits for the credits view
     */
    private fun createCredits() {
        val creditsSites = CreditsSites.createCreditSites()

        creditsSites.forEach { site ->

            if (site.site != Websites.PRIVACY_POLICE) {

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
                val marginParams = button.layoutParams as ViewGroup.MarginLayoutParams
                marginParams.setMargins(10, 50, 10, 5)

                button.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.accent_text_color,
                        null
                    )
                )
                button.background =
                    (ResourcesCompat.getDrawable(resources, R.drawable.transparentrounded, null))
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
                textView.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.primary_text_color,
                        null
                    )
                )
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
    fun onAboutUsClicked(tag: String) {
        //Starts the alarm with a new intent
        when (tag) {
            "improvement" -> {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto: sleepestapp@gmail.com")
                val packageInfo = actualContext.packageManager.getPackageInfo(actualContext.packageName, 0)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra(Intent.EXTRA_EMAIL, "Improvement")
                intent.putExtra(Intent.EXTRA_SUBJECT, "Version: " + packageInfo.versionName)

                actualContext.startActivity(intent)
            }
            "rate" -> {
                val uri = Uri.parse("market://details?id=" + actualContext.packageName)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                try {
                    actualContext.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(actualContext, getString(R.string.settings_unable_to_find_market), Toast.LENGTH_LONG).show()
                }
            }
            "error" -> {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto: sleepestapp@gmail.com")
                val packageInfo = actualContext.packageManager.getPackageInfo(actualContext.packageName, 0)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra(Intent.EXTRA_EMAIL, "Error")
                intent.putExtra(Intent.EXTRA_SUBJECT, "Version: " + packageInfo.versionName)

                actualContext.startActivity(intent)

            }
            "PRIVACY_POLICE" -> {
                val websiteUrl = Websites.getWebsite(Websites.getWebsiteByString(tag))
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                actualContext.startActivity(intent)
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

                exportResult.launch(intent)

            }
            "import" -> {

                // Choose a directory using the system's file picker.
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                    // Optionally, specify a URI for the directory that should be opened in
                    // the system file picker when it loads.
                }

                importResult.launch(intent)
            }
        }
    }

    private val exportResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            try {
                lifecycleScope.launch {
                    val gson = Gson()

                    val userSessions = viewModel.dataBaseRepository.allUserSleepSessions.first()

                    val userExportSessions = mutableListOf<UserSleepExportData>()

                    userSessions.forEach { session ->

                        val sessionSleepData = viewModel.dataBaseRepository.getSleepApiRawDataBetweenTimestamps(
                            session.sleepTimes.sleepTimeStart,
                            session.sleepTimes.sleepTimeEnd
                        ).first()

                        val userExportSession = sessionSleepData?.let {
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

                        userExportSession?.let { userExportSessions.add(it) }
                    }

                    val exportFile = gson.toJson(userExportSessions)

                    it.data?.data?.let { writeTextToUri(it, exportFile) }
                }

            } catch (e: IOException) {
                Toast.makeText(actualContext, getString(R.string.settings_export_failed), Toast.LENGTH_SHORT).show()
            }
        }

    private val importResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            val uri = it.data?.data

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
                type = "application/json"
            }

            loadFileResult.launch(intent)
        }

    private val loadFileResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            lifecycleScope.launch {
                ImportUtil.getLoadFileFromUri(
                    it.data?.data,
                    actualContext,
                    viewModel.dataBaseRepository
                )
            }
        }

    /**
     * Request permissions clicked
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun onPermissionClicked(view: View) {
        checkPermissions()
        when (view.tag.toString()) {
            "powerOptimization" -> if (viewModel.powerOptimizationDisabled.value != true) PermissionsUtil.setPowerPermission(requireActivity()
            ) else viewModel.showPermissionInfo("powerOptimization")
            "sleepActivity" -> if (viewModel.activityPermission.value != true) requestPermissionLauncher.launch(
                Manifest.permission.ACTIVITY_RECOGNITION
            ) else viewModel.showPermissionInfo("sleepActivity")
            "notificationPrivacy" -> if (viewModel.notificationPrivacyPermission.value != true) PermissionsUtil.setNotificationPolicyAccess(requireActivity()
            ) else viewModel.showPermissionInfo("notificationPrivacy")
            "overlay" -> if (viewModel.overlayPermission.value != true) PermissionsUtil.setOverlayPermission(requireActivity()
                ) else viewModel.showPermissionInfo("overlay")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        checkPermissions()
    }

    /**
     * Text to URI Function, used to write text to a file
     */
    private fun writeTextToUri(uri: Uri, text: String) {
        try {
            contentResolver.openFileDescriptor(uri, "w")?.use { file ->
                FileOutputStream(file.fileDescriptor).use {
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

        viewModel.powerOptimizationDisabled.value = (
                PermissionsUtil.isPowerPermissionGranted(actualContext) &&
                        PermissionsUtil.isAutoStartGranted(actualContext)
                )

        viewModel.notificationPrivacyPermission.value = (
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



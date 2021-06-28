package com.doitstudio.sleepest_master.ui.profile

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.alarmclock.AlarmClockReceiver
import com.doitstudio.sleepest_master.databinding.FragmentProfileBinding
import com.doitstudio.sleepest_master.databinding.FragmentSleepBinding
import com.doitstudio.sleepest_master.model.data.export.UserSleepExportData
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.ui.sleep.SleepFragment
import com.doitstudio.sleepest_master.ui.sleep.SleepViewModel
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class ProfileFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(this).get(ProfileViewModel::class.java)}
    private lateinit var binding: FragmentProfileBinding
    private val actualContext: Context by lazy {requireActivity().applicationContext}
    private val scope: CoroutineScope = MainScope()
    private val dataBaseRepository: DatabaseRepository by lazy {
        (actualContext as MainApplication).dataBaseRepository
    }

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

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    fun onDataClicked(view: View) {
        when (view.tag.toString()) {
            "export" -> {
                var gson = Gson()

                scope.launch {

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
                    val shareIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, exportFile)
                        type = "text/json"
                    }

                    startActivity(Intent.createChooser(shareIntent, "Export data"))
                }
            }
            "import" -> {

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



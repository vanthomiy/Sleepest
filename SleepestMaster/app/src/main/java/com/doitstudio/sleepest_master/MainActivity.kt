package com.doitstudio.sleepest_master

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.doitstudio.sleepest_master.databinding.ActivityMainBinding
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import com.doitstudio.sleepest_master.sleepapi.SleepHandler
import com.doitstudio.sleepest_master.ui.sleep.SleepFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomBar: BottomNavigationView

    private val scope: CoroutineScope = MainScope()
    private val sleepCalculationStoreRepository by lazy {  SleepCalculationStoreRepository.getRepo(applicationContext)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction().replace(R.id.navigationFrame,AlarmsFragment()).commit()

        bottomBar = binding.bottomBar

        bottomBar.setOnNavigationItemSelectedListener { item->

            var fragment: Fragment
            when (item.itemId) {
                R.id.home -> {
                    fragment = AlarmsFragment()
                    true
                }
                R.id.history -> {
                    fragment = HisotryFragment()
                    true
                }
                R.id.sleep -> {
                    fragment = SleepFragment()
                    true
                }
                else -> {
                    fragment = ProfileFragment()
                    true
                }
            }

            supportFragmentManager.beginTransaction().replace(R.id.navigationFrame,fragment).commit()

            true
        }

        // check permission
        if (!activityRecognitionPermissionApproved()) {
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }

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

    private val sleepHandler : SleepHandler by lazy {SleepHandler.getHandler(this)}

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

}


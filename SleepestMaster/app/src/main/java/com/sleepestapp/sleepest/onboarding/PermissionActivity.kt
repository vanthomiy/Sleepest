package com.sleepestapp.sleepest.onboarding

import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.sleepestapp.sleepest.R

/**
 * Workaround Activity to grant activity recognition permission from inside the tutorial
 */
class PermissionActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
    }

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                finish()
            } else {
                finish()
            }
        }
}
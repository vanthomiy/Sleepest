package com.doitstudio.sleepest_master.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.doitstudio.sleepest_master.DontKillMyAppFragment
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.background.AlarmReceiver
import com.doitstudio.sleepest_master.model.data.AlarmReceiverUsage
import com.doitstudio.sleepest_master.util.TimeConverterUtil
import kotlinx.coroutines.launch
import java.util.*

class PermissionActivity : AppCompatActivity() {
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
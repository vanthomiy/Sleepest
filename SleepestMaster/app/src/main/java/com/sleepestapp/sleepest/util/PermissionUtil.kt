package com.sleepestapp.sleepest.util

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.onboarding.PermissionActivity

/**
 * Util to check and set all permissions which are necessary
 */
object PermissionsUtil {
    /**
     * Check permission for do not disturb
     * @param context Activity Context
     * @return is granted
     */
    fun isNotificationPolicyAccessGranted(context: Context): Boolean {
        val notificationManager =
            context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    /**
     * Check permission to overlay app
     * @param context Activity Context
     * @return is granted
     */
    fun isOverlayPermissionGranted(context: Context?): Boolean {
        return Settings.canDrawOverlays(context)
    }


    /**
     * Check permission to disable power optimisation
     * @param context Activity Context
     * @return is granted
     */
    fun isPowerPermissionGranted(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = context.packageName
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    /**
     * Check Activity Recognition permission
     * @param context Activity Context
     * @return is granted
     */
    fun isActivityRecognitionPermissionGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACTIVITY_RECOGNITION
                )
            ) {
                return true
            }
        }
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                context,
                context.getString(R.string.permission_activity_recognition_old)
            )
        } else false
    }

    /**
     * Check all necessary permissions for the app
     * @param context ActivityContext
     * @return all permissions granted
     */
    fun checkAllNecessaryPermissions(context: Context): Boolean {
        return isNotificationPolicyAccessGranted(context) && isOverlayPermissionGranted(context) &&
                isActivityRecognitionPermissionGranted(context) && isPowerPermissionGranted(context)
    }

    /**
     * Set the permission for disable do not disturb
     * @param context Activity context
     */
    fun setNotificationPolicyAccess(context: Activity?) {
        if (context == null) {
            return
        }
        try {
            val intent = Intent()
            intent.action = Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse("package:" + context.packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            context.startActivityForResult(intent, 282)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            context.startActivityForResult(intent, 282)
        }
        Toast.makeText(
            context,
            context.getString(R.string.onboarding_toast_find_permission_in_list),
            Toast.LENGTH_LONG
        ).show()
    }

    /**
    * Set the permission for disable power safer
    * @param context Activity context
    */
    fun setPowerPermission(context: Activity) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = context.packageName
        val i = Intent()
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            i.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            i.data = Uri.parse("package:$packageName")
            context.startActivityForResult(i, 284)
        }
    }

    /**
     * Set the permission for overlay the screen
     * @param context Activity context
     */
    fun setOverlayPermission(context: Activity?) {
        if (context == null) {
            return
        }
        try {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_OVERLAY_PERMISSION
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse("package:" + context.packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            context.startActivityForResult(intent, 283)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_OVERLAY_PERMISSION
            context.startActivityForResult(intent, 283)
        }
        Toast.makeText(
            context,
            context.getString(R.string.onboarding_toast_find_permission_in_list),
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Set the permission for tracking activity data
     * @param context Activity context
     */
    fun setActivityRecognitionPermission(context: Context) {
        val intent = Intent(context, PermissionActivity::class.java)
        context.startActivity(intent)
    }
}
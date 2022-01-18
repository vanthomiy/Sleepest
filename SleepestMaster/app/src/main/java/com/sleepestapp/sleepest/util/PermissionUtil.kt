package com.sleepestapp.sleepest.util

import android.Manifest
import android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.annotation.SuppressLint
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
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.judemanutd.autostarter.AutoStartPermissionHelper
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.onboarding.PermissionActivity
import androidx.core.content.ContextCompat.startActivity




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
     * Check permission to enable auto start
     * @param context Activity Context
     * @return is granted
     */
    fun isAutoStartGranted(context: Context): Boolean {
        if (!AutoStartPermissionHelper.getInstance().isAutoStartPermissionAvailable(context))
            return true
        return AutoStartPermissionHelper.getInstance().getAutoStartPermission(context, false)
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
                && isAutoStartGranted(context)
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
    @SuppressLint("BatteryLife")
    fun setPowerPermission(context: Activity) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = context.packageName
        val i = Intent()
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            i.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            i.data = Uri.parse("package:$packageName")
            context.startActivityForResult(i, 284)
        }

        val intent = Intent()
        intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        context.startActivity(intent)

        setAutoStartGranted(context)

    }

    enum class WhiteListedInBatteryOptimizations {
        WHITE_LISTED, NOT_WHITE_LISTED, ERROR_GETTING_STATE, IRRELEVANT_OLD_ANDROID_API
    }

    fun getIfAppIsWhiteListedFromBatteryOptimizations(context: Context, packageName: String = context.packageName): WhiteListedInBatteryOptimizations {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return WhiteListedInBatteryOptimizations.IRRELEVANT_OLD_ANDROID_API
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager?
            ?: return WhiteListedInBatteryOptimizations.ERROR_GETTING_STATE
        return if (pm.isIgnoringBatteryOptimizations(packageName)) WhiteListedInBatteryOptimizations.WHITE_LISTED else WhiteListedInBatteryOptimizations.NOT_WHITE_LISTED
    }

    //@TargetApi(VERSION_CODES.M)
    @SuppressLint("BatteryLife", "InlinedApi")
    @RequiresPermission(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
    fun prepareIntentForWhiteListingOfBatteryOptimization(context: Context, packageName: String = context.packageName, alsoWhenWhiteListed: Boolean = false): Intent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return null
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) == PackageManager.PERMISSION_DENIED)
            return null
        val appIsWhiteListedFromPowerSave: WhiteListedInBatteryOptimizations = getIfAppIsWhiteListedFromBatteryOptimizations(context, packageName)
        var intent: Intent? = null
        when (appIsWhiteListedFromPowerSave) {
            WhiteListedInBatteryOptimizations.WHITE_LISTED -> if (alsoWhenWhiteListed) intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            WhiteListedInBatteryOptimizations.NOT_WHITE_LISTED -> intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(Uri.parse("package:$packageName"))
            WhiteListedInBatteryOptimizations.ERROR_GETTING_STATE, WhiteListedInBatteryOptimizations.IRRELEVANT_OLD_ANDROID_API -> {
            }
        }
        return intent
    }

    /**
     * Set permission to enable auto start
     * @param context Activity Context
     * @return is granted
     */
    fun setAutoStartGranted(context: Context) {

        AutoStartPermissionHelper.getInstance().getAutoStartPermission(context, true)
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
package com.sleepestapp.sleepest.util;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.sleepestapp.sleepest.R;
import com.sleepestapp.sleepest.background.ForegroundActivity;
import com.sleepestapp.sleepest.onboarding.PermissionActivity;
import com.sleepestapp.sleepest.onboarding.PermissionActivity;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

public class PermissionsUtil {

    public static boolean isNotificationPolicyAccessGranted(Context context) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE);

        if(notificationManager.isNotificationPolicyAccessGranted()) {
            return true;
        }

        return false;
    }

    public static boolean isOverlayPermissionGranted(Context context) {

        if (Settings.canDrawOverlays(context)) {
            return true;
        }

        return false;
    }

    public static boolean isActivityRecognitionPermissionGranted(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION)) {
                return true;
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if ((PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, context.getString(R.string.permission_activity_recognition_old)))) {
                return true;
            }
        }

        return false;
    }

    public static boolean checkAllNeccessaryPermissions (Context context) {
        if (PermissionsUtil.isNotificationPolicyAccessGranted(context) && PermissionsUtil.isOverlayPermissionGranted(context) &&
                PermissionsUtil.isActivityRecognitionPermissionGranted(context)) {
            return true;
        }

        return false;
    }

    /**
     * Set the permission for disable do not disturb
     * @param context Activity context
     */
    public static void setNotificationPolicyAccess(Context context) {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        context.startActivity(intent);
    }

    /**
     * Set the permission for overlay the screen
     * @param context Activity context
     */
    public static void setOverlayPermission(Context context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        context.startActivity(intent);
    }

    /**
     * Set the permission for tracking activity data
     * @param context Activity context
     */
    public static void setActivityRecognitionPermission(Context context) {
        Intent intent = new Intent(context, PermissionActivity.class);
        context.startActivity(intent);
    }
}
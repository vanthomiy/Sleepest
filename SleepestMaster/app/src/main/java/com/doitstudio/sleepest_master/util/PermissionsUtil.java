package com.doitstudio.sleepest_master.util;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
            if ((PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, "com.google.android.gms.permission.ACTIVITY_RECOGNITION"))) {
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
}

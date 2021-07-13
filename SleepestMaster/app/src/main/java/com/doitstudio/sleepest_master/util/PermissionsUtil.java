package com.doitstudio.sleepest_master.util;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
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

        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION)) {
            return true;
        }

        return false;
    }
}

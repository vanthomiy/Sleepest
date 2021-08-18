package com.doitstudio.sleepest_master.util;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.doitstudio.sleepest_master.background.ForegroundActivity;
import com.doitstudio.sleepest_master.onboarding.OnboardingActivity;
import com.doitstudio.sleepest_master.onboarding.PermissionActivity;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
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

    public static void setNotificationPolicyAccess(Context context) {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        context.startActivity(intent);
    }

    public static void setOverlayPermission(Context context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"));
        context.startActivity(intent);
    }

    public static void setActivityRecognitionPermission(Context context) {
        Intent intent = new Intent(context, PermissionActivity.class);
        context.startActivity(intent);
    }
}

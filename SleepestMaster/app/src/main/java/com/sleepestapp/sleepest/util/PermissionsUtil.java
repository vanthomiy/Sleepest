package com.sleepestapp.sleepest.util;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.sleepestapp.sleepest.R;
import com.sleepestapp.sleepest.onboarding.PermissionActivity;

public class PermissionsUtil {

    public static boolean isNotificationPolicyAccessGranted(Context context) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE);

        return notificationManager.isNotificationPolicyAccessGranted();
    }

    public static boolean isOverlayPermissionGranted(Context context) {

        return Settings.canDrawOverlays(context);
    }

    public static boolean isActivityRecognitionPermissionGranted(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION)) {
                return true;
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, context.getString(R.string.permission_activity_recognition_old));
        }

        return false;
    }

    public static boolean checkAllNecessaryPermissions(Context context) {
        return PermissionsUtil.isNotificationPolicyAccessGranted(context) && PermissionsUtil.isOverlayPermissionGranted(context) &&
                PermissionsUtil.isActivityRecognitionPermissionGranted(context);
    }

    /**
     * Set the permission for disable do not disturb
     * @param context Activity context
     */
    public static void setNotificationPolicyAccess(Activity context) {

        if (context == null) {
            return;
        }

        try {
            final Intent intent = new Intent();
            intent.setAction(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            context.startActivity(intent);
            Toast.makeText(context, context.getString(R.string.onboarding_toast_find_permission_in_list), Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Set the permission for overlay the screen
     * @param context Activity context
     */
    public static void setOverlayPermission(final Activity context) {
        if (context == null) {
            return;
        }
        try {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            context.startActivity(intent);
            Toast.makeText(context, context.getString(R.string.onboarding_toast_find_permission_in_list), Toast.LENGTH_LONG).show();
        }

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

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

/**
 * Util to check and set all permissions which are necessary
 */
public class PermissionsUtil {

    /**
     * Check permission for do not disturb
     * @param context Activity Context
     * @return is granted
     */
    public static boolean isNotificationPolicyAccessGranted(Context context) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE);

        return notificationManager.isNotificationPolicyAccessGranted();
    }

    /**
     * Check permission to overlay app
     * @param context Activity Context
     * @return is granted
     */
    public static boolean isOverlayPermissionGranted(Context context) {

        return Settings.canDrawOverlays(context);
    }

    /**
     * Check Activity Recognition permission
     * @param context Activity Context
     * @return is granted
     */
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

    /**
     * Check all necessary permissions for the app
     * @param context ActivityContext
     * @return all permissions granted
     */
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
            context.startActivityForResult(intent, 282);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            context.startActivityForResult(intent, 282);
        }

        Toast.makeText(context, context.getString(R.string.onboarding_toast_find_permission_in_list), Toast.LENGTH_LONG).show();

    }

    /**
     * Set the permission for overlay the screen
     * @param context Activity context
     */
    public static void setOverlayPermission(Activity context) {
        if (context == null) {
            return;
        }
        try {
            final Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivityForResult(intent, 283);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            context.startActivityForResult(intent, 283);
        }

        Toast.makeText(context, context.getString(R.string.onboarding_toast_find_permission_in_list), Toast.LENGTH_LONG).show();

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

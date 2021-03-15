package com.doitstudio.backgroundingtestproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Objects;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED) && new ServiceTracker().getServiceState(context) == ServiceState.STARTED) {
            intent = new Intent(context, EndlessService.class);
            intent.setAction(Actions.START.name());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
                return;
            }
            context.startService(intent);
        }
    }

}

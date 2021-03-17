package com.doitstudio.sleepest_master.Background;

/** This receiver is there to handle the restart of a foregroundservide after reboot of device */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Objects;

public class BootReceiver extends BroadcastReceiver {

    //Receives restart of an android device and restart foreground service
    @Override
    public void onReceive(Context context, Intent intent) {

        //Check if boot is finished and foreground service was started before reboot
        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)
                && new ServiceTracker().getServiceState(context) == ServiceState.STARTED) {

            intent = new Intent(context, EndlessService.class);
            intent.setAction(Actions.START.name());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                context.startForegroundService(intent); //restart foreground service
                return;
            }
            context.startService(intent);
        }
    }

}

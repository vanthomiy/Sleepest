package com.doitstudio.sleepest_master.background;

/**State of service will be saved in shared preferences here */

import android.content.Context;
import android.content.SharedPreferences;

//State of the service
enum ServiceState {
    STARTED,
    STOPPED,
}

class ServiceTracker {

    private String key = "SPYSERVICE_STATE"; /**TODO: Als Konstante zentral anlegen*/

    /** Save service state with shared preference here
     * @param context Application context
     * @param state State of the foreground service
     */
    void setServiceState(Context context, ServiceState state) {

        SharedPreferences settings = getPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, state.name());
        editor.apply();
    }

    /**Get name of the service state
     * @param context Application context
     * @return name of the service state
     */
    ServiceState getServiceState(Context context) {

        SharedPreferences settings = getPreferences(context);
        return ServiceState.valueOf(settings.getString(key, ServiceState.STOPPED.name()));
    }

    /** Get preferences instance
     *
     * @param context Application context
     * @return Instance of preferences
     */
    private SharedPreferences getPreferences(Context context) {

        String name = "SPYSERVICE_KEY";
        return context.getSharedPreferences(name, 0);
    }
}
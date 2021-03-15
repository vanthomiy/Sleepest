package com.doitstudio.backgroundingtestproject;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Enum of the state of the service.
 */
enum ServiceState {
    STARTED,
    STOPPED,
}

/**
 * Save the state of the service with SharedPreference.
 */
class ServiceTracker {
    /**
     * SharedPreference key.
     */
    private String key = "SPYSERVICE_STATE";

    /**
     * Saved Service state with shared preference.
     *
     * @param context : App context.
     * @param state   : State of the service.
     */
    void setServiceState(Context context, ServiceState state) {
        SharedPreferences settings = getPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, state.name());
        editor.apply();
    }

    /**
     * Get service state with SharedPreference.
     *
     * @param context : App context.
     * @return ServiceState : STARTED or STOPPED
     */
    ServiceState getServiceState(Context context) {
        SharedPreferences settings = getPreferences(context);
        return ServiceState.valueOf(settings.getString(key, ServiceState.STOPPED.name()));
    }

    /**
     * To simplify the use of SharedPreference.
     *
     * @param context : App context.
     * @return SharedPreferences.
     */
    private SharedPreferences getPreferences(Context context) {
        // SharedPreference name.
        String name = "SPYSERVICE_KEY";
        return context.getSharedPreferences(name, 0);
    }
}
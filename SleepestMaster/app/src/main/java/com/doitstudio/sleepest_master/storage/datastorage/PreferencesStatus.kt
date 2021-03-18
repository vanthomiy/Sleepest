package com.doitstudio.sleepest_master.storage.datastorage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val PREFERENCES_STATUS_NAME = "preferences_name"

/**
 * Save/Access single preferences over this class [DataStore].
 * Provides functions for update the values and flows to access the values
 */
class PreferencesStatus(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val ALARM_TIME = intPreferencesKey("alarm_time")
    }

    //region Observe Flows

    // Observed Flow will notify the observer when the the sleep subscription status has changed.
    val alarmTimeFlow: Flow<Int> = dataStore.data.map { preferences ->
        // Get the subscription value, defaults to false if not set:
        preferences[PreferencesKeys.ALARM_TIME] ?: 0
    }



    //endregion

    //region Update Values

    // Updates Alarm Time status.
    suspend fun updateAlarmTime(alarmTime: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALARM_TIME] = alarmTime
        }
    }


    //endregion

}
package com.doitstudio.sleepest_master.storage.datastorage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val ALARM_PREFERENCES_NAME = "alarm_preferences"

/**
 * Saves the algorithm data subscription status into a [DataStore].
 * Used to check if the app is still listening to changes in sleep data when the app is brought
 * back into the foreground.
 */
class AlarmPreferencesStatus(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val ALARM_TIME = intPreferencesKey("alarm_time")
        val ALARM_ACTIVE = booleanPreferencesKey("alarm_active")
    }

    //region Observe Flows

    // Observed Flow will notify the observer when the the sleep subscription status has changed.
    val alarmTimeFlow: Flow<Int> = dataStore.data.map { preferences ->
        // Get the subscription value, defaults to false if not set:
        preferences[PreferencesKeys.ALARM_TIME] ?: 0
    }

    // Observed Flow will notify the observer when the the sleep subscription status has changed.
    val alarmActiveFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        // Get the subscription value, defaults to false if not set:
        preferences[PreferencesKeys.ALARM_ACTIVE] ?: false
    }

    //endregion

    //region Update Values

    // Updates Alarm Time status.
    suspend fun updateAlarmTime(alarmTime: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALARM_TIME] = alarmTime
        }
    }

    // Updates Alarm Active status.
    suspend fun updateAlarmActive(alarmActive: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALARM_ACTIVE] = alarmActive
        }
    }

    //endregion

}
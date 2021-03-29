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
        val SUBSCRIBE_TO_SLEEP_DATA = booleanPreferencesKey("subscribe_to_sleep_data")
    }

    //region Sleep Data Subscription

    // Observed Flow will notify the observer when the the sleep subscription status has changed.
    val subscribedToSleepDataFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        // Get the subscription value, defaults to false if not set:
        preferences[PreferencesKeys.SUBSCRIBE_TO_SLEEP_DATA] ?: false
    }

    // Updates subscription to sleep data
    suspend fun updateSubscribeToSleepData(subscribe: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUBSCRIBE_TO_SLEEP_DATA] = subscribe
        }
    }

    //endregion

}
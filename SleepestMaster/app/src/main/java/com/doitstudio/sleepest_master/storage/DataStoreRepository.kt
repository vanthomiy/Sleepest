package com.doitstudio.sleepest_master.storage

import android.content.Context
import androidx.datastore.createDataStore
import androidx.datastore.preferences.createDataStore
import com.doitstudio.sleepest_master.Alarm
import com.doitstudio.sleepest_master.SleepApiData
import com.doitstudio.sleepest_master.storage.datastorage.*
import kotlinx.coroutines.flow.Flow

class DataStoreRepository(context: Context) {

    //region Alarm

    private val alarmStatus by lazy{ AlarmStatus(context.createDataStore(
        ALARM_STATUS_NAME,
        serializer = AlarmSerializer())
    )}


    val alarmFlow: Flow<Alarm> = alarmStatus.alarm

    suspend fun updateAlarmActive(alarmActive: Boolean) =
        alarmStatus.updateAlarmActive(alarmActive)

    suspend fun updateSoundId(alarmActive: Int) =
        alarmStatus.updateSoundId(alarmActive)

    suspend fun updateAlarmName(alarmActive: String) =
        alarmStatus.updateAlarmName(alarmActive)

    suspend fun updateAlarmTime(alarmActive: Long) =
            alarmStatus.updateAlarmTime(alarmActive)

    //endregion

    //region SleepApiData Status

    private val sleepApiDataStatus by lazy{ SleepApiDataStatus(context.createDataStore(
            SLEEP_API_DATA_NAME,
            serializer = SleepApiDataSerializer())
    )}


    val sleepApiDataFlow: Flow<SleepApiData> = sleepApiDataStatus.sleepApiData

    suspend fun updateSubscribeRequest(isActive:Boolean) =
            sleepApiDataStatus.updateSubscribeRequest(isActive)

    suspend fun updateIsSubscribed(isActive:Boolean) =
            sleepApiDataStatus.updateIsSubscribed(isActive)

    suspend fun updatePermissionActive(isActive:Boolean) =
            sleepApiDataStatus.updatePermissionActive(isActive)

    suspend fun updatePermissionRemovedError(isActive:Boolean) =
            sleepApiDataStatus.updatePermissionRemovedError(isActive)

    suspend fun updateSubscribeFailed(isActive:Boolean) =
            sleepApiDataStatus.updateSubscribeFailed(isActive)

    suspend fun updateUnsubscribeFailed(isActive:Boolean) =
            sleepApiDataStatus.updateUnsubscribeFailed(isActive)

    suspend fun updateSleepApiValuesAmount(amount:Int) =
            sleepApiDataStatus.updateSleepApiValuesAmount(amount)


    //endregion

    //region Single Preferences

    private val preferencesStatus by lazy {
        PreferencesStatus(
            context.createDataStore(name = PREFERENCES_STATUS_NAME)
        )
    }

    /**
     * Is subscribed to sleep data ?
     */
    val subscribedToSleepDataFlow: Flow<Boolean> = preferencesStatus.subscribedToSleepDataFlow

    /**
     * Update is subscribed to sleep data ?
     */
    suspend fun updateSubscribeToSleepData(subscribe: Boolean) =
        preferencesStatus.updateSubscribeToSleepData(subscribe)

    //endregion

}
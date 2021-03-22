package com.doitstudio.sleepest_master.storage

import android.content.Context
import androidx.datastore.createDataStore
import androidx.datastore.preferences.createDataStore
import com.doitstudio.sleepest_master.Alarm
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

    //region Single Preferences

    private val preferencesStatus by lazy {
        PreferencesStatus(
            context.createDataStore(name = PREFERENCES_STATUS_NAME)
        )
    }

    val alarmTimeFlow: Flow<Int> = preferencesStatus.alarmTimeFlow

    suspend fun updateAlarmTime(alarmTime: Int) =
        preferencesStatus.updateAlarmTime(alarmTime)

    //endregion

}
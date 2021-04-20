package com.doitstudio.sleepest_master.storage

import android.content.Context
import androidx.datastore.createDataStore
import com.doitstudio.sleepest_master.Alarm

import com.doitstudio.sleepest_master.storage.datastorage.*
import kotlinx.coroutines.flow.Flow

class DataStoreRepository(context: Context) {

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: DataStoreRepository? = null

        var a:Int = 0

        fun getRepo(context: Context): DataStoreRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = DataStoreRepository(context)
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

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

}
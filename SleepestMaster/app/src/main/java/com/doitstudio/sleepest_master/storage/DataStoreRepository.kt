package com.doitstudio.sleepest_master.storage

import android.content.Context
import androidx.datastore.createDataStore
import androidx.datastore.preferences.createDataStore

import com.doitstudio.sleepest_master.LiveUserSleepActivity
import com.doitstudio.sleepest_master.SleepApiData

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

    /*
    //region Alarm

    private val alarmStatus by lazy{ AlarmStatus(context.createDataStore(
        ALARM_STATUS_NAME,
        serializer = AlarmSerializer())
    )}


    val alarmFlow: Flow<Alarm> = alarmStatus.alarm

    suspend fun updateSleepDuration(duration: Int) =
        alarmStatus.updateSleepDuration(duration)

    suspend fun updateWakeUpEarly(timeOfDay: Int) =
        alarmStatus.updateWakeUpEarly(timeOfDay)

    suspend fun updateWakeUpLate(timeOfDay: Int) =
        alarmStatus.updateWakeUpLate(timeOfDay)

    //endregion*/

    //region SleepApiData Status

    private val sleepApiDataStatus by lazy{ SleepApiDataStatus(context.createDataStore(
            SLEEP_API_DATA_NAME,
            serializer = SleepApiDataSerializer())
    )}


    val sleepApiDataFlow: Flow<SleepApiData> = sleepApiDataStatus.sleepApiData

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

    suspend fun resetSleepApiValuesAmount() =
        sleepApiDataStatus.resetSleepApiValuesAmount()


    //endregion


    //region LiveUserSleepActivity Status

    private val liveUserSleepActivityStatus by lazy{ LiveUserSleepActivityStatus(context.createDataStore(
        LIVE_USER_ACTIVITY_DATA_NAME,
        serializer = LiveUserSleepActivitySerializer())
    )}


    val liveUserSleepActivityFlow: Flow<LiveUserSleepActivity> = liveUserSleepActivityStatus.liveUserSleepActivity

    suspend fun updateIsUserSleeping(isActive:Boolean) =
        liveUserSleepActivityStatus.updateIsUserSleeping(isActive)

    suspend fun updateIsDataAvailable(isActive:Boolean) =
        liveUserSleepActivityStatus.updateIsDataAvailable(isActive)

    suspend fun updateUserSleepTime(sleepTime:Int) =
        liveUserSleepActivityStatus.updateUserSleepTime(sleepTime)

    suspend fun addUserSleepHistory(sleepHistory:Int) =
        liveUserSleepActivityStatus.addUserSleepHistory(sleepHistory)

    suspend fun setUserSleepHistory(sleepHistory:List<Int>) =
        liveUserSleepActivityStatus.setUserSleepHistory(sleepHistory)

    suspend fun clearUserSleepHistory() =
        liveUserSleepActivityStatus.clearUserSleepHistory()


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
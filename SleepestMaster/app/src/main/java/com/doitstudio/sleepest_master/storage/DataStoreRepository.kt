package com.doitstudio.sleepest_master.storage

import android.content.Context
import androidx.datastore.createDataStore
import androidx.datastore.preferences.createDataStore
import com.doitstudio.sleepest_master.BackgroundService

import com.doitstudio.sleepest_master.LiveUserSleepActivity
import com.doitstudio.sleepest_master.SleepApiData
import com.doitstudio.sleepest_master.SleepParameters
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.MobileUseFrequency
import com.doitstudio.sleepest_master.storage.datastorage.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.LocalTime

class DataStoreRepository(context: Context) {

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: DataStoreRepository? = null

        fun getRepo(context: Context): DataStoreRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = DataStoreRepository(context)
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

    //region SleepParameter Status

    private val sleepParameterStatus by lazy{ SleepParameterStatus(context.createDataStore(
        SLEEP_PARAMETER_STATUS,
        serializer = SleepParameterSerializer())
    )
    }

    val sleepParameterFlow: Flow<SleepParameters> = sleepParameterStatus.sleepParameters

    /**
     * Returns if the time is in actual sleep time
     */
    suspend fun isInSleepTime(): Boolean {

        var times =  sleepParameterFlow.first()

        val time = LocalTime.now()

        return (time.toSecondOfDay() > times.sleepTimeStart && time.toSecondOfDay() < times.sleepTimeEnd)
    }

    suspend fun updateSleepTimeEnd(time:Int) =
        sleepParameterStatus.updateSleepTimeEnd(time)
    suspend fun updateSleepTimeStart(time:Int) =
        sleepParameterStatus.updateSleepTimeStart(time)
    suspend fun updateUserWantedSleepTime(time:Int) =
        sleepParameterStatus.updateUserWantedSleepTime(time)
    suspend fun updateStandardMobilePosition(time:MobilePosition) =
        sleepParameterStatus.updateStandardMobilePosition(time)
    suspend fun updateStandardMobilePosition(time:Int) =
        sleepParameterStatus.updateStandardMobilePosition(time)
    suspend fun updateUserMobileFequency(time:MobileUseFrequency) =
        sleepParameterStatus.updateUserMobileFequency(time)

    //endregion

    //region SleepApiData Status

    private val sleepApiDataStatus by lazy{ SleepApiDataStatus(context.createDataStore(
            SLEEP_API_DATA_NAME,
            serializer = SleepApiDataSerializer())
    )
    }


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
    )
    }


    val liveUserSleepActivityFlow: Flow<LiveUserSleepActivity> = liveUserSleepActivityStatus.liveUserSleepActivity

    suspend fun updateIsUserSleeping(isActive:Boolean) =
        liveUserSleepActivityStatus.updateIsUserSleeping(isActive)

    suspend fun updateIsDataAvailable(isActive:Boolean) =
        liveUserSleepActivityStatus.updateIsDataAvailable(isActive)

    suspend fun updateUserSleepTime(sleepTime:Int) =
        liveUserSleepActivityStatus.updateUserSleepTime(sleepTime)
    //endregion

    private val backgroundServiceStatus by lazy{ BackgroundServiceStatus(context.createDataStore(
        BACKGROUND_SERVICE_STATUS,
        serializer = BackgroundServiceSerializer())
    )
    }


    val backgroundServiceFlow: Flow<BackgroundService> = backgroundServiceStatus.backgroundService
    suspend fun backgroundUpdateIsActive(value:Boolean) =
        backgroundServiceStatus.updateIsActive(value)
    suspend fun backgroundUpdateShouldBeActive(value:Boolean) =
        backgroundServiceStatus.updateShouldBeActive(value)


}
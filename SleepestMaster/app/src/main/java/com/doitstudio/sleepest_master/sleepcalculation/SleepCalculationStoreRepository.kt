package com.doitstudio.sleepest_master.sleepcalculation

import android.content.Context
import androidx.datastore.createDataStore
import com.doitstudio.sleepest_master.LiveUserSleepActivity
import com.doitstudio.sleepest_master.SleepApiData
import com.doitstudio.sleepest_master.sleepcalculation.datastore.*
import kotlinx.coroutines.flow.Flow

class SleepCalculationStoreRepository(context: Context) {

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: SleepCalculationStoreRepository? = null

        fun getRepo(context: Context): SleepCalculationStoreRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = SleepCalculationStoreRepository(context)
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

    // region live user sleep activity

    private val liveUserSleepActivityStatus by lazy{ LiveUserSleepActivityStatus(context.createDataStore(
            LIVE_USER_ACTIVITY_DATA_NAME,
            serializer = LiveUserSleepActivitySerializer())
    )
    }


    val liveUserSleepActivityFlow: Flow<LiveUserSleepActivity> = liveUserSleepActivityStatus.liveUserSleepActivity

    suspend fun updateIsUserSleeping(isUserSleeping: Boolean) =
            liveUserSleepActivityStatus.updateIsUserSleeping(isUserSleeping)

    suspend fun updateIsDataAvailable(isDataAvailable: Boolean) =
            liveUserSleepActivityStatus.updateIsDataAvailable(isDataAvailable)

    suspend fun updateUserSleepTime(userSleepTime: Int) =
            liveUserSleepActivityStatus.updateUserSleepTime(userSleepTime)

    /*
    suspend fun updateUserSleepFound(isUserSleepFound: Boolean) =
            liveUserSleepActivityStatus.updateUserSleepFound(isUserSleepFound)

    suspend fun setUserSleepHistory(userSleepHistory: List<Int>) =
            liveUserSleepActivityStatus.setUserSleepHistory(userSleepHistory)

    suspend fun addUserSleepHistory(userSleepHistory: Int) =
            liveUserSleepActivityStatus.addUserSleepHistory(userSleepHistory)

    suspend fun clearUserSleepHistory()  =
            liveUserSleepActivityStatus.clearUserSleepHistory()
    */
    // endregion

    // region sleep api data status

    private val sleepApiDataStatus by lazy{ SleepApiDataStatus(context.createDataStore(
            SLEEP_API_DATA_NAME,
            serializer = SleepApiDataSerializer())
    )
    }


    val sleepApiDataFlow: Flow<SleepApiData> = sleepApiDataStatus.sleepApiData

    suspend fun updateIsSubscribed(isActive:Boolean) =
            sleepApiDataStatus.updateIsSubscribed(isActive)

    suspend fun updatePermissionRemovedError(isActive:Boolean) =
            sleepApiDataStatus.updatePermissionRemovedError(isActive)

    suspend fun updateSubscribeFailed(isActive:Boolean) =
            sleepApiDataStatus.updateSubscribeFailed(isActive)

    suspend fun updateUnsubscribeFailed(isActive:Boolean) =
            sleepApiDataStatus.updateUnsubscribeFailed(isActive)

    suspend fun updateSleepApiValuesAmount(amount:Int) =
            sleepApiDataStatus.updateSleepApiValuesAmount(amount)

    suspend fun resetSleepApiValuesAmount()  =
            sleepApiDataStatus.resetSleepApiValuesAmount()

    suspend fun updatePermissionActive(isActive:Boolean)  =
            sleepApiDataStatus.updatePermissionActive(isActive)


    // endregion

}
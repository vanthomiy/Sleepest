package com.doitstudio.sleepest_master.storage

import android.content.Context
import androidx.datastore.createDataStore
import androidx.datastore.preferences.createDataStore
import com.doitstudio.sleepest_master.*
import com.doitstudio.sleepest_master.model.data.LightConditions

import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.MobileUseFrequency
import com.doitstudio.sleepest_master.storage.datastorage.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    private val scope: CoroutineScope = MainScope()

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
    suspend fun isInSleepTime(givenTime:LocalTime? = null): Boolean {

        var times = sleepParameterFlow.first()

        val time = givenTime ?: LocalTime.now()
        val maxTime = (24*60*60) + 1
        val seconds = time.toSecondOfDay()

        val overTwoDays = times.sleepTimeStart > times.sleepTimeEnd

        val bla = ((overTwoDays && (seconds in times.sleepTimeStart..maxTime ||  seconds in 0 .. times.sleepTimeEnd)) || (!overTwoDays && seconds in times.sleepTimeStart..times.sleepTimeEnd))

        return bla
    }

    fun getSleepTimeBeginJob() : Int = runBlocking{
        return@runBlocking getSleepTimeBegin()
    }

    fun getSleepTimeEndJob() : Int = runBlocking{
        return@runBlocking getSleepTimeEnd()
    }

    suspend fun getSleepTimeBegin() : Int {
        return sleepParameterFlow.first().sleepTimeStart
    }

    suspend fun getSleepTimeEnd() : Int {
        return sleepParameterFlow.first().sleepTimeEnd
    }
    suspend fun updateActivityTracking(value:Boolean) =
            sleepParameterStatus.updateActivityTracking(value)
    suspend fun updateActivityInCalculation(value:Boolean) =
            sleepParameterStatus.updateActivityInCalculation(value)
    suspend fun updateEndAlarmAfterFired(value:Boolean) =
            sleepParameterStatus.updateEndAlarmAfterFired(value)
    suspend fun updateAlarmArt(value:Int) =
        sleepParameterStatus.updateAlarmArt(value)
    suspend fun updateAutoSleepTime(time:Boolean) =
            sleepParameterStatus.updateAutoSleepTime(time)
    suspend fun updateSleepTimeEnd(time:Int) =
        sleepParameterStatus.updateSleepTimeEnd(time)
    suspend fun updateSleepTimeStart(time:Int) =
        sleepParameterStatus.updateSleepTimeStart(time)
    suspend fun updateUserWantedSleepTime(time:Int) =
        sleepParameterStatus.updateUserWantedSleepTime(time)
    suspend fun updateStandardMobilePosition(time:Int) =
        sleepParameterStatus.updateStandardMobilePosition(time)
    suspend fun updateLigthCondition(time:Int) =
        sleepParameterStatus.updateLigthCondition(time)
    suspend fun updateUserMobileFequency(time:Int) =
        sleepParameterStatus.updateUserMobileFequency(time)

    //endregion

    //region SleepApiData Status

    private val sleepApiDataStatus by lazy{ SleepApiDataStatus(context.createDataStore(
            SLEEP_API_DATA_NAME,
            serializer = SleepApiDataSerializer())
    )
    }


    val sleepApiDataFlow: Flow<SleepApiData> = sleepApiDataStatus.sleepApiData

    suspend fun getSleepSubscribeStatus() : Boolean {
        return sleepApiDataStatus.sleepApiData.first().isSubscribed }

    suspend fun updateSleepIsSubscribed(isActive:Boolean) =
            sleepApiDataStatus.updateIsSubscribed(isActive)

    suspend fun updateSleepPermissionActive(isActive:Boolean) =
            sleepApiDataStatus.updatePermissionActive(isActive)

    suspend fun updateSleepPermissionRemovedError(isActive:Boolean) =
            sleepApiDataStatus.updatePermissionRemovedError(isActive)

    suspend fun updateSleepSubscribeFailed(isActive:Boolean) =
            sleepApiDataStatus.updateSubscribeFailed(isActive)

    suspend fun updateSleepUnsubscribeFailed(isActive:Boolean) =
            sleepApiDataStatus.updateUnsubscribeFailed(isActive)

    suspend fun updateSleepSleepApiValuesAmount(amount:Int) =
            sleepApiDataStatus.updateSleepApiValuesAmount(amount)

    suspend fun resetSleepApiValuesAmount() =
        sleepApiDataStatus.resetSleepApiValuesAmount()


    //endregion


    //region ActivityApiData Status

    private val ActivityApiDataStatus by lazy{ ActivityApiDataStatus(context.createDataStore(
            ACTIVITY_API_DATA_NAME,
            serializer = ActivityApiDataSerializer())
    )
    }


    val activityApiDataFlow: Flow<ActivityApiData> = ActivityApiDataStatus.activityApiData

    suspend fun getActivitySubscribeStatus() : Boolean {
        return ActivityApiDataStatus.activityApiData.first().isSubscribed }

    suspend fun updateActivityIsSubscribed(isActive:Boolean) =
            ActivityApiDataStatus.updateIsSubscribed(isActive)

    suspend fun updateActivityPermissionActive(isActive:Boolean) =
            ActivityApiDataStatus.updatePermissionActive(isActive)

    suspend fun updateActivityPermissionRemovedError(isActive:Boolean) =
            ActivityApiDataStatus.updatePermissionRemovedError(isActive)

    suspend fun updateActivitySubscribeFailed(isActive:Boolean) =
            ActivityApiDataStatus.updateSubscribeFailed(isActive)

    suspend fun updateActivityUnsubscribeFailed(isActive:Boolean) =
            ActivityApiDataStatus.updateUnsubscribeFailed(isActive)

    suspend fun updateActivityApiValuesAmount(amount:Int) =
            ActivityApiDataStatus.updateActivityApiValuesAmount(amount)

    suspend fun resetActivityApiValuesAmount() =
            ActivityApiDataStatus.resetActivityApiValuesAmount()


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
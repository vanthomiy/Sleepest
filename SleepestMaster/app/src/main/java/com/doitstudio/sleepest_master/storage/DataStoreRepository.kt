package com.doitstudio.sleepest_master.storage

import android.content.Context
import androidx.datastore.createDataStore
import com.doitstudio.sleepest_master.*
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler

import com.doitstudio.sleepest_master.storage.datastorage.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalTime

class DataStoreRepository(context: Context) {

    /**
     * Companion object is used for static fields in kotlin
     */
    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: DataStoreRepository? = null

        /**
         * This should be used to create or get the actual instance of the [DataStoreRepository] class
         */
        fun getRepo(context: Context): DataStoreRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = DataStoreRepository(context)
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

    /**
     *  Sets all data to default values. Its triggered by the "Delete Data" button in the settings fragment
     */
    suspend fun deleteAllData(){
        activityApiDataStatus.loadDefault()
        liveUserSleepActivityStatus.loadDefault()
        backgroundServiceStatus.loadDefault()
        settingsDataStatus.loadDefault()
        sleepApiDataStatus.loadDefault()
        sleepParameterStatus.loadDefault()

        updateRestartApp(true)
        updateAfterRestartApp(true)
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
    suspend fun isInSleepTime(givenTime:LocalTime? = null): Boolean {

        var times = sleepParameterFlow.first()

        val time = givenTime ?: LocalTime.now()
        val maxTime = (24*60*60) + 1
        val seconds = time.toSecondOfDay()

        val overTwoDays = times.sleepTimeStart > times.sleepTimeEnd

        return ((overTwoDays && (seconds in times.sleepTimeStart..maxTime ||  seconds in 0 .. times.sleepTimeEnd)) || (!overTwoDays && seconds in times.sleepTimeStart..times.sleepTimeEnd))
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

    suspend fun updateAutoSleepTime(time:Boolean) =
            sleepParameterStatus.updateAutoSleepTime(time)
    suspend fun updateSleepTimeEnd(time:Int){
        val day = 24*60*60
        var newTime = time
        if(time > day)
            newTime -= day

        if(time < 0)
            newTime += day
        sleepParameterStatus.updateSleepTimeEnd(newTime)

    }
    suspend fun updateSleepTimeStart(time:Int){
        val day = 24*60*60
        var newTime = time
        if(time > day)
            newTime -= day

        if(time < 0)
            newTime += day
        sleepParameterStatus.updateSleepTimeStart(newTime)
    }
    suspend fun updateUserWantedSleepTime(time:Int) =
        sleepParameterStatus.updateUserWantedSleepTime(time)
    suspend fun updateStandardMobilePosition(time:Int) =
        sleepParameterStatus.updateStandardMobilePosition(time)
    suspend fun updateLigthCondition(time:Int) =
        sleepParameterStatus.updateLigthCondition(time)
    suspend fun updateUserMobileFequency(time:Int) =
        sleepParameterStatus.updateUserMobileFequency(time)
    suspend fun triggerObserver() =
        sleepParameterStatus.triggerObserver()

    //endregion


    //region Alarm Status

    private val alarmParameterStatus by lazy{ AlarmParameterStatus(context.createDataStore(
        ALARM_PARAMETER_STATUS,
        serializer = AlarmParameterSerializer())
    )
    }

    val alarmParameterFlow: Flow<AlarmParameters> = alarmParameterStatus.alarmParameters

    fun getAlarmArtJob() : Int = runBlocking{
        return@runBlocking getAlarmArt()
    }

    fun getAlarmToneJob() : String = runBlocking{
        return@runBlocking getAlarmTone()
    }

    private suspend fun getAlarmArt() : Int {
        return alarmParameterFlow.first().alarmArt
    }
    private suspend fun getAlarmTone() : String {
        return alarmParameterFlow.first().alarmTone
    }

    suspend fun updateEndAlarmAfterFired(value:Boolean) =
        alarmParameterStatus.updateEndAlarmAfterFired(value)
    suspend fun updateAlarmArt(value:Int) =
        alarmParameterStatus.updateAlarmArt(value)
    suspend fun updateAlarmTone(value:String) =
        alarmParameterStatus.updateAlarmTone(value)
    suspend fun updateAlarmName(value:String) =
        alarmParameterStatus.updateAlarmName(value)

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

    //region Settings Status

    private val settingsDataStatus by lazy{ SettingsStatus(context.createDataStore(
            SETTINGS_STATUS_NAME,
            serializer = SettingsDataSerializer())
    )
    }


    val settingsDataFlow: Flow<SettingsData> = settingsDataStatus.settingsData

    suspend fun updateBannerShowAlarmActiv(isActive:Boolean) =
        settingsDataStatus.updateBannerShowAlarmActiv(isActive)
    suspend fun updateBannerShowActualWakeUpPoint(isActive:Boolean) =
        settingsDataStatus.updateBannerShowActualWakeUpPoint(isActive)
    suspend fun updateBannerShowActualSleepTime(isActive:Boolean) =
            settingsDataStatus.updateBannerShowActualSleepTime(isActive)
    suspend fun updateBannerShowSleepState(isActive:Boolean) =
                    settingsDataStatus.updateBannerShowSleepState(isActive)
    suspend fun updateAutoDarkMode(isActive:Boolean) =
            settingsDataStatus.updateAutoDarkMode(isActive)
    suspend fun updateAutoDarkModeAckn(isActive:Boolean) =
        settingsDataStatus.updateAutoDarkModeAckn(isActive)
    suspend fun updateDarkMode(isActive:Boolean) =
            settingsDataStatus.updateDarkMode(isActive)

    suspend fun updateRestartApp(isActive:Boolean) =
            settingsDataStatus.updateRestartApp(isActive)

    suspend fun updateAfterRestartApp(isActive:Boolean) =
            settingsDataStatus.updateAfterRestartApp(isActive)

    suspend fun updatePermissionSleepActivity(isActive:Boolean) =
            settingsDataStatus.updatePermissionSleepActivity(isActive)

    suspend fun updatePermissionDailyActivity(isActive:Boolean) =
            settingsDataStatus.updatePermissionDailyActivity(isActive)


    //endregion

    //region ActivityApiData Status

    private val activityApiDataStatus by lazy{ ActivityApiDataStatus(context.createDataStore(
            ACTIVITY_API_DATA_NAME,
            serializer = ActivityApiDataSerializer())
    )
    }


    val activityApiDataFlow: Flow<ActivityApiData> = activityApiDataStatus.activityApiData

    suspend fun getActivitySubscribeStatus() : Boolean {
        return activityApiDataStatus.activityApiData.first().isSubscribed }

    suspend fun updateActivityIsSubscribed(isActive:Boolean) =
            activityApiDataStatus.updateIsSubscribed(isActive)

    suspend fun updateActivityPermissionActive(isActive:Boolean) =
            activityApiDataStatus.updatePermissionActive(isActive)

    suspend fun updateActivityPermissionRemovedError(isActive:Boolean) =
            activityApiDataStatus.updatePermissionRemovedError(isActive)

    suspend fun updateActivitySubscribeFailed(isActive:Boolean) =
            activityApiDataStatus.updateSubscribeFailed(isActive)

    suspend fun updateActivityUnsubscribeFailed(isActive:Boolean) =
            activityApiDataStatus.updateUnsubscribeFailed(isActive)

    suspend fun updateActivityApiValuesAmount(amount:Int) =
            activityApiDataStatus.updateActivityApiValuesAmount(amount)

    suspend fun resetActivityApiValuesAmount() =
            activityApiDataStatus.resetActivityApiValuesAmount()


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

    //region Background Status

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

    //endregion

}
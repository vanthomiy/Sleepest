package com.sleepestapp.sleepest.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.sleepestapp.sleepest.storage.datastorage.TUTORIAL_STATUS_NAME
import com.sleepestapp.sleepest.storage.datastorage.TutorialStatus
import com.sleepestapp.sleepest.*
import com.sleepestapp.sleepest.model.data.Constants.DAY_IN_SECONDS
import com.sleepestapp.sleepest.storage.datastorage.*
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil.getActualAlarmTimeData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
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
    suspend fun deleteAllData() {
        activityApiDataStatus.loadDefault()
        liveUserSleepActivityStatus.loadDefault()
        backgroundServiceStatus.loadDefault()
        settingsDataStatus.loadDefault()
        sleepApiDataStatus.loadDefault()
        sleepParameterStatus.loadDefault()
        tutorialStatus.loadDefault()

        updateRestartApp(true)
        updateAfterRestartApp(true)
    }

    //region SleepParameter Status

    val Context.sleepParameterDataStore: DataStore<SleepParameters> by dataStore(
        fileName = SLEEP_PARAMETER_STATUS,
        serializer = SleepParameterSerializer
    )

    /**
     * Sleep parameter status
     */
    private val sleepParameterStatus by lazy {
        SleepParameterStatus(context.sleepParameterDataStore)
    }


    /**
     * Sleep parameter flow
     */
    val sleepParameterFlow: Flow<SleepParameters> =
        sleepParameterStatus.sleepParameters.distinctUntilChanged()

    /**
     * Returns if the time is in actual sleep time
     */
    @Deprecated("Dont use this crap anymore",  ReplaceWith("getActualAlarmTimeData(timestamp)"))
    suspend fun isInSleepTime(givenTime: LocalTime? = null): Boolean {

        val times = sleepParameterFlow.first()

        val time = givenTime ?: LocalTime.now()
        val maxTime = DAY_IN_SECONDS + 1
        val seconds = time.toSecondOfDay()

        val overTwoDays = times.sleepTimeStart > times.sleepTimeEnd

        return ((overTwoDays && (seconds in times.sleepTimeStart..maxTime || seconds in 0..times.sleepTimeEnd)) || (!overTwoDays && seconds in times.sleepTimeStart..times.sleepTimeEnd))
    }

    /**
     * Helper function to call [getSleepTimeBegin] from Java code
     */
    fun getSleepTimeBeginJob(): Int = runBlocking {
        return@runBlocking getSleepTimeBegin()
    }

    /**
     * Helper function to call [getSleepTimeEnd] from Java code
     */
    fun getSleepTimeEndJob(): Int = runBlocking {
        return@runBlocking getSleepTimeEnd()
    }

    /**
     * Returns the sleep duration of the user-defined sleep parameters
     */
    fun getSleepDurationJob(): Int = runBlocking {
        return@runBlocking getSleepDuration()
    }

    /**
     * Returns the sleep duration of the user-defined sleep parameters
     */
    suspend fun getSleepDuration(): Int {
        return sleepParameterFlow.first().sleepDuration
    }

    /**
     * Returns the sleep start of the user-defined sleep parameters
     */
    suspend fun getSleepTimeBegin(): Int {
        return sleepParameterFlow.first().sleepTimeStart
    }

    /**
     * Returns the sleep end of the user-defined sleep parameters
     */
    suspend fun getSleepTimeEnd(): Int {
        return sleepParameterFlow.first().sleepTimeEnd
    }

    /**
     * Returns the sleep start of the user-defined sleep parameters
     */
    suspend fun getSleepTimeStart(): Int {
        return sleepParameterFlow.first().sleepTimeStart
    }

    /**
     * Updates the activity tracking of the user-defined sleep parameters
     */
    suspend fun updateActivityTracking(value: Boolean): Unit =
        sleepParameterStatus.updateActivityTracking(value)

    /**
     * Updates the activity in calculation of the user-defined sleep parameters
     */
    suspend fun updateActivityInCalculation(value: Boolean): Unit =
        sleepParameterStatus.updateActivityInCalculation(value)

    /**
     * Updates the auto sleep time of the user-defined sleep parameters
     */
    suspend fun updateAutoSleepTime(time: Boolean): Unit =
        sleepParameterStatus.updateAutoSleepTime(time)

    /**
     * Updates the sleep time end of the user-defined sleep parameters
     */
    suspend fun updateSleepTimeEnd(time: Int) {
        var newTime = time
        if (time > DAY_IN_SECONDS)
            newTime -= DAY_IN_SECONDS

        if (time < 0)
            newTime += DAY_IN_SECONDS
        sleepParameterStatus.updateSleepTimeEnd(newTime)
    }

    /**
     * Updates the sleep time start of the user-defined sleep parameters
     */
    suspend fun updateSleepTimeStart(time: Int) {
        var newTime = time
        if (time > DAY_IN_SECONDS)
            newTime -= DAY_IN_SECONDS

        if (time < 0)
            newTime += DAY_IN_SECONDS
        sleepParameterStatus.updateSleepTimeStart(newTime)
    }

    /**
     * Updates the user wanted sleep time of the user-defined sleep parameters
     */
    suspend fun updateUserWantedSleepTime(time: Int): Unit =
        sleepParameterStatus.updateUserWantedSleepTime(time)

    /**
     * Updates the standard mobile position of the user-defined sleep parameters
     */
    suspend fun updateStandardMobilePosition(time: Int): Unit =
        sleepParameterStatus.updateStandardMobilePosition(time)

    /**
     * Updates the light condition of the user-defined sleep parameters
     */
    suspend fun updateLightCondition(time: Int): Unit =
        sleepParameterStatus.updateLigthCondition(time)

    /**
     * Updates the standard mobile position of the last week of the user-defined sleep parameters
     */
    suspend fun updateStandardMobilePositionOverLastWeek(time: Int): Unit =
        sleepParameterStatus.updateStandardMobilePositionOverLastWeek(time)

    /**
     * Updates the light condition of the last week of the user-defined sleep parameters
     */
    suspend fun updateLightConditionOverLastWeek(time: Int): Unit =
        sleepParameterStatus.updateLigthConditionOverLastWeek(time)

    /**
     * Updates the mobile use frequency of the user-defined sleep parameters
     */
    suspend fun updateUserMobileFrequency(time: Int): Unit =
        sleepParameterStatus.updateUserMobileFrequency(time)

    /**
     * Trigger this var to reload all data in some view models
     */
    suspend fun triggerSleepObserver(): Unit =
        sleepParameterStatus.triggerObserver()

    //endregion

    //region Alarm Status

    val Context.alarmsDataStore: DataStore<AlarmParameters> by dataStore(
        fileName = ALARM_PARAMETER_STATUS,
        serializer = AlarmParameterSerializer
    )

    /**
     * Alarm parameter status
     */
    private val alarmParameterStatus by lazy { AlarmParameterStatus(context.alarmsDataStore) }

    /**
     * Alarm parameter flow
     */
    val alarmParameterFlow: Flow<AlarmParameters> =
        alarmParameterStatus.alarmParameters.distinctUntilChanged()

    /**
     * Helper function to call [getAlarmType] from Java code
     */
    fun getAlarmArtJob(): Int = runBlocking {
        return@runBlocking getAlarmType()
    }

    /**
     * Helper function to call [getAlarmTone] from Java code
     */
    fun getAlarmToneJob(): String = runBlocking {
        return@runBlocking getAlarmTone()
    }

    /**
     * Returns the alarm type (vibration, sound or both)
     */
    private suspend fun getAlarmType(): Int {
        return alarmParameterFlow.first().alarmArt
    }

    /**
     * Returns the alarm sound
     */
    private suspend fun getAlarmTone(): String {
        return alarmParameterFlow.first().alarmTone
    }

    /**
     * Updates the alarm end after awake automatically
     */
    suspend fun updateEndAlarmAfterFired(value: Boolean): Unit =
        alarmParameterStatus.updateEndAlarmAfterFired(value)

    /**
     * Get properties to alarm end after awake automatically
     */
    suspend fun getEndAlarmAfterFired(): Boolean {
        return alarmParameterFlow.first().endAlarmAfterFired
    }

    /**
     * Updates the alarm type (vibration, sound or both)
     */
    suspend fun updateAlarmType(value: Int): Unit =
        alarmParameterStatus.updateAlarmType(value)

    /**
     * Updates the alarm sound
     */
    suspend fun updateAlarmTone(value: String): Unit =
        alarmParameterStatus.updateAlarmTone(value)

    /**
     * Updates the alarm name specified by the user
     */
    suspend fun updateAlarmName(value: String): Unit =
        alarmParameterStatus.updateAlarmName(value)

    /**
     * Trigger this var to reload all data in some view models
     */
    suspend fun triggerAlarmObserver(): Unit =
        alarmParameterStatus.triggerObserver()

    //endregion

    //region SleepApiData Status

    val Context.sleepApiDataStore: DataStore<SleepApiData> by dataStore(
        fileName = SLEEP_API_DATA_NAME,
        serializer = SleepApiDataSerializer
    )

    /**
     * Sleep api data status
     */
    private val sleepApiDataStatus by lazy { SleepApiDataStatus(context.sleepApiDataStore) }

    /**
     * Sleep api data flow
     */
    val sleepApiDataFlow: Flow<SleepApiData> =
        sleepApiDataStatus.sleepApiData.distinctUntilChanged()

    /**
     * Returns sleep api subscription status
     */
    suspend fun getSleepSubscribeStatus(): Boolean {
        return sleepApiDataStatus.sleepApiData.first().isSubscribed
    }

    /**
     * Update sleep subscription status
     */
    fun updateSleepIsSubscribed(isActive: Boolean): Unit = runBlocking {
        sleepApiDataStatus.updateIsSubscribed(isActive)
    }

    /**
     * Update sleep permission active
     */
    suspend fun updateSleepPermissionActive(isActive: Boolean): Unit =
        sleepApiDataStatus.updatePermissionActive(isActive)

    /**
     * Update sleep permission removed error
     */
    suspend fun updateSleepPermissionRemovedError(isActive: Boolean): Unit =
        sleepApiDataStatus.updatePermissionRemovedError(isActive)

    /**
     * Update sleep subscription failed
     */
    suspend fun updateSleepSubscribeFailed(isActive: Boolean): Unit =
        sleepApiDataStatus.updateSubscribeFailed(isActive)

    /**
     * Update sleep un-subscription failed
     */
    suspend fun updateSleepUnsubscribeFailed(isActive: Boolean): Unit =
        sleepApiDataStatus.updateUnsubscribeFailed(isActive)

    /**
     * Update sleep aou values amount
     */
    suspend fun updateSleepSleepApiValuesAmount(amount: Int): Unit =
        sleepApiDataStatus.updateSleepApiValuesAmount(amount)


    //endregion

    //region Settings Status

    val Context.settingsDataStore: DataStore<SettingsData> by dataStore(
        fileName = SETTINGS_STATUS_NAME,
        serializer = SettingsDataSerializer
    )

    /**
     * Settings status
     */
    private val settingsDataStatus by lazy { SettingsStatus(context.settingsDataStore) }

    /**
     * Settings flow
     */
    val settingsDataFlow: Flow<SettingsData> =
        settingsDataStatus.settingsData.distinctUntilChanged()

    /**
     * Update settings banner show alarm active
     */
    suspend fun updateBannerShowAlarmActive(isActive: Boolean): Unit =
        settingsDataStatus.updateBannerShowAlarmActive(isActive)

    /**
     * Update settings banner show actual wake up point
     */
    suspend fun updateBannerShowActualWakeUpPoint(isActive: Boolean): Unit =
        settingsDataStatus.updateBannerShowActualWakeUpPoint(isActive)

    /**
     * Update settings banner show actual sleep time
     */
    suspend fun updateBannerShowActualSleepTime(isActive: Boolean): Unit =
        settingsDataStatus.updateBannerShowActualSleepTime(isActive)

    /**
     * Update settings banner show sleep state
     */
    suspend fun updateBannerShowSleepState(isActive: Boolean): Unit =
        settingsDataStatus.updateBannerShowSleepState(isActive)

    /**
     * Update settings auto dark mode
     */
    suspend fun updateAutoDarkMode(isActive: Boolean): Unit =
        settingsDataStatus.updateAutoDarkMode(isActive)

    /**
     * Update settings auto dark mode ok
     */
    suspend fun updateAutoDarkModeAcknowledge(isActive: Boolean): Unit =
        settingsDataStatus.updateAutoDarkModeAcknowledge(isActive)

    /**
     * Update settings dark mode
     */
    suspend fun updateDarkMode(isActive: Boolean): Unit =
        settingsDataStatus.updateDarkMode(isActive)

    /**
     * Update settings restart app
     */
    suspend fun updateRestartApp(isActive: Boolean): Unit =
        settingsDataStatus.updateRestartApp(isActive)

    /**
     * Update settings after restarted app
     */
    suspend fun updateAfterRestartApp(isActive: Boolean): Unit =
        settingsDataStatus.updateAfterRestartApp(isActive)

    /**
     * Update settings permission sleep activity
     */
    suspend fun updatePermissionSleepActivity(isActive: Boolean): Unit =
        settingsDataStatus.updatePermissionSleepActivity(isActive)

    /**
     * Update settings permission daily activity
     */
    suspend fun updatePermissionDailyActivity(isActive: Boolean): Unit =
        settingsDataStatus.updatePermissionDailyActivity(isActive)


    //endregion

    //region ActivityApiData Status

    val Context.activityApiDataStore: DataStore<ActivityApiData> by dataStore(
        fileName = ACTIVITY_API_DATA_NAME,
        serializer = ActivityApiDataSerializer
    )

    /**
     * Activity api status
     */
    private val activityApiDataStatus by lazy { ActivityApiDataStatus(context.activityApiDataStore) }

    /**
     * Activity api flow
     */
    val activityApiDataFlow: Flow<ActivityApiData> =
        activityApiDataStatus.activityApiData.distinctUntilChanged()

    /**
     * Returns activity api subscription status
     */
    suspend fun getActivitySubscribeStatus(): Boolean {
        return activityApiDataStatus.activityApiData.first().isSubscribed
    }

    /**
     * Update activity api subscription status
     */
    suspend fun updateActivityIsSubscribed(isActive: Boolean): Unit =
        activityApiDataStatus.updateIsSubscribed(isActive)

    /**
     * Update activity api permission active
     */
    suspend fun updateActivityPermissionActive(isActive: Boolean): Unit =
        activityApiDataStatus.updatePermissionActive(isActive)

    /**
     * Update activity api permission removed error
     */
    suspend fun updateActivityPermissionRemovedError(isActive: Boolean): Unit =
        activityApiDataStatus.updatePermissionRemovedError(isActive)

    /**
     * Update activity api subscription failed
     */
    suspend fun updateActivitySubscribeFailed(isActive: Boolean): Unit =
        activityApiDataStatus.updateSubscribeFailed(isActive)

    /**
     * Update activity api un-subscription failed
     */
    suspend fun updateActivityUnsubscribeFailed(isActive: Boolean): Unit =
        activityApiDataStatus.updateUnsubscribeFailed(isActive)

    /**
     * Update activity api values amount
     */
    suspend fun updateActivityApiValuesAmount(amount: Int): Unit =
        activityApiDataStatus.updateActivityApiValuesAmount(amount)

    /**
     * Reset activity api values amount
     */
    suspend fun resetActivityApiValuesAmount(): Unit =
        activityApiDataStatus.resetActivityApiValuesAmount()


    //endregion

    //region LiveUserSleepActivity Status

    val Context.liveUserSleepActivityDataStore: DataStore<LiveUserSleepActivity> by dataStore(
        fileName = LIVE_USER_ACTIVITY_DATA_NAME,
        serializer = LiveUserSleepActivitySerializer
    )

    /**
     * Live user sleep activity status
     */
    private val liveUserSleepActivityStatus by lazy { LiveUserSleepActivityStatus(context.liveUserSleepActivityDataStore) }

    /**
     * Live user sleep activity flow
     */
    val liveUserSleepActivityFlow: Flow<LiveUserSleepActivity> =
        liveUserSleepActivityStatus.liveUserSleepActivity.distinctUntilChanged()

    /**
     * Update live is user sleeping
     */
    suspend fun updateIsUserSleeping(isActive: Boolean): Unit =
        liveUserSleepActivityStatus.updateIsUserSleeping(isActive)

    /**
     * Update live is data available
     */
    suspend fun updateIsDataAvailable(isActive: Boolean): Unit =
        liveUserSleepActivityStatus.updateIsDataAvailable(isActive)

    /**
     * Update live user sleep time
     */
    suspend fun updateUserSleepTime(sleepTime: Int): Unit =
        liveUserSleepActivityStatus.updateUserSleepTime(sleepTime)
    //endregion

    //region Background Status

    val Context.backgroundDataStore: DataStore<BackgroundService> by dataStore(
        fileName = BACKGROUND_SERVICE_STATUS,
        serializer = BackgroundServiceSerializer
    )

    /**
     * Background service status
     */
    private val backgroundServiceStatus by lazy { BackgroundServiceStatus(context.backgroundDataStore) }

    /**
     * Background service flow
     */
    val backgroundServiceFlow: Flow<BackgroundService> =
        backgroundServiceStatus.backgroundService.distinctUntilChanged()

    /**
     * Update background service is active
     */
    suspend fun backgroundUpdateIsActive(value: Boolean): Unit =
        backgroundServiceStatus.updateIsActive(value)

    /**
     * Update background service should be active
     */
    suspend fun backgroundUpdateShouldBeActive(value: Boolean): Unit =
        backgroundServiceStatus.updateShouldBeActive(value)

    //endregion

    //region Tutorial

    val Context.tutorialDataStore: DataStore<Tutorial> by dataStore(
        fileName = TUTORIAL_STATUS_NAME,
        serializer = TutorialStatusSerializer
    )

    /**
     * Tutorial status
     */
    private val tutorialStatus by lazy { TutorialStatus(context.tutorialDataStore) }

    val tutorialStatusFlow: Flow<Tutorial> = tutorialStatus.tutorialData

    /**
     * Helper function to call [updateTutorialCompleted] from Java code
     */
    fun updateTutorialCompletedJob(value: Boolean): Unit = runBlocking {
        updateTutorialCompleted(value)
    }

    /**
     * Update tutorial completed
     */
    suspend fun updateTutorialCompleted(value: Boolean): Unit =
        tutorialStatus.updateTutorialCompleted(value)

    /**
     * Helper function to call [updateEnergyOptionsShown] from Java code
     */
    fun updateEnergyOptionsShownJob(value: Boolean): Unit = runBlocking {
        updateEnergyOptionsShown(value)
    }

    /**
     * Update energy options shown
     */
    suspend fun updateEnergyOptionsShown(value: Boolean): Unit =
        tutorialStatus.updateEnergyOptionsShown(value)

    /**
     * Helper function to call [tutorialStatusFlow] property from Java code
     */
    fun getTutorialCompletedJob(): Boolean = runBlocking {
        return@runBlocking tutorialStatusFlow.first().tutorialCompleted
    }

    //endregion

}
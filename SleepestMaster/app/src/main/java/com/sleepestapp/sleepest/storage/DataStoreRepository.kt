package com.sleepestapp.sleepest.storage

import android.content.Context
import androidx.datastore.createDataStore
import com.doitstudio.sleepest_master.storage.datastorage.TUTORIAL_STATUS_NAME
import com.doitstudio.sleepest_master.storage.datastorage.TutorialStatus
import com.sleepestapp.sleepest.*
import com.sleepestapp.sleepest.model.data.Constants.DAY_IN_SECONDS
import com.sleepestapp.sleepest.storage.datastorage.*
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
    suspend fun deleteAllData(){
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

    /**
     * Sleep parameter status
     */
    private val sleepParameterStatus by lazy{ SleepParameterStatus(context.createDataStore(
        SLEEP_PARAMETER_STATUS,
        serializer = SleepParameterSerializer())
    )
    }

    /**
     * Sleep parameter flow
     */
    val sleepParameterFlow: Flow<SleepParameters> = sleepParameterStatus.sleepParameters.distinctUntilChanged()


    /**
     * Returns if the time is in actual sleep time
     */
    suspend fun isInSleepTime(givenTime:LocalTime? = null): Boolean {

        var times = sleepParameterFlow.first()

        val time = givenTime ?: LocalTime.now()
        val maxTime = DAY_IN_SECONDS + 1
        val seconds = time.toSecondOfDay()

        val overTwoDays = times.sleepTimeStart > times.sleepTimeEnd

        return ((overTwoDays && (seconds in times.sleepTimeStart..maxTime ||  seconds in 0 .. times.sleepTimeEnd)) || (!overTwoDays && seconds in times.sleepTimeStart..times.sleepTimeEnd))
    }

    /**
     * Returns if the time is after the sleep time
     */
    suspend fun isAfterSleepTime(givenTime:LocalTime? = null): Pair<Boolean, Boolean> {

        var times = sleepParameterFlow.first()

        val time = givenTime ?: LocalTime.now()
        val maxTime = DAY_IN_SECONDS + 1
        val seconds = time.toSecondOfDay()

        val overTwoDays = times.sleepTimeStart > times.sleepTimeEnd
        val afterSleepTime = (overTwoDays && seconds > times.sleepTimeEnd && seconds < times.sleepTimeStart) ||
                (!overTwoDays && seconds > times.sleepTimeEnd)

        return Pair(afterSleepTime, overTwoDays)
    }

    /**
     * Helper function to call [getSleepTimeBegin] from Java code
     */
    fun getSleepTimeBeginJob() : Int = runBlocking{
        return@runBlocking getSleepTimeBegin()
    }

    /**
     * Helper function to call [getSleepTimeEnd] from Java code
     */
    fun getSleepTimeEndJob() : Int = runBlocking{
        return@runBlocking getSleepTimeEnd()
    }

    /**
     * Returns the sleep duration of the user-defined sleep parameters
     */
    fun getSleepDurationJob() : Int = runBlocking {
        return@runBlocking getSleepDuration()
    }

    /**
     * Returns the sleep duration of the user-defined sleep parameters
     */
    suspend fun getSleepDuration() : Int {
        return sleepParameterFlow.first().sleepDuration
    }

    /**
     * Returns the sleep start of the user-defined sleep parameters
     */
    suspend fun getSleepTimeBegin() : Int {
        return sleepParameterFlow.first().sleepTimeStart
    }

    /**
     * Returns the sleep end of the user-defined sleep parameters
     */
    suspend fun getSleepTimeEnd() : Int {
        return sleepParameterFlow.first().sleepTimeEnd
    }

    /**
     * Updates the activity tracking of the user-defined sleep parameters
     */
    suspend fun updateActivityTracking(value:Boolean) =
            sleepParameterStatus.updateActivityTracking(value)

    /**
     * Updates the activity in calculation of the user-defined sleep parameters
     */
    suspend fun updateActivityInCalculation(value:Boolean) =
            sleepParameterStatus.updateActivityInCalculation(value)

    /**
     * Updates the auto sleep time of the user-defined sleep parameters
     */
    suspend fun updateAutoSleepTime(time:Boolean) =
            sleepParameterStatus.updateAutoSleepTime(time)

    /**
     * Updates the sleep time end of the user-defined sleep parameters
     */
    suspend fun updateSleepTimeEnd(time:Int){
        var newTime = time
        if(time > DAY_IN_SECONDS)
            newTime -= DAY_IN_SECONDS

        if(time < 0)
            newTime += DAY_IN_SECONDS
        sleepParameterStatus.updateSleepTimeEnd(newTime)

    }

    /**
     * Updates the sleep time start of the user-defined sleep parameters
     */
    suspend fun updateSleepTimeStart(time:Int){
        var newTime = time
        if(time > DAY_IN_SECONDS)
            newTime -= DAY_IN_SECONDS

        if(time < 0)
            newTime += DAY_IN_SECONDS
        sleepParameterStatus.updateSleepTimeStart(newTime)
    }

    /**
     * Updates the user wanted sleep time of the user-defined sleep parameters
     */
    suspend fun updateUserWantedSleepTime(time:Int) =
        sleepParameterStatus.updateUserWantedSleepTime(time)

    /**
     * Updates the standard mobile position of the user-defined sleep parameters
     */
    suspend fun updateStandardMobilePosition(time:Int) =
        sleepParameterStatus.updateStandardMobilePosition(time)

    /**
     * Updates the light condition of the user-defined sleep parameters
     */
    suspend fun updateLigthCondition(time:Int) =
        sleepParameterStatus.updateLigthCondition(time)

    /**
     * Updates the standard mobile position of the last week of the user-defined sleep parameters
     */
    suspend fun updateStandardMobilePositionOverLastWeek(time:Int) =
        sleepParameterStatus.updateStandardMobilePositionOverLastWeek(time)

    /**
     * Updates the light condition of the last week of the user-defined sleep parameters
     */
    suspend fun updateLigthConditionOverLastWeek(time:Int) =
        sleepParameterStatus.updateLigthConditionOverLastWeek(time)

    /**
     * Updates the mobile use frequency of the user-defined sleep parameters
     */
    suspend fun updateUserMobileFequency(time:Int) =
        sleepParameterStatus.updateUserMobileFrequency(time)

    /**
     * Trigger this var to reload all data in some view models
     */
    suspend fun triggerObserver() =
        sleepParameterStatus.triggerObserver()

    //endregion


    //region Alarm Status

    /**
     * Alarm parameter status
     */
    private val alarmParameterStatus by lazy{ AlarmParameterStatus(context.createDataStore(
        ALARM_PARAMETER_STATUS,
        serializer = AlarmParameterSerializer())
    )
    }

    /**
     * Alarm parameter flow
     */
    val alarmParameterFlow: Flow<AlarmParameters> = alarmParameterStatus.alarmParameters.distinctUntilChanged()

    /**
     * Helper function to call [getAlarmType] from Java code
     */
    fun getAlarmArtJob() : Int = runBlocking{
        return@runBlocking getAlarmType()
    }

    /**
     * Helper function to call [getAlarmTone] from Java code
     */
    fun getAlarmToneJob() : String = runBlocking{
        return@runBlocking getAlarmTone()
    }

    /**
     * Returns the alarm type (vibration, sound or both)
     */
    private suspend fun getAlarmType() : Int {
        return alarmParameterFlow.first().alarmArt
    }

    /**
     * Returns the alarm sound
     */
    private suspend fun getAlarmTone() : String {
        return alarmParameterFlow.first().alarmTone
    }

    /**
     * Updates the alarm end after awake automatically
     */
    suspend fun updateEndAlarmAfterFired(value:Boolean) =
        alarmParameterStatus.updateEndAlarmAfterFired(value)

    /**
     * Get propertie to alarm end after awake automatically
     */
    suspend fun getEndAlarmAfterFired() : Boolean {
        return alarmParameterFlow.first().endAlarmAfterFired
    }

    /**
     * Updates the alarm type (vibration, sound or both)
     */
    suspend fun updateAlarmType(value:Int) =
        alarmParameterStatus.updateAlarmType(value)

    /**
     * Updates the alarm sound
     */
    suspend fun updateAlarmTone(value:String) =
        alarmParameterStatus.updateAlarmTone(value)

    /**
     * Updates the alarm name specified by the user
     */
    suspend fun updateAlarmName(value:String) =
        alarmParameterStatus.updateAlarmName(value)

    //endregion

    //region SleepApiData Status

    /**
     * Sleep api data status
     */
    private val sleepApiDataStatus by lazy{ SleepApiDataStatus(context.createDataStore(
            SLEEP_API_DATA_NAME,
            serializer = SleepApiDataSerializer())
    )
    }

    /**
     * Sleep api data flow
     */
    val sleepApiDataFlow: Flow<SleepApiData> = sleepApiDataStatus.sleepApiData.distinctUntilChanged()

    /**
     * Returns sleep api subscription status
     */
    suspend fun getSleepSubscribeStatus() : Boolean {
        return sleepApiDataStatus.sleepApiData.first().isSubscribed }

    /**
     * Update sleep subscription status
     */
    fun updateSleepIsSubscribed(isActive:Boolean) = runBlocking {
        sleepApiDataStatus.updateIsSubscribed(isActive)
    }

    /**
     * Update sleep permission active
     */
    suspend fun updateSleepPermissionActive(isActive:Boolean) =
            sleepApiDataStatus.updatePermissionActive(isActive)

    /**
     * Update sleep permission removed error
     */
    suspend fun updateSleepPermissionRemovedError(isActive:Boolean) =
            sleepApiDataStatus.updatePermissionRemovedError(isActive)

    /**
     * Update sleep subscription failed
     */
    suspend fun updateSleepSubscribeFailed(isActive:Boolean) =
            sleepApiDataStatus.updateSubscribeFailed(isActive)

    /**
     * Update sleep un-subscription failed
     */
    suspend fun updateSleepUnsubscribeFailed(isActive:Boolean) =
            sleepApiDataStatus.updateUnsubscribeFailed(isActive)

    /**
     * Update sleep aou values amount
     */
    suspend fun updateSleepSleepApiValuesAmount(amount:Int) =
            sleepApiDataStatus.updateSleepApiValuesAmount(amount)

    /**
     * Reset sleep api values amount
     */
    suspend fun resetSleepApiValuesAmount() =
        sleepApiDataStatus.resetSleepApiValuesAmount()


    //endregion

    //region Settings Status

    /**
     * Settings status
     */
    private val settingsDataStatus by lazy{ SettingsStatus(context.createDataStore(
            SETTINGS_STATUS_NAME,
            serializer = SettingsDataSerializer())
    )
    }

    /**
     * Settings flow
     */
    val settingsDataFlow: Flow<SettingsData> = settingsDataStatus.settingsData.distinctUntilChanged()

    /**
     * Update settings banner show alarm active
     */
    suspend fun updateBannerShowAlarmActiv(isActive:Boolean) =
        settingsDataStatus.updateBannerShowAlarmActiv(isActive)

    /**
     * Update settings banner show actual wake up point
     */
    suspend fun updateBannerShowActualWakeUpPoint(isActive:Boolean) =
        settingsDataStatus.updateBannerShowActualWakeUpPoint(isActive)

    /**
     * Update settings banner show actual sleep time
     */
    suspend fun updateBannerShowActualSleepTime(isActive:Boolean) =
            settingsDataStatus.updateBannerShowActualSleepTime(isActive)

    /**
     * Update settings banner show sleep state
     */
    suspend fun updateBannerShowSleepState(isActive:Boolean) =
                    settingsDataStatus.updateBannerShowSleepState(isActive)

    /**
     * Update settings auto dark mode
     */
    suspend fun updateAutoDarkMode(isActive:Boolean) =
            settingsDataStatus.updateAutoDarkMode(isActive)

    /**
     * Update settings auto dark mode ok
     */
    suspend fun updateAutoDarkModeAckn(isActive:Boolean) =
        settingsDataStatus.updateAutoDarkModeAckn(isActive)

    /**
     * Update settings darke mode
     */
    suspend fun updateDarkMode(isActive:Boolean) =
            settingsDataStatus.updateDarkMode(isActive)

    /**
     * Update settings restart app
     */
    suspend fun updateRestartApp(isActive:Boolean) =
            settingsDataStatus.updateRestartApp(isActive)

    /**
     * Update settings after restarted app
     */
    suspend fun updateAfterRestartApp(isActive:Boolean) =
            settingsDataStatus.updateAfterRestartApp(isActive)

    /**
     * Update settings permission sleep activity
     */
    suspend fun updatePermissionSleepActivity(isActive:Boolean) =
            settingsDataStatus.updatePermissionSleepActivity(isActive)

    /**
     * Update settings permission daily activity
     */
    suspend fun updatePermissionDailyActivity(isActive:Boolean) =
            settingsDataStatus.updatePermissionDailyActivity(isActive)


    //endregion

    //region ActivityApiData Status

    /**
     * Activity api status
     */
    private val activityApiDataStatus by lazy{ ActivityApiDataStatus(context.createDataStore(
            ACTIVITY_API_DATA_NAME,
            serializer = ActivityApiDataSerializer())
    )
    }

    /**
     * Activity api flow
     */
    val activityApiDataFlow: Flow<ActivityApiData> = activityApiDataStatus.activityApiData.distinctUntilChanged()

    /**
     * Returns activity api subscription status
     */
    suspend fun getActivitySubscribeStatus() : Boolean {
        return activityApiDataStatus.activityApiData.first().isSubscribed }

    /**
     * Update activity api subscription status
     */
    suspend fun updateActivityIsSubscribed(isActive:Boolean) =
            activityApiDataStatus.updateIsSubscribed(isActive)

    /**
     * Update activity api permission active
     */
    suspend fun updateActivityPermissionActive(isActive:Boolean) =
            activityApiDataStatus.updatePermissionActive(isActive)

    /**
     * Update activity api permission removed error
     */
    suspend fun updateActivityPermissionRemovedError(isActive:Boolean) =
            activityApiDataStatus.updatePermissionRemovedError(isActive)

    /**
     * Update activity api subscription failed
     */
    suspend fun updateActivitySubscribeFailed(isActive:Boolean) =
            activityApiDataStatus.updateSubscribeFailed(isActive)

    /**
     * Update activity api un-subscription failed
     */
    suspend fun updateActivityUnsubscribeFailed(isActive:Boolean) =
            activityApiDataStatus.updateUnsubscribeFailed(isActive)

    /**
     * Update activity api values amount
     */
    suspend fun updateActivityApiValuesAmount(amount:Int) =
            activityApiDataStatus.updateActivityApiValuesAmount(amount)

    /**
     * Reset activity api values amount
     */
    suspend fun resetActivityApiValuesAmount() =
            activityApiDataStatus.resetActivityApiValuesAmount()


    //endregion

    //region LiveUserSleepActivity Status

    /**
     * Live user sleep activity status
     */
    private val liveUserSleepActivityStatus by lazy{ LiveUserSleepActivityStatus(context.createDataStore(
        LIVE_USER_ACTIVITY_DATA_NAME,
        serializer = LiveUserSleepActivitySerializer())
    )
    }

    /**
     * Live user sleep activity flow
     */
    val liveUserSleepActivityFlow: Flow<LiveUserSleepActivity> = liveUserSleepActivityStatus.liveUserSleepActivity.distinctUntilChanged()

    /**
     * Update live is user sleeping
     */
    suspend fun updateIsUserSleeping(isActive:Boolean) =
        liveUserSleepActivityStatus.updateIsUserSleeping(isActive)

    /**
     * Update live is data available
     */
    suspend fun updateIsDataAvailable(isActive:Boolean) =
        liveUserSleepActivityStatus.updateIsDataAvailable(isActive)

    /**
     * Update live user sleep time
     */
    suspend fun updateUserSleepTime(sleepTime:Int) =
        liveUserSleepActivityStatus.updateUserSleepTime(sleepTime)
    //endregion

    //region Background Status

    /**
     * Background service status
     */
    private val backgroundServiceStatus by lazy{ BackgroundServiceStatus(context.createDataStore(
        BACKGROUND_SERVICE_STATUS,
        serializer = BackgroundServiceSerializer())
    )
    }

    /**
     * Background service flow
     */
    val backgroundServiceFlow: Flow<BackgroundService> = backgroundServiceStatus.backgroundService.distinctUntilChanged()

    /**
     * Update background service is active
     */
    suspend fun backgroundUpdateIsActive(value:Boolean) =
        backgroundServiceStatus.updateIsActive(value)

    /**
     * Update background service should be active
     */
    suspend fun backgroundUpdateShouldBeActive(value:Boolean) =
        backgroundServiceStatus.updateShouldBeActive(value)

    //endregion

    //region Tutorial

    /**
     * Tutorial status
     */
    private val tutorialStatus by lazy{ TutorialStatus(context.createDataStore(
        TUTORIAL_STATUS_NAME,
        serializer = TutorialStatusSerializer())
    )
    }

    val tutorialStatusFlow: Flow<Tutorial> = tutorialStatus.tutorialData

    /**
     * Helper function to call [updateTutorialCompleted] from Java code
     */
    fun updateTutorialCompletedJob(value:Boolean) = runBlocking {
        updateTutorialCompleted(value)
    }

    /**
     * Update tutorial completed
     */
    suspend fun updateTutorialCompleted(value:Boolean) =
        tutorialStatus.updateTutorialCompleted(value)

    /**
     * Helper function to call [updateEnergyOptionsShown] from Java code
     */
    fun updateEnergyOptionsShownJob(value:Boolean) = runBlocking {
        updateEnergyOptionsShown(value)
    }

    /**
     * Update energy options shown
     */
    suspend fun updateEnergyOptionsShown(value:Boolean) =
        tutorialStatus.updateEnergyOptionsShown(value)

    /**
     * Helper function to call [tutorialStatusFlow] property from Java code
     */
    fun getTutorialCompletedJob() : Boolean = runBlocking{
        return@runBlocking tutorialStatusFlow.first().tutorialCompleted
    }

    //endregion

}
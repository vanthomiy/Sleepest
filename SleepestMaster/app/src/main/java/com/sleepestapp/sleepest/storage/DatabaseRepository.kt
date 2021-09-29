package com.sleepestapp.sleepest.storage


import com.sleepestapp.sleepest.model.data.MoodType
import com.sleepestapp.sleepest.model.data.Constants
import com.sleepestapp.sleepest.model.data.SleepState
import com.sleepestapp.sleepest.storage.db.UserSleepSessionDao
import com.sleepestapp.sleepest.storage.db.UserSleepSessionEntity
import com.sleepestapp.sleepest.storage.db.*
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil.getActiveAlarms
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.*
import java.util.*
import kotlin.collections.ArrayList


/**
 * This contains the interface for each SQL-Database and for DataStore.
 * ROOM API for SQL Database is used for storing large datasets like [SleepApiRawDataEntity]
 * DataStore is used for storing single classes or single values like {later} (Containing Alarm Time and Alarm Active etc.) and other key values.
 * More information about DataStore @see [link](https://developer.android.com/topic/libraries/architecture/datastore) and about ROOM SQL @see [link](https://developer.android.com/training/data-storage/room/#kotlin).
 *
 */
class DatabaseRepository(
    private val sleepApiRawDataDao: SleepApiRawDataDao,
    private val userSleepSessionDao: UserSleepSessionDao,
    private val alarmDao: AlarmDao,
    private val activityApiRawDataDao: ActivityApiRawDataDao

    ) {

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: DatabaseRepository? = null

        /**
         * Get the actual [DatabaseRepository]. Should be used to provide singleton behaviour
         */
        fun getRepo(
            sleepApiRawDataDao: SleepApiRawDataDao,
            userSleepSessionDao: UserSleepSessionDao,
            alarmDao: AlarmDao,
            activityApiRawDataDao: ActivityApiRawDataDao
        ): DatabaseRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = DatabaseRepository(sleepApiRawDataDao, userSleepSessionDao, alarmDao, activityApiRawDataDao)
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

    /**
    // Link to the documentation https://developer.android.com/training/data-storage/room/#kotlin

    // Why Suspend!
    // By default Room runs suspend queries off the main thread. Therefore, we don't need to
    // implement anything else to ensure we're not doing long-running database work off the
    // main thread.
    */

    //region Sleep API Data

    // Methods for SleepApiRawDataDao
    // Observed Flow will notify the observer when the data has changed.
    val allSleepApiRawData: Flow<List<SleepApiRawDataEntity>?> =
        sleepApiRawDataDao.getAll().distinctUntilChanged()

    /**
     * [time] the duration in seconds eg. 86200 would be from 24hours ago to now the data
     */
    fun getSleepApiRawDataSince(time:Int): Flow<List<SleepApiRawDataEntity>?>
    {
        val now = LocalDateTime.now(ZoneOffset.systemDefault())
        val seconds = now.atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
        return sleepApiRawDataDao.getSince(seconds-time).distinctUntilChanged()
    }

    /**
     * Gets the sleep api data from a specific state from a date in life time.
     * so we always getting the data from 15:00 the day or day before until the specific time
     * later we have to combine it with the actual sleep times
     */
    fun getSleepApiRawDataFromDateLive(actualTime:LocalDateTime, endTimeSecondsOfDay:Int): Flow<List<SleepApiRawDataEntity>?>
    {

        val endTime = LocalTime.ofSecondOfDay(endTimeSecondsOfDay.toLong())

        // We need to check wheter the time is before or after sleep time end.
        // Then we can decide if its this or the next day
        val startDateTime = if (actualTime.hour < endTime.hour)
            actualTime.toLocalDate().minusDays(1).atTime(endTime).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
        else actualTime.toLocalDate().atTime(endTime).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()

        val endDateTime = actualTime.atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()

        return sleepApiRawDataDao.getBetween(startDateTime,endDateTime).distinctUntilChanged()
    }

    /**
     * Gets the sleep api data from a specific state from a date
     * e.g. the dateTime 20.05.2021 at 20:00 returns all data in between 20.05.2021 15:00 to 21.05.2021 at 15:00
     * later we have to combine it with the actual sleep times
     */
    fun getSleepApiRawDataFromDate(actualDateTime:LocalDateTime, endTimeSecondsOfDay:Int, startTimeSecondsOfDay:Int): Flow<List<SleepApiRawDataEntity>?>
    {

        val startTime = LocalTime.ofSecondOfDay(startTimeSecondsOfDay.toLong())
        val endTime = LocalTime.ofSecondOfDay(endTimeSecondsOfDay.toLong())

        val sameDay = (startTime < endTime)

        // we are in the sleep time

        // We need to check whether the time is before or after sleep time end.
        // Then we can decide if its this or the next day

        val startDateTime = if(sameDay && actualDateTime.toLocalTime() < endTime)
            actualDateTime.toLocalDate().atTime(startTime.hour,startTime.minute).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
        else if(sameDay && actualDateTime.toLocalTime() > endTime)
            actualDateTime.toLocalDate().plusDays(1).atTime(startTime.hour,startTime.minute).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
        else if(!sameDay && actualDateTime.toLocalTime() > startTime)
            actualDateTime.toLocalDate().atTime(startTime.hour,startTime.minute).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
        else if(!sameDay && actualDateTime.toLocalTime() < endTime)
            actualDateTime.toLocalDate().minusDays(1).atTime(startTime.hour,startTime.minute).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
        else
            actualDateTime.toLocalDate().atTime(startTime.hour,startTime.minute).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()

        val endDateTime = if(sameDay && actualDateTime.toLocalTime() < endTime)
            actualDateTime.toLocalDate().atTime(endTime.hour,endTime.minute).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
        else if(sameDay && actualDateTime.toLocalTime() > endTime)
            actualDateTime.toLocalDate().plusDays(1).atTime(endTime.hour,endTime.minute).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
        else if(!sameDay && actualDateTime.toLocalTime() > startTime)
            actualDateTime.toLocalDate().plusDays(1).atTime(endTime.hour,endTime.minute).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
        else if(!sameDay && actualDateTime.toLocalTime() < endTime)
            actualDateTime.toLocalDate().atTime(endTime.hour,endTime.minute).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
        else
            actualDateTime.toLocalDate().plusDays(1).atTime(endTime.hour,endTime.minute).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()

        return sleepApiRawDataDao.getBetween(startDateTime,endDateTime).distinctUntilChanged()
    }

    /**
     * Gets the sleep api data between to timestamps (UTC seconds)
     */
    fun getSleepApiRawDataBetweenTimestamps(startTime: Int, endTime: Int): Flow<List<SleepApiRawDataEntity>?>
    {
        return sleepApiRawDataDao.getBetween(startTime, endTime).distinctUntilChanged()
    }

    /**
     * Insert or update [SleepApiRawDataEntity]
     */
    suspend fun insertSleepApiRawData(sleepClassifyEventEntity: SleepApiRawDataEntity) {
        sleepApiRawDataDao.insert(sleepClassifyEventEntity)
    }

    /**
     * Delete all [SleepApiRawDataEntity]
     */
    suspend fun deleteSleepApiRawData() {
        sleepApiRawDataDao.deleteAll()
    }

    /**
     * Delete list of [SleepApiRawDataEntity]
     */
    suspend fun deleteSleepApiRawData(id:Int) {
        sleepApiRawDataDao.delete(id)
    }

    /**
     * Insert or update list of [SleepApiRawDataEntity]
     */
    suspend fun insertSleepApiRawData(sleepClassifyEventEntities: List<SleepApiRawDataEntity>) {
        sleepApiRawDataDao.insertAll(sleepClassifyEventEntities)
    }

    /**
     * Update [SleepState] of [SleepApiRawDataEntity]
     */
    suspend fun updateSleepApiRawDataSleepState(id: Int, sleepState: SleepState){
        sleepApiRawDataDao.updateSleepState(id,sleepState )
    }

    /**
     * Update old [SleepState] of [SleepApiRawDataEntity]
     */
    suspend fun updateOldSleepApiRawDataSleepState(id: Int, sleepState: SleepState){
        sleepApiRawDataDao.updateOldSleepState(id,sleepState )
    }

    /**
     * Update wakeup point of [SleepApiRawDataEntity]
     */
    suspend fun updateSleepApiRawDataWakeUp(id: Int, wakeup: Int){
        sleepApiRawDataDao.updateWakeUp(id,wakeup )
    }
    //endregion

    //region Activity API Data

    // Methods for ActivityApiRawDataDao
    // Observed Flow will notify the observer when the data has changed.
    val allActivityApiRawData: Flow<List<ActivityApiRawDataEntity>> =
            activityApiRawDataDao.getAll().distinctUntilChanged()

    /**
     * [time] the duration in seconds eg. 86200 would be from 24hours ago to now the data
     */
    fun getActivityApiRawDataSince(time:Int): Flow<List<ActivityApiRawDataEntity>>
    {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val seconds = now.atZone(ZoneOffset.UTC).toEpochSecond().toInt()
        return activityApiRawDataDao.getSince(seconds-time).distinctUntilChanged()
    }


    /**
     * Gets the activity api data from a specific state from a date in life time.
     * so we always getting the data from 15:00 the day or day before until the specific time
     * later we have to combine it with the actual activity times
     */
    fun getActivityApiRawDataFromDateLive(actualTime:LocalDateTime, startTimeSecondsOfDay:Int): Flow<List<ActivityApiRawDataEntity>>
    {
        val startTime = LocalTime.ofSecondOfDay(startTimeSecondsOfDay.toLong())

        // We need to check wheter the time is before or after sleep time end.
        // Then we can decide if its this or the next day

        val startDateTime = if (actualTime.hour < startTime.hour)
            actualTime.toLocalDate().minusDays(1).atTime(startTime).atZone(ZoneOffset.UTC).toEpochSecond().toInt()
        else actualTime.toLocalDate().atTime(startTime).atZone(ZoneOffset.UTC).toEpochSecond().toInt()

        val endDateTime = actualTime.atZone(ZoneOffset.UTC).toEpochSecond().toInt()

        return activityApiRawDataDao.getBetween(startDateTime,endDateTime).distinctUntilChanged()
    }

    /**
     * Gets the activity api data from a specific state from a date
     * e.g. the dateTime 20.05.2021 at 20:00 returns all data in between 20.05.2021 15:00 to 21.05.2021 at 15:00
     * later we have to combine it with the actual activity times
     */
    fun getActivityApiRawDataFromDate(actualTime:LocalDateTime): Flow<List<ActivityApiRawDataEntity>>
    {
        val startTime = actualTime.toLocalDate().minusDays(1).atTime(0,0).atZone(ZoneOffset.UTC).toEpochSecond().toInt()
        val endTime = actualTime.toLocalDate().minusDays(1).atTime(23,59).atZone(ZoneOffset.UTC).toEpochSecond().toInt()

        return activityApiRawDataDao.getBetween(startTime,endTime).distinctUntilChanged()
    }

    /**
     * Insert or update [ActivityApiRawDataEntity]
     */
    suspend fun insertActivityApiRawData(activityClassifyEventEntity: ActivityApiRawDataEntity) {
        activityApiRawDataDao.insert(activityClassifyEventEntity)
    }

    /**
     * Delete all [ActivityApiRawDataEntity]
     */
    suspend fun deleteActivityApiRawData() {
        activityApiRawDataDao.deleteAll()
    }

    /**
     * Insert or update list of [ActivityApiRawDataEntity]
     */
    suspend fun insertActivityApiRawData(activityClassifyEventEntities: List<ActivityApiRawDataEntity>) {
        activityApiRawDataDao.insertAll(activityClassifyEventEntities)
    }

//endregion

    //region User Sleep Sessions

    // Methods for ActivityApiRawDataDao
    // Observed Flow will notify the observer when the data has changed.
    val allUserSleepSessions: Flow<List<UserSleepSessionEntity>> =
            userSleepSessionDao.getAll().distinctUntilChanged()

    /**
     * returns a specific [UserSleepSessionEntity] by its ID
     */
    fun getSleepSessionById(id: Int): Flow<List<UserSleepSessionEntity?>> =
            userSleepSessionDao.getById(id).distinctUntilChanged()

    /**
     * Returns True if a user session is available by id
     */
    suspend fun checkIfUserSessionIsDefinedById(id:Int): Boolean
    {
        return !userSleepSessionDao.getById(id).first().isNullOrEmpty()
    }

    /**
     * Returns a specific [UserSleepSessionEntity] by its ID
     * If not present, it creates a new session by this ID
     */
    suspend fun getOrCreateSleepSessionById(id: Int): UserSleepSessionEntity {

        val userSession = userSleepSessionDao.getById(id).first().firstOrNull()
        //val userSession = allData.firstOrNull { x -> x.id == id }

        if(userSession == null){
            val session  = UserSleepSessionEntity(id)
            insertUserSleepSession(session)
            return session

        }

        return userSession
    }

    /**
     * [days] the duration in seconds eg. 86200 would be from 24hours ago to now the data
     */
    fun getUserSleepSessionSinceDays(days:Long): Flow<List<UserSleepSessionEntity>>
    {
        val now = LocalDateTime.now(ZoneOffset.systemDefault()).minusDays(days)
        val seconds = now.atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
        return userSleepSessionDao.getSince(seconds).distinctUntilChanged()
    }

    /**
     * Inserts or updates a [UserSleepSessionEntity]
     */
    suspend fun insertUserSleepSession(userSleepSession: UserSleepSessionEntity) {
        userSleepSessionDao.insert(userSleepSession)
    }

    /**
     * Inserts or updates a list of [UserSleepSessionEntity]
     */
    suspend fun insertUserSleepSessions(userSleepSession: List<UserSleepSessionEntity>) {
        userSleepSessionDao.insertAll(userSleepSession)
    }

    /**
     * Deletes all [UserSleepSessionEntity]
     */
    suspend fun deleteAllUserSleepSessions() {
        userSleepSessionDao.deleteAll()
    }

    /**
     * Delete a specific [UserSleepSessionEntity]
     */
    suspend fun deleteUserSleepSession(sleepSessionEntity: UserSleepSessionEntity) {
        userSleepSessionDao.delete(sleepSessionEntity)
    }

    /**
     * Updates [MoodType] in a  [UserSleepSessionEntity]
     */
    suspend fun updateMoodAfterSleep(moodType: MoodType, sessionId: Int) {
        userSleepSessionDao.updateMoodAfterSleep(moodType, sessionId)
    }

    //endregion

    //region Alarm

    // Methods for Alarm
    // Observed Flow will notify the observer when the data has changed.
    val alarmFlow: Flow<List<AlarmEntity>> =
            alarmDao.getAll().distinctUntilChanged()


    /**
     * Returns an [AlarmEntity] by its ID
     */
    fun getAlarmById(alarmId: Int): Flow<AlarmEntity?> =
        alarmDao.getAlarmById(alarmId).distinctUntilChanged()

    /**
     * Workaround to call function from JAVA code
     * calls [getNextActiveAlarm]
     */
    fun getNextActiveAlarmJob(dataStoreRepository: DataStoreRepository) : AlarmEntity? = runBlocking{
        return@runBlocking getNextActiveAlarm(dataStoreRepository)
    }

    /**
     * Returns the next alarm that is active or null is no alarm is active in that time duration
     * Pass true/false if the actual time is in sleep time or not
     */
    suspend fun getNextActiveAlarm(dataStoreRepository: DataStoreRepository) : AlarmEntity?{

        val list = getActiveAlarms(alarmFlow.first(), dataStoreRepository)
        // get first alarm
        return list.minByOrNull { x-> x.wakeupEarly }
    }

    /**
     * Returns true or false wheter a alarm is active for the actual/next day or not
     * Pass true/false if the actual time is in sleep time or not
     */
    suspend fun isAlarmActiv(dataStoreRepository: DataStoreRepository) : Boolean{
        val list = getActiveAlarms(alarmFlow.first(), dataStoreRepository)
        // get first alarm
        return list.isNotEmpty()
    }

    /**
     * Inserts or updates a [AlarmEntity]
     */
    suspend fun insertAlarm(alarm: AlarmEntity) {
        alarmDao.insert(alarm)
    }

    /**
     * Updates alarm was fired by id of a [AlarmEntity]
     */
    suspend fun updateAlarmWasFired(alarmFired: Boolean, alarmId: Int) {
        alarmDao.updateAlarmWasFired(alarmFired, alarmId)
    }

    /**
     * Updates alarm was fired by id of a [AlarmEntity]
     */
    suspend fun updateAlreadyAwake(alreadyAwake: Boolean, alarmId: Int) {
        alarmDao.updateAlreadyAwake(alreadyAwake, alarmId)
    }

    /**
     * Workaround to call function from JAVA code
     * calls [updateAlarmTempDisabled]
     */
    fun updateAlarmTempDisabledJob(alarmDisabled: Boolean, alarmId: Int) = runBlocking {
        updateAlarmTempDisabled(alarmDisabled, alarmId)
    }

    /**
     * Updates alarm temp disabled by id of a [AlarmEntity]
     */
    suspend fun updateAlarmTempDisabled(alarmDisabled: Boolean, alarmId: Int) {
        alarmDao.updateAlarmTempDisabled(alarmDisabled, alarmId)
    }


    /**
     * Updates alarm sleep duration by id of a [AlarmEntity]
     */
    suspend fun updateSleepDuration(sleepDuration: Int, alarmId: Int) {
        alarmDao.updateSleepDuration(sleepDuration, alarmId)
    }

    /**
     * Updates alarm wakeup early by id of a [AlarmEntity]
     */
    suspend fun updateWakeupEarly(wakeupEarly: Int, alarmId: Int) {
        alarmDao.updateWakeupEarly(wakeupEarly, alarmId)
        updateWakeupTime(wakeupEarly, alarmId)
    }

    /**
     * Updates alarm wakeup late by id of a [AlarmEntity]
     */
    suspend fun updateWakeupLate(wakeupLate: Int, alarmId: Int) {
        val day = Constants.DAY_IN_SECONDS
        var newTime = wakeupLate
        if(wakeupLate > day)
            newTime -= day

        if(wakeupLate < 0)
            newTime += day
        alarmDao.updateWakeupLate(newTime, alarmId)
    }

    /**
     * Updates alarm wakeup time by id of a [AlarmEntity]
     */
    suspend fun updateWakeupTime(wakeupTime: Int, alarmId: Int) {
        val day = Constants.DAY_IN_SECONDS
        var newTime = wakeupTime
        if(wakeupTime > day)
            newTime -= day

        if(wakeupTime < 0)
            newTime += day
        alarmDao.updateWakeupTime(newTime, alarmId)
    }

    /**
     * Updates alarm is active by id of a [AlarmEntity]
     */
    suspend fun updateIsActive(isActive: Boolean, alarmId: Int) {
        alarmDao.updateIsActive(isActive, alarmId)
    }

    /**
     * Updates alarm day of week by id of a [AlarmEntity]
     */
    suspend fun updateActiveDayOfWeek(activeDayOfWeek: ArrayList<DayOfWeek>, alarmId: Int) {
        alarmDao.updateActiveDayOfWeek(activeDayOfWeek, alarmId)
    }

    /**
     * Deletes alarm by
     */
    suspend fun deleteAlarmById(alarmId: Int) {
        alarmDao.deleteById(alarmId)
    }

    /**
     * Deletes all alarms
     */
    suspend fun deleteAllAlarms() {
        alarmDao.deleteAll()
    }

    /**
     * Updates alarm name by id of a [AlarmEntity]
     */
    suspend fun updateAlarmName(alarmName: String, alarmId: Int) {
        alarmDao.updateAlarmName(alarmName, alarmId)
    }

    /**
     * Resets alarm temp disabled by id of a [AlarmEntity]
     */
    suspend fun resetAlarmTempDisabledWasFired() {
        alarmDao.resetTempDisabled()
        alarmDao.resetWasFired()
        alarmDao.resetAlreadyAwake()
    }

    /**
     * Resets alarm actual wakeup time by id of a [AlarmEntity]
     */
    suspend fun resetActualWakeupTime(time : Int) {
        alarmDao.resetActualWakeup(time)
    }

    //endregion
}
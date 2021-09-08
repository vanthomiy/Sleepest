package com.sleepestapp.sleepest.storage


import com.sleepestapp.sleepest.model.data.MoodType
import com.sleepestapp.sleepest.model.data.Constants
import com.sleepestapp.sleepest.model.data.SleepState
import com.sleepestapp.sleepest.storage.db.UserSleepSessionDao
import com.sleepestapp.sleepest.storage.db.UserSleepSessionEntity
import com.sleepestapp.sleepest.storage.db.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.*
import java.util.*
import kotlin.collections.ArrayList


/**
 * This contains the interface for each SQL-Database and for DataStore.
 * ROOM API for SQL Database is used for storing large datasets like [SleepApiRawDataEntity]
 * DataStore is used for storing single classes or single values like {later} [AlarmPreferences] (Containing Alarm Time and Alarm Active etc.) and [AlgorithmPreferences] and other key values.
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
        sleepApiRawDataDao.getAll()

    /**
     * [time] the duration in seconds eg. 86200 would be from 24hours ago to now the data
     */
    fun getSleepApiRawDataSince(time:Int): Flow<List<SleepApiRawDataEntity>?>
    {
        val now = LocalDateTime.now(ZoneOffset.systemDefault())
        val seconds = now.atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
        return sleepApiRawDataDao.getSince(seconds-time)
    }

    /**
     * Gets the sleep api data from a specific state from a date in life time.
     * so we always getting the data from 15:00 the day or day before until the specific time
     * later we have to combine it with the actual sleeptimes
     */
    fun getSleepApiRawDataFromDateLive(actualTime:LocalDateTime): Flow<List<SleepApiRawDataEntity>?>
    {
        val startTime = if (actualTime.hour < 15)
            actualTime.toLocalDate().minusDays(1).atTime(15,0).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
        else actualTime.toLocalDate().atTime(15,0).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()

        val endTime = actualTime.atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()

        return sleepApiRawDataDao.getBetween(startTime,endTime)
    }

    /**
     * Gets the sleep api data from a specific state from a date
     * e.g. the dateTime 20.05.2021 at 20:00 returns all data in between 20.05.2021 15:00 to 21.05.2021 at 15:00
     * later we have to combine it with the actual sleeptimes
     */
    fun getSleepApiRawDataFromDate(actualTime:LocalDateTime): Flow<List<SleepApiRawDataEntity>?>
    {
        val startTime = if (actualTime.hour < 15)
            actualTime.toLocalDate().minusDays(1).atTime(15,0).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
        else actualTime.toLocalDate().atTime(15,0).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()

        val endTime = if (actualTime.hour >= 15)
            actualTime.toLocalDate().plusDays(1).atTime(15,0).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
        else actualTime.toLocalDate().atTime(15,0).atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()

        return sleepApiRawDataDao.getBetween(startTime,endTime)
    }

    /**
     * Gets the sleep api data between to timestamps (UTC seconds)
     */
    fun getSleepApiRawDataBetweenTimestamps(startTime: Int, endTime: Int): Flow<List<SleepApiRawDataEntity>?>
    {
        return sleepApiRawDataDao.getBetween(startTime, endTime)
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
            activityApiRawDataDao.getAll()

    /**
     * [time] the duration in seconds eg. 86200 would be from 24hours ago to now the data
     */
    fun getActivityApiRawDataSince(time:Int): Flow<List<ActivityApiRawDataEntity>>
    {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val seconds = now.atZone(ZoneOffset.UTC).toEpochSecond().toInt()
        return activityApiRawDataDao.getSince(seconds-time)
    }


    /**
     * Gets the activity api data from a specific state from a date in life time.
     * so we always getting the data from 15:00 the day or day before until the specific time
     * later we have to combine it with the actual activitytimes
     */
    fun getActivityApiRawDataFromDateLive(actualTime:LocalDateTime): Flow<List<ActivityApiRawDataEntity>>
    {
        val startTime = if (actualTime.hour < 15)
            actualTime.toLocalDate().minusDays(1).atTime(15,0).atZone(ZoneOffset.UTC).toEpochSecond().toInt()
        else actualTime.toLocalDate().atTime(15,0).atZone(ZoneOffset.UTC).toEpochSecond().toInt()

        val endTime = actualTime.atZone(ZoneOffset.UTC).toEpochSecond().toInt()

        return activityApiRawDataDao.getBetween(startTime,endTime)
    }

    /**
     * Gets the activity api data from a specific state from a date
     * e.g. the dateTime 20.05.2021 at 20:00 returns all data in between 20.05.2021 15:00 to 21.05.2021 at 15:00
     * later we have to combine it with the actual activitytimes
     */
    fun getActivityApiRawDataFromDate(actualTime:LocalDateTime): Flow<List<ActivityApiRawDataEntity>>
    {
        val startTime = actualTime.toLocalDate().minusDays(1).atTime(0,0).atZone(ZoneOffset.UTC).toEpochSecond().toInt()
        val endTime = actualTime.toLocalDate().minusDays(1).atTime(23,59).atZone(ZoneOffset.UTC).toEpochSecond().toInt()

        return activityApiRawDataDao.getBetween(startTime,endTime)
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
            userSleepSessionDao.getAll()

    /**
     * returns a specific [UserSleepSessionEntity] by its ID
     */
    fun getSleepSessionById(id: Int): Flow<List<UserSleepSessionEntity?>> =
            userSleepSessionDao.getById(id)

    /**
     * Returns a specific [UserSleepSessionEntity] by its ID
     * If not present, it creates a new session by this ID
     */
    suspend fun getOrCreateSleepSessionById(id: Int): UserSleepSessionEntity {

        var userSession = userSleepSessionDao.getById(id).first().firstOrNull()
        //val userSession = allData.firstOrNull { x -> x.id == id }

        if(userSession == null){
            val session  = UserSleepSessionEntity(id)
            insertUserSleepSession(session)
            return session

        }

        return userSession
    }

    /**
     * [time] the duration in seconds eg. 86200 would be from 24hours ago to now the data
     */
    fun getUserSleepSessionSinceDays(days:Long): Flow<List<UserSleepSessionEntity>>
    {
        val now = LocalDateTime.now(ZoneOffset.systemDefault()).minusDays(days)
        val seconds = now.atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
        return userSleepSessionDao.getSince(seconds)
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
            alarmDao.getAll()

    /**
     * All active alarms and on that specific day
     */
    fun activeAlarmsFlow() : Flow<List<AlarmEntity>> {
        val ldt:LocalDateTime = LocalDateTime.now()
        val date = if(ldt.hour > 15) ldt.plusDays(1).toLocalDate() else ldt.toLocalDate()
        val dayOfWeek = "%" + date.dayOfWeek + "%"

        return alarmDao.getAllActiveOnDay(dayOfWeek.toString())
    }

    /**
     * Returns an [AlarmEntity] by its ID
     */
    fun getAlarmById(alarmId: Int): Flow<AlarmEntity> = alarmDao.getAlarmById(alarmId)

    /**
     * Workaround to call function from JAVA code
     * calls [getNextActiveAlarm]
     */
    fun getNextActiveAlarmJob() : AlarmEntity? = runBlocking{
        return@runBlocking getNextActiveAlarm()
    }

    /**
     * Returns the next alarm that is active or null is no alarm is active in that time duration
     */
    suspend fun getNextActiveAlarm() : AlarmEntity?{

        val list = activeAlarmsFlow().first()
        // get first alarm
        return list.minByOrNull { x-> x.wakeupEarly }
    }

    /**
     * Returns true or false wheter a alarm is active for the actual/next day or not
     */
    suspend fun isAlarmActiv() : Boolean{
        val list = activeAlarmsFlow().first()
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
    }

    /**
     * Resets alarm actual wakeup time by id of a [AlarmEntity]
     */
    suspend fun resetActualWakeupTime(time : Int) {
        alarmDao.resetActualWakeup(time)
    }

    //endregion
}
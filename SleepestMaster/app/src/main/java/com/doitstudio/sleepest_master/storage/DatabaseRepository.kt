package com.doitstudio.sleepest_master.storage


import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionDao
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity
import com.doitstudio.sleepest_master.storage.db.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.collections.ArrayList


/**
 * This contains the interface for each SQL-Database and for DataStore.
 * ROOM API for SQL Database is used for storing large datasets like [SleepApiRawDataEntity] or [SleepSegmentEntity].
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

    // Link to the documentation https://developer.android.com/training/data-storage/room/#kotlin

    // Why Suspend!
    // By default Room runs suspend queries off the main thread. Therefore, we don't need to
    // implement anything else to ensure we're not doing long-running database work off the
    // main thread.

    //region Sleep API Data

    // Methods for SleepApiRawDataDao
    // Observed Flow will notify the observer when the data has changed.
    val allSleepApiRawData: Flow<List<SleepApiRawDataEntity>> =
        sleepApiRawDataDao.getAll()

    /**
     * [time] the duration in seconds eg. 86200 would be from 24hours ago to now the data
     */
    suspend fun getSleepApiRawDataSince(time:Int): Flow<List<SleepApiRawDataEntity>>
    {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val seconds = now.atZone(ZoneOffset.UTC).toEpochSecond().toInt()
        return sleepApiRawDataDao.getSince(seconds-time)
    }

    /**
     * [time] the duration in seconds eg. 86200 would be from 24hours ago to now the data
     */
    suspend fun getSleepApiRawDataSinceSeconds(time:Int): Flow<List<SleepApiRawDataEntity>>
    {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val seconds = now.atZone(ZoneOffset.UTC).toEpochSecond().toInt()
        return sleepApiRawDataDao.getSince(time)
    }

    /**
     * Gets the sleep api data from a specific state from a date in life time.
     * so we always getting the data from 15:00 the day or day before until the specific time
     * later we have to combine it with the actual sleeptimes
     */
    fun getSleepApiRawDataFromDateLive(actualTime:LocalDateTime): Flow<List<SleepApiRawDataEntity>>
    {
        val startTime = if (actualTime.hour < 15)
            actualTime.toLocalDate().minusDays(1).atTime(15,0).atZone(ZoneOffset.UTC).toEpochSecond().toInt()
        else actualTime.toLocalDate().atTime(15,0).atZone(ZoneOffset.UTC).toEpochSecond().toInt()

        val endTime = actualTime.atZone(ZoneOffset.UTC).toEpochSecond().toInt()

        return sleepApiRawDataDao.getBetween(startTime,endTime)
    }

    /**
     * Gets the sleep api data from a specific state from a date
     * e.g. the dateTime 20.05.2021 at 20:00 returns all data in between 20.05.2021 15:00 to 21.05.2021 at 15:00
     * later we have to combine it with the actual sleeptimes
     */
    fun getSleepApiRawDataFromDate(actualTime:LocalDateTime): Flow<List<SleepApiRawDataEntity>>
    {
        val startTime = if (actualTime.hour < 15)
            actualTime.toLocalDate().minusDays(1).atTime(15,0).atZone(ZoneOffset.UTC).toEpochSecond().toInt()
        else actualTime.toLocalDate().atTime(15,0).atZone(ZoneOffset.UTC).toEpochSecond().toInt()

        val endTime = if (actualTime.hour >= 15)
            actualTime.toLocalDate().plusDays(1).atTime(15,0).atZone(ZoneOffset.UTC).toEpochSecond().toInt()
        else actualTime.toLocalDate().atTime(15,0).atZone(ZoneOffset.UTC).toEpochSecond().toInt()

        return sleepApiRawDataDao.getBetween(startTime,endTime)
    }

    fun getSleepApiRawDataBetweenTimestamps(startTime: Int, endTime: Int): Flow<List<SleepApiRawDataEntity>>
    {
        return sleepApiRawDataDao.getBetween(startTime, endTime)
    }

    suspend fun insertSleepApiRawData(sleepClassifyEventEntity: SleepApiRawDataEntity) {
        sleepApiRawDataDao.insert(sleepClassifyEventEntity)
    }

    suspend fun deleteSleepApiRawData() {
        sleepApiRawDataDao.deleteAll()
    }

    suspend fun insertSleepApiRawData(sleepClassifyEventEntities: List<SleepApiRawDataEntity>) {
        sleepApiRawDataDao.insertAll(sleepClassifyEventEntities)
    }

    suspend fun updateSleepApiRawDataSleepState(id: Int, sleepState: SleepState){
        sleepApiRawDataDao.updateSleepState(id,sleepState )
    }

    suspend fun updateOldSleepApiRawDataSleepState(id: Int, sleepState: SleepState){
        sleepApiRawDataDao.updateOldSleepState(id,sleepState )
    }

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
    suspend fun getActivityApiRawDataSince(time:Int): Flow<List<ActivityApiRawDataEntity>>
    {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val seconds = now.atZone(ZoneOffset.UTC).toEpochSecond().toInt()
        return activityApiRawDataDao.getSince(seconds-time)
    }

    /**
     * [time] the duration in seconds eg. 86200 would be from 24hours ago to now the data
     */
    suspend fun getActivityApiRawDataSinceSeconds(time:Int): Flow<List<ActivityApiRawDataEntity>>
    {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val seconds = now.atZone(ZoneOffset.UTC).toEpochSecond().toInt()
        return activityApiRawDataDao.getSince(time)
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

    suspend fun insertActivityApiRawData(activityClassifyEventEntity: ActivityApiRawDataEntity) {
        activityApiRawDataDao.insert(activityClassifyEventEntity)
    }

    suspend fun deleteActivityApiRawData() {
        activityApiRawDataDao.deleteAll()
    }

    suspend fun insertActivityApiRawData(activityClassifyEventEntities: List<ActivityApiRawDataEntity>) {
        activityApiRawDataDao.insertAll(activityClassifyEventEntities)
    }

//endregion

    //region User Sleep Sessions

    val allUserSleepSessions: Flow<List<UserSleepSessionEntity>> =
            userSleepSessionDao.getAll()

    fun getSleepSessionById(id: Int): Flow<UserSleepSessionEntity?> =
            userSleepSessionDao.getById(id)

    suspend fun getOrCreateSleepSessionById(id: Int): UserSleepSessionEntity {
        var userSession = userSleepSessionDao.getById(id).first()

        if(userSession == null){
            userSession = UserSleepSessionEntity(id)
            insertUserSleepSession(userSession)
        }

        return userSession
    }

    suspend fun insertUserSleepSession(userSleepSession: UserSleepSessionEntity) {
        userSleepSessionDao.insert(userSleepSession)
    }

    suspend fun deleteUserSleepSession() {
        userSleepSessionDao.deleteAll()
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

    fun getAlarmById(alarmId: Int): Flow<AlarmEntity> = alarmDao.getAlarmById(alarmId)

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


    suspend fun insertAlarm(alarm: AlarmEntity) {
        alarmDao.insert(alarm)
    }


    suspend fun updateAlarmWasFired(alarmFired: Boolean, alarmId: Int) {
        alarmDao.updateAlarmWasFired(alarmFired, alarmId)
    }

    suspend fun updateSleepDuration(sleepDuration: Int, alarmId: Int) {
        alarmDao.updateSleepDuration(sleepDuration, alarmId)
    }

    suspend fun updateWakeupEarly(wakeupEarly: Int, alarmId: Int) {
        alarmDao.updateWakeupEarly(wakeupEarly, alarmId)
        updateWakeupTime(wakeupEarly, alarmId)
    }

    suspend fun updateWakeupLate(wakeupLate: Int, alarmId: Int) {
        alarmDao.updateWakeupLate(wakeupLate, alarmId)
    }

    suspend fun updateWakeupTime(wakeupTime: Int, alarmId: Int) {
        alarmDao.updateWakeupTime(wakeupTime, alarmId)
    }

    suspend fun updateIsActive(isActive: Boolean, alarmId: Int) {
        alarmDao.updateIsActive(isActive, alarmId)
    }

    suspend fun updateActiveDayOfWeek(activeDayOfWeek: ArrayList<DayOfWeek>, alarmId: Int) {
        alarmDao.updateActiveDayOfWeek(activeDayOfWeek, alarmId)
    }

    suspend fun deleteAlarm(alarm: AlarmEntity) {
        alarmDao.delete(alarm)
    }

    suspend fun deleteAllAlarms() {
        alarmDao.deleteAll()
    }

    suspend fun updateAlarmName(alarmName: String, alarmId: Int) {
        alarmDao.updateAlarmName(alarmName, alarmId)
    }

    //endregion
}
package com.doitstudio.sleepest_master.storage

import com.doitstudio.sleepest_master.sleepcalculation.db.UserSleepSessionDao
import com.doitstudio.sleepest_master.sleepcalculation.db.UserSleepSessionEntity
import com.doitstudio.sleepest_master.storage.db.*
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek


/**
 * This contains the interface for each SQL-Database and for DataStore.
 * ROOM API for SQL Database is used for storing large datasets like [SleepApiRawDataEntity] or [SleepSegmentEntity].
 * DataStore is used for storing single classes or single values like {later} [AlarmPreferences] (Containing Alarm Time and Alarm Active etc.) and [AlgorithmPreferences] and other key values.
 * More information about DataStore @see [link](https://developer.android.com/topic/libraries/architecture/datastore) and about ROOM SQL @see [link](https://developer.android.com/training/data-storage/room/#kotlin).
 *
 */
class DbRepository(
        private val sleepSegmentDao: SleepSegmentDao,
        private val userSleepSessionDataDao: UserSleepSessionDao,
        private val alarmDao: AlarmDao

) {

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: DbRepository? = null

        var a:Int = 0

        fun getRepo(sleepSegmentDao: SleepSegmentDao, userSleepSessionDataDao: UserSleepSessionDao, alarmDao: AlarmDao): DbRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = DbRepository(sleepSegmentDao, userSleepSessionDataDao, alarmDao)
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

    //region Sleep Segments

    // Methods for SleepSegmentDao
    // Observed Flow will notify the observer when the data has changed.
    val allSleepSegments: Flow<List<SleepSegmentEntity>> =
            sleepSegmentDao.getAll()

    suspend fun insertSleepSegment(sleepClassifyEventEntity: SleepSegmentEntity) {
        sleepSegmentDao.insert(sleepClassifyEventEntity)
    }

    suspend fun deleteSleepSegments() {
        sleepSegmentDao.deleteAll()
    }

    suspend fun insertSleepSegments(sleepClassifyEventEntities: List<SleepSegmentEntity>) {
        sleepSegmentDao.insertAll(sleepClassifyEventEntities)
    }

    //endregion

    //region User Sleep Segment

    // Methods for UserSleepSegmentDao
    // Observed Flow will notify the observer when the data has changed.
    val userSleepSessionFlow: Flow<List<UserSleepSessionEntity>> =
        userSleepSessionDataDao.getAll()



    suspend fun insertUserSleepSession(userSleepSessionEntity: UserSleepSessionEntity) {
        userSleepSessionDataDao.insert(userSleepSessionEntity)
    }
/*
    suspend fun updateUserSleepSession(userSleepRating: UserSleepRating, userCalculationRating: UserCalculationRating, sleepTimeStart: Int) {
        userSleepSessionDataDao.update(userSleepRating, userCalculationRating, sleepTimeStart)
    }
*/

    suspend fun insertSleepSession(userSleepSessionEntity: List<UserSleepSessionEntity>) {
        userSleepSessionDataDao.insertAll(userSleepSessionEntity)
    }

    suspend fun deleteUserSleepSessions() {
        userSleepSessionDataDao.deleteAll()
    }

    suspend fun deleteUserSleepSession(userSleepSessionEntity: UserSleepSessionEntity) {
        userSleepSessionDataDao.delete(userSleepSessionEntity)
    }


    //endregion


    //region Alarm

    // Methods for Alarm
    // Observed Flow will notify the observer when the data has changed.
    val alarmFlow: Flow<List<AlarmEntity>> =
            alarmDao.getAll()

    fun getAlarmById(alarmId: Int): Flow<AlarmEntity> = alarmDao.getAlarmById(alarmId)

    suspend fun insertAlarm(alarm: AlarmEntity) {
        alarmDao.insert(alarm)
    }

    suspend fun updateSleepDuration(sleepDuration: Int, alarmId:Int) {
        alarmDao.updateSleepDuration(sleepDuration, alarmId)
    }

    suspend fun updateWakeupEarly(wakeupEarly: Int, alarmId:Int) {
        alarmDao.updateWakeupEarly(wakeupEarly, alarmId)
    }

    suspend fun updateWakeupLate(wakeupLate: Int, alarmId:Int) {
        alarmDao.updateWakeupLate(wakeupLate, alarmId)
    }

    suspend fun updateIsActive(isActive: Boolean, alarmId:Int) {
        alarmDao.updateIsActive(isActive, alarmId)
    }

    suspend fun updateActiveDayOfWeek(activeDayOfWeek: ArrayList<DayOfWeek>, alarmId:Int) {
        alarmDao.updateActiveDayOfWeek(activeDayOfWeek, alarmId)
    }

    suspend fun deleteAlarm(alarm:AlarmEntity) {
        alarmDao.delete(alarm)
    }

    suspend fun deleteAllAlarms() {
        alarmDao.deleteAll()
    }

    //endregion
}

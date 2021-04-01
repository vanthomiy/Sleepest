package com.doitstudio.sleepest_master.storage


import com.doitstudio.sleepest_master.model.data.sleepcalculation.SleepSegmentEntity
import com.doitstudio.sleepest_master.model.data.sleepcalculation.UserSleepSessionEntity

import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataDao
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.doitstudio.sleepest_master.storage.db.SleepSegmentDao
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionDao
import kotlinx.coroutines.flow.Flow


/**
 * This contains the interface for each SQL-Database and for DataStore.
 * ROOM API for SQL Database is used for storing large datasets like [SleepApiRawDataEntity] or [SleepSegmentEntity].
 * DataStore is used for storing single classes or single values like {later} [AlarmPreferences] (Containing Alarm Time and Alarm Active etc.) and [AlgorithmPreferences] and other key values.
 * More information about DataStore @see [link](https://developer.android.com/topic/libraries/architecture/datastore) and about ROOM SQL @see [link](https://developer.android.com/training/data-storage/room/#kotlin).
 *
 */
class DbRepository(
    private val sleepSegmentDao: SleepSegmentDao,
    private val sleepApiRawDataDao: SleepApiRawDataDao,
    private val userSleepSessionDataDao: UserSleepSessionDao

) {

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: DbRepository? = null

        var a:Int = 0

        fun getRepo(sleepSegmentDao: SleepSegmentDao, sleepApiRawDataDao: SleepApiRawDataDao, userSleepSessionDataDao: UserSleepSessionDao): DbRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = DbRepository(sleepSegmentDao, sleepApiRawDataDao, userSleepSessionDataDao)
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

    suspend fun insertSleepApiRawData(sleepClassifyEventEntity: SleepApiRawDataEntity) {
        sleepApiRawDataDao.insert(sleepClassifyEventEntity)
    }

    suspend fun deleteSleepApiRawData() {
        sleepApiRawDataDao.deleteAll()
    }

    suspend fun insertSleepApiRawData(sleepClassifyEventEntities: List<SleepApiRawDataEntity>) {
        sleepApiRawDataDao.insertAll(sleepClassifyEventEntities)
    }

    //endregion

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


}

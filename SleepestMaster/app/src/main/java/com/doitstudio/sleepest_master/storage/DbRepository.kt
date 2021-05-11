package com.doitstudio.sleepest_master.storage


import com.doitstudio.sleepest_master.sleepcalculation.db.UserSleepSessionDao
import com.doitstudio.sleepest_master.sleepcalculation.db.UserSleepSessionEntity
import com.doitstudio.sleepest_master.storage.db.SleepSegmentEntity
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataDao
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.doitstudio.sleepest_master.storage.db.SleepSegmentDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first


/**
 * This contains the interface for each SQL-Database and for DataStore.
 * ROOM API for SQL Database is used for storing large datasets like [SleepApiRawDataEntity] or [SleepSegmentEntity].
 * DataStore is used for storing single classes or single values like {later} [AlarmPreferences] (Containing Alarm Time and Alarm Active etc.) and [AlgorithmPreferences] and other key values.
 * More information about DataStore @see [link](https://developer.android.com/topic/libraries/architecture/datastore) and about ROOM SQL @see [link](https://developer.android.com/training/data-storage/room/#kotlin).
 *
 */
class DbRepository(
        private val sleepSegmentDao: SleepSegmentDao,
        private val userSleepSessionDao: UserSleepSessionDao
) {

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: DbRepository? = null

        fun getRepo(sleepSegmentDao: SleepSegmentDao, userSleepSessionDao: UserSleepSessionDao): DbRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = DbRepository(sleepSegmentDao, userSleepSessionDao)
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



    //region User Sleep Sessions

    val allUserSleepSessions: Flow<List<UserSleepSessionEntity>> =
            userSleepSessionDao.getAll()

    fun getSleepSessionById(id:Int): Flow<UserSleepSessionEntity?> =
            userSleepSessionDao.getById(id)

    suspend fun getOrCreateSleepSessionById(id:Int): UserSleepSessionEntity {
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

    //endregion


    //region Sleep Segments

    // Methods for SleepSegmentDao
    // Observed Flow will notify the observer when the data has changed.
    val allSleepSegments: Flow<List<SleepSegmentEntity>> =
            sleepSegmentDao.getAll()

    suspend fun insertSleepSegment(sleepClassifyEventEntity: SleepSegmentEntity) {
        sleepSegmentDao.insert(sleepClassifyEventEntity)
    }

    suspend fun deleteSleepSegmentsWithin(start:Int, end:Int) {
        sleepSegmentDao.deleteWithin(start, end)
    }

    suspend fun deleteSleepSegments() {
        sleepSegmentDao.deleteAll()
    }

    suspend fun insertSleepSegments(sleepClassifyEventEntities: List<SleepSegmentEntity>) {
        sleepSegmentDao.insertAll(sleepClassifyEventEntities)
    }

    //endregion

}
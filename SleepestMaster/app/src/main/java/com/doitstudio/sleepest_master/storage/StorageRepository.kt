package com.doitstudio.sleepest_master.storage

import com.doitstudio.sleepest_master.model.data.SleepSegmentEntity
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataDao
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.doitstudio.sleepest_master.storage.db.SleepSegmentDao
import com.doitstudio.sleepest_master.storage.datastorage.AlarmPreferencesStatus
import kotlinx.coroutines.flow.Flow

/**
 * This contains the interface for each SQL-Database and for DataStore.
 * ROOM API for SQL Database is used for storing large datasets like [SleepApiRawDataEntity] or [SleepSegmentEntity].
 * DataStore is used for storing single classes or single values like {later} [AlarmPreferences] (Containing Alarm Time and Alarm Active etc.) and [AlgorithmPreferences] and other key values.
 * More information about DataStore @see [link](https://developer.android.com/topic/libraries/architecture/datastore) and about ROOM SQL @see [link](https://developer.android.com/training/data-storage/room/#kotlin).
 *
 */
class StorageRepository(
        private val alarmPreferencesStatus: AlarmPreferencesStatus,
        private val sleepSegmentDao: SleepSegmentDao,
        private val sleepApiRawDataDao: SleepApiRawDataDao
) {

    //region Data Storage Flows

    // Methods for SleepSubscriptionStatus
    // Uses [DataStore] to save the subscription to sleep data status. This is used to check if the
    // app is still listening to changes in sleep data when the app is brought back into
    // the foreground.
    val alarmActiveFlow: Flow<Boolean> = alarmPreferencesStatus.alarmActiveFlow

    suspend fun updateAlarmActive(alarmActive: Boolean) =
            alarmPreferencesStatus.updateAlarmActive(alarmActive)

    val alarmTimeFlow: Flow<Int> = alarmPreferencesStatus.alarmTimeFlow

    suspend fun updateAlarmTime(alarmTime: Int) =
            alarmPreferencesStatus.updateAlarmTime(alarmTime)

    //endregion

    //region SQL-Database using Room

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

    //endregion

}

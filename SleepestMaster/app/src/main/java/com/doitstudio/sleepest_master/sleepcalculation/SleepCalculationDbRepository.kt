package com.doitstudio.sleepest_master.sleepcalculation

import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataDao
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset


/**
 *
 */
class SleepCalculationDbRepository(
        private val sleepApiRawDataDao: SleepApiRawDataDao
) {

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: SleepCalculationDbRepository? = null

        fun getRepo(sleepApiRawDataDao: SleepApiRawDataDao): SleepCalculationDbRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = SleepCalculationDbRepository(sleepApiRawDataDao)
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

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
    suspend fun getSleepApiRawDataFromDateLive(actualTimeInt:Int): Flow<List<SleepApiRawDataEntity>>
    {
        val actualTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(actualTimeInt.toLong()*1000), ZoneOffset.UTC)

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
    suspend fun getSleepApiRawDataFromDate(actualTime:LocalDateTime): Flow<List<SleepApiRawDataEntity>>
    {
        val startTime = if (actualTime.hour < 15)
            actualTime.toLocalDate().minusDays(1).atTime(15,0).atZone(ZoneOffset.UTC).toEpochSecond().toInt()
        else actualTime.toLocalDate().atTime(15,0).atZone(ZoneOffset.UTC).toEpochSecond().toInt()

        val endTime = if (actualTime.hour >= 15)
            actualTime.toLocalDate().plusDays(1).atTime(15,0).atZone(ZoneOffset.UTC).toEpochSecond().toInt()
        else actualTime.toLocalDate().atTime(15,0).atZone(ZoneOffset.UTC).toEpochSecond().toInt()

        return sleepApiRawDataDao.getBetween(startTime,endTime)
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
    //endregion
}
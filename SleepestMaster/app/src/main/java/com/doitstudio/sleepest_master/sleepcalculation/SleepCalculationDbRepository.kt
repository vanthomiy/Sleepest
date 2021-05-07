package com.doitstudio.sleepest_master.sleepcalculation

import androidx.room.Query
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataDao
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
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

    suspend fun getSleepApiRawDataFromDate(dateTime:LocalDate): Flow<List<SleepApiRawDataEntity>>
    {
        val startTime = dateTime.minusDays(1).atTime(12,0).atZone(ZoneOffset.UTC).toEpochSecond().toInt()
        val endTime = dateTime.minusDays(1).atTime(12,0).atZone(ZoneOffset.UTC).toEpochSecond().toInt()
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




    //endregion

}

package com.doitstudio.sleepest_master.storage.db

import androidx.room.*
import com.doitstudio.sleepest_master.model.data.SleepState
import kotlinx.coroutines.flow.Flow

/**
 * Defines [SleepApiRawDataDao] database operations for the [SleepApiRawDataEntity] class.
 */
@Dao
interface SleepApiRawDataDao {
    @Query("SELECT * FROM sleep_api_raw_data_table ORDER BY time_stamp_seconds DESC")
    fun getAll(): Flow<List<SleepApiRawDataEntity>?>

    @Query("SELECT * FROM sleep_api_raw_data_table WHERE time_stamp_seconds >= :time ORDER BY time_stamp_seconds DESC")
    fun getSince(time:Int): Flow<List<SleepApiRawDataEntity>?>

    @Query("SELECT * FROM sleep_api_raw_data_table WHERE time_stamp_seconds >= :startTime AND time_stamp_seconds <= :endTime ORDER BY time_stamp_seconds DESC")
    fun getBetween(startTime:Int, endTime:Int): Flow<List<SleepApiRawDataEntity>?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sleepSegmentEventEntityRaw: SleepApiRawDataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sleepSegmentEventEntityRaws: List<SleepApiRawDataEntity>)

    @Query("UPDATE sleep_api_raw_data_table SET sleepState = :sleepState WHERE time_stamp_seconds = :id")
    suspend fun updateSleepState(id: Int, sleepState: SleepState)

    @Query("UPDATE sleep_api_raw_data_table SET oldSleepState = :sleepState WHERE time_stamp_seconds = :id")
    suspend fun updateOldSleepState(id: Int, sleepState: SleepState)

    @Query("UPDATE sleep_api_raw_data_table SET wakeUpTime = :wakeUp WHERE time_stamp_seconds = :id")
    suspend fun updateWakeUp(id: Int, wakeUp: Int)

    @Delete
    suspend fun delete(sleepSegmentEventEntityRaw: SleepApiRawDataEntity)

    @Query("DELETE FROM sleep_api_raw_data_table")
    suspend fun deleteAll()



}
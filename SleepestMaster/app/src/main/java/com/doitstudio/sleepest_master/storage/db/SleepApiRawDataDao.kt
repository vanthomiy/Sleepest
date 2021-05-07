package com.doitstudio.sleepest_master.storage.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Defines [SleepApiRawDataDao] database operations for the [SleepApiRawDataEntity] class.
 */
@Dao
interface SleepApiRawDataDao {
    @Query("SELECT * FROM sleep_api_raw_data_table ORDER BY time_stamp_seconds DESC")
    fun getAll(): Flow<List<SleepApiRawDataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sleepSegmentEventEntityRaw: SleepApiRawDataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sleepSegmentEventEntityRaws: List<SleepApiRawDataEntity>)

    @Delete
    suspend fun delete(sleepSegmentEventEntityRaw: SleepApiRawDataEntity)

    @Query("DELETE FROM sleep_api_raw_data_table")
    suspend fun deleteAll()



}
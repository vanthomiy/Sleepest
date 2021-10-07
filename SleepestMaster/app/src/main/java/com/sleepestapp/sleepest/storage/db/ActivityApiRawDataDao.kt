package com.sleepestapp.sleepest.storage.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Defines [ActivityApiRawDataDao] database operations for the [SleepApiRawDataEntity] class.
 */
@Dao
interface ActivityApiRawDataDao {
    @Query("SELECT * FROM activity_api_raw_data_table ORDER BY time_stamp_seconds DESC")
    fun getAll(): Flow<List<ActivityApiRawDataEntity>>

    @Query("SELECT * FROM activity_api_raw_data_table WHERE time_stamp_seconds >= :time ORDER BY time_stamp_seconds DESC")
    fun getSince(time:Int): Flow<List<ActivityApiRawDataEntity>>

    @Query("SELECT * FROM activity_api_raw_data_table WHERE time_stamp_seconds >= :startTime AND time_stamp_seconds <= :endTime ORDER BY time_stamp_seconds DESC")
    fun getBetween(startTime:Int, endTime:Int): Flow<List<ActivityApiRawDataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activitySegmentEventEntityRaw: ActivityApiRawDataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activitySegmentEventEntityRaw: List<ActivityApiRawDataEntity>)

    @Delete
    suspend fun delete(activitySegmentEventEntityRaw: ActivityApiRawDataEntity)

    @Query("DELETE FROM activity_api_raw_data_table")
    suspend fun deleteAll()



}


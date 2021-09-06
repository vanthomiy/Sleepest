package com.sleepestapp.sleepest.storage.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Defines [SleepSegmentDao] database operations for the [SleepSegmentEntity] class.
 */
@Dao
interface SleepSegmentDao {
    @Query("SELECT * FROM sleep_segment_table ORDER BY timestampSecondsStart DESC")
    fun getAll(): Flow<List<SleepSegmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sleepSegmentEventEntityRaw: SleepSegmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sleepSegmentEventEntityRaws: List<SleepSegmentEntity>)

    @Query("DELETE FROM sleep_segment_table WHERE timestampSecondsStart >= :start AND timestampSecondsEnd <= :end")
    suspend fun deleteWithin(start:Int, end:Int)

    @Delete
    suspend fun delete(sleepSegmentEventEntityRaw: SleepSegmentEntity)

    @Query("DELETE FROM sleep_segment_table")
    suspend fun deleteAll()




}
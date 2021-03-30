package com.doitstudio.sleepest_master.storage.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.doitstudio.sleepest_master.model.data.sleepcalculation.SleepSegmentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Defines [SleepSegmentDao] database operations for the [SleepSegmentEntity] class.
 */
@Dao
interface SleepSegmentDao {
    @Query("SELECT * FROM sleep_segment_table ORDER BY time_stamp_seconds_start DESC")
    fun getAll(): Flow<List<SleepSegmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sleepSegmentEventEntityRaw: SleepSegmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sleepSegmentEventEntityRaws: List<SleepSegmentEntity>)

    @Delete
    suspend fun delete(sleepSegmentEventEntityRaw: SleepSegmentEntity)

    @Query("DELETE FROM sleep_segment_table")
    suspend fun deleteAll()
}
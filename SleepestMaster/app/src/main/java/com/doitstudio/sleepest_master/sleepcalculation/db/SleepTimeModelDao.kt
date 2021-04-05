package com.doitstudio.sleepest_master.sleepcalculation.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Defines [SleepTimeModelDao] database operations for the [SleepTimeModelEntity] class.
 */
@Dao
interface SleepTimeModelDao {
    @Query("SELECT * FROM sleep_time_model_entity ORDER BY id DESC")
    fun getAll(): Flow<List<SleepTimeModelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sleepSegmentEventEntityRaw: SleepTimeModelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sleepSegmentEventEntityRaws: List<SleepTimeModelEntity>)

    @Delete
    suspend fun delete(sleepSegmentEventEntityRaw: SleepTimeModelEntity)

    @Query("DELETE FROM sleep_time_model_entity")
    suspend fun deleteAll()
}
package com.doitstudio.sleepest_master.sleepcalculation.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Defines [SleepStateModelDao] database operations for the [SleepStateModelEntity] class.
 */
@Dao
interface SleepStateModelDao {
    @Query("SELECT * FROM sleep_state_model_entity ORDER BY id DESC")
    fun getAll(): Flow<List<SleepStateModelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sleepSegmentEventEntityRaw: SleepStateModelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sleepSegmentEventEntityRaws: List<SleepStateModelEntity>)

    @Delete
    suspend fun delete(sleepSegmentEventEntityRaw: SleepStateModelEntity)

    @Query("DELETE FROM sleep_state_model_entity")
    suspend fun deleteAll()
}
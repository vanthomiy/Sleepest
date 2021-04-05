package com.doitstudio.sleepest_master.sleepcalculation.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Defines [SleepStateFactorModelDao] database operations for the [SleepStateFactorModelEntity] class.
 */
@Dao
interface SleepStateFactorModelDao {
    @Query("SELECT * FROM sleep_state_factor_model_entity ORDER BY id DESC")
    fun getAll(): Flow<List<SleepStateFactorModelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sleepSegmentEventEntityRaw: SleepStateFactorModelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sleepSegmentEventEntityRaws: List<SleepStateFactorModelEntity>)

    @Delete
    suspend fun delete(sleepSegmentEventEntityRaw: SleepStateFactorModelEntity)

    @Query("DELETE FROM sleep_state_factor_model_entity")
    suspend fun deleteAll()
}
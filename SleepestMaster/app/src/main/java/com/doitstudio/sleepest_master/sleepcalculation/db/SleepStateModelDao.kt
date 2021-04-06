package com.doitstudio.sleepest_master.sleepcalculation.db

import androidx.room.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

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

    fun setupDatabase(){
        val scope: CoroutineScope = MainScope()

        scope.launch{
            insertAll(SleepStateModelEntity.setupDefaultEntities())
        }
    }
}
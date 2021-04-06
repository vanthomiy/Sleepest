package com.doitstudio.sleepest_master.sleepcalculation.db

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
 * Defines [SleepTimeFactorModelDao] database operations for the [SleepTimeFactorModelEntity] class.
 */
@Dao
interface SleepTimeFactorModelDao {
    @Query("SELECT * FROM sleep_time_factor_model_entity ORDER BY id DESC")
    fun getAll(): Flow<List<SleepTimeFactorModelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sleepSegmentEventEntityRaw: SleepTimeFactorModelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sleepSegmentEventEntityRaws: List<SleepTimeFactorModelEntity>)

    @Delete
    suspend fun delete(sleepSegmentEventEntityRaw: SleepTimeFactorModelEntity)

    @Query("DELETE FROM sleep_time_factor_model_entity")
    suspend fun deleteAll()

    fun setupDatabase(){
        val scope: CoroutineScope = MainScope()

        scope.launch{
            insertAll(SleepTimeFactorModelEntity.setupDefaultEntities())
        }
    }
}

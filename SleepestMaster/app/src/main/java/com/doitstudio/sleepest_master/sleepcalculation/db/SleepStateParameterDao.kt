package com.doitstudio.sleepest_master.sleepcalculation.db

import android.content.Context
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
 * Defines [SleepStateParameterDao] database operations for the [SleepStateFactorModelEntity] class.
 */
@Dao
interface SleepStateParameterDao {
    @Query("SELECT * FROM sleep_state_parameter_entity ORDER BY id DESC")
    fun getAll(): Flow<List<SleepStateParameterEntity>>

    @Query("SELECT * FROM sleep_state_parameter_entity WHERE id LIKE :parameterId")
    fun getParameterById(parameterId: String): Flow<SleepStateParameterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sleepSegmentEventEntityRaw: SleepStateParameterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sleepSegmentEventEntityRaws: List<SleepStateParameterEntity>)

    @Delete
    suspend fun delete(sleepSegmentEventEntityRaw: SleepStateParameterEntity)

    @Query("DELETE FROM sleep_state_parameter_entity")
    suspend fun deleteAll()


    fun setupDatabase(context: Context){
        val scope: CoroutineScope = MainScope()

        scope.launch{
            insertAll(SleepStateParameterEntity.setupDefaultEntities(context))
        }
    }
}
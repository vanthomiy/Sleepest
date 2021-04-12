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
 * Defines [SleepTimeParameterDao] database operations for the [SleepTimeParameterEntity] class.
 */
@Dao
interface SleepTimeParameterDao {
    @Query("SELECT * FROM sleep_time_parameter_entity ORDER BY id DESC")
    fun getAll(): Flow<List<SleepTimeParameterEntity>>

    @Query("SELECT * FROM sleep_time_parameter_entity WHERE id LIKE :parameterId")
    fun getParameterById(parameterId: String): SleepTimeParameterEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sleepSegmentEventEntityRaw: SleepTimeParameterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sleepSegmentEventEntityRaws: List<SleepTimeParameterEntity>)

    @Delete
    suspend fun delete(sleepSegmentEventEntityRaw: SleepTimeParameterEntity)

    @Query("DELETE FROM sleep_time_parameter_entity")
    suspend fun deleteAll()


    fun setupDatabase(context: Context){
        val scope: CoroutineScope = MainScope()

        scope.launch{
            insertAll(SleepTimeParameterEntity.setupDefaultEntities(context))
        }
    }
}

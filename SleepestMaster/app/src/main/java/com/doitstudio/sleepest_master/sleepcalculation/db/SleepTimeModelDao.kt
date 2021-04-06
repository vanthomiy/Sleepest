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

    fun setupDatabase(context: Context){
        val scope: CoroutineScope = MainScope()

        scope.launch{
            insertAll(SleepTimeModelEntity.setupDefaultEntities(context))
        }
    }
}
package com.doitstudio.sleepest_master.storage.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Defines [UserSleepSessionDao] database operations for the [UserSleepSessionEntity] class.
 */
@Dao
interface UserSleepSessionDao {
    @Query("SELECT * FROM user_sleep_session_entity ORDER BY id DESC")
    fun getAll(): Flow<List<UserSleepSessionEntity>>

    @Query("SELECT * FROM user_sleep_session_entity WHERE id LIKE :id")
    fun getById(id:Int): Flow<UserSleepSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sleepSegmentEventEntityRaw: UserSleepSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sleepSegmentEventEntityRaws: List<UserSleepSessionEntity>)

    @Delete
    suspend fun delete(sleepSegmentEventEntityRaw: UserSleepSessionEntity)

    @Query("DELETE FROM user_sleep_session_entity")
    suspend fun deleteAll()
}
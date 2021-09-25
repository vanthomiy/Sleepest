package com.sleepestapp.sleepest.storage.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sleepestapp.sleepest.model.data.MoodType
import kotlinx.coroutines.flow.Flow

/**
 * Defines [UserSleepSessionDao] database operations for the [UserSleepSessionEntity] class.
 */
@Dao
interface UserSleepSessionDao {
    @Query("SELECT * FROM user_sleep_session_entity ORDER BY id DESC")
    fun getAll(): Flow<List<UserSleepSessionEntity>>

    @Query("SELECT * FROM user_sleep_session_entity WHERE id LIKE :id")
    fun getById(id:Int): Flow<List<UserSleepSessionEntity>>

    @Query("SELECT * FROM user_sleep_session_entity WHERE id >= :time ORDER BY id DESC")
    fun getSince(time:Int): Flow<List<UserSleepSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sleepSegmentEventEntityRaw: UserSleepSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sleepSegmentEventEntityRaw: List<UserSleepSessionEntity>)

    @Delete
    suspend fun delete(sleepSegmentEventEntityRaw: UserSleepSessionEntity)

    @Query("DELETE FROM user_sleep_session_entity")
    suspend fun deleteAll()

    @Query("UPDATE user_sleep_session_entity SET sleepRatingmoodAfterSleep =:mood WHERE id LIKE :sessionId")
    suspend fun updateMoodAfterSleep(mood: MoodType, sessionId: Int)
}
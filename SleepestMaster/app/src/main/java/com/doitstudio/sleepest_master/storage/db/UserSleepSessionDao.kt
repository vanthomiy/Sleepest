package com.doitstudio.sleepest_master.storage.db

import androidx.room.*
import com.doitstudio.sleepest_master.model.data.sleepcalculation.UserCalculationRating
import com.doitstudio.sleepest_master.model.data.sleepcalculation.UserSleepRating
import com.doitstudio.sleepest_master.model.data.sleepcalculation.UserSleepSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Defines [UserSleepSessionDao] database operations for the [UserSleepSessionEntity] class.
 */
@Dao
interface UserSleepSessionDao {
    @Query("SELECT * FROM user_sleep_session_table ORDER BY sleep_time_start DESC")
    fun getAll(): Flow<List<UserSleepSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userSleepSessionEntity: UserSleepSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(userSleepSessionEntities: List<UserSleepSessionEntity>)

    /*
     /**
     * Updating [UserCalculationRating] and [UserSleepRating]
     * By sleep_time_start
     */
    @Query("UPDATE user_sleep_session_table SET user_rat = :userSleepRating, session_user_calculation_rating= :userCalculationRating WHERE sleep_time_start =:sleepTimeStart")
    suspend fun update(userSleepRating: UserSleepRating, userCalculationRating: UserCalculationRating, sleepTimeStart: Int)
*/

    @Delete
    suspend fun delete(userSleepSessionEntity: UserSleepSessionEntity)

    @Query("DELETE FROM user_sleep_session_table")
    suspend fun deleteAll()
}



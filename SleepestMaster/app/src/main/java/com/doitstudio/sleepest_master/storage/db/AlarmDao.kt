package com.doitstudio.sleepest_master.storage.db

import androidx.room.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.DayOfWeek

/**
 * Defines [AlarmDao] database operations for the [SleepApiRawDataEntity] class.
 */
@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarm_properties_table ORDER BY id DESC")
    fun getAll(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarm_properties_table Where isActive=1 And activeDayOfWeek Like :dayOfWeek ORDER BY id DESC")
    fun getAllActiveOnDay(dayOfWeek: String): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarm_properties_table Where isActive=1 ORDER BY id DESC")
    fun getAllActive(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarm_properties_table Where id Like :alarmId")
    fun getAlarmById(alarmId: Int): Flow<AlarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarmEntity: AlarmEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(alarmEntitys: List<AlarmEntity>)

    @Query("UPDATE alarm_properties_table SET sleepDuration =:sleepDuration Where id LIKE :alarmId")
    suspend fun updateSleepDuration(sleepDuration: Int, alarmId:Int)

    @Query("UPDATE alarm_properties_table SET wasFired =:wasFired Where id LIKE :alarmId")
    suspend fun updateAlarmWasFired(wasFired: Boolean, alarmId:Int)

    @Query("UPDATE alarm_properties_table SET tempDisabled =:tempDisabled Where id LIKE :alarmId")
    suspend fun updateAlarmTempDisabled(tempDisabled: Boolean, alarmId:Int)

    @Query("UPDATE alarm_properties_table SET wakeupEarly =:wakeupEarly Where id LIKE :alarmId")
    suspend fun updateWakeupEarly(wakeupEarly: Int, alarmId:Int)

    @Query("UPDATE alarm_properties_table SET actualWakeup =:wakeupTime Where id LIKE :alarmId")
    suspend fun updateWakeupTime(wakeupTime: Int, alarmId:Int)

    @Query("UPDATE alarm_properties_table SET wakeupLate =:wakeupLate Where id LIKE :alarmId")
    suspend fun updateWakeupLate(wakeupLate: Int, alarmId:Int)

    @Query("UPDATE alarm_properties_table SET isActive =:isActive Where id LIKE :alarmId")
    suspend fun updateIsActive(isActive: Boolean, alarmId:Int)

    @Query("UPDATE alarm_properties_table SET activeDayOfWeek =:activeDayOfWeek Where id LIKE :alarmId")
    suspend fun updateActiveDayOfWeek(activeDayOfWeek: ArrayList<DayOfWeek>, alarmId:Int)

    @Query("UPDATE alarm_properties_table SET alarmName =:alarmName WHERE id LIKE :alarmId")
    suspend fun updateAlarmName(alarmName: String, alarmId: Int)

    @Query("DELETE FROM alarm_properties_table WHERE id LIKE :alarmId")
    suspend fun deleteById(alarmId: Int)

    @Delete
    suspend fun delete(alarmEntity: AlarmEntity)

    @Query("DELETE FROM alarm_properties_table")
    suspend fun deleteAll()
}
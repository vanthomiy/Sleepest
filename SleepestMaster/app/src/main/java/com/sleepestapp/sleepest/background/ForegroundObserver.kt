package com.sleepestapp.sleepest.background

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.storage.db.AlarmEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Helper class for the foreground service to get live data.
 */

class ForegroundObserver(private val fs: ForegroundService) {

    private val scope: CoroutineScope = MainScope()
    private val databaseRepository: DatabaseRepository by lazy {
        (fs.applicationContext as MainApplication).dataBaseRepository
    }
    private val dataStoreRepository: DataStoreRepository by lazy {
        (fs.applicationContext as MainApplication).dataStoreRepository
    }
    private lateinit var alarmLivedata : LiveData<List<AlarmEntity>> //by lazy{databaseRepository.activeAlarmsFlow().asLiveData()}
    private val userSleepTime by lazy{dataStoreRepository.sleepApiDataFlow.asLiveData()}
    private val liveUserSleepActivityData by lazy{dataStoreRepository.liveUserSleepActivityFlow.asLiveData()}
    private val bannerConfigLivedata by lazy{dataStoreRepository.settingsDataFlow.asLiveData()}

    /**
     * Reset sleep time, needed at begin of sleep time
     */
    fun resetSleepTime(){
        scope.launch {
            dataStoreRepository.updateUserSleepTime(0)
            dataStoreRepository.updateIsUserSleeping(false)
        }
    }

    /**
     * Get next active alarm and its properties
     * @return Instance of next active alarm
     */
    fun getNextAlarm() : AlarmEntity? = runBlocking {
        val isAfterSleepTime = dataStoreRepository.isAfterSleepTime()
        return@runBlocking databaseRepository.getNextActiveAlarm(isAfterSleepTime.first, isAfterSleepTime.second)
    }

    /**
     * Update the property of the next alarm, that it was already fired
     * @param alarmFired true = fired
     * @param alarmId ID of the alarm to be updated
     */
    fun updateAlarmWasFired(alarmFired: Boolean, alarmId: Int) {
        scope.launch {
            databaseRepository.updateAlarmWasFired(alarmFired, alarmId)
        }
    }

    /**
     * Change the status property of the foreground service
     * @param status Status
     */
    fun setForegroundStatus(status: Boolean) {
        scope.launch {
            dataStoreRepository.backgroundUpdateIsActive(status)
        }
    }

    /**
     * Get the subscription status of the Sleep API
     * @return Status
     */
    fun getSubscribeStatus() : Boolean {
        var status = false
        scope.launch {
            status =  dataStoreRepository.getSleepSubscribeStatus()
        }

        return status
    }

    /**
     * Get the status of the foreground service
     */
    fun getForegroundStatus() : Boolean {
        var status = false
        scope.launch {
            status = dataStoreRepository.backgroundServiceFlow.first().isForegroundActive
        }

        return status
    }

    /**
     * Initialisation of the live data
     */
    init {

        scope.launch {
            val isAfterSleepTime = dataStoreRepository.isAfterSleepTime()
            alarmLivedata = databaseRepository.activeAlarmsFlow(isAfterSleepTime.first, isAfterSleepTime.second).asLiveData()
        }

        alarmLivedata.observe(fs) {

            val nextAlarm = it.minByOrNull { x->x.actualWakeup }

            if(nextAlarm != null)
                fs.OnAlarmChanged(nextAlarm)
        }

        userSleepTime.observe(fs){ ust->
            fs.OnSleepApiDataChanged(ust)
        }

        liveUserSleepActivityData.observe(fs){ la->
            fs.OnSleepTimeChanged(la)
        }

        bannerConfigLivedata.observe(fs) { bcl->
            fs.OnBannerConfigChanged(bcl)

        }
    }
}
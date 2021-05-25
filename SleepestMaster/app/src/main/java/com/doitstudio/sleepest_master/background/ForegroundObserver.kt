package com.doitstudio.sleepest_master.background

import androidx.lifecycle.asLiveData
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.storage.db.AlarmEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ForegroundObserver(private val fs: ForegroundService) {

    private val scope: CoroutineScope = MainScope()
    private val sleepCalculationStoreRepository by lazy {  DataStoreRepository.getRepo(fs)}
    private val databaseRepository: DatabaseRepository by lazy {
        (fs.applicationContext as MainApplication).dataBaseRepository
    }
    private val dataStoreRepository: DataStoreRepository by lazy {
        (fs.applicationContext as MainApplication).dataStoreRepository
    }
    private val alarmLivedata by lazy{databaseRepository.activeAlarmsFlow().asLiveData()}
    private val userSleepTime by lazy{sleepCalculationStoreRepository.sleepApiDataFlow.asLiveData()}
    private val liveUserSleepActivityData by lazy{sleepCalculationStoreRepository.liveUserSleepActivityFlow.asLiveData()}

    fun resetSleepTime(){
        scope.launch {
            sleepCalculationStoreRepository.updateUserSleepTime(0)
            sleepCalculationStoreRepository.updateIsUserSleeping(false)
        }
    }

    fun getNextAlarm() : AlarmEntity? = runBlocking {
        return@runBlocking databaseRepository.getNextActiveAlarm()
    }

    fun updateAlarmWasFired(alarmFired: Boolean, alarmId: Int) {
        scope.launch {
            databaseRepository.updateAlarmWasFired(alarmFired, alarmId)
        }
    }

    fun setForegroundStatus(status: Boolean) {
        scope.launch {
            dataStoreRepository.backgroundUpdateIsActive(status)
        }
    }

    fun getForegroundStatus() : Boolean {
        var status = false
        scope.launch {
            status = dataStoreRepository.backgroundServiceFlow.first().isForegroundActive
        }

        return status
    }

    init {
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
    }
}
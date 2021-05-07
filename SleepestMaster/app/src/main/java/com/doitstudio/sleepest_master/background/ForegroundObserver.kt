package com.doitstudio.sleepest_master.background

import androidx.lifecycle.asLiveData
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationStoreRepository
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ForegroundObserver(private val fs:ForegroundService) {

    private val dataStoreRepository by lazy {  DataStoreRepository.getRepo(fs)}
    private val sleepCalculationStoreRepository by lazy {  SleepCalculationStoreRepository.getRepo(fs)}


    private val alarmActiveLifeData by lazy{dataStoreRepository.alarmFlow.asLiveData()}
    private val liveUserSleepActivityData by lazy{sleepCalculationStoreRepository.liveUserSleepActivityFlow.asLiveData()}
    private val userSleepTime by lazy{sleepCalculationStoreRepository.sleepApiDataFlow.asLiveData()}

    private val scope: CoroutineScope = MainScope()

    fun setAlarmTime(time:Int) {
        scope.launch {
            dataStoreRepository.updateAlarmTime(time.toLong())
        }
    }

    fun resetSleepTime(){
        scope.launch {
            sleepCalculationStoreRepository.updateUserSleepTime(0)
            sleepCalculationStoreRepository.updateIsUserSleeping(false)
            sleepCalculationStoreRepository.updateIsUserSleeping(false)
        }
    }

    init {
        alarmActiveLifeData.observe(fs){ alarm->
            fs.OnAlarmChanged(alarm)
        }

        liveUserSleepActivityData.observe(fs){la->
            fs.OnSleepTimeChanged(la)
        }

        userSleepTime.observe(fs){
            ust->
            fs.OnSleepApiDataChanged(ust)
        }


    }
}
package com.doitstudio.sleepest_master.background

import androidx.lifecycle.asLiveData
import com.doitstudio.sleepest_master.storage.DataStoreRepository

class ForegroundObserver(private val fs:ForegroundService) {

    private val dataStoreRepository by lazy {  DataStoreRepository.getRepo(fs)}

    private val alarmActiveLifeData by lazy{dataStoreRepository.alarmFlow.asLiveData()}
    private val sleepApiLifeData by lazy{dataStoreRepository.sleepApiDataFlow.asLiveData()}
    private val userSleepTime by lazy{dataStoreRepository.liveUserSleepActivityFlow.asLiveData()}

    init {
        alarmActiveLifeData.observe(fs){ alarm->
            fs.OnAlarmChanged(alarm)
        }

        sleepApiLifeData.observe(fs) { sleep->
            fs.OnSleepApiDataChanged(sleep)
        }

        userSleepTime.observe(fs) { sleeptime->
            fs.OnSleepTimeChanged(sleeptime)

        }
    }
}
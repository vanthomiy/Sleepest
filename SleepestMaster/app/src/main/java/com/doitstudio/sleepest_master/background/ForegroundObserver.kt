package com.doitstudio.sleepest_master.Background

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DataStoreRepository.Companion.getRepo

class ForegroundObserver(private val fs:ForegroundService) {

    /*
    private val dataStoreRepository by lazy {  DataStoreRepository.getRepo(fs)}

    private val alarmActiveLifeData by lazy{dataStoreRepository.alarmFlow.asLiveData()}

    init {
        alarmActiveLifeData.observe(fs){ alarm->
            fs.OnAlarmChanged(alarm)
        }
    }
     */
}
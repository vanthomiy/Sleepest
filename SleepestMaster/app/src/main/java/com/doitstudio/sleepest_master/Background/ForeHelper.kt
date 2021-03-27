package com.doitstudio.sleepest_master.Background

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import com.doitstudio.sleepest_master.storage.DataStoreRepository

class ForeHelper(val ds: DataStoreRepository, val lifedata:LifecycleOwner) {

    private val alarmActiveLiveData = ds.alarmFlow.asLiveData()

    public fun ObserveAlarm(fs: ForegroundService){

        alarmActiveLiveData.observe(lifedata)
        {
            alarm ->
            fs.OnAlarmChanged(alarm)
        }
    }
}

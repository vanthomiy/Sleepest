package com.doitstudio.sleepest_master.background

import androidx.lifecycle.asLiveData
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationStoreRepository
import com.doitstudio.sleepest_master.storage.DbRepository
import com.doitstudio.sleepest_master.storage.db.AlarmEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ForegroundObserver(private val fs:ForegroundService) {

    private val scope: CoroutineScope = MainScope()
    private val sleepCalculationStoreRepository by lazy {  SleepCalculationStoreRepository.getRepo(fs)}
    private val dbRepository: DbRepository by lazy {
        (fs.applicationContext as MainApplication).dbRepository
    }
    private val alarmLivedata by lazy{dbRepository.activeAlarmsFlow().asLiveData()}
    private val userSleepTime by lazy{sleepCalculationStoreRepository.sleepApiDataFlow.asLiveData()}
    private val liveUserSleepActivityData by lazy{sleepCalculationStoreRepository.liveUserSleepActivityFlow.asLiveData()}

    fun resetSleepTime(){
        scope.launch {
            sleepCalculationStoreRepository.updateUserSleepTime(0)
            sleepCalculationStoreRepository.updateIsUserSleeping(false)
        }
    }

    init {
        alarmLivedata.observe(fs) {

            val nextAlarm = it.minByOrNull { x->x.actualWakeup }

            if(nextAlarm != null)
                fs.OnAlarmChanged(nextAlarm)
        }

        userSleepTime.observe(fs){
                ust->
            fs.OnSleepApiDataChanged(ust)
        }

        liveUserSleepActivityData.observe(fs){la->
            fs.OnSleepTimeChanged(la)
        }
    }
}
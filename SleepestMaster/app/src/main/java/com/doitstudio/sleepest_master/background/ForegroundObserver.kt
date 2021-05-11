package com.doitstudio.sleepest_master.background

import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ForegroundObserver(private val fs:ForegroundService) {

    private val scope: CoroutineScope = MainScope()
    private val sleepCalculationStoreRepository by lazy {  SleepCalculationStoreRepository.getRepo(fs)}

    fun resetSleepTime(){
        scope.launch {
            sleepCalculationStoreRepository.updateUserSleepTime(0)
            sleepCalculationStoreRepository.updateIsUserSleeping(false)
        }
    }
}
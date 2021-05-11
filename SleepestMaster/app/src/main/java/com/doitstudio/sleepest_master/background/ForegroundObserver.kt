package com.doitstudio.sleepest_master.background

class ForegroundObserver(private val fs:ForegroundService) {

    /*
    private val dataStoreRepository by lazy {  DataStoreRepository.getRepo(fs)}

    private val alarmActiveLifeData by lazy{dataStoreRepository.alarmFlow.asLiveData()}

    fun resetSleepTime(){
        scope.launch {
            sleepCalculationStoreRepository.updateUserSleepTime(0)
            sleepCalculationStoreRepository.updateIsUserSleeping(false)
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
    }
     */
}
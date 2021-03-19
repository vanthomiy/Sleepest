package com.doitstudio.sleepest_master

import androidx.lifecycle.*
import com.doitstudio.sleepest_master.model.data.SleepSegmentEntity
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DbRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import kotlinx.coroutines.launch

class MainViewModel(private val dbRepository: DbRepository, private val storageRepository: DataStoreRepository) : ViewModel() {


    val alarmLiveData = storageRepository.alarmFlow.asLiveData()

    fun updateAlarmActive(alarmActive: Boolean) = viewModelScope.launch {
        storageRepository.updateAlarmActive(alarmActive)
    }

    fun updateAlarmTime(alarmTime: Long) = viewModelScope.launch {
        storageRepository.updateAlarmTime(alarmTime)
    }

    fun deleteAllSleepData() = viewModelScope.launch {
        dbRepository.deleteSleepApiRawData()
        dbRepository.deleteSleepSegments()
    }

    val allSleepSegmentsEntities: LiveData<List<SleepSegmentEntity>> =
        dbRepository.allSleepSegments.asLiveData()

    val allSleepApiRawDataEntities: LiveData<List<SleepApiRawDataEntity>> =
        dbRepository.allSleepApiRawData.asLiveData()

}

class MainViewModelFactory(private val dbRepository: DbRepository, private val storageRepository: DataStoreRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(dbRepository,storageRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



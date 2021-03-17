package com.doitstudio.sleepest_master

import androidx.lifecycle.*
import com.doitstudio.sleepest_master.model.data.SleepSegmentEntity
import com.doitstudio.sleepest_master.storage.StorageRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import kotlinx.coroutines.launch

class MainViewModel(private val repository: StorageRepository) : ViewModel() {


    val alarmActiveLiveData = repository.alarmActiveFlow.asLiveData()
    fun updateAlarmActive(alarmActive: Boolean) = viewModelScope.launch {
        repository.updateAlarmActive(alarmActive)
    }

    val alarmTimeLiveData = repository.alarmTimeFlow.asLiveData()
    fun updateAlarmTime(alarmTime: Int) = viewModelScope.launch {
        repository.updateAlarmTime(alarmTime)
    }

    fun deleteAllSleepData() = viewModelScope.launch {
        repository.deleteSleepApiRawData()
        repository.deleteSleepSegments()
    }

    val allSleepSegmentsEntities: LiveData<List<SleepSegmentEntity>> =
            repository.allSleepSegments.asLiveData()

    val allSleepApiRawDataEntities: LiveData<List<SleepApiRawDataEntity>> =
            repository.allSleepApiRawData.asLiveData()

}

class MainViewModelFactory(private val repository: StorageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



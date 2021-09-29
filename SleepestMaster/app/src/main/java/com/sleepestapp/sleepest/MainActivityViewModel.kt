package com.sleepestapp.sleepest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository


class MainActivityViewModel(
    val dataStoreRepository: DataStoreRepository,
    val dataBaseRepository: DatabaseRepository
) : ViewModel() {

    val alarmsLiveData by lazy {
        dataBaseRepository.alarmFlow.asLiveData()
    }

    val sleepParametersLiveData by lazy {
        dataStoreRepository.sleepParameterFlow.asLiveData()
    }

    val settingsLiveData by lazy {
        dataStoreRepository.settingsDataFlow.asLiveData()
    }
}


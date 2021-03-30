package com.doitstudio.sleepest_master

import androidx.lifecycle.*
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DbRepository
import kotlinx.coroutines.launch

class MainViewModel(private val dbRepository: DbRepository, private val storageRepository: DataStoreRepository) : ViewModel() {

    val sleepApiLiveData = storageRepository.sleepApiDataFlow.asLiveData()

    val liveUserSleepActivityLiveData = storageRepository.liveUserSleepActivityFlow.asLiveData()

    val userSleepSessionLiveData = dbRepository.userSleepSessionFlow.asLiveData()

    fun updatePermissionActive(permissionActive: Boolean) = viewModelScope.launch {
        storageRepository.updatePermissionActive(permissionActive)
    }

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



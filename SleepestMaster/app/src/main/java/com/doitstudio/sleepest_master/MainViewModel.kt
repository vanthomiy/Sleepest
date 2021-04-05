package com.doitstudio.sleepest_master

import androidx.lifecycle.*
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationStoreRepository
import com.doitstudio.sleepest_master.storage.DbRepository
import kotlinx.coroutines.launch

class MainViewModel(private val dbRepository: DbRepository, private val storageStoreRepository: SleepCalculationStoreRepository) : ViewModel() {

    val sleepApiLiveData = storageStoreRepository.sleepApiDataFlow.asLiveData()

    val liveUserSleepActivityLiveData = storageStoreRepository.liveUserSleepActivityFlow.asLiveData()

    fun updatePermissionActive(permissionActive: Boolean) = viewModelScope.launch {
        storageStoreRepository.updatePermissionActive(permissionActive)
    }

}

class MainViewModelFactory(private val dbRepository: DbRepository, private val storageStoreRepository: SleepCalculationStoreRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(dbRepository,storageStoreRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



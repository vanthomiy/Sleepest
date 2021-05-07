package com.doitstudio.sleepest_master

import androidx.lifecycle.*
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationDbRepository
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationStoreRepository
import com.doitstudio.sleepest_master.storage.DbRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import kotlinx.coroutines.launch

class MainViewModel(private val dbRepository: DbRepository, private val storageStoreRepository: SleepCalculationStoreRepository, private val sleepCalculationDbRepository: SleepCalculationDbRepository) : ViewModel() {

    val rawSleepApiData = sleepCalculationDbRepository.allSleepApiRawData.asLiveData()


    fun updatePermissionActive(permissionActive: Boolean) = viewModelScope.launch {
        storageStoreRepository.updatePermissionActive(permissionActive)
    }

    fun deleteApi()= viewModelScope.launch {
        sleepCalculationDbRepository.deleteSleepApiRawData()
    }

    fun insertSingleApi(data:SleepApiRawDataEntity)= viewModelScope.launch {
        sleepCalculationDbRepository.insertSleepApiRawData(data)
    }

    fun insertApi(data:List<SleepApiRawDataEntity>)= viewModelScope.launch {
        sleepCalculationDbRepository.insertSleepApiRawData(data)
    }



}

class MainViewModelFactory(private val dbRepository: DbRepository, private val storageStoreRepository: SleepCalculationStoreRepository,  private val sleepCalculationDbRepository: SleepCalculationDbRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(dbRepository,storageStoreRepository, sleepCalculationDbRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



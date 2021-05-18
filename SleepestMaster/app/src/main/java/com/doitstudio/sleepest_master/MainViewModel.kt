package com.doitstudio.sleepest_master

import androidx.lifecycle.*
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import kotlinx.coroutines.launch

class MainViewModel(private val databaseRepository: DatabaseRepository, private val storageStoreRepository: DataStoreRepository) : ViewModel() {



}

class MainViewModelFactory(private val databaseRepository: DatabaseRepository, private val storageStoreRepository: DataStoreRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(databaseRepository,storageStoreRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



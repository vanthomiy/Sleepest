package com.doitstudio.sleepest_master

import androidx.lifecycle.*
import com.doitstudio.sleepest_master.model.data.SleepState
import com.doitstudio.sleepest_master.model.data.SleepTimePattern
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationDbRepository
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationStoreRepository
import com.doitstudio.sleepest_master.sleepcalculation.db.SleepTimeModelEntity
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepModel
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepModelTypes
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepModelValues
import com.doitstudio.sleepest_master.sleepcalculation.model.algorithm.SleepTimeParameter
import com.doitstudio.sleepest_master.storage.DbRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.doitstudio.sleepest_master.storage.db.SleepSegmentEntity
import kotlinx.coroutines.launch

class MainViewModel(private val dbRepository: DbRepository, private val storageStoreRepository: SleepCalculationStoreRepository, private val sleepCalculationDbRepository: SleepCalculationDbRepository) : ViewModel() {

    val rawSleepApiData = sleepCalculationDbRepository.allSleepApiRawData.asLiveData()

    val allSleepTimeModels = sleepCalculationDbRepository.allSleepTimeModels.asLiveData()
    val allSleepStateModels = sleepCalculationDbRepository.allSleepStateModels.asLiveData()
    val allSleepStateFactorModels = sleepCalculationDbRepository.allSleepStateFactorModels.asLiveData()
    val allSleepTimeFactorModels = sleepCalculationDbRepository.allSleepTimeFactorModels.asLiveData()

    fun updatePermissionActive(permissionActive: Boolean) = viewModelScope.launch {
        storageStoreRepository.updatePermissionActive(permissionActive)
    }

    fun insertApi()= viewModelScope.launch {
        sleepCalculationDbRepository.insertSleepApiRawData(SleepApiRawDataEntity(1,2,3,2))
        sleepCalculationDbRepository.insertSleepApiRawData(SleepApiRawDataEntity(2,5,3,2))
        sleepCalculationDbRepository.insertSleepApiRawData(SleepApiRawDataEntity(3,7,3,2))
        sleepCalculationDbRepository.insertSleepApiRawData(SleepApiRawDataEntity(4,2,3,2))
        sleepCalculationDbRepository.insertSleepApiRawData(SleepApiRawDataEntity(5,24,3,2))
        //dbRepository.insertSleepSegment(SleepSegmentEntity(1,2,3,sleepState = SleepState.AWAKE))
    }

    fun insertSometing() = viewModelScope.launch {
        //dbRepository.insertSleepSegment(SleepSegmentEntity(1,2,3,sleepState = SleepState.AWAKE))
        val a = SleepTimeModelEntity(3,SleepTimePattern.ASLEEPTOEARLY,
            SleepModel(
                SleepModelTypes(
                    SleepModelValues(1f,1f,1f,1f,1f),
                    SleepModelValues(1f,1f,1f,1f,1f),
                    SleepModelValues(1f,1f,1f,1f,1f)
                    ),
                SleepModelTypes(
                    SleepModelValues(1f,1f,1f,1f,1f),
                    SleepModelValues(1f,1f,1f,1f,1f),
                    SleepModelValues(1f,1f,1f,1f,1f)
                ),
                SleepModelTypes(
                    SleepModelValues(1f,1f,1f,1f,1f),
                    SleepModelValues(1f,1f,1f,1f,1f),
                    SleepModelValues(1f,1f,1f,1f,1f)
                )),
            SleepModel(
                SleepModelTypes(
                    SleepModelValues(1f,1f,1f,1f,1f),
                    SleepModelValues(1f,1f,1f,1f,1f),
                    SleepModelValues(1f,1f,1f,1f,1f)
                ),
                SleepModelTypes(
                    SleepModelValues(1f,1f,1f,1f,1f),
                    SleepModelValues(1f,1f,1f,1f,1f),
                    SleepModelValues(1f,1f,1f,1f,1f)
                ),
                SleepModelTypes(
                    SleepModelValues(1f,1f,1f,1f,1f),
                    SleepModelValues(1f,1f,1f,1f,1f),
                    SleepModelValues(1f,1f,1f,1f,1f)
                )),
            SleepTimeParameter(1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,1)
        )

        sleepCalculationDbRepository.insertSleepTimeSegment(a)
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



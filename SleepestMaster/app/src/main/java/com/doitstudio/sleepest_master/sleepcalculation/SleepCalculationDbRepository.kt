package com.doitstudio.sleepest_master.sleepcalculation

import com.doitstudio.sleepest_master.sleepcalculation.db.*
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataDao
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import kotlinx.coroutines.flow.Flow


/**
*
 */
class SleepCalculationDbRepository(
        private val sleepStateModelDao: SleepStateModelDao,
        private val sleepTimeModelDao: SleepTimeModelDao,
        private val sleepStateParameterDao: SleepStateParameterDao,
        private val sleepTimeParameterDao: SleepTimeParameterDao,
        private val sleepApiRawDataDao: SleepApiRawDataDao
) {

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: SleepCalculationDbRepository? = null

        fun getRepo(sleepStateModelDao: SleepStateModelDao, sleepTimeModelDao: SleepTimeModelDao, sleepStateParameterDao: SleepStateParameterDao, sleepTimeParameterDao: SleepTimeParameterDao, sleepApiRawDataDao: SleepApiRawDataDao): SleepCalculationDbRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = SleepCalculationDbRepository(sleepStateModelDao, sleepTimeModelDao, sleepStateParameterDao, sleepTimeParameterDao, sleepApiRawDataDao)
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

    //region Sleep Time Models

    val allSleepTimeModels: Flow<List<SleepTimeModelEntity>> =
        sleepTimeModelDao.getAll()


    suspend fun insertSleepTimeSegment(sleepTimeModel: SleepTimeModelEntity) {
        sleepTimeModelDao.insert(sleepTimeModel)
    }

    suspend fun insertSleepTimeSegments(sleepTimeModels: List<SleepTimeModelEntity>) {
        sleepTimeModelDao.insertAll(sleepTimeModels)
    }

    //endregion

    //region Sleep State Models

    val allSleepStateModels: Flow<List<SleepStateModelEntity>> =
        sleepStateModelDao.getAll()


    suspend fun insertSleepStateSegments(sleepStateModels: List<SleepStateModelEntity>) {
        sleepStateModelDao.insertAll(sleepStateModels)
    }

    //endregion

    //region Sleep Time Parameters

    val allSleepTimeParameters: Flow<List<SleepTimeParameterEntity>> =
        sleepTimeParameterDao.getAll()


    suspend fun getSleepTimeParameterById(parameterId: Int) : SleepTimeParameterEntity? {
        return sleepTimeParameterDao.getParameterById(parameterId)
    }

    suspend fun insertSleepTimeParameters(sleepTimeParameters: List<SleepTimeParameterEntity>) {
        sleepTimeParameterDao.insertAll(sleepTimeParameters)
    }

    //endregion

    //region Sleep State Parameters

    val allSleepStateParameters: Flow<List<SleepStateParameterEntity>> =
        sleepStateParameterDao.getAll()

    suspend fun getSleepStateParameterById(parameterId: Int) : SleepStateParameterEntity {
        return sleepStateParameterDao.getParameterById(parameterId)
    }

    suspend fun insertSleepStateParameters(sleepStateParameterEntity: List<SleepStateParameterEntity>) {
        sleepStateParameterDao.insertAll(sleepStateParameterEntity)
    }

    //endregion


    //region Sleep API Data

    // Methods for SleepApiRawDataDao
    // Observed Flow will notify the observer when the data has changed.
    val allSleepApiRawData: Flow<List<SleepApiRawDataEntity>> =
        sleepApiRawDataDao.getAll()

    suspend fun insertSleepApiRawData(sleepClassifyEventEntity: SleepApiRawDataEntity) {
        sleepApiRawDataDao.insert(sleepClassifyEventEntity)
    }

    suspend fun deleteSleepApiRawData() {
        sleepApiRawDataDao.deleteAll()
    }

    suspend fun insertSleepApiRawData(sleepClassifyEventEntities: List<SleepApiRawDataEntity>) {
        sleepApiRawDataDao.insertAll(sleepClassifyEventEntities)
    }



    //endregion

}

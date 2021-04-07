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
    private val sleepStateFactorModelDao: SleepStateFactorModelDao,
    private val sleepTimeFactorModelDao: SleepTimeFactorModelDao,
    private val sleepApiRawDataDao: SleepApiRawDataDao
) {

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: SleepCalculationDbRepository? = null

        fun getRepo(sleepStateModelDao: SleepStateModelDao, sleepTimeModelDao: SleepTimeModelDao, sleepStateFactorModelDao: SleepStateFactorModelDao, sleepTimeFactorModelDao: SleepTimeFactorModelDao, sleepApiRawDataDao: SleepApiRawDataDao): SleepCalculationDbRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = SleepCalculationDbRepository(sleepStateModelDao, sleepTimeModelDao, sleepStateFactorModelDao, sleepTimeFactorModelDao, sleepApiRawDataDao)
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

    //region Sleep Time Models

    val allSleepTimeModels: Flow<List<SleepTimeModelEntity>> =
        sleepTimeModelDao.getAll()

    suspend fun getSleepTimeModelById(parameterId: Int) : Flow<List<SleepTimeModelEntity>> {
        return sleepTimeModelDao.getModelById(parameterId)
    }

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

    //region Sleep Time Factor Models

    val allSleepTimeFactorModels: Flow<List<SleepTimeFactorModelEntity>> =
        sleepTimeFactorModelDao.getAll()


    suspend fun insertSleepTimeFactorSegments(sleepTimeFactorModels: List<SleepTimeFactorModelEntity>) {
        sleepTimeFactorModelDao.insertAll(sleepTimeFactorModels)
    }

    //endregion

    //region Sleep State Models

    val allSleepStateFactorModels: Flow<List<SleepStateFactorModelEntity>> =
        sleepStateFactorModelDao.getAll()


    suspend fun insertSleepStateFactorSegments(sleepStateFactorModels: List<SleepStateFactorModelEntity>) {
        sleepStateFactorModelDao.insertAll(sleepStateFactorModels)
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

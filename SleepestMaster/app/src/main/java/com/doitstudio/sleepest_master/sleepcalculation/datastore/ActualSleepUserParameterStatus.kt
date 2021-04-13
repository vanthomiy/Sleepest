package com.doitstudio.sleepest_master.sleepcalculation.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import com.doitstudio.sleepest_master.ActualSleepUserParameter
import com.doitstudio.sleepest_master.LiveUserSleepActivity
import com.doitstudio.sleepest_master.model.data.SleepStatePattern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException


const val ACTUAL_SLEEP_USER_PARAMETER = "actual_sleep_user_parameter"

class ActualSleepUserParameterStatus(private val dataStore: DataStore<ActualSleepUserParameter>) {

    val actualSleepUserParameter: Flow<ActualSleepUserParameter> = dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.d("Error", exception.message.toString())
                    emit(ActualSleepUserParameter.getDefaultInstance())
                } else {
                    throw exception
                }
            }

    suspend fun updateSleepStatePattern(sleepStatePattern: String) {
        dataStore.updateData { preference ->
            preference.toBuilder().addSleepStatePattern(sleepStatePattern).build()
        }
    }

    suspend fun setSleepStatePatterns(sleepStatePatterns: List<String>) {
        dataStore.updateData { preference ->
            preference.toBuilder().addAllSleepStatePattern(sleepStatePatterns).build()
        }
    }

    suspend fun setSleepTimePatterns(sleepTimePatterns: List<String>) {
        dataStore.updateData { preference ->
            preference.toBuilder().addAllSleepTimePattern(sleepTimePatterns).build()
        }
    }

    suspend fun updateSleepTimePattern(sleepTimePattern: String) {
        dataStore.updateData { preference ->
            preference.toBuilder().addSleepStatePattern(sleepTimePattern).build()
        }
    }

    suspend fun updateUserStartPattern(userFactorPattern: String) {
        dataStore.updateData { preference ->
            preference.toBuilder().setUserFactorPattern(userFactorPattern).build()
        }
    }

}
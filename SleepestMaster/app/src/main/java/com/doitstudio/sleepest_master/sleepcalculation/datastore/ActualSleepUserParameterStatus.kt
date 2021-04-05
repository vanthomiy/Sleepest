package com.doitstudio.sleepest_master.sleepcalculation.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import com.doitstudio.sleepest_master.ActualSleepUserParameter
import com.doitstudio.sleepest_master.LiveUserSleepActivity
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

    suspend fun updateSleepStatePattern(sleepStatePattern: Int) {
        dataStore.updateData { preference ->
            preference.toBuilder().setSleepStatePattern(sleepStatePattern).build()
        }
    }

    suspend fun updateSleepTimePattern(sleepTimePattern: Int) {
        dataStore.updateData { preference ->
            preference.toBuilder().setSleepTimePattern(sleepTimePattern).build()
        }
    }

    suspend fun updateUserStartPattern(userStartPattern: Int) {
        dataStore.updateData { preference ->
            preference.toBuilder().setUserStartPattern(userStartPattern).build()
        }
    }

}
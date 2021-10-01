package com.sleepestapp.sleepest.model.data

import com.sleepestapp.sleepest.storage.db.SleepApiRawDataEntity
import com.sleepestapp.sleepest.storage.db.UserSleepSessionEntity

data class SleepDataAnalysis(
    val sleepSessionId: Int,
    val sleepApiRawDataEntity: List<SleepApiRawDataEntity>,
    val userSleepSessionEntity: UserSleepSessionEntity
)

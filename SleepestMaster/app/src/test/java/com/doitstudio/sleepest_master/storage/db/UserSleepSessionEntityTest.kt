package com.doitstudio.sleepest_master.storage.db

import android.service.autofill.Validators.not
import com.google.common.base.Predicates.equalTo
import org.hamcrest.CoreMatchers
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import org.hamcrest.CoreMatchers.*

class UserSleepSessionEntityTest{

    @Test
    fun IdAndTimestampDateCreation(){

        var idDate = UserSleepSessionEntity.getIdByDateTime(LocalDate.of(2021, 6, 3))
        // 7:36
        var idStamp = UserSleepSessionEntity.getIdByTimeStamp(1622612190)
        assertThat(idDate, CoreMatchers.equalTo(idStamp))

        // 20:36
        idStamp = UserSleepSessionEntity.getIdByTimeStamp(1622650832)
        assertThat((idDate != idStamp), CoreMatchers.equalTo(true))

        idDate = UserSleepSessionEntity.getIdByDateTime(LocalDate.of(2021, 6, 4))
        assertThat(idDate, CoreMatchers.equalTo(idStamp))

    }
}
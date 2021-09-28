package com.sleepestapp.sleepest.storage.db

import org.hamcrest.CoreMatchers
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class UserSleepSessionEntityTest{

    @Test
    fun idAndTimestampDateCreation(){

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
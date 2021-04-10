package com.doitstudio.sleepest_master.sleepcalculation.model.algorithm

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.UserFactorPattern


@Entity(tableName = "sleep_user_type")
data class SleepUserType(

        @PrimaryKey
        val userId:Int,

        val mobilePosition: MobilePosition,
        @Embedded(prefix = "live") val sleepLiveModel : SleepModel,	// Das Berechnete Live Model aus den Daten
        @Embedded(prefix = "full") val sleepFullModel : SleepModel,	 //Das Berechnete Model aus den Daten

        val userFactorPattern: UserFactorPattern, //	Faktor, je nach dem wie die allgemeinen werte kommt

        val sleepTimeLiveParams :	ArrayList<Int>, //	Erkanntes muster während des Schlafs
        val sleepStateLiveParams : ArrayList<Int> //	Erkanntes muster während des Schlafs

)
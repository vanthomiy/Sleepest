package com.doitstudio.sleepest_master.sleepcalculation.model.algorithm

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.SleepStatePattern
import com.doitstudio.sleepest_master.model.data.SleepTimePattern
import com.doitstudio.sleepest_master.model.data.UserFactorPattern


@Entity(tableName = "sleep_user_type")
data class SleepUserType(

       // @PrimaryKey
       // val userId:Int,

        var mobilePosition: MobilePosition = MobilePosition.UNIDENTIFIED,
        @Embedded(prefix = "live") var sleepLiveModel : SleepModel = SleepModel(),	// Das Berechnete Live Model aus den Daten
        @Embedded(prefix = "full") var sleepFullModel : SleepModel = SleepModel(),	 //Das Berechnete Model aus den Daten

        var userFactorPattern: UserFactorPattern = UserFactorPattern.NONE, //	Faktor, je nach dem wie die allgemeinen werte kommt

        var sleepTimeLiveParams :	ArrayList<SleepTimePattern> = arrayListOf(), //	Erkanntes muster während des Schlafs
        var sleepStateLiveParams : ArrayList<SleepStatePattern> = arrayListOf()//	Erkanntes muster während des Schlafs

)
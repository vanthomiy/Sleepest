package com.doitstudio.sleepest_master.sleepcalculation.model.algorithm

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.SleepTimePattern
import com.doitstudio.sleepest_master.model.data.UserStartPattern

@Entity(tableName = "sleep_user_type")
data class SleepUserType(

        @PrimaryKey
        val id:Int,

        val mobilePosition: MobilePosition,
        @Embedded val sleepLiveModel : SleepModel,	// Das Berechnete Live Model aus den Daten
        @Embedded val sleepFullModel : SleepModel,	 //Das Berechnete Model aus den Daten

        val userStartPattern:	UserStartPattern, //	Faktor, je nach dem wie die allgemeinen werte kommt
        val sleepTimeLiveParams :	List<SleepTimePattern>, //	Erkanntes muster während des Schlafs
        val sleepStateLiveParams : List<SleepTimePattern> //	Erkanntes muster während des Schlafs

)
package com.doitstudio.sleepest_master.sleepcalculation.model.algorithm

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.doitstudio.sleepest_master.model.data.SleepStatePattern

@Entity
data class SleepStateParameter(

        val sleepSleepBorder	:Int,//	Schlaf-Schlafgrenze
        val deepSleepSleepBorder:	Int,//	Schlaf-Tiefschlafphasengrenze
        val remSleepSleepBorder	:Int,	//Schlaf-Remschlafphasengrenze
        val sleepMotionBorder	:Int,	//Motion-Schlafgrenze
        val deepSleepMotionBorder:	Int,	//Motion-Tiefschlafphasengrenze
        val remSleepMotionBorder:	Int,//	Motion-Remschlafphasengrenze
        val sleepLightBorder	:Int	,//Licht-Schlafgrenze
        val deepSleepLightBorder:	Int,//	Licht-Tiefschlafphasengrenze
        val remSleepLightBorder	:Int,//	Licht-Remschlafphasengrenze
        val soundClearSleepBorder:	Int,	//Soundbereinigung Schlafgrenze
        val soundClearMotionBorder:	Int,//	Soundbereinigung Motion grenze
        val modelMatchPercentage	:Int//	Zu wie viel Prozent sollten die Models übereinstimmen



)
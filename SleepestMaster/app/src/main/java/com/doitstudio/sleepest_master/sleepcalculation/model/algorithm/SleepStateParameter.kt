package com.doitstudio.sleepest_master.sleepcalculation.model.algorithm

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.doitstudio.sleepest_master.model.data.SleepStatePattern

@Entity
data class SleepStateParameter(

        val sleepSleepBorder	:Float,//	Schlaf-Schlafgrenze
        val deepSleepSleepBorder:	Float,//	Schlaf-Tiefschlafphasengrenze
        val remSleepSleepBorder	:Float,	//Schlaf-Remschlafphasengrenze
        val sleepMotionBorder	:Float,	//Motion-Schlafgrenze
        val deepSleepMotionBorder:	Float,	//Motion-Tiefschlafphasengrenze
        val remSleepMotionBorder:	Float,//	Motion-Remschlafphasengrenze
        val sleepLightBorder	:Float	,//Licht-Schlafgrenze
        val deepSleepLightBorder:	Float,//	Licht-Tiefschlafphasengrenze
        val remSleepLightBorder	:Float,//	Licht-Remschlafphasengrenze
        val soundClearSleepBorder:	Float,	//Soundbereinigung Schlafgrenze
        val soundClearMotionBorder:	Float,//	Soundbereinigung Motion grenze
        val modelMatchPercentage	:Int//	Zu wie viel Prozent sollten die Models übereinstimmen



)
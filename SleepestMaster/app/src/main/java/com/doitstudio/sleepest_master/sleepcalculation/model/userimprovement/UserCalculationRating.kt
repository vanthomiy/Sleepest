package com.doitstudio.sleepest_master.sleepcalculation.model.userimprovement

import com.doitstudio.sleepest_master.model.data.SleepTimeAdjustment

data class UserCalculationRating(

        val sleepStartDetection	: SleepTimeAdjustment = SleepTimeAdjustment.NONE,//	Später oder früher geschlafen
        val sleepEndDetection	: SleepTimeAdjustment= SleepTimeAdjustment.NONE,//	Später oder früher aufgewacht
        val awakeDetection	:Int = 0, //	Wie gut wurde die kalkulation empfunden (1-10)
        val sleepCalcRating	:Int = 0//	Wie gut wurde die kalkulation empfunden (1-10)

)
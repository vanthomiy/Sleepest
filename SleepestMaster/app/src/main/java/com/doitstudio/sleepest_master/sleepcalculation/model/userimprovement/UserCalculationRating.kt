package com.doitstudio.sleepest_master.sleepcalculation.model.userimprovement

import com.doitstudio.sleepest_master.model.data.SleepTimePattern

data class UserCalculationRating(

        val sleepStartDetection	: SleepTimePattern = SleepTimePattern.NONE,//	Später oder früher geschlafen
        val sleepEndDetection	: SleepTimePattern= SleepTimePattern.NONE,//	Später oder früher aufgewacht
        val awakeDetection	:Int = 0, //	Wie gut wurde die kalkulation empfunden (1-10)
        val sleepCalcRating	:Int = 0//	Wie gut wurde die kalkulation empfunden (1-10)

)
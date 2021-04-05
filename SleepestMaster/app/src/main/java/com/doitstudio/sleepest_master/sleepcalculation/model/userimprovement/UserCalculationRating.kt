package com.doitstudio.sleepest_master.sleepcalculation.model.userimprovement

import com.doitstudio.sleepest_master.model.data.SleepTimePattern

data class UserCalculationRating(

    val sleepStartDetection	: SleepTimePattern,//	Später oder früher geschlafen
    val sleepEndDetection	: SleepTimePattern,//	Später oder früher aufgewacht
    val awakeDetection	:Int, //	Wie gut wurde die kalkulation empfunden (1-10)
    val sleepCalcRating	:Int//	Wie gut wurde die kalkulation empfunden (1-10)

)
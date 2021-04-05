package com.doitstudio.sleepest_master.sleepcalculation.model.algorithm


data class SleepTimeParameter(


        val awakeTime:Float,//	wie lange soll die awake zeit gezählt werden (Zukunft)
        val sleepTime:	Float,//	wie lange sollen vergangenheitswerte für einschlaf gezählt werden
        val wakeUpTime:	Float,//	 wie lange sollen vergangenheitswerte für aufwachen gezählt werden
        val sleepSleepBorder:	Float,//	grenze zum einschlafen
        val awakeSleepBorder	:Float,	//grenze zum aufwachen
        val sleepMotionBorder:	Float,//	grenze zum einschlafen
        val awakeMotionBorder	:Float,//	grenze zum aufwachen
        val sleepMedianOverTime:	Float,//	grenze welche der median für einschlafen haben sollte
        val diffSleep:	Float,	//grenze welche die diff im median für einschlafen haben sollte
        val diffSleepFuture:	Float,//	grenze welche die diff im median für einschlafen in zukunft haben sollte
        val awakeMedianOverTime:	Float,//	grenze welche der median für aufwachen haben sollte
        val diffAwake	:Float,	//grenze welche die diff im median für aufwachen haben sollte
        val modelMatchPercentage:	Int//	Zu wie viel Prozent sollten die Models übereinstimmen
)
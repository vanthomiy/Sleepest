package com.doitstudio.sleepest_master.sleepcalculation.model.algorithm

import androidx.room.Entity
import com.doitstudio.sleepest_master.model.data.SleepTimePattern
import kotlin.math.absoluteValue

@Entity
data class SleepTimeParameter(


        var awakeTime:Int = 1800,//	wie lange soll die awake zeit gezählt werden (Zukunft)
        var sleepTime:	Int = 3000 ,//	wie lange sollen vergangenheitswerte für einschlaf gezählt werden
        var wakeUpTime:	Int = 5400,//	 wie lange sollen vergangenheitswerte für aufwachen gezählt werden
        var sleepSleepBorder:	Float = 50f,//	grenze zum einschlafen
        var awakeSleepBorder	:Float = 20f,	//grenze zum aufwachen
        var sleepMotionBorder:	Float = 4f,//	grenze zum einschlafen
        var awakeMotionBorder	:Float = 0f,//	grenze zum aufwachen
        var sleepMedianOverTime:	Float = 75f,//	grenze welche der median für einschlafen haben sollte
        var diffSleep:	Float = 50f,	//grenze welche die diff im median für einschlafen haben sollte
        var diffSleepFuture:	Float = 0f,//	grenze welche die diff im median für einschlafen in zukunft haben sollte
        var awakeMedianOverTime:	Float = 30f,//	grenze welche der median für aufwachen haben sollte
        var diffAwake	:Float = -5f,	//grenze welche die diff im median für aufwachen haben sollte
        var modelMatchPercentage:	Int = 95//	Zu wie viel Prozent sollten die Models übereinstimmen
)
{
    companion object{

        /**
         * Functions for multiplying two parameters with each other
         */
        fun multiplyParameterByParameter(sleepTimeParam: SleepTimeParameter, userFactorParam: SleepTimeParameter) : SleepTimeParameter {

            return SleepTimeParameter(

                    sleepTimeParam.awakeTime * userFactorParam.awakeTime,
                    sleepTimeParam.sleepTime * userFactorParam.sleepTime,
                    sleepTimeParam.wakeUpTime * userFactorParam.wakeUpTime,
                    sleepTimeParam.sleepSleepBorder * userFactorParam.sleepSleepBorder,
                    sleepTimeParam.awakeSleepBorder * userFactorParam.awakeSleepBorder,
                    sleepTimeParam.sleepMotionBorder * userFactorParam.sleepMotionBorder,
                    sleepTimeParam.awakeMotionBorder * userFactorParam.awakeMotionBorder,
                    sleepTimeParam.sleepMedianOverTime * userFactorParam.sleepMedianOverTime,
                    sleepTimeParam.diffSleep * userFactorParam.diffSleep,
                    sleepTimeParam.diffSleepFuture * userFactorParam.diffSleepFuture,
                    sleepTimeParam.awakeMedianOverTime * userFactorParam.awakeMedianOverTime,
                    sleepTimeParam.diffAwake * userFactorParam.diffAwake,
                    sleepTimeParam.modelMatchPercentage * userFactorParam.modelMatchPercentage
            )
        }

        /**
         * Functions for merge a bunch of paremeters together
         */
        fun mergeParameters(list:List<SleepTimeParameter>) : SleepTimeParameter {

            // merge the parameters together
            // we use the.. what is the most far away from the normal to define whats the new parameter
            // so we create a "Standard" parameter and check which param is away the most

            val reference = SleepTimeParameter()
            var parameter = SleepTimeParameter()

            list.forEach {

                if ((parameter.sleepSleepBorder - it.sleepSleepBorder).absoluteValue > (parameter.sleepSleepBorder - reference.sleepSleepBorder).absoluteValue) {
                    parameter.sleepSleepBorder = it.sleepSleepBorder;
                }

                if ((parameter.awakeSleepBorder - it.awakeSleepBorder).absoluteValue > (parameter.awakeSleepBorder - reference.awakeSleepBorder).absoluteValue) {
                    parameter.awakeSleepBorder = it.awakeSleepBorder;
                }

                if ((parameter.sleepMotionBorder - it.sleepMotionBorder).absoluteValue > (parameter.sleepMotionBorder - reference.sleepMotionBorder).absoluteValue) {
                    parameter.sleepMotionBorder = it.sleepMotionBorder;
                }

                if ((parameter.sleepMotionBorder - it.sleepMotionBorder).absoluteValue > (parameter.sleepMotionBorder - reference.sleepMotionBorder).absoluteValue) {
                    parameter.sleepMotionBorder = it.sleepMotionBorder;
                }

                if ((parameter.awakeMotionBorder - it.awakeMotionBorder).absoluteValue > (parameter.awakeMotionBorder - reference.awakeMotionBorder).absoluteValue) {
                    parameter.awakeMotionBorder = it.awakeMotionBorder;
                }

                if ((parameter.sleepMedianOverTime - it.sleepMedianOverTime).absoluteValue > (parameter.sleepMedianOverTime - reference.sleepMedianOverTime).absoluteValue) {
                    parameter.sleepMedianOverTime = it.sleepMedianOverTime;
                }

                if ((parameter.diffSleep - it.diffSleep).absoluteValue > (parameter.diffSleep - reference.diffSleep).absoluteValue) {
                    parameter.diffSleep = it.diffSleep;
                }

                if ((parameter.diffSleepFuture - it.diffSleepFuture).absoluteValue > (parameter.diffSleepFuture - reference.diffSleepFuture).absoluteValue) {
                    parameter.diffSleepFuture = it.diffSleepFuture;
                }

                if ((parameter.awakeMedianOverTime - it.awakeMedianOverTime).absoluteValue > (parameter.awakeMedianOverTime - reference.awakeMedianOverTime).absoluteValue) {
                    parameter.awakeMedianOverTime = it.awakeMedianOverTime;
                }

                if ((parameter.diffAwake - it.diffAwake).absoluteValue > (parameter.diffAwake - reference.diffAwake).absoluteValue) {
                    parameter.diffAwake = it.diffAwake;

                }
            }

            return parameter
        }

    }

}
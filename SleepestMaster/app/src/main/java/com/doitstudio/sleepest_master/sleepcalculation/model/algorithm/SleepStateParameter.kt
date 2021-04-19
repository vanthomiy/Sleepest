package com.doitstudio.sleepest_master.sleepcalculation.model.algorithm

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.doitstudio.sleepest_master.model.data.SleepStatePattern
import kotlin.math.absoluteValue

@Entity
data class SleepStateParameter(

        var sleepSleepBorder	:Float = 20f,//	Schlaf-Schlafgrenze
        var deepSleepSleepBorder:	Float = 90f,//	Schlaf-Tiefschlafphasengrenze
        var remSleepSleepBorder	:Float = 95f,	//Schlaf-Remschlafphasengrenze
        var sleepMotionBorder	:Float = 5f,	//Motion-Schlafgrenze
        var deepSleepMotionBorder:	Float = 3f,	//Motion-Tiefschlafphasengrenze
        var remSleepMotionBorder:	Float = 1f,//	Motion-Remschlafphasengrenze
        var sleepLightBorder	:Float = 5f	,//Licht-Schlafgrenze
        var deepSleepLightBorder:	Float = 3f,//	Licht-Tiefschlafphasengrenze
        var remSleepLightBorder	:Float = 1f,//	Licht-Remschlafphasengrenze
        var soundClearSleepBorder:	Float = 7f,	//Soundbereinigung Schlafgrenze
        var soundClearMotionBorder:	Float = 1f,//	Soundbereinigung Motion grenze
        var modelMatchPercentage	:Int = 98//	Zu wie viel Prozent sollten die Models übereinstimmen



)

{
    companion object{

        /**
         * Functions for merge a bunch of paremeters together
         */
        fun mergeParameters(list:List<SleepStateParameter>) : SleepStateParameter {

            // merge the parameters together
            // we use the.. what is the most far away from the normal to define whats the new parameter
            // so we create a "Standard" parameter and check which param is away the most

            val reference = SleepStateParameter()
            var parameter = SleepStateParameter()

            list.forEach {

                if ((reference.sleepSleepBorder - it.sleepSleepBorder).absoluteValue > (parameter.sleepSleepBorder - reference.sleepSleepBorder).absoluteValue) {
                    parameter.sleepSleepBorder = it.sleepSleepBorder;
                }

                if ((reference.deepSleepSleepBorder - it.deepSleepSleepBorder).absoluteValue > (parameter.deepSleepSleepBorder - reference.deepSleepSleepBorder).absoluteValue) {
                    parameter.deepSleepSleepBorder = it.deepSleepSleepBorder;
                }

                if ((reference.remSleepSleepBorder- it.remSleepSleepBorder).absoluteValue > (parameter. remSleepSleepBorder - reference. remSleepSleepBorder).absoluteValue) {
                    parameter. remSleepSleepBorder = it. remSleepSleepBorder;
                }

                if ((reference.sleepMotionBorder - it.sleepMotionBorder).absoluteValue > (parameter.sleepMotionBorder - reference.sleepMotionBorder).absoluteValue) {
                    parameter.sleepMotionBorder = it.sleepMotionBorder;
                }

                if ((reference.deepSleepMotionBorder - it.deepSleepMotionBorder).absoluteValue > (parameter.deepSleepMotionBorder - reference.deepSleepMotionBorder).absoluteValue) {
                    parameter.deepSleepMotionBorder = it.deepSleepMotionBorder;
                }

                if ((reference.remSleepMotionBorder - it.remSleepMotionBorder).absoluteValue > (parameter.remSleepMotionBorder - reference.remSleepMotionBorder).absoluteValue) {
                    parameter.remSleepMotionBorder = it.remSleepMotionBorder;
                }

                if ((reference.sleepLightBorder - it.sleepLightBorder).absoluteValue > (parameter.sleepLightBorder - reference.sleepLightBorder).absoluteValue) {
                    parameter.sleepLightBorder = it.sleepLightBorder;
                }

                if ((reference.deepSleepLightBorder - it.deepSleepLightBorder).absoluteValue > (parameter.deepSleepLightBorder - reference.deepSleepLightBorder).absoluteValue) {
                    parameter.deepSleepLightBorder = it.deepSleepLightBorder;
                }

                if ((reference.remSleepLightBorder - it.remSleepLightBorder).absoluteValue > (parameter.remSleepLightBorder - reference.remSleepLightBorder).absoluteValue) {
                    parameter.remSleepLightBorder = it.remSleepLightBorder;
                }

                if ((reference.soundClearSleepBorder - it.soundClearSleepBorder).absoluteValue > (parameter.soundClearSleepBorder - reference.soundClearSleepBorder).absoluteValue) {
                    parameter.soundClearSleepBorder = it.soundClearSleepBorder;
                }

                if ((reference.soundClearMotionBorder - it.soundClearMotionBorder).absoluteValue > (parameter.soundClearMotionBorder - reference.soundClearMotionBorder).absoluteValue) {
                    parameter.soundClearMotionBorder = it.soundClearMotionBorder;
                }

                if ((reference.modelMatchPercentage - it.modelMatchPercentage).absoluteValue > (parameter.modelMatchPercentage - reference.modelMatchPercentage).absoluteValue) {
                    parameter.modelMatchPercentage = it.modelMatchPercentage;
                }

            }

            return parameter
        }

    }

}
package com.doitstudio.sleepest_master.ui.info

import android.content.Context
import android.graphics.drawable.Drawable
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.model.data.Info
import com.doitstudio.sleepest_master.model.data.InfoEntityStlye

data class InfoEntity (
    val image : Int?,
    val lottie : Int?,
    val textHeader : String?,
    val textDescription : String?,
    val infoEntityStlye : InfoEntityStlye = InfoEntityStlye.RANDOM
)
{
    companion object{

        fun getInfo(info: Info, id:Int, context: Context) : List<InfoEntity>{
            return when(info){
                Info.MONTH_HISTORY -> monthHistoryInfo(id, context)
                Info.DAY_HISTORY -> dayHistoryInfo(id, context)
                Info.WEEK_HISTORY -> weekHistoryInfo(id, context)
                Info.SETTINGS -> settingsInfo(id, context)
                Info.SLEEP -> sleepInfo(id, context)
                else -> noInfo(context)
            }
        }

        private fun dayHistoryInfo(id:Int, context: Context) : List<InfoEntity>{
            return when(id){
                0 ->  listOf(
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.history_day_information_sleepPhases_lineChart),
                        infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                    )
                )
                1 ->  listOf(
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.history_day_information_timeInPhase),
                        infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                    )
                )
                2 ->  listOf(
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.history_day_information_sleepQualityRating),
                        infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                    )
                )
                3 ->  listOf(
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.history_day_information_activity),
                        infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                    )
                )
                else -> noInfo(context)
            }
        }

        private fun weekHistoryInfo(id:Int, context: Context) : List<InfoEntity>{
            return when(id){
                0 ->  listOf(
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.history_week_information_sleepPhases_barChart),
                        infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                    )
                )
                1 ->  listOf(
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.history_week_information_activity_lineChart),
                        infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                    )
                )
                else -> noInfo(context)
            }
        }

        private fun monthHistoryInfo(id:Int, context: Context) : List<InfoEntity>{
            return when(id){
                0 ->  listOf(
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.history_month_information_sleepPhases_barChart),
                        infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                    )
                )
                1 ->  listOf(
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.history_month_information_activity_lineChart),
                        infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                    )
                )
                else -> noInfo(context)
            }
        }


        private fun settingsInfo(id:Int, context: Context) : List<InfoEntity>{
            return when(id){
                0 ->  listOf(InfoEntity(
                    image = R.drawable.ic_settings_black_24dp,
                    lottie = null,
                    textHeader = "",
                    textDescription = context.resources.getString(R.string.sleep_general_info_1),
                    infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                    )
                )
                else -> noInfo(context)
            }
        }

        private fun sleepInfo(id:Int, context: Context) : List<InfoEntity>{
            return when(id){
                7 ->  listOf(
                    InfoEntity(
                    image = null,
                    lottie = R.raw.sleeping_polar_bear,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_general_info_1),
                    infoEntityStlye = InfoEntityStlye.PICTURE_TOP
                    ),

                    InfoEntity(
                    image = null,
                    lottie = null,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_general_info_2),
                    infoEntityStlye = InfoEntityStlye.PICTURE_RIGHT
                    ),

                    InfoEntity(
                        image = null,
                        lottie = R.raw.gold_scores_icon,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_general_info_3),
                        infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                    )
                )
                0 ->  listOf(InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_sleeptimes_info_1),
                    infoEntityStlye = InfoEntityStlye.PICTURE_RIGHT
                    ),
                    InfoEntity(
                        image = R.drawable.monitoring,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_sleeptimes_info_2),
                        infoEntityStlye = InfoEntityStlye.PICTURE_RIGHT
                    ),
                    InfoEntity(
                        image = null,
                        lottie = R.raw.idea_bulb,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_sleeptimes_info_3),
                        infoEntityStlye = InfoEntityStlye.PICTURE_RIGHT
                    )
                )
                1 ->  listOf(InfoEntity(
                    image = null,
                    lottie = null,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_sleepduration_info_1),
                    infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                ),
                    InfoEntity(
                        image = null,
                        lottie = R.raw.idea_bulb,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_sleepduration_info_2),
                        infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                    )
                )
                6 ->  listOf(InfoEntity(
                    image = null,
                    lottie = null,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_lightcondition_info_1),
                    infoEntityStlye = InfoEntityStlye.PICTURE_RIGHT
                ),
                    InfoEntity(
                        image = null,
                        lottie = R.raw.idea_bulb,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_lightcondition_info_2),
                        infoEntityStlye = InfoEntityStlye.PICTURE_RIGHT
                    )
                )
                2 ->  listOf(InfoEntity(
                    image = null,
                    lottie = null,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_phoneposition_info_1),
                    infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                ),
                    InfoEntity(
                        image = null,
                        lottie = R.raw.idea_bulb,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_phoneposition_info_2),
                        infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                    )
                )
                3 ->  listOf(InfoEntity(
                    image = null,
                    lottie = R.raw.using_mobile_phone,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_phoneusage_info_1),
                    infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                ),
                    InfoEntity(
                        image = null,
                        lottie = R.raw.idea_bulb,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_phoneusage_info_2),
                        infoEntityStlye = InfoEntityStlye.PICTURE_RIGHT
                    )
                )
                4 ->  listOf(InfoEntity(
                    image = null,
                    lottie = R.raw.character_walk,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_activitytracking_info_1),
                    infoEntityStlye = InfoEntityStlye.PICTURE_TOP
                ),
                    InfoEntity(
                        image = null,
                        lottie = R.raw.idea_bulb,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_activitytracking_info_2),
                        infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                    )
                )
                else -> noInfo(context)
            }
        }

        private fun noInfo(context: Context) : List<InfoEntity>{
            return listOf(InfoEntity(
                image = R.drawable.empty_alarms,
                lottie = R.raw.empty,
                textHeader = "Wrong",
                textDescription = null
                )
            )
        }

    }
}


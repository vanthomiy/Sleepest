package com.sleepestapp.sleepest.ui.info

import android.content.Context
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.model.data.Info
import com.sleepestapp.sleepest.model.data.InfoEntityStyle

/**
 * This is used to display information to the user in the
 * We can handle the information at one point and it provides the same layout for all sites.
 */
data class InfoEntity (
    val image : Int?,
    val lottie : Int?,
    val textHeader : String?,
    val textDescription : String?,
    val infoEntityStyle : InfoEntityStyle = InfoEntityStyle.RANDOM
)
{
    companion object{

        /**
         * Returns a list of all Info segments that are stored for a specific [Info] and [id]
         */
        fun getInfo(info: Info, id:Int, context: Context) : List<InfoEntity>{
            return when(info){
                Info.MONTH_HISTORY -> monthHistoryInfo(id, context)
                Info.DAY_HISTORY -> dayHistoryInfo(id, context)
                Info.WEEK_HISTORY -> weekHistoryInfo(id, context)
                Info.SETTINGS -> settingsInfo(id, context)
                Info.SLEEP -> sleepInfo(id, context)
                Info.HISTORY -> historyInfo(id, context)
                else -> noInfo(context)
            }
        }

        /**
         * Actual information for the [Info.HISTORY]
         */
        private fun historyInfo(id:Int, context: Context) : List<InfoEntity>{
            return when(id){
                0 ->  listOf(
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = context.resources.getString(R.string.history_sleep_phases_header_light_sleep),
                        textDescription = context.resources.getString(R.string.history_sleep_phases_information_light),
                        infoEntityStyle = InfoEntityStyle.PICTURE_LEFT
                    ),
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = context.resources.getString(R.string.history_sleep_phases_header_deep_sleep),
                        textDescription = context.resources.getString(R.string.history_sleep_phases_information_deep),
                        infoEntityStyle = InfoEntityStyle.PICTURE_LEFT
                    ),
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = context.resources.getString(R.string.history_sleep_phases_header_rem_sleep),
                        textDescription = context.resources.getString(R.string.history_sleep_phases_information_rem),
                        infoEntityStyle = InfoEntityStyle.PICTURE_LEFT
                    )
                )
                else -> noInfo(context)
            }
        }

        /**
         * Actual information for the [Info.DAY_HISTORY]
         */
        private fun dayHistoryInfo(id:Int, context: Context) : List<InfoEntity>{
            return when(id){
                0 ->  listOf(
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.history_day_information_sleepPhases_lineChart),
                        infoEntityStyle = InfoEntityStyle.PICTURE_LEFT
                    )
                )
                1 ->  listOf(
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.history_day_information_timeInPhase),
                        infoEntityStyle = InfoEntityStyle.PICTURE_LEFT
                    )
                )
                2 ->  listOf(
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.history_day_information_sleepQualityRating),
                        infoEntityStyle = InfoEntityStyle.PICTURE_LEFT
                    )
                )
                3 ->  listOf(
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.history_day_information_activity),
                        infoEntityStyle = InfoEntityStyle.PICTURE_LEFT
                    )
                )
                else -> noInfo(context)
            }
        }

        /**
         * Actual information for the [Info.WEEK_HISTORY]
         */
        private fun weekHistoryInfo(id:Int, context: Context) : List<InfoEntity>{
            return when(id){
                0 ->  listOf(
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.history_week_information_sleepPhases_barChart),
                        infoEntityStyle = InfoEntityStyle.PICTURE_LEFT
                    )
                )
                1 ->  listOf(
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.history_week_information_activity_lineChart),
                        infoEntityStyle = InfoEntityStyle.PICTURE_LEFT
                    )
                )
                else -> noInfo(context)
            }
        }

        /**
         * Actual information for the [Info.MONTH_HISTORY]
         */
        private fun monthHistoryInfo(id:Int, context: Context) : List<InfoEntity>{
            return when(id){
                0 ->  listOf(
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.history_month_information_sleepPhases_barChart),
                        infoEntityStyle = InfoEntityStyle.PICTURE_LEFT
                    )
                )
                1 ->  listOf(
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.history_month_information_activity_lineChart),
                        infoEntityStyle = InfoEntityStyle.PICTURE_LEFT
                    )
                )
                else -> noInfo(context)
            }
        }

        /**
         * Actual information for the [Info.SETTINGS]
         */
        private fun settingsInfo(id:Int, context: Context) : List<InfoEntity>{
            return when(id){
                0 ->  listOf(InfoEntity(
                    image = R.drawable.ic_settings_black_24dp,
                    lottie = null,
                    textHeader = "",
                    textDescription = context.resources.getString(R.string.sleep_general_info_1),
                    infoEntityStyle = InfoEntityStyle.PICTURE_LEFT
                    )
                )
                else -> noInfo(context)
            }
        }

        /**
         * Actual information for the [Info.SLEEP]
         */
        private fun sleepInfo(id:Int, context: Context) : List<InfoEntity>{
            return when(id){
                7 ->  listOf(
                    InfoEntity(
                    image = null,
                    lottie = R.raw.sleeping_polar_bear,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_general_info_1),
                    infoEntityStyle = InfoEntityStyle.PICTURE_TOP
                    ),

                    InfoEntity(
                    image = null,
                    lottie = null,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_general_info_2),
                    infoEntityStyle = InfoEntityStyle.PICTURE_RIGHT
                    ),

                    InfoEntity(
                        image = null,
                        lottie = R.raw.gold_scores_icon,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_general_info_3),
                        infoEntityStyle = InfoEntityStyle.PICTURE_LEFT
                    )
                )
                0 ->  listOf(InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_sleeptimes_info_1),
                    infoEntityStyle = InfoEntityStyle.PICTURE_RIGHT
                    ),
                    InfoEntity(
                        image = R.drawable.monitoring,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_sleeptimes_info_2),
                        infoEntityStyle = InfoEntityStyle.PICTURE_RIGHT
                    ),
                    InfoEntity(
                        image = R.drawable.light_bulb,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_sleeptimes_info_3),
                        infoEntityStyle = InfoEntityStyle.PICTURE_LEFT
                    )
                )
                1 ->  listOf(InfoEntity(
                    image = null,
                    lottie = null,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_sleepduration_info_1),
                    infoEntityStyle = InfoEntityStyle.PICTURE_LEFT
                ),
                    InfoEntity(
                        image = R.drawable.light_bulb,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_sleepduration_info_2),
                        infoEntityStyle = InfoEntityStyle.PICTURE_LEFT
                    )
                )
                6 ->  listOf(InfoEntity(
                    image = null,
                    lottie = null,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_lightcondition_info_1),
                    infoEntityStyle = InfoEntityStyle.PICTURE_RIGHT
                ),
                    InfoEntity(
                        image = R.drawable.light_bulb,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_lightcondition_info_2),
                        infoEntityStyle = InfoEntityStyle.PICTURE_RIGHT
                    )
                )
                2 ->  listOf(InfoEntity(
                    image = null,
                    lottie = null,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_phoneposition_info_1),
                    infoEntityStyle = InfoEntityStyle.PICTURE_LEFT
                ),
                    InfoEntity(
                        image = R.drawable.light_bulb,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_phoneposition_info_2),
                        infoEntityStyle = InfoEntityStyle.PICTURE_LEFT
                    )
                )
                3 ->  listOf(InfoEntity(
                    image = null,
                    lottie = R.raw.using_mobile_phone,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_phoneusage_info_1),
                    infoEntityStyle = InfoEntityStyle.PICTURE_LEFT
                ),
                    InfoEntity(
                        image = R.drawable.light_bulb,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_phoneusage_info_2),
                        infoEntityStyle = InfoEntityStyle.PICTURE_RIGHT
                    )
                )
                4 ->  listOf(InfoEntity(
                    image = null,
                    lottie = R.raw.character_walk,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_activitytracking_info_1),
                    infoEntityStyle = InfoEntityStyle.PICTURE_TOP
                ),
                    InfoEntity(
                        image = R.drawable.light_bulb,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_activitytracking_info_2),
                        infoEntityStyle = InfoEntityStyle.PICTURE_LEFT
                    )
                )
                else -> noInfo(context)
            }
        }

        /**
         * Handler if no information is found
         */
        private fun noInfo(context:Context) : List<InfoEntity>{
            return listOf(InfoEntity(
                image = R.drawable.empty_alarms,
                lottie = R.raw.empty,
                textHeader = context.getString(R.string.info_no_info_found),
                textDescription = null
                )
            )
        }

    }
}


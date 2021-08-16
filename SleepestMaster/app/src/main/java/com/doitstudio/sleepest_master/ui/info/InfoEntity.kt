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
                Info.HISTORY -> historyInfo(id, context)
                Info.SETTINGS -> settingsInfo(id, context)
                Info.SLEEP -> sleepInfo(id, context)
                else -> noInfo(context)
            }
        }

        private fun historyInfo(id:Int, context: Context) : List<InfoEntity>{
            return when(id){
                0 ->  listOf(
                    InfoEntity(
                        image = R.drawable.sleep_mood_bad,
                        lottie = null,
                        textHeader = null,
                        textDescription = "There is some information to pass here",
                        infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                    ),
                    InfoEntity(
                        image = null,
                        lottie = R.raw.empty,
                        textHeader = null,
                        textDescription = "Nothing much to see here haha",
                        infoEntityStlye = InfoEntityStlye.PICTURE_TOP
                    ),
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = null,
                        textDescription = "Nothing much to see here haha",
                        infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                    ),
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = "New Header",
                        textDescription = "Nothing much to see here haha"
                    )
                )
                1 ->  listOf(InfoEntity(
                        image = R.drawable.empty_alarms,
                        lottie = null,
                        textHeader = "Sleep",
                        textDescription = "Nothing much to see here haha"
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
                7 ->  listOf(InfoEntity(
                    image = R.drawable.sleep_mood_bad,
                    lottie = null,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_general_info_1),
                    infoEntityStlye = InfoEntityStlye.PICTURE_TOP
                    ),
                    InfoEntity(
                    image = R.drawable.sleep_mood_bad,
                    lottie = null,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_general_info_2),
                    infoEntityStlye = InfoEntityStlye.PICTURE_RIGHT
                    ),
                    InfoEntity(
                        image = null,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_general_info_3),
                        infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                    )
                )
                0 ->  listOf(InfoEntity(
                        image = null,
                        lottie = R.raw.settings,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_sleeptimes_info_1),
                    infoEntityStlye = InfoEntityStlye.PICTURE_RIGHT
                    ),
                    InfoEntity(
                        image = null,
                        lottie = R.raw.settings,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_sleeptimes_info_2),
                        infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                    ),
                    InfoEntity(
                        image = null,
                        lottie = R.raw.settings,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_sleeptimes_info_3),
                        infoEntityStlye = InfoEntityStlye.PICTURE_RIGHT
                    )
                )
                1 ->  listOf(InfoEntity(
                    image = R.drawable.sleep_mood_bad,
                    lottie = null,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_sleepduration_info_1),
                    infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                ),
                    InfoEntity(
                        image = R.drawable.sleep_mood_bad,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_sleepduration_info_2),
                        infoEntityStlye = InfoEntityStlye.PICTURE_RIGHT
                    )
                )
                6 ->  listOf(InfoEntity(
                    image = null,
                    lottie = R.raw.settings,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_lightcondition_info_1),
                    infoEntityStlye = InfoEntityStlye.PICTURE_RIGHT
                ),
                    InfoEntity(
                        image = null,
                        lottie = R.raw.settings,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_lightcondition_info_2),
                        infoEntityStlye = InfoEntityStlye.PICTURE_RIGHT
                    )
                )
                2 ->  listOf(InfoEntity(
                    image = R.drawable.sleep_mood_bad,
                    lottie = null,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_phoneposition_info_1),
                    infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                ),
                    InfoEntity(
                        image = R.drawable.sleep_mood_bad,
                        lottie = null,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_phoneposition_info_2),
                        infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                    )
                )
                3 ->  listOf(InfoEntity(
                    image = null,
                    lottie = R.raw.settings,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_phoneusage_info_1),
                    infoEntityStlye = InfoEntityStlye.PICTURE_RIGHT
                ),
                    InfoEntity(
                        image = null,
                        lottie = R.raw.settings,
                        textHeader = null,
                        textDescription = context.resources.getString(R.string.sleep_phoneusage_info_2),
                        infoEntityStlye = InfoEntityStlye.PICTURE_RIGHT
                    )
                )
                4 ->  listOf(InfoEntity(
                    image = R.drawable.sleep_mood_bad,
                    lottie = null,
                    textHeader = null,
                    textDescription = context.resources.getString(R.string.sleep_activitytracking_info_1),
                    infoEntityStlye = InfoEntityStlye.PICTURE_LEFT
                ),
                    InfoEntity(
                        image = R.drawable.sleep_mood_bad,
                        lottie = null,
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


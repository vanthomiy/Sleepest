package com.doitstudio.sleepest_master.ui.info

import android.graphics.drawable.Drawable
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.model.data.Info

data class InfoEntity (
    val image : Int?,
    val lottie : Int?,
    val textHeader : String?,
    val textDescription : String?
    )
{
    companion object{

        fun getInfo(info: Info, id:Int) : List<InfoEntity>{
            return when(info){
                Info.HISTORY -> historyInfo(id)
                Info.SETTINGS -> settingsInfo(id)
                Info.SLEEP -> sleepInfo(id)
                else -> noInfo()
            }
        }

        private fun historyInfo(id:Int) : List<InfoEntity>{
            return when(id){
                0 ->  listOf(InfoEntity(
                    image = R.drawable.history_icon,
                    lottie = R.raw.settings,
                    textHeader = "Sleep",
                    textDescription = ""
                ))
                else -> noInfo()
            }
        }

        private fun settingsInfo(id:Int) : List<InfoEntity>{
            return when(id){
                0 ->  listOf(InfoEntity(
                    image = R.drawable.ic_settings_black_24dp,
                    lottie = R.raw.settings,
                    textHeader = "Settings",
                    textDescription = ""
                ))
                else -> noInfo()
            }
        }

        private fun sleepInfo(id:Int) : List<InfoEntity>{
            return when(id){
                0 ->  listOf(InfoEntity(
                    image = R.drawable.sleep_mood_bad,
                    lottie = R.raw.settings,
                    textHeader = "Sleeep",
                    textDescription = ""
                ))
                else -> noInfo()
            }
        }

        private fun noInfo() : List<InfoEntity>{
            return listOf(InfoEntity(
                image = R.drawable.empty_alarms,
                lottie = R.raw.empty,
                textHeader = "Wrong",
                textDescription = null
            ))
        }

    }
}


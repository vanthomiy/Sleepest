package com.doitstudio.sleepest_master

import android.os.Bundle
import android.util.Range
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.appyvet.rangebar.RangeBar
import com.doitstudio.sleepest_master.storage.db.AlarmEntity

class AlarmInstance : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val sBar: SeekBar? = view?.findViewById(R.id.sBar_sleepAmount) //Selecting the sleep amount
        val rBar: RangeBar? = view?.findViewById(R.id.rBar_wakeupRange) //Selecting the wake up range https://github.com/Fedorkz/material-range-bar
        val tViewSleepAmount: TextView? = view?.findViewById(R.id.tV_sleepAmountSelection) //Display the selected sleep amount
        val tViewWakeupTime : TextView? = view?.findViewById(R.id.tV_swakeupRangeSelection) //Display the selected wake up range
        val tViewExpandAlarmSettings: TextView? = view?.findViewById(R.id.tV_expandAlarmSettings) //Used for expanding the alarmview
        val tViewAlarmViewTopic : TextView? = view?.findViewById(R.id.tV_AlarmViewTopic) //Topic of the alarm
        val viewExtendedAlarmSettings : View? = view?.findViewById(R.id.cL_extendedAlarmSettings) //Display extended alarm settings
        val btnWeekdaySelect : Button? = view?.findViewById(R.id.btn_alarmDaysWeek) //Popup window for selecting the weekdays for alarm
        val swAlarmActive : Switch? = view?.findViewById(R.id.sw_alarmOnOff) //Select whether alarm is on or off

        //Inflate the layout for this fragment
        return inflater?.inflate(R.layout.alarm_entity, container, false)
    }
}
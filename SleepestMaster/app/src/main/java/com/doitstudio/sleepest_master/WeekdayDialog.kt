package com.doitstudio.sleepest_master

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.NonCancellable.cancel
import java.lang.NullPointerException
import java.time.DayOfWeek

class WeekdayDialog: DialogFragment() {

    //region
    lateinit var swAlarmDayMonday : Switch //
    lateinit var swAlarmDayTuesday : Switch //
    lateinit var swAlarmDayWednesday : Switch //
    lateinit var swAlarmDayThursday : Switch //
    lateinit var swAlarmDayFriday : Switch //
    lateinit var swAlarmDaySaturday : Switch //
    lateinit var swAlarmDaySunday : Switch //
    lateinit var btn : Button
    lateinit var listOfActiveDayOfWeek: ArrayList<DayOfWeek>
    //endregion

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.view_alarm_weekday, container)

        swAlarmDayMonday = root.findViewById(R.id.sw_AlarmDayMonday)
        swAlarmDayTuesday = root.findViewById(R.id.sw_AlarmDayTuesday)
        swAlarmDayWednesday = root.findViewById(R.id.sw_AlarmDayWednesday)
        swAlarmDayThursday = root.findViewById(R.id.sw_AlarmDayThursday)
        swAlarmDayFriday = root.findViewById(R.id.sw_AlarmDayFriday)
        swAlarmDaySaturday = root.findViewById(R.id.sw_AlarmDaySaturday)
        swAlarmDaySunday = root.findViewById(R.id.sw_AlarmDaySunday)
        btn = root.findViewById(R.id.button)

        return inflater.inflate(R.layout.view_alarm_weekday, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val width = (resources.displayMetrics.widthPixels * 0.75).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.4).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onStart() {
        super.onStart()
        swAlarmDaySunday.setOnClickListener {
            test()
        }
    }

    fun test() {
        listOfActiveDayOfWeek = ArrayList()

        if (swAlarmDayMonday.isChecked) listOfActiveDayOfWeek.add(DayOfWeek.MONDAY)
        else listOfActiveDayOfWeek.remove(DayOfWeek.MONDAY)

        if (swAlarmDayTuesday.isChecked) listOfActiveDayOfWeek.add(DayOfWeek.TUESDAY)
        else listOfActiveDayOfWeek.remove(DayOfWeek.TUESDAY)

        if (swAlarmDayWednesday.isChecked) listOfActiveDayOfWeek.add(DayOfWeek.WEDNESDAY)
        else listOfActiveDayOfWeek.remove(DayOfWeek.WEDNESDAY)

        if (swAlarmDayThursday.isChecked) listOfActiveDayOfWeek.add(DayOfWeek.THURSDAY)
        else listOfActiveDayOfWeek.remove(DayOfWeek.THURSDAY)

        if (swAlarmDayFriday.isChecked) listOfActiveDayOfWeek.add(DayOfWeek.FRIDAY)
        else listOfActiveDayOfWeek.remove(DayOfWeek.FRIDAY)

        if (swAlarmDaySaturday.isChecked) listOfActiveDayOfWeek.add(DayOfWeek.SATURDAY)
        else listOfActiveDayOfWeek.remove(DayOfWeek.SATURDAY)

        if (swAlarmDaySunday.isChecked) listOfActiveDayOfWeek.add(DayOfWeek.SUNDAY)
        else listOfActiveDayOfWeek.remove(DayOfWeek.SUNDAY)

        AlarmSettings().saveAlarmDaysWeek(listOfActiveDayOfWeek)
    }

    override fun onDestroy() {
        super.onDestroy()
        listOfActiveDayOfWeek = ArrayList()

        if (swAlarmDayMonday.isChecked) listOfActiveDayOfWeek.add(DayOfWeek.MONDAY)
        else listOfActiveDayOfWeek.remove(DayOfWeek.MONDAY)

        if (swAlarmDayTuesday.isChecked) listOfActiveDayOfWeek.add(DayOfWeek.TUESDAY)
        else listOfActiveDayOfWeek.remove(DayOfWeek.TUESDAY)

        if (swAlarmDayWednesday.isChecked) listOfActiveDayOfWeek.add(DayOfWeek.WEDNESDAY)
        else listOfActiveDayOfWeek.remove(DayOfWeek.WEDNESDAY)

        if (swAlarmDayThursday.isChecked) listOfActiveDayOfWeek.add(DayOfWeek.THURSDAY)
        else listOfActiveDayOfWeek.remove(DayOfWeek.THURSDAY)

        if (swAlarmDayFriday.isChecked) listOfActiveDayOfWeek.add(DayOfWeek.FRIDAY)
        else listOfActiveDayOfWeek.remove(DayOfWeek.FRIDAY)

        if (swAlarmDaySaturday.isChecked) listOfActiveDayOfWeek.add(DayOfWeek.SATURDAY)
        else listOfActiveDayOfWeek.remove(DayOfWeek.SATURDAY)

        if (swAlarmDaySunday.isChecked) listOfActiveDayOfWeek.add(DayOfWeek.SUNDAY)
        else listOfActiveDayOfWeek.remove(DayOfWeek.SUNDAY)

        AlarmSettings().saveAlarmDaysWeek(listOfActiveDayOfWeek)
    }

    /*
    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.75).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.4).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        swAlarmDayMonday.findViewById<Switch>(R.id.sw_AlarmDayMonday)

        if (swAlarmDayMonday.isChecked == true) {
            swAlarmDayMonday.isChecked = false
        }
    }
     */
}
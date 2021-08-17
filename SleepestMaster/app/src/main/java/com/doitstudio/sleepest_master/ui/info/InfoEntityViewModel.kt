package com.doitstudio.sleepest_master.ui.info

import android.app.ActionBar
import android.app.Application
import android.app.TimePickerDialog
import android.graphics.drawable.GradientDrawable
import android.util.LayoutDirection
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.Observable
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.model.data.AlarmSleepChangeFrom
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.util.SleepTimeValidationUtil
import com.doitstudio.sleepest_master.util.WeekDaysUtil.getWeekDayByNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime


class InfoEntityViewModel(application: Application) : AndroidViewModel(application) {


    //val backgroundColor = ObservableField(R.color.transparent_overlay_tertiary)
    val textHeader = ObservableField("")
    val textDescription = ObservableField("")
    val orientation = ObservableField(LinearLayout.HORIZONTAL)
    val layoutFormat = ObservableField(LayoutDirection.LTR)

    val imageVisible = ObservableField(View.GONE)
    val lottieVisible = ObservableField(View.GONE)
    val headerVisible = ObservableField(View.GONE)
    val descrriptionVisible = ObservableField(View.GONE)

    //endregion

}



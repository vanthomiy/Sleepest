package com.sleepestapp.sleepest

import android.app.Application
import android.app.TimePickerDialog
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.background.BackgroundAlarmTimeHandler
import com.sleepestapp.sleepest.googleapi.ActivityTransitionHandler
import com.sleepestapp.sleepest.model.data.*
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.storage.db.AlarmEntity
import com.sleepestapp.sleepest.util.IconAnimatorUtil
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil
import com.sleepestapp.sleepest.util.SleepTimeValidationUtil.Is24HourFormat
import com.sleepestapp.sleepest.util.StringUtil.getStringXml
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.*


class MainActivityViewModel(
    val dataStoreRepository: DataStoreRepository,
    val dataBaseRepository: DatabaseRepository
) : ViewModel() {

    val activeAlarmsLiveData by lazy {
        dataBaseRepository.activeAlarmsFlow(dataStoreRepository).asLiveData()
    }

    val sleepParametersLiveData by lazy {
        dataStoreRepository.sleepParameterFlow.asLiveData()
    }

    val settingsLiveData by lazy {
        dataStoreRepository.settingsDataFlow.asLiveData()
    }
}


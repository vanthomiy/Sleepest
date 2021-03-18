package com.doitstudio.sleepest_master

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.doitstudio.sleepest_master.Background.ForegroundService
import com.doitstudio.sleepest_master.Background.Workmanager
import com.doitstudio.sleepest_master.databinding.ActivityMainBinding
import com.doitstudio.sleepest_master.model.data.Actions
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Status of subscription to sleep data. This is stored in [SleepSubscriptionStatus] which saves
    // the data in a [DataStore] in case the user navigates away from the app.
    private var alarmActive = false
        set(newAlarmActive) {
            field = newAlarmActive
            if (newAlarmActive) {
                binding.buttonAlarmToogle.text = getString(R.string.alarm_active)
            } else {
                binding.buttonAlarmToogle.text = getString(R.string.alarm_disabled)
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel.alarmLiveData.observe(this) { alarmData ->
            if (alarmActive != alarmData?.isActive) {
                alarmActive = alarmData?.isActive == true
            }

            binding.sleepSegmentsText.text = alarmData.alarmName
        }
    }

    private val mainViewModel: MainViewModel by lazy {
        MainViewModel(
            (application as MainApplication).dbRepository,
            (application as MainApplication).dataStoreRepository
        )
    }

    private val sch:SleepCalculationHandler by lazy {
        SleepCalculationHandler.getDatabase(this)
    }

    fun buttonClick1(view: View){
        sch.calculateSleepData()
    }


    fun buttonClick2(view: View){
        ForegroundService.startOrStopForegroundService(Actions.START, this)
        Workmanager.startPeriodicWorkmanager(15);
    }
}
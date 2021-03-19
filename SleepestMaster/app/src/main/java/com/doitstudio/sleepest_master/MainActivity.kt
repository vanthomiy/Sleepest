package com.doitstudio.sleepest_master

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.doitstudio.sleepest_master.databinding.ActivityMainBinding
import com.doitstudio.sleepest_master.sleepapi.SleepHandler
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
        MainViewModel((application as MainApplication).dbRepository, (application as MainApplication).dataStoreRepository)
    }

    private val sch:SleepCalculationHandler by lazy {
        SleepCalculationHandler(this)
    }

    fun buttonClick1(view: View){
        sch.calculateSleepData()
    }

    private val sleepHandler : SleepHandler by lazy {SleepHandler.getHandler(this)}

    fun buttonClick2(view: View){
        sleepHandler.StartSleepHandler()
    }
}
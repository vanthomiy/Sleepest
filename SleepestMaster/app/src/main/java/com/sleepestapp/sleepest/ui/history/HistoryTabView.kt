package com.sleepestapp.sleepest.ui.history

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.Observable
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.viewpager2.widget.ViewPager2
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.databinding.FragmentHistoryTabviewBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.model.data.MobilePosition
import com.sleepestapp.sleepest.model.data.SleepState
import com.sleepestapp.sleepest.sleepcalculation.SleepCalculationHandler
import com.sleepestapp.sleepest.storage.db.SleepApiRawDataEntity
import com.sleepestapp.sleepest.storage.db.UserSleepSessionEntity
import com.sleepestapp.sleepest.ui.settings.SettingsViewModel
import com.sleepestapp.sleepest.util.StringUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.*
import java.time.temporal.WeekFields
import java.util.*
import kotlin.collections.ArrayList

class HistoryTabView : Fragment() {
    private lateinit var adapter: HistoryTabViewAdapter
    private lateinit var viewPager: ViewPager2

    private val actualContext: Context by lazy { requireActivity().applicationContext }
    private val viewModel by lazy { ViewModelProvider(requireActivity(), factory).get(HistoryViewModel::class.java) }
    private lateinit var binding: FragmentHistoryTabviewBinding
    private lateinit var btnPrevious: Button
    private lateinit var btnNext : Button
    private lateinit var tVActualDayTabView : TextView
    private lateinit var previousMonthAnalysisDate : Month

    var factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return  HistoryViewModel(
                (actualContext as MainApplication).dataStoreRepository,
                (actualContext as MainApplication).dataBaseRepository
            ) as T
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHistoryTabviewBinding.inflate(inflater, container, false)
        binding.historyTabView = viewModel

        setUpHistoryViewModelValues()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = HistoryTabViewAdapter(this)
        viewPager = binding.pager
        viewPager.adapter = adapter

        btnPrevious = view.findViewById(R.id.btn_Previous)
        btnNext = view.findViewById(R.id.btn_Next)
        tVActualDayTabView = view.findViewById(R.id.tV_actualDayTabView)
        previousMonthAnalysisDate = LocalDate.now().month

        val tabs = listOf(getString(R.string.history_day_title), getString(R.string.history_week_title), getString(R.string.history_month_title))

        val tabLayout = binding.tabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(
            object: TabLayout.OnTabSelectedListener {

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    updateDateInformation(tabLayout.selectedTabPosition)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    updateDateInformation(tabLayout.selectedTabPosition)
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    updateDateInformation(tabLayout.selectedTabPosition)
                }
            }
        )

        btnPrevious.setOnClickListener {
            viewModel.onPreviousDateClick(tabLayout.selectedTabPosition)
        }

        btnNext.setOnClickListener {
            viewModel.onNextDateClick(tabLayout.selectedTabPosition)
        }

        tVActualDayTabView.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = activity?.let { picker ->
                DatePickerDialog(
                    picker,
                    R.style.TimePickerTheme,
                    { _, year, monthOfYear, dayOfMonth ->
                        viewModel.analysisDate.set(LocalDate.of(year, monthOfYear + 1, dayOfMonth))
                    },
                    year,
                    month,
                    day
                )
            }

            dpd?.datePicker?.maxDate = System.currentTimeMillis()
            dpd?.show()
        }

        tVActualDayTabView.setOnLongClickListener {
            viewModel.analysisDate.set(LocalDate.now())
            return@setOnLongClickListener true
        }

        viewModel.analysisDate.addOnPropertyChangedCallback(
            object: Observable.OnPropertyChangedCallback() {

                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {

                    if (viewModel.analysisDate.get()?.month != previousMonthAnalysisDate) {
                        getSleepData()
                    }

                    previousMonthAnalysisDate = viewModel.analysisDate.get()?.month!!

                    updateDateInformation(tabLayout.selectedTabPosition)
                }
            }
        )

        updateDateInformation(tabLayout.selectedTabPosition)

        viewModel.dataBaseRepository.allUserSleepSessions.asLiveData().observe(viewLifecycleOwner) {
            if (!viewModel.onWork) {
                getSleepData()
            }
        }
    }

    fun getSleepData() {
        val ids = mutableSetOf<Int>()
        viewModel.analysisDate.get()?.let {
            val startDayToGet = it.minusMonths(1L).withDayOfMonth(1)
            val endDayToGet = it.withDayOfMonth(it.lengthOfMonth())
            val dayDifference = (endDayToGet.toEpochDay() - startDayToGet.toEpochDay()).toInt()

            for (day in 0..dayDifference) {
                ids.add(
                    UserSleepSessionEntity.getIdByDateTime(
                        LocalDate.of(
                            startDayToGet.plusDays(day.toLong()).year,
                            startDayToGet.plusDays(day.toLong()).month,
                            startDayToGet.plusDays(day.toLong()).dayOfMonth,
                        )
                    )
                )
            }
        }

        lifecycleScope.launch {
            for (id in ids) {
                val session = viewModel.dataBaseRepository.getSleepSessionById(id).first().firstOrNull()
                session?.let {
                    viewModel.sleepSessionData[id] = Triple(
                        viewModel.dataBaseRepository.getSleepApiRawDataBetweenTimestamps(
                            session.sleepTimes.sleepTimeStart,
                            session.sleepTimes.sleepTimeEnd).first()?.sortedBy { x -> x.timestampSeconds },
                        session.sleepTimes.sleepDuration,
                        session
                    ) as Triple<List<SleepApiRawDataEntity>, Int, UserSleepSessionEntity>
                }
            }
            checkSessionIntegrity()
            viewModel.dataReceived.set(true)
        }
    }

    /** Checks if the previously received sleep session data is correct and contains no errors.
     * If unusual data was received, the sleep phase determination algorithm ist triggered again to interpret the api data. */
    private fun checkSessionIntegrity() {
        viewModel.onWork = true
        for (key in viewModel.sleepSessionData.keys) {
            val session = viewModel.sleepSessionData[key]?.first
            session?.let {
                val mobilePosition = viewModel.sleepSessionData[key]?.third?.mobilePosition
                val isSleeping = it.any { x -> x.sleepState == SleepState.SLEEPING }
                val isUnidentified = it.any { x -> x.sleepState == SleepState.NONE }

                if ((isUnidentified)) {
                    lifecycleScope.launch {
                        SleepCalculationHandler.getHandler(actualContext).checkIsUserSleeping(
                            LocalDateTime.ofInstant(
                                Instant.ofEpochMilli((viewModel.sleepSessionData[key]?.third?.sleepTimes?.sleepTimeStart?.toLong())!! * 1000),
                                ZoneOffset.systemDefault()
                            ),
                            true
                        )
                    }
                }

                if ((mobilePosition == MobilePosition.INBED && isSleeping)) { // || isUnidentified) {
                    lifecycleScope.launch {
                        SleepCalculationHandler.getHandler(actualContext).defineUserWakeup(
                            LocalDateTime.ofInstant(
                                Instant.ofEpochMilli((viewModel.sleepSessionData[key]?.third?.sleepTimes?.sleepTimeStart?.toLong())!! * 1000),
                                ZoneOffset.systemDefault()
                            ),
                            false
                        )
                    }
                }
            }
        }
        viewModel.onWork = false
    }


    fun setUpHistoryViewModelValues() {
        val application = requireActivity().application

        viewModel.xAxisValuesWeek = arrayListOf(
            StringUtil.getStringXml(
                R.string.alarm_entity_day_mo,
                application
            ),
            StringUtil.getStringXml(
                R.string.alarm_entity_day_tu,
                application
            ),
            StringUtil.getStringXml(
                R.string.alarm_entity_day_we,
                application
            ),
            StringUtil.getStringXml(
                R.string.alarm_entity_day_th,
                application
            ),
            StringUtil.getStringXml(
                R.string.alarm_entity_day_fr,
                application
            ),
            StringUtil.getStringXml(
                R.string.alarm_entity_day_sa,
                application
            ),
            StringUtil.getStringXml(
                R.string.alarm_entity_day_su,
                application
            )
        )

        val sleepStates = SleepState.getListOfSleepStates()
        sleepStates.forEach {
            viewModel.sleepStateString[it] = SleepState.getString(it, application)
            viewModel.sleepStateColor[it] = SleepState.getColor(it, application)
        }

        viewModel.activityBackgroundDrawable = ContextCompat.getDrawable(application, R.drawable.bg_spark_line)
    }

    fun updateDateInformation(range: Int) {
        when (range) {
            0 -> tVActualDayTabView.text = createCalendarDayInformation()
            1 -> tVActualDayTabView.text = createCalendarWeekInformation()
            2 -> tVActualDayTabView.text = createCalendarMonthInformation()
        }
    }

    private fun createCalendarDayInformation(): String {
        val actualDay = LocalDate.now()
        var information = actualContext.getString(R.string.history_failure_title)

        viewModel.analysisDate.get()?.let {

            information = when {
                actualDay.dayOfYear == it.dayOfYear -> {
                    actualContext.getString(R.string.history_today_title)
                }
                (actualDay.dayOfYear - 1) == it.dayOfYear -> {
                    actualContext.getString(R.string.history_yesterday_title)
                }
                else -> {
                    ("${it.dayOfMonth}.${it.monthValue}.${it.year}")
                }
            }
        }

        return information
    }

    private fun createCalendarWeekInformation(): String {
        var information = getString(R.string.history_failure_title)
        val actualDate = LocalDate.now()
        val actualWeekOfYear = actualDate.get(WeekFields.of(Locale.GERMANY).weekOfYear())

        viewModel.analysisDate.get()?.let {
            val analysisDate = LocalDate.of(it.year, it.monthValue, it.dayOfMonth)
            val analysisWeekOfYear = analysisDate.get(WeekFields.of(Locale.GERMANY).weekOfYear())

            information = when {
                actualWeekOfYear == analysisWeekOfYear -> {
                    getString(R.string.history_currentWeek_title)
                }
                (actualWeekOfYear - 1) == analysisWeekOfYear -> {
                    getString(R.string.history_previousWeek_title)
                }
                else -> {
                    getWeekRange(it)
                }
            }
        }

        return information
    }

    private fun getWeekRange(date: LocalDate) : String {
        val sundayOfWeek : LocalDate = when (date.dayOfWeek.value) {
            1 -> date.plusDays(6L) // Monday
            2 -> date.plusDays(5L) // Tuesday
            3 -> date.plusDays(4L) // Wednesday
            4 -> date.plusDays(3L) // Thursday
            5 -> date.plusDays(2L) // Friday
            6 -> date.plusDays(1L) // Saturday
            else -> date.plusDays(0L) // Sunday
        }
        val mondayOfWeek : LocalDate = sundayOfWeek.minusDays(6L)

        val mondayString : String = mondayOfWeek.dayOfMonth.toString() + "." + mondayOfWeek.monthValue
        val sundayString : String = sundayOfWeek.dayOfMonth.toString() + "." + sundayOfWeek.monthValue

        return ("$mondayString - $sundayString")
    }

    private fun createCalendarMonthInformation(): String {
        viewModel.analysisDate.get()?.let {
            return when (it.monthValue) {
                1 -> getString(R.string.history_january_title)
                2 -> getString(R.string.history_february_title)
                3 -> getString(R.string.history_march_title)
                4 -> getString(R.string.history_april_title)
                5 -> getString(R.string.history_may_title)
                6 -> getString(R.string.history_june_title)
                7 -> getString(R.string.history_july_title)
                8 -> getString(R.string.history_august_title)
                9 -> getString(R.string.history_september_title)
                10 -> getString(R.string.history_october_title)
                11 -> getString(R.string.history_november_title)
                else -> getString(R.string.history_december_title)
            }
        }

        return getString(R.string.history_failure_title)
    }
}
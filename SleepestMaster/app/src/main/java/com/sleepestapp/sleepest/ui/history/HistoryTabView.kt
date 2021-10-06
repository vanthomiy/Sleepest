package com.sleepestapp.sleepest.ui.history

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.transition.TransitionManager
import android.transition.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.data.LineRadarDataSet
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.databinding.FragmentHistoryTabviewBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.model.data.SleepState
import com.sleepestapp.sleepest.sleepcalculation.SleepCalculationHandler
import com.sleepestapp.sleepest.util.StringUtil
import java.time.*
import java.time.temporal.WeekFields
import java.util.*

class HistoryTabView : Fragment() {
    private lateinit var adapter: HistoryTabViewAdapter
    private lateinit var viewPager: ViewPager2

    private val actualContext: Context by lazy { requireActivity().applicationContext }
    private val viewModel by lazy { ViewModelProvider(requireActivity(), factory).get(HistoryViewModel::class.java) }
    private lateinit var binding: FragmentHistoryTabviewBinding
    private lateinit var previousMonthAnalysisDate : Month

    var analysisDateString = MutableLiveData("")

    var factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return  HistoryViewModel(
                (actualContext as MainApplication).dataStoreRepository,
                (actualContext as MainApplication).dataBaseRepository,
                SleepCalculationHandler(actualContext)
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
        binding.lifecycleOwner = this

        setUpHistoryViewModelValues()

        return binding.root
    }

    override fun onViewCreated(
        view: View
        , savedInstanceState: Bundle?
    ) {
        adapter = HistoryTabViewAdapter(this)
        viewPager = binding.pager
        viewPager.adapter = adapter

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

        binding.btnPrevious.setOnClickListener {
            viewModel.onPreviousDateClick(tabLayout.selectedTabPosition)
        }

        binding.btnNext.setOnClickListener {
            viewModel.onNextDateClick(tabLayout.selectedTabPosition)
        }

        binding.lLDateInformation.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = activity?.let { picker ->
                DatePickerDialog(
                    picker,
                    R.style.DatePickerTheme,
                    { _, year, monthOfYear, dayOfMonth ->
                        viewModel.analysisDate.value = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                    },
                    year,
                    month,
                    day
                )
            }

            dpd?.datePicker?.maxDate = System.currentTimeMillis()
            dpd?.show()
        }

        binding.lLDateInformation.setOnLongClickListener {
            viewModel.analysisDate.value = LocalDate.now()
            return@setOnLongClickListener true
        }

        viewModel.analysisDate.observe(viewLifecycleOwner) {
            if (viewModel.analysisDate.value?.month != previousMonthAnalysisDate) {
                viewModel.getSleepData()
            }

            previousMonthAnalysisDate = viewModel.analysisDate.value?.month!!

            updateDateInformation(tabLayout.selectedTabPosition)
        }

        updateDateInformation(tabLayout.selectedTabPosition)

        viewModel.dataBaseRepository.allUserSleepSessions.asLiveData().observe(viewLifecycleOwner) {
            if (!viewModel.onWork) {
                viewModel.getSleepData()
            }
        }

        viewModel.actualExpand.observe(viewLifecycleOwner) {
            TransitionManager.beginDelayedTransition(binding.lLLinearAnimationLayoutTabView)
        }
    }

    /**
     * Sets up some values for the [HistoryViewModel].
     */
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

    /**
     * Updates the currently displayed date at the TabView.
     */
    fun updateDateInformation(
        range: Int
    ) {
        TransitionManager.beginDelayedTransition(binding.lLDateInformation)
        when (range) {
            0 -> {
                binding.tVActualYearTabView.visibility = View.GONE
                viewModel.analysisRangeString.value = createCalendarDayInformation()
                //binding.tVActualDayTabView.text = createCalendarDayInformation()
            }
            1 -> {
                binding.tVActualYearTabView.visibility = View.VISIBLE
                viewModel.analysisRangeString.value = createCalendarWeekInformation()
                viewModel.analysisRangeYearString.value = createCalendarYearInformation()
            }
            2 -> {
                binding.tVActualYearTabView.visibility = View.VISIBLE
                viewModel.analysisRangeString.value = createCalendarMonthInformation()
                viewModel.analysisRangeYearString.value = createCalendarYearInformation()
            }
        }
    }

    /**
     * Creates a formatted string for the function [updateDateInformation].
     */
    private fun createCalendarYearInformation(): String {
        var information = actualContext.getString(R.string.history_failure_title)

        viewModel.analysisDate.value?.let {
            information = it.year.toString()
        }

        return information
    }

    /**
     * Creates a formatted string for the function [updateDateInformation].
     */
    private fun createCalendarDayInformation(): String {
        val actualDay = LocalDate.now()
        var information = actualContext.getString(R.string.history_failure_title)

        viewModel.analysisDate.value?.let {

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

    /**
     * Creates a formatted string for the function [updateDateInformation].
     */
    private fun createCalendarWeekInformation(): String {
        var information = getString(R.string.history_failure_title)
        val actualDate = LocalDate.now()
        val actualWeekOfYear = actualDate.get(WeekFields.of(Locale.GERMANY).weekOfYear())

        viewModel.analysisDate.value?.let {
            val analysisDate = LocalDate.of(it.year, it.monthValue, it.dayOfMonth)

            when (analysisDate.get(WeekFields.of(Locale.GERMANY).weekOfYear())) {
                actualWeekOfYear -> {
                    information = getString(R.string.history_currentWeek_title)
                    binding.tVActualYearTabView.visibility = View.GONE

                }
                (actualWeekOfYear - 1) -> {
                    information = getString(R.string.history_previousWeek_title)
                    binding.tVActualYearTabView.visibility = View.GONE

                }
                else -> {
                    information = getWeekRange(it)
                }
            }
        }

        return information
    }

    /**
     * Auxiliary function for determining the range of a week in order to display its border dates.
     */
    private fun getWeekRange(
        date: LocalDate
    ) : String {
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

    /**
     * Creates a formatted string for the function [updateDateInformation].
     */
    private fun createCalendarMonthInformation(): String {
        viewModel.analysisDate.value?.let {
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
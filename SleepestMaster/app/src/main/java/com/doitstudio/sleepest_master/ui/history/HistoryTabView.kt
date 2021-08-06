package com.doitstudio.sleepest_master.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.databinding.Observable
import androidx.databinding.ObservableInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.viewpager2.widget.ViewPager2
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.databinding.FragmentHistoryTabviewBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.time.LocalDate
import java.time.Month
import java.util.*

class HistoryTabView : Fragment() {
    private lateinit var adapter: HistoryTabViewAdapter
    private lateinit var viewPager: ViewPager2

    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java) }
    private lateinit var binding: FragmentHistoryTabviewBinding
    private lateinit var btnPrevious: Button
    private lateinit var btnNext : Button
    private lateinit var tVActualDayTabView : TextView
    private lateinit var previousMonthAnalysisDate : Month

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHistoryTabviewBinding.inflate(inflater, container, false)
        binding.historyTabView = viewModel

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

        val tabs = listOf(getString(R.string.histroy_day_title), getString(R.string.histroy_week_title), getString(R.string.history_month_title))

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
            viewModel.analysisDate.set(LocalDate.now())
        }

        viewModel.analysisDate.addOnPropertyChangedCallback(
            object: Observable.OnPropertyChangedCallback() {

                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {

                    if (viewModel.analysisDate.get()?.month != previousMonthAnalysisDate) {
                        viewModel.getSleepData()
                    }

                    previousMonthAnalysisDate = viewModel.analysisDate.get()?.month!!

                    updateDateInformation(tabLayout.selectedTabPosition)
                }
            }
        )

        updateDateInformation(tabLayout.selectedTabPosition)

        viewModel.dataBaseRepository.allUserSleepSessions.asLiveData().observe(viewLifecycleOwner) {
            if (!viewModel.onWork) {
                viewModel.getSleepData()
            }
        }
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
        var information = getString(R.string.history_failure_title)

        viewModel.analysisDate.get()?.let {

            information = when {
                actualDay.dayOfYear == it.dayOfYear -> {
                    getString(R.string.history_today_title)
                }
                (actualDay.dayOfYear - 1) == it.dayOfYear -> {
                    getString(R.string.history_yesterday_title)
                }
                else -> {
                    ("${it.dayOfMonth}.${it.monthValue}.${it.year}")
                }
            }
        }

        return information
    }

    private fun createCalendarWeekInformation(): String {
        var analysisWeekOfYear: Int
        val actualDate = LocalDate.now()
        val actualWeekOfYear: Int
        var information = getString(R.string.history_failure_title)

        val actualCalendar = Calendar.getInstance()
        actualCalendar.set(actualDate.year, actualDate.monthValue, actualDate.dayOfMonth)
        actualWeekOfYear = actualCalendar.get(Calendar.WEEK_OF_YEAR)

        viewModel.analysisDate.get()?.let {
            val analysisCalendar = Calendar.getInstance()
            analysisCalendar.set(it.year, it.monthValue, it.dayOfMonth)
            analysisWeekOfYear = analysisCalendar.get(Calendar.WEEK_OF_YEAR)

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
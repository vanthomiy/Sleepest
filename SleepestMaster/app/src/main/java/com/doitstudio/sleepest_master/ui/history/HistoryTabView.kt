package com.doitstudio.sleepest_master.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.databinding.Observable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTabHost
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.databinding.FragmentHistoryDayBinding
import com.doitstudio.sleepest_master.databinding.FragmentHistoryTabviewBinding
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.time.LocalDate
import java.time.Month
import java.time.temporal.WeekFields
import java.util.*

class HistoryTabView : Fragment() {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private lateinit var demoCollectionAdapter: HistoryTabViewAdapter
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
        demoCollectionAdapter = HistoryTabViewAdapter(this)
        viewPager = binding.pager
        viewPager.adapter = demoCollectionAdapter

        btnPrevious = view.findViewById(R.id.btn_Previous)
        btnNext = view.findViewById(R.id.btn_Next)
        tVActualDayTabView = view.findViewById(R.id.tV_actualDayTabView)
        previousMonthAnalysisDate = LocalDate.now().month

        val tabs = listOf("Day", "Week", "Month")

        val tabLayout = binding.tabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()

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
        var information : String

        viewModel.analysisDate.get()?.let {

            information = when {
                actualDay.dayOfYear == it.dayOfYear -> {
                    "Today"
                }
                (actualDay.dayOfYear - 1) == it.dayOfYear -> {
                    "Yesterday"
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
        var information : String

        val actualCalendar = Calendar.getInstance()
        actualCalendar.set(actualDate.year, actualDate.monthValue, actualDate.dayOfMonth)
        actualWeekOfYear = actualCalendar.get(Calendar.WEEK_OF_YEAR)

        viewModel.analysisDate.get()?.let {
            val analysisCalendar = Calendar.getInstance()
            analysisCalendar.set(it.year, it.monthValue, it.dayOfMonth)
            analysisWeekOfYear = analysisCalendar.get(Calendar.WEEK_OF_YEAR)

            information = when {
                actualWeekOfYear == analysisWeekOfYear -> {
                    "This week"
                }
                (actualWeekOfYear - 1) == analysisWeekOfYear -> {
                    "Last week"
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
                1 -> "January"
                2 -> "February"
                3 -> "March"
                4 -> "April"
                5 -> "Mai"
                6 -> "June"
                7 -> "July"
                8 -> "August"
                9 -> "September"
                10 -> "October"
                11 -> "November"
                else -> "December"
            }
        }

        return "none"
    }
}
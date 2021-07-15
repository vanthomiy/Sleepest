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

        val tabLayout = binding.tabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = "Day ${(position + 1)}"
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

                    tVActualDayTabView.text = viewModel.analysisDate.get().toString()
                }
            }
        )

        viewModel.analysisDate.set(LocalDate.now())
    }
}
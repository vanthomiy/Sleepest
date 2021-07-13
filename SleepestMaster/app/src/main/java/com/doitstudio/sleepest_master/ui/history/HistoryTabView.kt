package com.doitstudio.sleepest_master.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.databinding.FragmentHistoryDayBinding
import com.doitstudio.sleepest_master.databinding.FragmentHistoryMonthBinding
import com.doitstudio.sleepest_master.databinding.FragmentHistoryWeekBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HistoryTabView : Fragment() {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private lateinit var demoCollectionAdapter: DemoCollectionAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history_tabview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        demoCollectionAdapter = DemoCollectionAdapter(this)
        viewPager = view.findViewById(R.id.pager)
        viewPager.adapter = demoCollectionAdapter

        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = "OBJECT ${(position + 1)}"
        }.attach()
    }
}

class DemoCollectionAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
         when (COUNT) {
             1 -> {
                 val fragment = HistoryDayFragment()
                 fragment.arguments = Bundle().apply {
                     // Our object is just an integer :-P
                     putInt(ARG_OBJECT, position + 1)
                 }
                 COUNT += 1
                 return fragment
             }
             2 -> {
                 val fragment = HistoryWeekFragment()
                 fragment.arguments = Bundle().apply {
                     // Our object is just an integer :-P
                     putInt(ARG_OBJECT, position + 1)
                 }
                 COUNT += 1
                 return fragment
             }
             else -> {
                 val fragment = HistoryMonthFragment()
                 fragment.arguments = Bundle().apply {
                     // Our object is just an integer :-P
                     putInt(ARG_OBJECT, position + 1)
                 }
                 return fragment
             }
         }
    }
}

private const val ARG_OBJECT = "object"
private var COUNT = 1

// Instances of this class are fragments representing a single
// object in our collection.
class HistoryDayFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java) }
    private lateinit var binding: FragmentHistoryDayBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHistoryDayBinding.inflate(inflater, container, false)
        binding.historyDayViewModel = viewModel
        //viewModel.activityPermissionDescription

        return binding.root
        //return inflater.inflate(R.layout.fragment_history_day, container, false)
    }
}

class HistoryWeekFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java) }
    private lateinit var binding: FragmentHistoryWeekBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHistoryWeekBinding.inflate(inflater, container, false)
        binding.historyWeekViewModel = viewModel
        //viewModel.activityPermissionDescription

        return binding.root
        //return inflater.inflate(R.layout.fragment_history_day, container, false)
    }
}


class HistoryMonthFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java) }
    private lateinit var binding: FragmentHistoryMonthBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHistoryMonthBinding.inflate(inflater, container, false)
        binding.historyMonthViewModel = viewModel
        //viewModel.activityPermissionDescription

        return binding.root
        //return inflater.inflate(R.layout.fragment_history_day, container, false)
    }
}
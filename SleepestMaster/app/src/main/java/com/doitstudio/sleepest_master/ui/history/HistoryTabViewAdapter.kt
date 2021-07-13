package com.doitstudio.sleepest_master.ui.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

private const val ARG_OBJECT = "object"
private var COUNT = 1

class HistoryTabViewAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

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
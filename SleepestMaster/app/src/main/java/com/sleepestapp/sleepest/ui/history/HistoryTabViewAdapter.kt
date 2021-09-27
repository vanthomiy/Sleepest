package com.sleepestapp.sleepest.ui.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

private const val ARG_OBJECT = "object"


class HistoryTabViewAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private var count = 1

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)

        when (count) {
            1 -> {
                val fragment = HistoryDayFragment()
                fragment.arguments = Bundle().apply {
                    putInt(ARG_OBJECT, position + 1)
                }
                count += 1
                return fragment
            }
            2 -> {
                val fragment = HistoryWeekFragment()
                fragment.arguments = Bundle().apply {
                    putInt(ARG_OBJECT, position + 1)
                }
                count += 1
                return fragment
            }
            else -> {
                val fragment = HistoryMonthFragment()
                fragment.arguments = Bundle().apply {
                    putInt(ARG_OBJECT, position + 1)
                }
                return fragment
            }
        }
    }
}
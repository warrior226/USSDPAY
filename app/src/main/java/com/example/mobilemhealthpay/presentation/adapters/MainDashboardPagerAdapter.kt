package com.example.mobilemhealthpay.presentation.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mobilemhealthpay.presentation.fragment.StatusContainerFragment

class MainDashboardPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> StatusContainerFragment.newInstance("REFUND")
            else -> StatusContainerFragment.newInstance("VIREMENT")
        }
    }
}

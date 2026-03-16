package com.example.mobilemhealthpay.presentation.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mobilemhealthpay.presentation.fragment.PaiementEchecFragment
import com.example.mobilemhealthpay.presentation.fragment.PaiementEnCoursFragment
import com.example.mobilemhealthpay.presentation.fragment.PaiementExecuted

class PaiementViewPagerAdapter: FragmentStateAdapter {
    constructor(fragmentManager: FragmentManager, lifecycle: Lifecycle) : super(fragmentManager, lifecycle)

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PaiementEnCoursFragment()
            1 -> PaiementExecuted()
            2-> PaiementEchecFragment()
            else -> PaiementEnCoursFragment()
        }

    }

    override fun getItemCount(): Int {
        return 3
    }

}
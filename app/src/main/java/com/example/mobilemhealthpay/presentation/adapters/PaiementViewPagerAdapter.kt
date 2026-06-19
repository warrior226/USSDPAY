package com.example.mobilemhealthpay.presentation.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mobilemhealthpay.presentation.fragment.PaiementEchecFragment
import com.example.mobilemhealthpay.presentation.fragment.PaiementEnCoursFragment
import com.example.mobilemhealthpay.presentation.fragment.PaiementExecuted

class PaiementViewPagerAdapter(
    fragmentManager: FragmentManager, 
    lifecycle: Lifecycle,
    private val type: String = "REFUND"
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PaiementEnCoursFragment.newInstance(type)
            1 -> PaiementExecuted.newInstance(type)
            else -> PaiementEchecFragment.newInstance(type)
        }
    }
}

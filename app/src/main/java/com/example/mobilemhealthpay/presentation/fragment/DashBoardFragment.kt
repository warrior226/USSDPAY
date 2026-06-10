package com.example.mobilemhealthpay.presentation.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.example.mobilemhealthpay.R
import com.example.mobilemhealthpay.databinding.FragmentDashBoardBinding
import com.example.mobilemhealthpay.presentation.adapters.PaiementViewPagerAdapter
import com.example.mobilemhealthpay.utils.SharedRepository
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DashBoardFragment : Fragment() {
    private lateinit var binding: FragmentDashBoardBinding

    @Inject
    lateinit var repository: SharedRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDashBoardBinding.inflate(layoutInflater)
        val fragmentManager: FragmentManager = childFragmentManager
        val adapter = PaiementViewPagerAdapter(fragmentManager, lifecycle)
        binding.viewpager2.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewpager2) { tab, position ->
            tab.text = when (position) {
                0 -> "En attente"
                1 -> "Effectuées"
                2 -> "Echouées"
                else -> null
            }
        }.attach()

        observeBalance()

        return binding.root
    }

    private fun observeBalance() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.currentBalance.collect { balance ->
                binding.soldePrincipalValeur.text = balance ?: "XXXX"
            }
        }
    }
}

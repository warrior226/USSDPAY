package com.example.mobilemhealthpay.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mobilemhealthpay.databinding.FragmentStatusContainerBinding
import com.example.mobilemhealthpay.presentation.adapters.PaiementViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatusContainerFragment : Fragment() {

    private lateinit var binding: FragmentStatusContainerBinding
    private var type: String = "REFUND"

    companion object {
        fun newInstance(type: String): StatusContainerFragment {
            val fragment = StatusContainerFragment()
            val args = Bundle()
            args.putString("TYPE", type)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getString("TYPE") ?: "REFUND"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatusContainerBinding.inflate(layoutInflater)
        
        val adapter = PaiementViewPagerAdapter(childFragmentManager, lifecycle, type)
        binding.viewpagerStatus.adapter = adapter

        TabLayoutMediator(binding.tabLayoutStatus, binding.viewpagerStatus) { tab, position ->
            tab.text = when (position) {
                0 -> "En attente"
                1 -> "Effectuées"
                2 -> "Echouées"
                else -> null
            }
        }.attach()

        return binding.root
    }
}

package com.example.mobilemhealthpay.presentation.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mobilemhealthpay.databinding.FragmentDashBoardBinding
import com.example.mobilemhealthpay.presentation.adapters.MainDashboardPagerAdapter
import com.example.mobilemhealthpay.utils.SharedRepository
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DashBoardFragment : Fragment() {
    private lateinit var binding: FragmentDashBoardBinding

    @Inject
    lateinit var repository: SharedRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashBoardBinding.inflate(layoutInflater)
        
        val adapter = MainDashboardPagerAdapter(childFragmentManager, lifecycle)
        binding.viewpager2.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewpager2) { tab, position ->
            tab.text = when (position) {
                0 -> "Remboursements"
                1 -> "Virements"
                else -> null
            }
        }.attach()

        binding.btnRefresh.setOnClickListener {
            if (!repository.isQueueEmpty.value) {
                Toast.makeText(requireContext(), "Une opération est en cours. Veuillez patienter.", Toast.LENGTH_SHORT).show()
            } else {
                refreshBalance()
            }
        }

        observeBalance()
        observeProcessingState()

        return binding.root
    }

    private fun observeProcessingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.isQueueEmpty.collectLatest { isEmpty ->
                binding.btnRefresh.alpha = if (isEmpty) 1.0f else 0.5f
            }
        }
    }

    private fun refreshBalance() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Permission d'appel non accordée", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val ussdCode = "*144*9*1#"
            val encodedUssd = ussdCode.replace("#", Uri.encode("#"))
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$encodedUssd"))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erreur lors du lancement du code USSD", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeBalance() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.currentBalance.collect { balance ->
                binding.soldePrincipalValeur.text = balance ?: "XXXX"
                if (balance?.contains("insuffisant", ignoreCase = true) == true) {
                    binding.soldePrincipalValeur.setTextColor(
                        ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                    )
                } else {
                    binding.soldePrincipalValeur.setTextColor(
                        ContextCompat.getColor(requireContext(), android.R.color.white)
                    )
                }
            }
        }
    }
}

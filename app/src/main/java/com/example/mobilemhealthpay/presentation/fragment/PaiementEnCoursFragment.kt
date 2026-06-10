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
import timber.log.Timber
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobilemhealthpay.Resource
import com.example.mobilemhealthpay.data.AppDataBase
import com.example.mobilemhealthpay.data.entity.TransactionInfoTable
import com.example.mobilemhealthpay.data.entity.TransactionResponseEntity
import com.example.mobilemhealthpay.databinding.FragmentPaiementEncoursBinding
import com.example.mobilemhealthpay.presentation.adapters.PaiementEncoursAdapter
import com.example.mobilemhealthpay.presentation.adapters.TransactionInfoItem
import com.example.mobilemhealthpay.utils.Global
import com.example.mobilemhealthpay.utils.SharedRepository
import com.example.mobilemhealthpay.viewmodel.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PaiementEnCoursFragment : Fragment() {

    @Inject lateinit var appDataBase: AppDataBase
    @Inject lateinit var repository: SharedRepository
    
    private lateinit var binding: FragmentPaiementEncoursBinding
    private val viewModel: TransactionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPaiementEncoursBinding.inflate(layoutInflater)
        
        setupRecyclerView()
        setupListeners()
        observeData()
        updateControlButtons()
        
        return binding.root
    }

    private fun setupRecyclerView() {
        binding.scrollView.setHasFixedSize(true)
        binding.scrollView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupListeners() {
        binding.button.setOnClickListener {
            Toast.makeText(requireContext(), "Chargement des transactions...", Toast.LENGTH_SHORT).show()
            showLoader(true)
            viewModel.getTransaction(Global.NUMBER_OF_REQUEST, Global.token)
        }

        binding.btnStart.setOnClickListener {
            repository.setPaused(false)
            updateControlButtons()
            Toast.makeText(requireContext(), "Processus démarré", Toast.LENGTH_SHORT).show()
        }

        binding.btnStop.setOnClickListener {
            repository.setPaused(true)
            updateControlButtons()
            Toast.makeText(requireContext(), "Processus arrêté", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateControlButtons() {
        val isPaused = repository.isPaused()
        binding.btnStart.isEnabled = isPaused
        binding.btnStop.isEnabled = !isPaused
    }

    private fun observeData() {
        // 1. Observe API Response for manual fetch
        viewModel.getTransactionResult.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Success<*>) {
                val data = result.data
                if (data is TransactionResponseEntity && data.status == 1) {
                    data.data.forEach { tx ->
                        val table = TransactionInfoTable(
                            user_id       = tx.user_id,
                            montant       = tx.montant,
                            transactionId = tx.transaction_id,
                            numero        = tx.numero,
                            operateur     = tx.operateur,
                            comment       = tx.comment,
                            date_creation = tx.date_creation,
                            status        = 0
                        )
                        viewModel.registerTransaction(table)
                    }
                }
                showLoader(false)
            } else if (result is Resource.Failure) {
                showLoader(false)
                Toast.makeText(requireContext(), "Erreur: ${result.throwable.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. Observe Database for rendering and processing
        appDataBase.transactionDao()
            .getTransactionEnCoursFromDb()
            .observe(viewLifecycleOwner) { pendingList ->
                if (pendingList.isNullOrEmpty()) {
                    binding.info.visibility = View.VISIBLE
                    binding.scrollView.visibility = View.INVISIBLE
                    binding.button.visibility = View.VISIBLE
                    binding.processControls.visibility = View.GONE
                } else {
                    binding.info.visibility = View.INVISIBLE
                    binding.scrollView.visibility = View.VISIBLE
                    binding.button.visibility = View.GONE
                    binding.processControls.visibility = View.VISIBLE
                    
                    updateUi(pendingList)
                    
                    // Start sequential processing
                    viewLifecycleOwner.lifecycleScope.launch {
                        repository.enqueueTransactions(pendingList)
                        repository.startProcessing { numero, montant ->
                            launchUssdCode(numero, montant)
                        }
                    }
                }
            }
    }

    private fun updateUi(list: List<TransactionInfoTable>) {
        val items = list.map { tx ->
            TransactionInfoItem(
                montant       = tx.montant,
                transactionId = tx.transactionId,
                numero        = tx.numero,
                operateur     = tx.operateur,
                dateCreation  = tx.date_creation,
                comment       = tx.comment ?: "",
                status        = tx.status
            )
        }
        binding.scrollView.adapter = PaiementEncoursAdapter(items)
    }

    private fun launchUssdCode(numero: String, montant: Int) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) return

        try {
            val ussdCode = "*144*2*$numero*$montant#"
            val encodedUssd = ussdCode.replace("#", Uri.encode("#"))
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$encodedUssd"))
            startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "USSD Error")
        }
    }

    private fun showLoader(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}

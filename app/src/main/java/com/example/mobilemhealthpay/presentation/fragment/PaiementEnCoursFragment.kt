package com.example.mobilemhealthpay.presentation.fragment

import android.Manifest
import android.app.DatePickerDialog
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobilemhealthpay.Resource
import com.example.mobilemhealthpay.data.AppDataBase
import com.example.mobilemhealthpay.data.entity.RefundInfoTable
import com.example.mobilemhealthpay.data.entity.RefundResponseEntity
import com.example.mobilemhealthpay.databinding.FragmentPaiementEncoursBinding
import com.example.mobilemhealthpay.presentation.adapters.PaiementEncoursAdapter
import com.example.mobilemhealthpay.presentation.adapters.TransactionInfoItem
import com.example.mobilemhealthpay.utils.Global
import com.example.mobilemhealthpay.utils.SharedRepository
import com.example.mobilemhealthpay.viewmodel.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class PaiementEnCoursFragment : Fragment() {

    @Inject lateinit var appDataBase: AppDataBase
    @Inject lateinit var repository: SharedRepository
    
    private lateinit var binding: FragmentPaiementEncoursBinding
    private val viewModel: TransactionViewModel by viewModels()
    private var type: String = "REFUND"
    val listOM=listOf('4','5','6','7')

    private var selectedDay: String? = null
    private var selectedMonth: String? = null
    private var selectedYear: String? = null
    private var currentLiveData: LiveData<List<RefundInfoTable>>? = null

    companion object {
        fun newInstance(type: String): PaiementEnCoursFragment {
            val fragment = PaiementEnCoursFragment()
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
        binding = FragmentPaiementEncoursBinding.inflate(layoutInflater)
        
        setupRecyclerView()
        setupListeners()
        setupFilterListeners()
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
            Toast.makeText(requireContext(), "Chargement des données...", Toast.LENGTH_SHORT).show()
            showLoader(true)
            if (type == "REFUND") {
                viewModel.getRefunds(Global.secret_key, Global.TO_REFUND, Global.PAGE_SIZE)
            } else {
                viewModel.getVirements(Global.secret_key, "pending", Global.PAGE_SIZE)
            }
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

    private fun setupFilterListeners() {
        binding.btnFilterDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnClearFilter.setOnClickListener {
            clearFilter()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, y, m, d ->
            selectedYear = y.toString()
            selectedMonth = String.format("%02d", m + 1)
            selectedDay = String.format("%02d", d)

            binding.tvSelectedDate.text = "$selectedDay/$selectedMonth/$selectedYear"
            binding.btnClearFilter.visibility = View.VISIBLE

            observeData()
        }, year, month, day).show()
    }

    private fun clearFilter() {
        selectedDay = null
        selectedMonth = null
        selectedYear = null
        binding.tvSelectedDate.text = "Toutes les dates"
        binding.btnClearFilter.visibility = View.GONE
        observeData()
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
                if (data is RefundResponseEntity && data.status == 1) {
                    data.data.forEach { rf ->
                        viewModel.registerRefund(rf.toTable())
                    }
                }
                showLoader(false)
            } else if (result is Resource.Failure) {
                showLoader(false)
                Toast.makeText(requireContext(), "Erreur: ${result.throwable.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. Observe Database for rendering and processing
        val isRefund = if (type == "REFUND") 1 else 0
        currentLiveData?.removeObservers(viewLifecycleOwner)

        currentLiveData = if (selectedDay != null && selectedMonth != null && selectedYear != null) {
            appDataBase.refundDao().getRefundsByDate(0, isRefund, selectedDay!!, selectedMonth!!, selectedYear!!)
        } else {
            appDataBase.refundDao().getPendingRefunds(isRefund)
        }

        currentLiveData?.observe(viewLifecycleOwner) { pendingList ->
            val isEmpty = pendingList.isNullOrEmpty()
            
            binding.info.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.scrollView.visibility = if (isEmpty) View.INVISIBLE else View.VISIBLE
            
            // Show start/stop buttons only if list is not empty
            binding.processControls.visibility = if (isEmpty) View.GONE else View.VISIBLE
            
            // Keep the fetch button always visible
            binding.button.visibility = View.VISIBLE
            
            if (!isEmpty) {
                updateUi(pendingList!!)
                
                // As soon as there are transactions, ensure we are not paused and proceed
                if (repository.isPaused()) {
                    repository.setPaused(false)
                    updateControlButtons()
                }
                
                // Start sequential processing
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.enqueueRefunds(pendingList!!)
                    repository.startProcessing { transaction ->
                        launchUssdCode(transaction)
                    }
                }
            }
        }
    }

    private fun updateUi(list: List<RefundInfoTable>) {
        val items = list.map { rf ->
            val TwoFirstDigitOfNumber=rf.phoneNumber.take(2)
            val carrier = if(TwoFirstDigitOfNumber.last() in listOM)"Orange money" else "Moov money"
            TransactionInfoItem(
                montant       = rf.amount,
                transactionId = rf.refundId,
                numero        = rf.phoneNumber,
                operateur     = carrier,
                dateCreation  = rf.createdAt,
                comment       = rf.refundStatus,
                status        = rf.status
            )
        }
        binding.scrollView.adapter = PaiementEncoursAdapter(items)
    }

    private fun launchUssdCode(transaction: RefundInfoTable) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) return

        try {
            val ussdCode = "*144*2*${transaction.phoneNumber}*${transaction.amount}#"
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

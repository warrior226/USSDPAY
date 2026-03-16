package com.example.mobilemhealthpay.presentation.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobilemhealthpay.R
import com.example.mobilemhealthpay.data.AppDataBase
import com.example.mobilemhealthpay.data.entity.TransactionInfoTable
import com.example.mobilemhealthpay.databinding.FragmentPaiementEchecBinding
import com.example.mobilemhealthpay.presentation.adapters.PaiementEchecAdapter
import com.example.mobilemhealthpay.presentation.adapters.PaiementEncoursAdapter
import com.example.mobilemhealthpay.presentation.adapters.TransactionInfoItem
import com.example.mobilemhealthpay.viewmodel.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class PaiementEchecFragment : Fragment() {

    @Inject
    lateinit var appDataBase: AppDataBase
    private lateinit var binding: FragmentPaiementEchecBinding
    private val viewModel: TransactionViewModel by viewModels ()
    private  lateinit var transactionInfoTable: TransactionInfoTable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentPaiementEchecBinding.inflate(layoutInflater)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                oberveViewModel()
            }
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    fun oberveViewModel() {
        appDataBase.transactionDao()
            .getTransactionEnCoursFromDb()
            .observe(viewLifecycleOwner) { joinedList ->
                if (joinedList.isNotEmpty()) {
                    binding.info.visibility = View.INVISIBLE
                    binding.scrollView.visibility = View.VISIBLE
                    // Transform joined data into adapter items
                    val adapterItems = joinedList.map { transaction ->
                        TransactionInfoItem(
                            montant = transaction.montant,
                            transactionId = transaction.transaction_id,
                            numero = transaction.numero,
                            operateur = transaction.operateur,
                            dateCreation = transaction.date_creation,
                            comment= transaction.comment ?: "",
                            status = transaction.status
                        )
                    }
                    setRecyclerView(adapterItems)
                } else {
                    binding.info.visibility = View.VISIBLE
                    binding.scrollView.visibility = View.INVISIBLE
                }
            }
    }

    private fun setRecyclerView(items: List<TransactionInfoItem>) {
        binding.scrollView.setHasFixedSize(true)
        binding.scrollView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = PaiementEchecAdapter(items)
        binding.scrollView.adapter = adapter
    }



}
package com.example.mobilemhealthpay.presentation.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobilemhealthpay.R
import com.example.mobilemhealthpay.data.AppDataBase
import com.example.mobilemhealthpay.data.entity.TransactionInfoTable
import com.example.mobilemhealthpay.databinding.FragmentPaiementExecutedBinding
import com.example.mobilemhealthpay.presentation.adapters.PaiementEncoursAdapter
import com.example.mobilemhealthpay.presentation.adapters.PaiementExecutedAdapter
import com.example.mobilemhealthpay.presentation.adapters.TransactionInfoItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PaiementExecuted : Fragment() {

    @Inject
    lateinit var appDataBase: AppDataBase
    private lateinit var binding: FragmentPaiementExecutedBinding
    private  lateinit var transactionInfoTable: TransactionInfoTable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentPaiementExecutedBinding.inflate(layoutInflater)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                oberveViewModel()
            }
        }

        return binding.root
    }

    fun oberveViewModel() {
        appDataBase.transactionDao()
            .getTransactionEffectueFromDb()
            .observe(viewLifecycleOwner) { joinedList ->
                if (joinedList.isNotEmpty()) {
                    binding.info.visibility = View.INVISIBLE
                    binding.scrollView.visibility = View.VISIBLE
                    // Transform joined data into adapter items
                    val adapterItems = joinedList.map { transaction ->
                        TransactionInfoItem(
                            montant = transaction.montant,
                            transactionId = transaction.transactionId,
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
        val adapter = PaiementExecutedAdapter(items)
        binding.scrollView.adapter = adapter
    }



}
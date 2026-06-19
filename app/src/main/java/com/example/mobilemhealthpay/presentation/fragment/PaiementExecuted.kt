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
import com.example.mobilemhealthpay.data.AppDataBase
import com.example.mobilemhealthpay.databinding.FragmentPaiementExecutedBinding
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
    private var type: String = "REFUND"
    val listOM=listOf('4','5','6','7')

    companion object {
        fun newInstance(type: String): PaiementExecuted {
            val fragment = PaiementExecuted()
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
        binding = FragmentPaiementExecutedBinding.inflate(layoutInflater)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                oberveViewModel()
            }
        }

        return binding.root
    }

    fun oberveViewModel() {
        val isRefund = if (type == "REFUND") 1 else 0
        appDataBase.refundDao()
            .getSuccessfulRefunds(isRefund)
            .observe(viewLifecycleOwner) { joinedList ->
                updateUi(joinedList)
            }
    }

    private fun updateUi(joinedList: List<com.example.mobilemhealthpay.data.entity.RefundInfoTable>?) {
        if (!joinedList.isNullOrEmpty()) {
            binding.info.visibility = View.INVISIBLE
            binding.scrollView.visibility = View.VISIBLE
            val adapterItems = joinedList.map { refund ->
                val TwoFirstDigitOfNumber=refund.phoneNumber.take(2)
                val carrier = if(TwoFirstDigitOfNumber.last() in listOM)"Orange money" else "Moov money"
                TransactionInfoItem(
                    montant = refund.amount,
                    transactionId = refund.refundId,
                    numero = refund.phoneNumber,
                    operateur = carrier,
                    dateCreation = refund.createdAt,
                    comment = refund.refundStatus,
                    status = refund.status
                )
            }

            setRecyclerView(adapterItems)
        } else {
            binding.info.visibility = View.VISIBLE
            binding.scrollView.visibility = View.INVISIBLE
        }
    }

    private fun setRecyclerView(items: List<TransactionInfoItem>) {
        binding.scrollView.setHasFixedSize(true)
        binding.scrollView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = PaiementExecutedAdapter(items)
        binding.scrollView.adapter = adapter
    }
}

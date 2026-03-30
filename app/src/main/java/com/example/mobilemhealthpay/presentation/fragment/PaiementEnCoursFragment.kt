package com.example.mobilemhealthpay.presentation.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobilemhealthpay.Resource
import com.example.mobilemhealthpay.data.AppDataBase
import com.example.mobilemhealthpay.data.entity.TransactionInfoTable
import com.example.mobilemhealthpay.data.entity.TransactionResponseEntity
import com.example.mobilemhealthpay.databinding.FragmentPaiementEncoursBinding
import com.example.mobilemhealthpay.presentation.adapters.PaiementBeneficiaireItem
import com.example.mobilemhealthpay.presentation.adapters.PaiementEncoursAdapter
import com.example.mobilemhealthpay.presentation.adapters.TransactionInfoItem
import com.example.mobilemhealthpay.presentation.viewmodel.PaiementStatus
import com.example.mobilemhealthpay.presentation.viewmodel.PaiementViewModel
import com.example.mobilemhealthpay.utils.Global
import com.example.mobilemhealthpay.utils.SharedRepository
import com.example.mobilemhealthpay.viewmodel.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.toString

@AndroidEntryPoint
class PaiementEnCoursFragment() : Fragment() {

    @Inject
    lateinit var appDataBase: AppDataBase
    @Inject
    lateinit var repository: SharedRepository
    private lateinit var binding: FragmentPaiementEncoursBinding
    private  val viewModel : TransactionViewModel by viewModels()
    private  lateinit var transactionInfoTable: TransactionInfoTable

    // Create repository manually
 //   private lateinit var ussdRepository: UssdRepository<PaiementEnCoursFragmentInterface, PaiementEnCoursFragmentInteractorInterface>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentPaiementEncoursBinding.inflate(layoutInflater)
        // Your existing code...
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                //initObserver()
               // oberveViewModel()
                observeApiResponse()
                observeDatabase()

            }
        }

        //getPaiementWithBeneFiciaires
       // viewModel.loadPaiementsWithBeneficiaires()
        // Inflate the layout for this fragment
        binding.button.setOnClickListener {
            Toast.makeText(requireContext(),"Chargement des transactions en cours ...", Toast.LENGTH_SHORT).show()
            showLoader(true)
            viewModel.getTransaction(Global.NUMBER_OF_REQUEST,Global.token)
        }
        return binding.root
    }


    fun oberveViewModel() {
        appDataBase.transactionDao()
            .getTransactionEnCoursFromDb()
            .observe(viewLifecycleOwner) { joinedList ->
                if (joinedList.isNotEmpty()) {
                    binding.info.visibility = View.INVISIBLE
                    binding.scrollView.visibility = View.VISIBLE
                    binding.button.visibility=View.INVISIBLE
                    showLoader(false)
                    // Transform joined data into adapter items
                    val adapterItems = joinedList.map { transaction ->
//                        PaiementBeneficiaireItem(
//                            beneficiaire = joined.beneficiaire,
//                            montant = joined.paiement.montant,
//                            datePaiement = joined.paiement.createdAt,
//                            dateExcution = joined.executedAt,
//                            status = joined.status
//                        )
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
                    //Launch Ussd then
//                    for(transaction in joinedList){
////                        repository.updateTransactionInfo(transaction)
////                        //Si une transaction a été traitée
////                        while(repository.operationState.value){
////                            launchUssdCode(transaction.numero,transaction.montant)
////                            if(!transaction.comment.isNullOrEmpty()){
////                                viewModel.validateTransaction(transaction.transaction_id,transaction.comment.trim(), encryptWithHmacSha256(transactionInfoTable.montant.toString() + transactionInfoTable.operateur + "##" + transactionInfoTable.transactionId, "--" + transactionInfoTable.transactionId + "--"), Global.token)
////                                repository.updateOperationState(false)
////
////                            }
////                        }
////
////                        do {
////
////                            launchUssdCode(transaction.numero,transaction.montant)
////
////                        }while (repository.operationState.value)
//                    }
                } else {
                    binding.info.visibility = View.VISIBLE
                    binding.scrollView.visibility = View.INVISIBLE
                    binding.button.visibility=View.VISIBLE
//                    showLoader(true)
                }
            }
    }


    private fun showLoader(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setRecyclerView(items: List<TransactionInfoItem>) {
        binding.scrollView.setHasFixedSize(true)
        binding.scrollView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = PaiementEncoursAdapter(items)
        binding.scrollView.adapter = adapter
    }

    //Init observer
    private fun initObserver(){

        viewModel.getTransactionResult.observe(viewLifecycleOwner){ it ->
            if(it is Resource.Success<*>){

                val message = when (val data=it.data){
                    is TransactionResponseEntity -> data.message
                    else -> ""
                }

                when(it.data){
                    is TransactionResponseEntity->{
                        Log.d("TAG", "initObserver: the data sent is ${it.data  } ")
                        when(it.data.status){
                            1->{
                                //L'opération s'est bien passée
                                //ussdNumber= "*144*2*${it.data.data[0].numero}*${it.data.data[0].montant}#"

                                //Icic j'enregistre les data de la transaction dans ma BD
                                val transactionInfoEntity=it.data.data[0]
                                for (transactionInfo in it.data.data){
                                    transactionInfoTable= TransactionInfoTable()
                                    transactionInfoTable.user_id=transactionInfo.user_id
                                    transactionInfoTable.montant=transactionInfo.montant
                                    transactionInfoTable.transactionId=transactionInfo.transaction_id
                                    transactionInfoTable.numero=transactionInfo.numero
                                    transactionInfoTable.operateur=transactionInfo.operateur
                                    transactionInfoTable.comment=transactionInfo.comment
                                    transactionInfoTable.date_creation=transactionInfo.date_creation
                                    //Register transaction in Database
                                    viewModel.registerTransaction(transactionInfoTable)
                                }


                                //Ici j'enregistre les informations d'une transaction dans la BD

                                //appDatabase.transactionInfoDAO().insert(transactionInfoTable)
//                                lifecycleScope.launch {
//                                    if (callViewModel.hasNoFlavorSet())
//                                        callViewModel.setDialUpType(getString(R.string.normal))
//                                    activity?.let {
//                                        if (ussdApi.verifyAccessibilityAccess(it)) {
//                                            when (callViewModel.dialUpType.value) {
//                                                getString(R.string.custom) ->homePageFragmentPresenterInterface.callOverlay(it)
//                                                getString(R.string.splash) -> homePageFragmentPresenterInterface.callSplashOverlay(it)
//                                                else -> homePageFragmentPresenterInterface.call(it,transactionInfoTable)
//                                            }
//                                        }
//                                    }
//                                }


                            }
                        }
                    }
                }
            }
        }




    }

    private fun launchUssdCode(numero:String,montant:Int) {
        // Check permission one more time before dialing
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Call permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val ussdCode = "*144*2*$numero*$montant#"
            val encodedUssd = ussdCode.replace("#", Uri.encode("#"))
            val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$encodedUssd"))
            startActivity(callIntent)

            //    Log.d("TAG", "launchUssdCode: USSD code dialed successfully")

        } catch (e: SecurityException) {
            //  Log.e("TAG", "launchUssdCode: Security Exception", e)
            Toast.makeText(
                requireContext(),
                "Permission error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
            // Log.e("TAG", "launchUssdCode: Error", e)
            Toast.makeText(
                requireContext(),
                "Error dialing USSD: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun encryptWithHmacSha256(data: String, secret: String): String {
        val secretKeySpec = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKeySpec)
        val hash = mac.doFinal(data.toByteArray())
        return Base64.getEncoder().encodeToString(hash)
    }

    // ----- Observe API Response → Save to DB -----

    private fun observeApiResponse() {
        viewModel.getTransactionResult.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Success<*>) {
                val data = result.data
                if (data is TransactionResponseEntity && data.status == 1) {
                    data.data.forEach { transactionInfo ->
                        val table = TransactionInfoTable(
                            user_id       = transactionInfo.user_id,
                            montant       = transactionInfo.montant,
                            transactionId = transactionInfo.transaction_id,
                            numero        = transactionInfo.numero,
                            operateur     = transactionInfo.operateur,
                            comment       = transactionInfo.comment,
                            date_creation = transactionInfo.date_creation
                        )
                        viewModel.registerTransaction(table)
                    }
                }
            }
        }
    }

    // ----- Observe Room DB → Render UI + Start Processor -----

    private fun observeDatabase() {
        appDataBase.transactionDao()
            .getTransactionEnCoursFromDb()  // LiveData of status=0 transactions
            .observe(viewLifecycleOwner) { pendingList ->
                if (pendingList.isNotEmpty()) {
                    binding.info.visibility    = View.INVISIBLE
                    binding.scrollView.visibility = View.VISIBLE
                    binding.button.visibility  = View.INVISIBLE
                    showLoader(false)

                    val adapterItems = pendingList.map { tx ->
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
                    setRecyclerView(adapterItems)

                    // Enqueue and start processing
                    viewLifecycleOwner.lifecycleScope.launch {
                        repository.enqueueTransactions(pendingList)
                        startProcessing()
                    }

                } else {
                    binding.info.visibility    = View.VISIBLE
                    binding.scrollView.visibility = View.INVISIBLE
                    binding.button.visibility  = View.VISIBLE
                }
            }
    }

    // ----- Sequential Processor Launcher -----

    private suspend fun startProcessing() {
        repository.startProcessing(
            onLaunchUssd = { numero, montant ->
                launchUssdCode(numero, montant)
            },
            onValidate = { transactionId, comment,montant,operateur ->
                viewModel.validateTransaction(transactionId, comment.trim(),encryptWithHmacSha256("$montant$operateur##$transactionId","--$transactionId--"),Global.token)
            },
            onTransactionFailed = { failedTransaction ->
                Log.d("TAG", "Transaction failed: ${failedTransaction.transactionId} — ${failedTransaction.comment}")
                viewModel.validateTransaction(failedTransaction.transactionId, failedTransaction.comment!!.trim(),encryptWithHmacSha256("${failedTransaction.montant}${failedTransaction.operateur}##${failedTransaction.transactionId}","--${failedTransaction.transactionId}--"),Global.token)
            },
            onInsufficientFunds = { transaction ->
//                requireActivity().runOnUiThread {
//                    showInsufficientFundsDialog()
//                }
                viewModel.validateTransaction(transaction.transactionId, transaction.comment!!.trim(),encryptWithHmacSha256("${transaction.montant}${transaction.operateur}##${transaction.transactionId}","--${transaction.transactionId}--"),Global.token)

            }
        )
    }


}
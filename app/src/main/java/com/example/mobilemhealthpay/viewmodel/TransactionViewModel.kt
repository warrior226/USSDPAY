package com.example.mobilemhealthpay.viewmodel

import timber.log.Timber
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.mobilemhealthpay.Resource
import com.example.mobilemhealthpay.domain.usecases.TransactionUseCase
import com.example.mobilemhealthpay.utils.contextIO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(private val transactionUseCase: TransactionUseCase):ViewModel() {

    private val params = MutableLiveData<Params>()

    val getTransactionResult: LiveData<Resource<*>> = params.switchMap { params ->

        when (params) {

            is Params.GetTransactionInfo -> {
                transactionUseCase.getTransactionInformation(params.numberOfRequest, params.token).asLiveData(contextIO())
            }

            is Params.ValidateTransaction -> {
                transactionUseCase.validateTransaction(params.transactionId, params.message, params.hash, params.token).asLiveData(contextIO())
            }

            is Params.GetRefunds -> {
                transactionUseCase.getRefunds(params.secretKey, params.refundStatus, params.numberOfRequest).asLiveData(contextIO())
            }

            is Params.CompleteRefund -> {
                transactionUseCase.completeRefund(params.secretKey, params.refundId, params.reason).asLiveData(contextIO())
            }

            is Params.FailRefund -> {
                transactionUseCase.failRefund(params.secretKey, params.refundId, params.reason).asLiveData(contextIO())
            }

            is Params.GetVirements -> {
                transactionUseCase.getVirements(params.secretKey, params.status, params.numberOfRequest).asLiveData(contextIO())
            }

            is Params.ReportTransactionAttempt -> {
                transactionUseCase.reportTransactionAttempt(params.secretKey, params.transactionId, params.status).asLiveData(contextIO())
            }
        }

    }

    fun getTransaction(numberOfRequest: Int, token: String) {
        params.value = Params.GetTransactionInfo(numberOfRequest, token)
        Timber.d("getTransaction: The function is called ")
    }

    fun validateTransaction(transactionId: String, message: String, hash: String, token: String) {
        params.value = Params.ValidateTransaction(transactionId, message, hash, token)
        Timber.d("validateTransaction: The function is called ")
    }

    fun getRefunds(secretKey: String, refundStatus: String, numberOfRequest: Int) {
        params.value = Params.GetRefunds(secretKey, refundStatus, numberOfRequest)
        Timber.d("getRefunds: The function is called ")
    }

    fun completeRefund(secretKey: String, refundId: String, reason: String) {
        params.value = Params.CompleteRefund(secretKey, refundId, reason)
        Timber.d("completeRefund: The function is called ")
    }

    fun failRefund(secretKey: String, refundId: String, reason: String) {
        params.value = Params.FailRefund(secretKey, refundId, reason)
        Timber.d("failRefund: The function is called ")
    }

    fun getVirements(secretKey: String, status: String, numberOfRequest: Int) {
        params.value = Params.GetVirements(secretKey, status, numberOfRequest)
        Timber.d("getVirements: The function is called ")
    }

    fun reportTransactionAttempt(secretKey: String, transactionId: String, status: String) {
        params.value = Params.ReportTransactionAttempt(secretKey, transactionId, status)
        Timber.d("reportTransactionAttempt: The function is called ")
    }

    fun registerRefund(refundInfoTable: com.example.mobilemhealthpay.data.entity.RefundInfoTable) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionUseCase.registerRefund(refundInfoTable)
        }
    }

    sealed class Params {

        data class GetTransactionInfo(val numberOfRequest: Int, val token: String) : Params()
        data class ValidateTransaction(val transactionId: String, val message: String, val hash: String, val token: String) : Params()
        data class GetRefunds(val secretKey: String, val refundStatus: String, val numberOfRequest: Int) : Params()
        data class CompleteRefund(val secretKey: String, val refundId: String, val reason: String) : Params()
        data class FailRefund(val secretKey: String, val refundId: String, val reason: String) : Params()
        data class GetVirements(val secretKey: String, val status: String, val numberOfRequest: Int) : Params()
        data class ReportTransactionAttempt(val secretKey: String, val transactionId: String, val status: String) : Params()

    }
}

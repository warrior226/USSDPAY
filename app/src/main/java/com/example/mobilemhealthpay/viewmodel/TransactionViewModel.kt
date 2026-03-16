package com.example.mobilemhealthpay.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.mobilemhealthpay.Resource
import com.example.mobilemhealthpay.data.entity.TransactionInfoTable
import com.example.mobilemhealthpay.domain.usecases.TransactionUseCase
import com.example.mobilemhealthpay.utils.contextIO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(private val transactionUseCase: TransactionUseCase):ViewModel() {

    private val params = MutableLiveData<Params>()

    val getTransactionResult: LiveData<Resource<*>> = params.switchMap{ params->

        when (params){

            is Params.GetTransactionInfo -> {
               transactionUseCase.getTransactionInformation(params.numberOfRequest,params.token).asLiveData(contextIO())
            }

            is Params.ValidateTransaction->{
                transactionUseCase.validateTransaction(params.transactionId,params.message,params.hash,params.token).asLiveData(contextIO())
            }
        }

    }

    fun getTransaction(numberOfRequest: Int,token: String){
        params.value=Params.GetTransactionInfo(numberOfRequest,token)
        Log.d("TAG", "getTransaction: The function is called ")
    }

    fun validateTransaction(transactionId:String,message:String,hash:String,token: String){
        params.value=Params.ValidateTransaction(transactionId,message,hash,token)
        Log.d("TAG", "validateTransaction: The function is called ")
    }

    fun registerTransaction(transactionInfoTable: TransactionInfoTable){
        viewModelScope.launch(Dispatchers.IO) {
            transactionUseCase.registerTransaction(transactionInfoTable)
        }
    }

    sealed class Params {

        data class GetTransactionInfo(val numberOfRequest:Int,val token:String):Params()
        data class ValidateTransaction(val transactionId:String,val message:String,val hash:String,val token:String):Params()

    }

}
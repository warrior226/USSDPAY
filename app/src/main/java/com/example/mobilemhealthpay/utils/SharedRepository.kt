package com.example.mobilemhealthpay.utils

import com.example.mobilemhealthpay.data.Dao.TransactionInfoDao
import com.example.mobilemhealthpay.data.entity.TransactionInfoEntity
import com.example.mobilemhealthpay.data.entity.TransactionInfoTable
import com.example.mobilemhealthpay.domain.usecases.TransactionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedRepository @Inject constructor(
    private val transactionUseCase: TransactionUseCase
) {

    private  val _transactionInfo= MutableStateFlow<TransactionInfoEntity?>(TransactionInfoEntity())
    val transactionInfo: StateFlow<TransactionInfoEntity?> = _transactionInfo.asStateFlow()
    val operationState=MutableStateFlow(true)
    fun updateTransactionInfo(transactionInfoEntity: TransactionInfoEntity){
        _transactionInfo.value=transactionInfoEntity
    }



    fun clearTransactionInfo(){
        _transactionInfo.value=null
    }
    fun updateOperationState(state:Boolean){
        operationState.value=state
    }

    suspend fun registerTransaction(transactionInfoTable: TransactionInfoTable){
        transactionUseCase.registerTransaction(transactionInfoTable)
    }
}

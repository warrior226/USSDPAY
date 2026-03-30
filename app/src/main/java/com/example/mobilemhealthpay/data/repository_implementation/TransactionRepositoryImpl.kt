package com.example.mobilemhealthpay.data.repository_implementation

import com.example.mobilemhealthpay.data.AppDataBase
import com.example.mobilemhealthpay.data.entity.TransactionInfoTable
import com.example.mobilemhealthpay.data.entity.TransactionResponseEntity
import com.example.mobilemhealthpay.data.remote.Api_Service.PaiementApiService
import com.example.mobilemhealthpay.repository.TransactionRepository
import com.example.mobilemhealthpay.utils.Global
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(private val PaiementApiService: PaiementApiService,private val appDataBase: AppDataBase): TransactionRepository {

    override suspend fun getTransactionInformation(
        numberOfRequest: Int,
        token: String
    ): TransactionResponseEntity {
       val transactionToken= Global.Bearer+Global.token
        return PaiementApiService.getTransactionInformation(numberOfRequest,transactionToken).toModel()
    }

    override suspend fun validateTransaction(
        transactionId: String,
        message: String,
        hash:String,
        token: String
    ): TransactionResponseEntity {
        val  transactionToken=Global.Bearer+Global.token
        return  PaiementApiService.validateTransaction(transactionId,message,hash,transactionToken).toModel()
    }

    override suspend fun registerTransaction(transactionInfoTable: TransactionInfoTable) {
        appDataBase.transactionDao().insert(transactionInfoTable)
    }

    override suspend fun getTransactionById(transactionId: Int): TransactionInfoTable {
      return  appDataBase.transactionDao().getTransactionById(transactionId).value!!
    }

}
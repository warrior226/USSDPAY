package com.example.mobilemhealthpay.repository

import com.example.mobilemhealthpay.data.entity.TransactionInfoTable
import com.example.mobilemhealthpay.data.entity.TransactionResponseEntity

interface TransactionRepository {

    suspend fun getTransactionInformation(numberOfRequest:Int,token:String): TransactionResponseEntity


    suspend fun validateTransaction(transactionId:String,message:String,hash:String,token: String):TransactionResponseEntity

    suspend fun registerTransaction(transactionInfoTable: TransactionInfoTable)

}
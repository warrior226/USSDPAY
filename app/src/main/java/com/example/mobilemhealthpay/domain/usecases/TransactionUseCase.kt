package com.example.mobilemhealthpay.domain.usecases

import android.util.Log
import com.example.mobilemhealthpay.Resource
import com.example.mobilemhealthpay.data.entity.TransactionInfoEntity
import com.example.mobilemhealthpay.data.entity.TransactionInfoTable
import com.example.mobilemhealthpay.repository.TransactionRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionUseCase @Inject constructor(private val transactionRepository: TransactionRepository) {

     fun getTransactionInformation(numberOfRequest:Int,transactionToken: String)= flow {
         emit(Resource.loading())
         val result =
             transactionRepository.getTransactionInformation(numberOfRequest, transactionToken)
         Log.d("TAG", "getTransactionInformation: the result is ${result} ")
         emit(Resource.success(result))
     }.catch {
         emit(Resource.failure(it))
         Log.d("TAG", "getTransactionInformation: in the catch block,the result is ${it.message} ")

     }
     fun validateTransaction(transactionId: String, message: String,hash:String,token: String)= flow {
         emit(Resource.loading())
         val result = transactionRepository.validateTransaction(transactionId, message, hash, token)
         Log.d("TAG", "validateTransaction: the result ${result} ")
         emit(Resource.success(result))
     }.catch {
         emit(Resource.failure(it))
         Log.d("TAG", "validateTransaction: ${it.message}")
     }

    suspend fun registerTransaction(transactionInfoTable: TransactionInfoTable){
        transactionRepository.registerTransaction(transactionInfoTable)
    }

   suspend fun getTransactionById(transactionId:Int): TransactionInfoTable{
       return transactionRepository.getTransactionById(transactionId)
    }
}
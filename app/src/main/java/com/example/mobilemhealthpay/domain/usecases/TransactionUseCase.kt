package com.example.mobilemhealthpay.domain.usecases

import timber.log.Timber
import com.example.mobilemhealthpay.Resource
import com.example.mobilemhealthpay.repository.TransactionRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionUseCase @Inject constructor(private val transactionRepository: TransactionRepository) {

    fun getTransactionInformation(numberOfRequest: Int, transactionToken: String) = flow {
        emit(Resource.loading())
        val result = transactionRepository.getTransactionInformation(numberOfRequest, transactionToken)
        Timber.d("getTransactionInformation: the result is $result")
        emit(Resource.success(result))
    }.catch {
        emit(Resource.failure(it))
        Timber.e(it, "getTransactionInformation failure")
    }

    fun validateTransaction(transactionId: String, message: String, hash: String, token: String) = flow {
        emit(Resource.loading())
        val result = transactionRepository.validateTransaction(transactionId, message, hash, token)
        Timber.d("validateTransaction: the result $result")
        emit(Resource.success(result))
    }.catch {
        emit(Resource.failure(it))
        Timber.e(it, "validateTransaction failure")
    }

    fun getRefunds(secretKey: String, refundStatus: String, numberOfRequest: Int) = flow {
        emit(Resource.loading())
        val result = transactionRepository.getRefunds(secretKey, refundStatus, numberOfRequest)
        Timber.d("getRefunds: the result $result")
        emit(Resource.success(result))
    }.catch {
        emit(Resource.failure(it))
        Timber.e(it, "getRefunds failure")
    }

    fun completeRefund(secretKey: String, refundId: String, reason: String) = flow {
        emit(Resource.loading())
        val result = transactionRepository.completeRefund(secretKey, refundId, reason)
        Timber.d("completeRefund: the result $result")
        emit(Resource.success(result))
    }.catch {
        emit(Resource.failure(it))
        Timber.e(it, "completeRefund failure")
    }

    fun failRefund(secretKey: String, refundId: String, reason: String) = flow {
        emit(Resource.loading())
        val result = transactionRepository.failRefund(secretKey, refundId, reason)
        Timber.d("failRefund: the result $result")
        emit(Resource.success(result))
    }.catch {
        emit(Resource.failure(it))
        Timber.e(it, "failRefund failure")
    }

    suspend fun registerRefund(refundInfoTable: com.example.mobilemhealthpay.data.entity.RefundInfoTable) {
        transactionRepository.registerRefund(refundInfoTable)
    }

    suspend fun getRefundById(refundId: String): com.example.mobilemhealthpay.data.entity.RefundInfoTable? {
        return transactionRepository.getRefundById(refundId)
    }

    fun getVirements(secretKey: String, status: String, numberOfRequest: Int) = flow {
        emit(Resource.loading())
        val result = transactionRepository.getVirements(secretKey, status, numberOfRequest)
        Timber.d("getVirements: the result $result")
        emit(Resource.success(result))
    }.catch {
        emit(Resource.failure(it))
        Timber.e(it, "getVirements failure")
    }

    fun reportTransactionAttempt(secretKey: String, transactionId: String, status: String) = flow {
        emit(Resource.loading())
        val result = transactionRepository.reportTransactionAttempt(secretKey, transactionId, status)
        Timber.d("reportTransactionAttempt: the result $result")
        emit(Resource.success(result))
    }.catch {
        emit(Resource.failure(it))
        Timber.e(it, "reportTransactionAttempt failure")
    }
}

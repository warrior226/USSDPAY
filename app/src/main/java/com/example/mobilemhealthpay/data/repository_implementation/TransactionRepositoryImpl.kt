package com.example.mobilemhealthpay.data.repository_implementation

import com.example.mobilemhealthpay.data.AppDataBase
import com.example.mobilemhealthpay.data.entity.RefundInfoTable
import com.example.mobilemhealthpay.data.entity.RefundResponseEntity
import com.example.mobilemhealthpay.data.entity.TransactionResponseEntity
import com.example.mobilemhealthpay.data.remote.Api_Service.PaiementApiService
import com.example.mobilemhealthpay.data.remote.dto.ApiResponse
import com.example.mobilemhealthpay.data.remote.dto.RefundCompleteDto
import com.example.mobilemhealthpay.data.remote.dto.RefundCompleteRequest
import com.example.mobilemhealthpay.data.remote.dto.RefundFailRequest
import com.example.mobilemhealthpay.data.remote.dto.TransactionAttemptRequest
import com.example.mobilemhealthpay.data.remote.dto.VirementDto
import com.example.mobilemhealthpay.repository.TransactionRepository
import com.example.mobilemhealthpay.utils.Global
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val paiementApiService: PaiementApiService,
    private val appDataBase: AppDataBase
) : TransactionRepository {

    override suspend fun getTransactionInformation(
        numberOfRequest: Int,
        token: String
    ): TransactionResponseEntity {
        val transactionToken = Global.Bearer + Global.token
        return paiementApiService.getTransactionInformation(numberOfRequest, transactionToken).toModel()
    }

    override suspend fun validateTransaction(
        transactionId: String,
        message: String,
        hash: String,
        token: String
    ): TransactionResponseEntity {
        val transactionToken = Global.Bearer + Global.token
        return paiementApiService.validateTransaction(transactionId, message, hash, transactionToken).toModel()
    }

    override suspend fun registerRefund(refundInfoTable: RefundInfoTable) {
        appDataBase.refundDao().insert(refundInfoTable)
    }

    override suspend fun getRefunds(secretKey: String, refundStatus: String, numberOfRequest: Int): RefundResponseEntity {
        return paiementApiService.getRefunds(secretKey, refundStatus, numberOfRequest).toModel()
    }

    override suspend fun completeRefund(
        secretKey: String,
        refundId: String,
        reason: String
    ): ApiResponse<RefundCompleteDto> {
        val request = RefundCompleteRequest(reason)
        return paiementApiService.completeRefund(secretKey, refundId, request)
    }

    override suspend fun failRefund(
        secretKey: String,
        refundId: String,
        reason: String
    ): ApiResponse<RefundCompleteDto> {
        val request = RefundFailRequest(reason)
        return paiementApiService.failRefund(secretKey, refundId, request)
    }

    override suspend fun getRefundById(refundId: String): RefundInfoTable? {
        return appDataBase.refundDao().getRefundById(refundId)
    }

    override suspend fun getVirements(secretKey: String, status: String, numberOfRequest: Int): RefundResponseEntity {
        return paiementApiService.getVirements(secretKey, status, numberOfRequest).toModel()
    }

    override suspend fun reportTransactionAttempt(
        secretKey: String,
        transactionId: String,
        status: String
    ): ApiResponse<VirementDto> {
        val request = TransactionAttemptRequest(status)
        return paiementApiService.reportTransactionAttempt(secretKey, transactionId, request)
    }

}

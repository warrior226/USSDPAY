package com.example.mobilemhealthpay.repository

import com.example.mobilemhealthpay.data.entity.RefundInfoTable
import com.example.mobilemhealthpay.data.entity.RefundResponseEntity
import com.example.mobilemhealthpay.data.entity.TransactionResponseEntity
import com.example.mobilemhealthpay.data.remote.dto.ApiResponse
import com.example.mobilemhealthpay.data.remote.dto.RefundCompleteDto
import com.example.mobilemhealthpay.data.remote.dto.VirementDto

interface TransactionRepository {

    suspend fun getTransactionInformation(numberOfRequest: Int, token: String): TransactionResponseEntity

    suspend fun validateTransaction(
        transactionId: String,
        message: String,
        hash: String,
        token: String
    ): TransactionResponseEntity

    suspend fun registerRefund(refundInfoTable: RefundInfoTable)

    suspend fun getRefunds(secretKey: String, refundStatus: String, numberOfRequest: Int): RefundResponseEntity

    suspend fun completeRefund(
        secretKey: String,
        refundId: String,
        reason: String
    ): ApiResponse<RefundCompleteDto>

    suspend fun failRefund(
        secretKey: String,
        refundId: String,
        reason: String
    ): ApiResponse<RefundCompleteDto>

    suspend fun getRefundById(refundId: String): RefundInfoTable?

    suspend fun getVirements(secretKey: String, status: String, numberOfRequest: Int): RefundResponseEntity

    suspend fun reportTransactionAttempt(
        secretKey: String,
        transactionId: String,
        status: String
    ): ApiResponse<VirementDto>
}

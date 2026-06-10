package com.example.mobilemhealthpay.data.remote.dto

import androidx.annotation.Keep
import com.example.mobilemhealthpay.data.entity.RefundInfoEntity
import com.example.mobilemhealthpay.data.entity.RefundInfoTable
import com.squareup.moshi.Json

@Keep
data class RefundInfoDto(
    @field:Json(name = "refund_id")
    val refund_id: String,
    @field:Json(name = "phone_number")
    val phone_number: String,
    @field:Json(name = "amount")
    val amount: Int,
    @field:Json(name = "provider_transaction_id")
    val provider_transaction_id: String,
    @field:Json(name = "refund_status")
    val refund_status: String,
    @field:Json(name = "attempt_count")
    val attempt_count: Int,
    @field:Json(name = "expires_at")
    val expires_at: String,
    @field:Json(name = "created_at")
    val created_at: String
) {
    fun toModel() = RefundInfoEntity(
        refundId = refund_id,
        phoneNumber = phone_number,
        amount = amount,
        providerTransactionId = provider_transaction_id,
        refundStatus = refund_status,
        attemptCount = attempt_count,
        expiresAt = expires_at,
        createdAt = created_at
    )

    fun toTable() = RefundInfoTable(
        refundId = refund_id,
        phoneNumber = phone_number,
        amount = amount,
        providerTransactionId = provider_transaction_id,
        refundStatus = refund_status,
        attemptCount = attempt_count,
        expiresAt = expires_at,
        createdAt = created_at
    )
}

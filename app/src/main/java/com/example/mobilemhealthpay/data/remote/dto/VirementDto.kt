package com.example.mobilemhealthpay.data.remote.dto

import com.squareup.moshi.Json
import com.example.mobilemhealthpay.data.entity.RefundInfoEntity
import com.google.gson.annotations.SerializedName

data class VirementDto(
    @field:Json(name = "id")
    @SerializedName("id")
    val id: String,

    @field:Json(name = "user_id")
    @SerializedName("user_id")
    val userId: String,

    @field:Json(name = "phone_number")
    @SerializedName("phone_number")
    val phoneNumber: String,

    @field:Json(name = "amount")
    @SerializedName("amount")
    val amount: Int,

    @field:Json(name = "description")
    @SerializedName("description")
    val description: String?,

    @field:Json(name = "payment_ref")
    @SerializedName("payment_ref")
    val paymentRef: String?,

    @field:Json(name = "service_type")
    @SerializedName("service_type")
    val serviceType: String?,

    @field:Json(name = "status")
    @SerializedName("status")
    val status: String,

    @field:Json(name = "created_at")
    @SerializedName("created_at")
    val createdAt: String,

    @field:Json(name = "updated_at")
    @SerializedName("updated_at")
    val updatedAt: String
) {
    fun toModel(): RefundInfoEntity {
        return RefundInfoEntity(
            refundId = id,
            phoneNumber = phoneNumber,
            amount = amount,
            providerTransactionId = paymentRef ?: "",
            refundStatus = status,
            attemptCount = 0,
            expiresAt = updatedAt,
            createdAt = createdAt,
            isRefund = 0, // 0 for Virement
            virementUserId = userId,
            virementDescription = description,
            virementPaymentRef = paymentRef,
            virementServiceType = serviceType
        )
    }
}

package com.example.mobilemhealthpay.data.entity

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class RefundInfoEntity(
    val refundId: String,
    val phoneNumber: String,
    val amount: Int,
    val providerTransactionId: String,
    val refundStatus: String,
    val attemptCount: Int,
    val expiresAt: String,
    val createdAt: String,
    val isRefund: Int = 1,
    val virementUserId: String? = null,
    val virementDescription: String? = null,
    val virementPaymentRef: String? = null,
    val virementServiceType: String? = null
) : Parcelable {
    fun toTable(): RefundInfoTable {
        return RefundInfoTable(
            refundId = refundId,
            phoneNumber = phoneNumber,
            amount = amount,
            providerTransactionId = providerTransactionId,
            refundStatus = refundStatus,
            attemptCount = attemptCount,
            expiresAt = expiresAt,
            createdAt = createdAt,
            isRefund = isRefund,
            virementUserId = virementUserId,
            virementDescription = virementDescription,
            virementPaymentRef = virementPaymentRef,
            virementServiceType = virementServiceType
        )
    }
}

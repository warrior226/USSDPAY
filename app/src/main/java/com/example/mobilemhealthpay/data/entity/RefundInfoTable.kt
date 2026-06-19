package com.example.mobilemhealthpay.data.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "refund_info")
@Keep
data class RefundInfoTable(
    @PrimaryKey
    @ColumnInfo(name = "refund_id")
    val refundId: String,
    
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,
    
    @ColumnInfo(name = "amount")
    val amount: Int,
    
    @ColumnInfo(name = "provider_transaction_id")
    val providerTransactionId: String,
    
    @ColumnInfo(name = "refund_status")
    val refundStatus: String,
    
    @ColumnInfo(name = "attempt_count")
    val attemptCount: Int,
    
    @ColumnInfo(name = "expires_at")
    val expiresAt: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "status")
    val status: Int = 0, // 0: Pending, 1: Success, 2: Failed

    @ColumnInfo(name = "last_attempt_at")
    val lastAttemptAt: Long = 0L,

    @ColumnInfo(name = "is_refund")
    val isRefund: Int = 1, // 1: Refund, 0: Virement

    @ColumnInfo(name = "virement_user_id")
    val virementUserId: String? = null,

    @ColumnInfo(name = "virement_description")
    val virementDescription: String? = null,

    @ColumnInfo(name = "virement_payment_ref")
    val virementPaymentRef: String? = null,

    @ColumnInfo(name = "virement_service_type")
    val virementServiceType: String? = null
) {
    fun toModel(): RefundInfoEntity {
        return RefundInfoEntity(
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

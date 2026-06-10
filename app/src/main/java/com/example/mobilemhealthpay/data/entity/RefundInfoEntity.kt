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
    val createdAt: String
) : Parcelable

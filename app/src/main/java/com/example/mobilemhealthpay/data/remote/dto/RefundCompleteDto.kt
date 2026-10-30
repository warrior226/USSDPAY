package com.example.mobilemhealthpay.data.remote.dto

import com.squareup.moshi.Json
import com.google.gson.annotations.SerializedName

data class RefundCompleteDto(
    @field:Json(name = "refund_id")
    @SerializedName("refund_id")
    val refundId: String,

    @field:Json(name = "refund_status")
    @SerializedName("refund_status")
    val refundStatus: String,

    @field:Json(name = "attempt_count")
    @SerializedName("attempt_count")
    val attemptCount: Int
)

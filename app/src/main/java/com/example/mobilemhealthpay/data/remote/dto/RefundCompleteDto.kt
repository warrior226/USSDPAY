package com.example.mobilemhealthpay.data.remote.dto

import com.squareup.moshi.Json

data class RefundCompleteDto(
    @field:Json(name = "refund_id")
    val refundId: String,
    @field:Json(name = "refund_status")
    val refundStatus: String,
    @field:Json(name = "attempt_count")
    val attemptCount: Int
)

package com.example.mobilemhealthpay.data.remote.dto

import com.squareup.moshi.Json

data class RefundFailRequest(
    @field:Json(name = "reason")
    val reason: String
)

package com.example.mobilemhealthpay.data.remote.dto

import com.squareup.moshi.Json

data class TransactionAttemptRequest(
    @field:Json(name = "status") val status: String
)

package com.example.mobilemhealthpay.data.remote.dto

data class ApiResponse<T>(
    val status: Int,
    val message: String,
    val data: T?,
    val page: Int?,
    val page_size: Int?,
    val total: Int?
)

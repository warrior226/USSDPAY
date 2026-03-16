package com.example.mobilemhealthpay.data.remote.dto

data class ApiResponse<T>(
    val status: Int,
    val status_code: Int,
    val message: String,
    val data: T?,
    val page: Int?,
    val nb_per_page: Int?,
    val total: Int?
)

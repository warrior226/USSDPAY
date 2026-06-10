package com.example.mobilemhealthpay.data.remote.dto

import com.example.mobilemhealthpay.data.entity.RefundResponseEntity
import com.squareup.moshi.Json

data class RefundDto(
    @field:Json(name = "status")
    val status: Int,
    @field:Json(name = "message")
    val message: String,
    @field:Json(name = "data")
    val data: List<RefundInfoDto>,
    @field:Json(name = "page")
    val page: Int,
    @field:Json(name = "page_size")
    val page_size: Int,
    @field:Json(name = "total")
    val total: Int
) {

    fun toModel() = RefundResponseEntity(
        status = status,
        message = message,
        data = data.map { it.toModel() },
        page = page,
        pageSize = page_size,
        total = total
    )
}

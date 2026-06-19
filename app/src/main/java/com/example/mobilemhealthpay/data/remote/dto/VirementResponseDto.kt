package com.example.mobilemhealthpay.data.remote.dto

import com.squareup.moshi.Json
import com.example.mobilemhealthpay.data.entity.RefundResponseEntity

data class VirementResponseDto(
    @field:Json(name = "status") val status: Int,
    @field:Json(name = "message") val message: String,
    @field:Json(name = "data") val data: List<VirementDto>,
    @field:Json(name = "page") val page: Int,
    @field:Json(name = "page_size") val pageSize: Int,
    @field:Json(name = "total") val total: Int
) {
    fun toModel(): RefundResponseEntity {
        return RefundResponseEntity(
            status = status,
            message = message,
            data = data.map { it.toModel() },
            page = page,
            pageSize = pageSize,
            total = total
        )
    }
}

package com.example.mobilemhealthpay.data.remote.dto

import com.example.mobilemhealthpay.data.entity.TransactionResponseEntity
import com.squareup.moshi.Json

data class TransactionResponseDto(
    @field:Json(name = "status")
    val status:Int,
    @field:Json(name="message")
    val message:String,
    @field:Json(name="data")
    val data:List<TransactionInfoDto>
) {

    fun toModel()= TransactionResponseEntity(
        status = status,
        message = message,
        data = data.map { it.toModel() }
    )
}
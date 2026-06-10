package com.example.mobilemhealthpay.data.entity

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class RefundResponseEntity(
    val status: Int,
    val message: String,
    val data: List<RefundInfoEntity>,
    val page: Int,
    val pageSize: Int,
    val total: Int
) : Parcelable

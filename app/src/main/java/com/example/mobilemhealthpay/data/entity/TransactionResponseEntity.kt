package com.example.mobilemhealthpay.data.entity

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize

data class TransactionResponseEntity(
    val status:Int,
    val message:String,
    val data:List<TransactionInfoEntity>
):Parcelable
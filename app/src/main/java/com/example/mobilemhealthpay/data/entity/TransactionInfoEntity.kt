package com.example.mobilemhealthpay.data.entity

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class TransactionInfoEntity (
    val user_id:Int=0,
    val montant:Int=0,
    val transaction_id:String="",
    val numero:String="",
    val operateur:String="",
    val comment:String?="",
    val date_creation:String="",
    val status:Int=0
):Parcelable

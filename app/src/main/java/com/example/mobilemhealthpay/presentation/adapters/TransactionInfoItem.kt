package com.example.mobilemhealthpay.presentation.adapters

import com.example.mobilemhealthpay.data.entity.BeneficiaireEntity

data class TransactionInfoItem(
    val montant: Int,
    val transactionId:String,
    val numero:String,
    val operateur:String,
    val dateCreation:String,
    val comment: String,
    val status:Int
)


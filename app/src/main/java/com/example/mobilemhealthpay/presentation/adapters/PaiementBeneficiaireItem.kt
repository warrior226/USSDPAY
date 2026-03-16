package com.example.mobilemhealthpay.presentation.adapters

import com.example.mobilemhealthpay.data.entity.BeneficiaireEntity

data class PaiementBeneficiaireItem(
    val beneficiaire: BeneficiaireEntity,
    val montant: Int,
    val datePaiement: Long, // if you want to reuseng
    val dateExcution:Long?,
    val status:String
)


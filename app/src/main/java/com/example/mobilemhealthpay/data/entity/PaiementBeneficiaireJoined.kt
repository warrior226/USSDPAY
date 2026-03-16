package com.example.mobilemhealthpay.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class PaiementBeneficiaireJoined(
    @Embedded
    val paiement: PaiementEntity,
    @Embedded
    val beneficiaire: BeneficiaireEntity,
    @ColumnInfo(name = "status")
    val status: String,
    @ColumnInfo(name = "executed_at")
    val executedAt: Long?

)

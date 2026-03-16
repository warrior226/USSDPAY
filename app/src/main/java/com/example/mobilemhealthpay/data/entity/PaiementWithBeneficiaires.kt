package com.example.mobilemhealthpay.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PaiementWithBeneficiaires(
    @Embedded val paiement: PaiementEntity,
    @Relation(
        parentColumn = "paiement_id",
        entityColumn = "id",
        associateBy = Junction(PaiementBeneficiaireCrossRef::class,
            parentColumn = "paiement_id",
            entityColumn = "beneficiaire_id")
    )
    val beneficiaires: List<BeneficiaireEntity>
)

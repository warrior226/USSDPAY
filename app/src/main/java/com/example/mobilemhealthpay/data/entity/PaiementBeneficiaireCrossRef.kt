package com.example.mobilemhealthpay.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.mobilemhealthpay.presentation.viewmodel.PaiementStatus


@Entity(
    tableName = "paiement_beneficiaire",
    primaryKeys =["paiement_id","beneficiaire_id"],
    foreignKeys = [
        ForeignKey(
            entity = PaiementEntity::class,
            parentColumns = ["paiement_id"],
            childColumns = ["paiement_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BeneficiaireEntity::class,
            parentColumns = ["id"],
            childColumns = ["beneficiaire_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PaiementBeneficiaireCrossRef(
    @ColumnInfo(name = "paiement_id")
    val paiementId: Int,
    @ColumnInfo(name = "beneficiaire_id")
    val beneficiaireId: Int,
    @ColumnInfo(name = "status")
    val status: String= PaiementStatus.EN_COURS.value,
    @ColumnInfo(name = "executed_at")
    val executedAt: Long? = null


)
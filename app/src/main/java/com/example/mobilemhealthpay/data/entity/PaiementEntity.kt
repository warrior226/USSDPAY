package com.example.mobilemhealthpay.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "paiements")
data class PaiementEntity(
    @PrimaryKey
    val paiement_id: Int,
    @ColumnInfo(name = "historique_paiement_id")
    val historiquePaiementId: Int,
    @ColumnInfo(name = "libelle")
    val libelle: String,
    @ColumnInfo(name = "montant")
    val montant: Int,
    @ColumnInfo(name = "recurrence")
    val recurrence: String,
    @ColumnInfo(name = "date_execution")
    val dateExecution: String,
    @ColumnInfo(name = "moyen_paiement_id")
    val moyenPaiementId: Int,
    @ColumnInfo(name = "montant_total")
    val montantTotal: Int,
    @ColumnInfo(name = "solde_disponible")
    val soldeDisponible: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

package com.example.mobilemhealthpay.data.remote.dto

data class PaiementDto(
    val paiement_id: Int,
    val historique_paiement_id: Int,
    val libelle: String,
    val montant: Int,
    val recurrence: String,
    val date_execution: String,
    val moyen_paiement_id: Int,
    val beneficiaires: List<BeneficiaireDto>,
    val montant_total: Int,
    val solde_disponible: Int
)
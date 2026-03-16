package com.example.mobilemhealthpay.data.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mobilemhealthpay.data.entity.PaiementBeneficiaireCrossRef
import com.example.mobilemhealthpay.data.entity.PaiementBeneficiaireJoined


@Dao
interface PaiementBeneficiaireDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaiementBeneficiaire(crossRef: PaiementBeneficiaireCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaiementBeneficiaires(crossRefs: List<PaiementBeneficiaireCrossRef>)

    @Delete
    suspend fun deletePaiementBeneficiaire(crossRef: PaiementBeneficiaireCrossRef)

    @Query("DELETE FROM paiement_beneficiaire WHERE paiement_id = :paiementId")
    suspend fun deleteBeneficiairesForPaiement(paiementId: Int)

    @Query("""
        UPDATE paiement_beneficiaire 
        SET status = :newStatus,executed_at= :date
        WHERE paiement_id = :paiementId AND beneficiaire_id = :beneficiaireId
    """)
    suspend fun updatePaiementStatusForBeneficiaire(
        paiementId: Int,
        beneficiaireId: Int,
        newStatus: String,
        date:Long
    )

    // Optional: Read status if needed
    @Query("""
        SELECT status FROM paiement_beneficiaire 
        WHERE paiement_id = :paiementId AND beneficiaire_id = :beneficiaireId
    """)
    suspend fun getStatusForPaiementBeneficiaire(
        paiementId: Int,
        beneficiaireId: Int
    ): String
}
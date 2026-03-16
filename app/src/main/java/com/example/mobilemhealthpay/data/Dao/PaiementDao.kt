package com.example.mobilemhealthpay.data.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.mobilemhealthpay.data.entity.PaiementBeneficiaireJoined
import com.example.mobilemhealthpay.data.entity.PaiementEntity
import com.example.mobilemhealthpay.data.entity.PaiementWithBeneficiaires

@Dao
interface PaiementDao {
    @Query("SELECT * FROM paiements")
    fun getAllPaiements(): LiveData<List<PaiementEntity>>

    @Query("SELECT * FROM paiements WHERE paiement_id = :id")
    suspend fun getPaiementById(id: Int): PaiementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaiement(paiement: PaiementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaiements(paiements: List<PaiementEntity>)

    @Delete
    suspend fun deletePaiement(paiement: PaiementEntity)

    @Query("DELETE FROM paiements")
    suspend fun deleteAllPaiements()

    @Transaction
    @Query("SELECT * FROM paiements")
    fun getPaiementsWithBeneficiaires(): LiveData<List<PaiementWithBeneficiaires>>

    @Transaction
    @Query("SELECT * FROM paiements WHERE paiement_id = :paiementId")
    suspend fun getPaiementWithBeneficiaires(paiementId: Int): PaiementWithBeneficiaires?

    @Query("""
        SELECT 
            p.*, 
            b.*, 
            pb.status AS status
        FROM paiement_beneficiaire pb
        INNER JOIN paiements p ON pb.paiement_id = p.paiement_id
        INNER JOIN beneficiaires b ON pb.beneficiaire_id = b.id
        WHERE pb.status = :status
    """)
    fun getPaiementsWithBeneficiairesByStatus(status: String): LiveData<List<PaiementBeneficiaireJoined>>


}
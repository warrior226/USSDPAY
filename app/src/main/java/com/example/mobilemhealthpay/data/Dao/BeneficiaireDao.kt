package com.example.mobilemhealthpay.data.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mobilemhealthpay.data.entity.BeneficiaireEntity


@Dao
interface BeneficiaireDao {
    @Query("SELECT * FROM beneficiaires")
    fun getAllBeneficiaires(): LiveData<List<BeneficiaireEntity>>

    @Query("SELECT * FROM beneficiaires WHERE id = :id")
    suspend fun getBeneficiaireById(id: Int): BeneficiaireEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBeneficiaire(beneficiaire: BeneficiaireEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBeneficiaires(beneficiaires: List<BeneficiaireEntity>)

    @Delete
    suspend fun deleteBeneficiaire(beneficiaire: BeneficiaireEntity) {
    }

    @Query("DELETE FROM beneficiaires")
    suspend fun deleteAllBeneficiaires()
}
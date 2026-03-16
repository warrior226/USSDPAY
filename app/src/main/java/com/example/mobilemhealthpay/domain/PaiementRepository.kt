package com.example.mobilemhealthpay.domain

import androidx.lifecycle.LiveData
import com.example.mobilemhealthpay.data.entity.BeneficiaireEntity
import com.example.mobilemhealthpay.data.entity.PaiementBeneficiaireJoined
import com.example.mobilemhealthpay.data.entity.PaiementEntity
import com.example.mobilemhealthpay.data.entity.PaiementWithBeneficiaires
import com.example.mobilemhealthpay.data.remote.dto.PaiementDto

interface PaiementRepository {
    suspend fun fetchAndStorePaiements(): Result<List<PaiementDto>>
    suspend fun getAllPaiements(): LiveData<List<PaiementEntity>>
    suspend fun getAllBeneficiaires(): LiveData<List<BeneficiaireEntity>>
    suspend fun getPaiementsWithBeneficiaires(): LiveData<List<PaiementWithBeneficiaires>>
    suspend fun getPaiementById(id: Int): PaiementEntity?
    suspend fun getBeneficiaireById(id: Int): BeneficiaireEntity?
    suspend fun getPaiementWithBeneficiaires(paiementId: Int): PaiementWithBeneficiaires?
    suspend fun getPaiementsWithBeneficiairesByStatus(status:String): LiveData<List<PaiementBeneficiaireJoined>>
}
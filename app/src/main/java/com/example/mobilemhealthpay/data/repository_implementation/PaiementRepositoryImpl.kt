package com.example.mobilemhealthpay.data.repository_implementation

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.mobilemhealthpay.data.Dao.BeneficiaireDao
import com.example.mobilemhealthpay.data.Dao.PaiementBeneficiaireDao
import com.example.mobilemhealthpay.data.Dao.PaiementDao
import com.example.mobilemhealthpay.data.entity.BeneficiaireEntity
import com.example.mobilemhealthpay.data.entity.PaiementBeneficiaireCrossRef
import com.example.mobilemhealthpay.data.entity.PaiementBeneficiaireJoined
import com.example.mobilemhealthpay.data.entity.PaiementEntity
import com.example.mobilemhealthpay.data.entity.PaiementWithBeneficiaires
import com.example.mobilemhealthpay.data.remote.Api_Service.PaiementApiService
import com.example.mobilemhealthpay.data.remote.dto.PaiementDto
import com.example.mobilemhealthpay.domain.PaiementRepository
import com.example.mobilemhealthpay.utils.Constant
import jakarta.inject.Inject

class PaiementRepositoryImpl @Inject constructor(
    private val apiService: PaiementApiService,
    private val paiementDao: PaiementDao,
    private val beneficiaireDao: BeneficiaireDao,
    private val paiementBeneficiaireDao: PaiementBeneficiaireDao
): PaiementRepository{
    override suspend fun fetchAndStorePaiements(): Result<List<PaiementDto>> {
        return try{
            val response=apiService.getPaiements(Constant.token,3)

            if (response.status == 1 && response.data != null) {
                // Store paiements and beneficiaires in database
                Log.d("TAG", "fetchAndStorePaiements:response is ${response} data are ${response.data} ")
                storePaiementsData(response.data)
                Result.success(response.data)
            } else {
                Log.d("TAG", "fetchAndStorePaiements: there are no response")
                Result.failure(Exception("API Error: ${response.message}"))

            }
        } catch (e: Exception) {
            Result.failure(e)
        }
        }

    private suspend fun storePaiementsData(paiements: List<PaiementDto>) {
        // Extract unique beneficiaires from all paiements
        val allBeneficiaires = paiements.flatMap { it.beneficiaires }.distinctBy { it.id }
        val beneficiaireEntities = allBeneficiaires.map { beneficiaire ->
            BeneficiaireEntity(
                id = beneficiaire.id,
                nomPrenom = beneficiaire.nom_prenom,
                telephone = beneficiaire.telephone,
                code = beneficiaire.code
            )
        }
        // Store beneficiaires first
        beneficiaireDao.insertBeneficiaires(beneficiaireEntities)

        // Convert and store paiements
        val paiementEntities = paiements.map { paiement ->
            PaiementEntity(
                paiement_id = paiement.paiement_id,
                historiquePaiementId = paiement.historique_paiement_id,
                libelle = paiement.libelle,
                montant = paiement.montant,
                recurrence = paiement.recurrence,
                dateExecution = paiement.date_execution,
                moyenPaiementId = paiement.moyen_paiement_id,
                montantTotal = paiement.montant_total,
                soldeDisponible = paiement.solde_disponible
            )
        }

        //Store paiements
        paiementDao.insertPaiements(paiementEntities)

        // Create and store relationships
        val crossRefs = paiements.flatMap { paiement ->
            paiement.beneficiaires.map { beneficiaire ->
                PaiementBeneficiaireCrossRef(
                    paiementId = paiement.paiement_id,
                    beneficiaireId = beneficiaire.id
                )
            }
        }

        paiementBeneficiaireDao.insertPaiementBeneficiaires(crossRefs)

    }



    override suspend fun getAllPaiements(): LiveData<List<PaiementEntity>>{
        return paiementDao.getAllPaiements()
    }

    override suspend fun getAllBeneficiaires(): LiveData<List<BeneficiaireEntity>> {
        return beneficiaireDao.getAllBeneficiaires()
    }

    override suspend fun getPaiementsWithBeneficiaires(): LiveData<List<PaiementWithBeneficiaires>> {
        return paiementDao.getPaiementsWithBeneficiaires()
    }

    override suspend fun getPaiementById(id: Int): PaiementEntity? {
        return paiementDao.getPaiementById(id)
    }

    override suspend fun getBeneficiaireById(id: Int): BeneficiaireEntity? {
        return beneficiaireDao.getBeneficiaireById(id)
    }

    override suspend fun getPaiementWithBeneficiaires(paiementId: Int): PaiementWithBeneficiaires? {
        return paiementDao.getPaiementWithBeneficiaires(paiementId)
    }

    override suspend fun getPaiementsWithBeneficiairesByStatus(status: String): LiveData<List<PaiementBeneficiaireJoined>> {
        return paiementDao.getPaiementsWithBeneficiairesByStatus(status)
    }
}

//Store paiement


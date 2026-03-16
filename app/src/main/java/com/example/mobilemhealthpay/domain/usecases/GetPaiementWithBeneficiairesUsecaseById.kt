package com.example.mobilemhealthpay.domain.usecases

import com.example.mobilemhealthpay.data.entity.PaiementWithBeneficiaires
import com.example.mobilemhealthpay.domain.PaiementRepository
import jakarta.inject.Inject

class GetPaiementWithBeneficiairesUsecaseById @Inject constructor(
    private val repository: PaiementRepository
) {
    suspend operator fun invoke(paiementId: Int): PaiementWithBeneficiaires? {
        return repository.getPaiementWithBeneficiaires(paiementId)
    }
}
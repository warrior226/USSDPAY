package com.example.mobilemhealthpay.domain.usecases

import androidx.lifecycle.LiveData
import com.example.mobilemhealthpay.data.entity.PaiementWithBeneficiaires
import com.example.mobilemhealthpay.domain.PaiementRepository
import jakarta.inject.Inject

class GetPaiementWithBeneficiairesUsecase @Inject constructor(
    private val repository: PaiementRepository
) {
    suspend operator fun invoke(): LiveData<List<PaiementWithBeneficiaires>>{
        return repository.getPaiementsWithBeneficiaires()
    }
}
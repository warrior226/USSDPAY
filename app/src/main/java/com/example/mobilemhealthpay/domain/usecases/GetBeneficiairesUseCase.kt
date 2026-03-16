package com.example.mobilemhealthpay.domain.usecases

import androidx.lifecycle.LiveData
import com.example.mobilemhealthpay.data.entity.BeneficiaireEntity
import com.example.mobilemhealthpay.domain.PaiementRepository

class GetBeneficiairesUseCase(
    private val repository: PaiementRepository
) {
    suspend operator fun invoke(): LiveData<List<BeneficiaireEntity>> {
        return repository.getAllBeneficiaires()
    }
}
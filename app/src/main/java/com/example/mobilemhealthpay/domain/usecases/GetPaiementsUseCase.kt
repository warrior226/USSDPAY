package com.example.mobilemhealthpay.domain.usecases

import androidx.lifecycle.LiveData
import com.example.mobilemhealthpay.data.entity.PaiementEntity
import com.example.mobilemhealthpay.domain.PaiementRepository
import jakarta.inject.Inject

class GetPaiementsUseCase @Inject constructor(
    private val repository: PaiementRepository
) {
    suspend operator fun invoke(): LiveData<List<PaiementEntity>> {
        return repository.getAllPaiements()
    }
}
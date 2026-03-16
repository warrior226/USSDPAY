package com.example.mobilemhealthpay.domain.usecases

import com.example.mobilemhealthpay.data.remote.dto.PaiementDto
import com.example.mobilemhealthpay.domain.PaiementRepository
import jakarta.inject.Inject

class FetchPaiementsUseCase@Inject constructor(
    private val repository: PaiementRepository
) {
    suspend operator fun invoke(): Result<List<PaiementDto>>{

        return repository.fetchAndStorePaiements()
    }
}
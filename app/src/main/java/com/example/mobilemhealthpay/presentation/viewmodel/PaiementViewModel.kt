package com.example.mobilemhealthpay.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilemhealthpay.data.AppDataBase
import com.example.mobilemhealthpay.data.entity.BeneficiaireEntity
import com.example.mobilemhealthpay.data.entity.PaiementEntity
import com.example.mobilemhealthpay.data.entity.PaiementWithBeneficiaires
import com.example.mobilemhealthpay.data.remote.dto.PaiementDto
import com.example.mobilemhealthpay.data.remote.dto.TransactionInfoDto
import com.example.mobilemhealthpay.domain.usecases.FetchPaiementsUseCase
import com.example.mobilemhealthpay.domain.usecases.GetBeneficiairesUseCase
import com.example.mobilemhealthpay.domain.usecases.GetPaiementWithBeneficiairesUsecase
import com.example.mobilemhealthpay.domain.usecases.GetPaiementWithBeneficiairesUsecaseById
import com.example.mobilemhealthpay.domain.usecases.GetPaiementsUseCase
import com.example.mobilemhealthpay.utils.NetworkConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.List

enum class PaiementStatus(val value: String) {
    EN_COURS("EN_COURS"),
    SUCCES("SUCCES"),
    ECHEC("ECHEC")
}

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
@HiltViewModel
class PaiementViewModel @Inject constructor(
    networkObserver: NetworkConnectivityObserver,
    private val fetchPaiementsUseCase: FetchPaiementsUseCase,
    private val getBeneficiairesUseCase: GetBeneficiairesUseCase,
    private val getPaiementsUseCase: GetPaiementsUseCase,
    private val getPaiementsWithBeneficiairesUsecase: GetPaiementWithBeneficiairesUsecase,
    private val getPaiementWithBeneficiairesUsecaseById: GetPaiementWithBeneficiairesUsecaseById
): ViewModel(){

   @Inject
   lateinit var appDataBase: AppDataBase
    val isConnected = networkObserver.networkStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _paiementsState = MutableLiveData<List<PaiementDto>>()
    val paiementsState: LiveData<List<PaiementDto>> = _paiementsState

    private val _storedPaiementsState = MutableLiveData<List<PaiementEntity>>()
    val storedPaiementsState: LiveData<List<PaiementEntity>> = _storedPaiementsState

    private val _beneficiairesState = MutableLiveData<List<BeneficiaireEntity>>()
    val beneficiairesState: LiveData<List<BeneficiaireEntity>> = _beneficiairesState

    private val _paiementsWithBeneficiairesState = MutableLiveData<List<PaiementWithBeneficiaires>>()
    val paiementsWithBeneficiairesState: LiveData<List<PaiementWithBeneficiaires>> = _paiementsWithBeneficiairesState

    private val _listTransactionEnCours= MutableLiveData<List<TransactionInfoDto>>()
    val listTransactionEncours: LiveData<List<TransactionInfoDto>> =_listTransactionEnCours

//    fun getListTransactonEnCours(){
//        viewModelScope.launch {
//            _listTransactionEnCours.value=appDataBase.transactionDao().
//        }
//    }

    fun fetchPaiements() {
        Log.d("TAG", "fetchPaiements: the function has been called ")
        viewModelScope.launch {
            _paiementsState.value = emptyList<PaiementDto>()

            fetchPaiementsUseCase().fold(
                onSuccess = { paiements ->
                    _paiementsState.value =paiements
                    // Load stored data after successful API call
                    loadStoredPaiements()
                    loadBeneficiaires()
                    loadPaiementsWithBeneficiaires()
                    Log.d("TAG", "fetchPaiements: the useCase is called ")
                },
                onFailure = { error ->
                    _paiementsState.value = emptyList<PaiementDto>()
                    Log.d("TAG", "fetchPaiements: the useCase is not called and the error is ${error.message}")
                }
            )
        }
    }

    fun loadStoredPaiements() {
        viewModelScope.launch {
            _storedPaiementsState.value =emptyList<PaiementEntity>()
            try {
                getPaiementsUseCase()
                _storedPaiementsState.value =appDataBase.paiementDao().getAllPaiements().value
            } catch (e: Exception) {
                _storedPaiementsState.value =emptyList<PaiementEntity>()
            }
        }
    }

    fun loadBeneficiaires() {
        viewModelScope.launch {
            _beneficiairesState.value =emptyList<BeneficiaireEntity>()
            try {
                getBeneficiairesUseCase()
                _beneficiairesState.value =appDataBase.beneficiaireDao().getAllBeneficiaires().value
            } catch (e: Exception) {
                _beneficiairesState.value =emptyList<BeneficiaireEntity>()
            }
        }
    }

    fun loadPaiementsWithBeneficiaires() {
        viewModelScope.launch {
            _paiementsWithBeneficiairesState.value =emptyList<PaiementWithBeneficiaires>()
            try {
                getPaiementsWithBeneficiairesUsecase()
                _paiementsWithBeneficiairesState.value = appDataBase.paiementDao().getPaiementsWithBeneficiaires().value
            } catch (e: Exception) {
                _paiementsWithBeneficiairesState.value = emptyList<PaiementWithBeneficiaires>()
                Log.d("TAG", "loadPaiementsWithBeneficiaires: error")
            }
        }
    }


}
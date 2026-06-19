package com.example.mobilemhealthpay.utils

import com.example.mobilemhealthpay.Resource
import com.example.mobilemhealthpay.data.entity.RefundInfoTable
import com.example.mobilemhealthpay.data.remote.dto.ApiResponse
import com.example.mobilemhealthpay.data.remote.dto.RefundCompleteDto
import com.example.mobilemhealthpay.data.remote.dto.VirementDto
import com.example.mobilemhealthpay.domain.usecases.TransactionUseCase
import timber.log.Timber
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedRepository @Inject constructor(
    private val transactionUseCase: TransactionUseCase
) {

    private val _refundInfo = MutableStateFlow<RefundInfoTable?>(null)
    val refundInfo: StateFlow<RefundInfoTable?> = _refundInfo.asStateFlow()

    // Flag to block new enqueuing when balance is insufficient
    private val _isFundsInsufficient = MutableStateFlow(false)
    val isFundsInsufficient: StateFlow<Boolean> = _isFundsInsufficient.asStateFlow()

    private val _currentBalance = MutableStateFlow<String?>(null)
    val currentBalance: StateFlow<String?> = _currentBalance.asStateFlow()

    // ----- Queue & Processor State -----

    private var refundQueue = Channel<RefundInfoTable>(Channel.UNLIMITED)
    private var currentDeferred: CompletableDeferred<UssdResult>? = null
    private val enqueuedIds = Collections.synchronizedSet(mutableSetOf<String>())
    private val _isQueueEmpty = MutableStateFlow(true)
    val isQueueEmpty: StateFlow<Boolean> = _isQueueEmpty.asStateFlow()
    private val isProcessing = AtomicBoolean(false)
    private val isPaused = AtomicBoolean(false)

    sealed class UssdResult {
        object Success : UssdResult()
        data class Failure(val reason: String) : UssdResult()
        object InsufficientFunds : UssdResult()
    }

    // ----- Public API -----

    suspend fun enqueueRefunds(refunds: List<RefundInfoTable>) {
        refunds.forEach { refund ->
            if (!_isFundsInsufficient.value && !enqueuedIds.contains(refund.refundId)) {
                enqueuedIds.add(refund.refundId)
                _isQueueEmpty.value = false
                refundQueue.send(refund)
            }
        }
    }

    fun signalOperationComplete(result: UssdResult) {
        currentDeferred?.complete(result)
    }

    suspend fun resumeAfterRecharge(pendingRefunds: List<RefundInfoTable>) {
        _isFundsInsufficient.value = false
        enqueuedIds.clear()
        _isQueueEmpty.value = true
        refundQueue = Channel(Channel.UNLIMITED)
        enqueueRefunds(pendingRefunds)
    }

    fun signalInsufficientFunds() {
        _isFundsInsufficient.value = true
    }

    fun setPaused(paused: Boolean) {
        isPaused.set(paused)
    }

    fun isPaused(): Boolean = isPaused.get()

    // ----- Core Sequential Processor -----

    suspend fun startProcessing(
        launchUssd: (RefundInfoTable) -> Unit
    ) {
        if (!isProcessing.compareAndSet(false, true)) return

        try {
            for (refund in refundQueue) {
                // Check pause state
                while (isPaused.get()) {
                    delay(1000)
                }

                _refundInfo.value = refund
                launchUssd(refund)

                currentDeferred = CompletableDeferred()
                val result = withTimeoutOrNull(TransactionConfig.USSD_TIMEOUT_MS) {
                    currentDeferred!!.await()
                }

                when (result) {
                    null -> {
                        handleFailure(refund, FailureReason.USSD_TIMEOUT)
                    }
                    is UssdResult.InsufficientFunds -> {
                        handleFailure(refund, FailureReason.INSUFFICIENT_FUNDS)
                        signalInsufficientFunds()
                        refundQueue.cancel()
                        refundQueue = Channel(Channel.UNLIMITED)
                        return // Stop processing
                    }
                    is UssdResult.Failure -> {
                        handleFailure(refund, result.reason)
                    }
                    is UssdResult.Success -> {
                        enqueuedIds.remove(refund.refundId)
                        _isQueueEmpty.value = enqueuedIds.isEmpty()
                        // Success -> mark as 1 in DB
                        val updated = refund.copy(status = 1)
                        registerRefund(updated)
                        // Call report API
                        reportTransactionStatus(updated, "success", "Opération réussie")
                    }
                }
                delay(TransactionConfig.BETWEEN_TX_DELAY_MS)
            }
        } finally {
            isProcessing.set(false)
        }
    }

    // ----- Private Helpers -----

    private suspend fun reportTransactionStatus(refund: RefundInfoTable, status: String, reason: String) {
        try {
            val reportFlow = if (refund.isRefund == 1) {
                if (status == "success") {
                    transactionUseCase.completeRefund(Global.secret_key, refund.refundId, reason)
                } else {
                    transactionUseCase.failRefund(Global.secret_key, refund.refundId, reason)
                }
            } else {
                transactionUseCase.reportTransactionAttempt(Global.secret_key, refund.refundId, status)
            }

            reportFlow.collect { resource ->
                if (resource is Resource.Success) {
                    val data = resource.data
                    if (data is ApiResponse<*>) {
                        if (data.status == 1) {
                            val responseData = data.data
                            val existingTransaction = transactionUseCase.getRefundById(refund.refundId)
                            existingTransaction?.let { it ->
                                val updated = when (responseData) {
                                    is VirementDto -> it.copy(
                                        refundStatus = responseData.status,
                                        providerTransactionId = responseData.paymentRef ?: it.providerTransactionId,
                                        lastAttemptAt = System.currentTimeMillis()
                                    )
                                    is RefundCompleteDto -> it.copy(
                                        refundStatus = responseData.refundStatus,
                                        lastAttemptAt = System.currentTimeMillis()
                                    )
                                    else -> it
                                }
                                registerRefund(updated)
                            }
                            Timber.d("reportTransactionStatus success: ${refund.refundId} -> $status")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to report transaction status on server")
        }
    }

    private suspend fun handleFailure(
        refund: RefundInfoTable,
        reason: String
    ) {
        val newAttemptCount = refund.attemptCount + 1
        val permanentlyFailed = newAttemptCount >= 3 // Maximum 3 attempts

        val updatedRefund = refund.copy(
            status        = if (permanentlyFailed) 2 else 0,
            attemptCount  = newAttemptCount,
            lastAttemptAt = System.currentTimeMillis()
        )

        registerRefund(updatedRefund)

        if (permanentlyFailed) {
            enqueuedIds.remove(refund.refundId)
            _isQueueEmpty.value = enqueuedIds.isEmpty()
            // Call report API on server when permanently failed after 3 attempts
            reportTransactionStatus(updatedRefund, "failed", reason)
        } else {
            // Wait 10 seconds before trying again
            delay(10000)
            refundQueue.send(updatedRefund)
        }
    }

    suspend fun registerRefund(refund: RefundInfoTable) {
        transactionUseCase.registerRefund(refund)
    }

    fun updateBalance(balance: String) {
        _currentBalance.value = balance
    }

    fun clearRefundInfo() {
        _refundInfo.value = null
    }
}

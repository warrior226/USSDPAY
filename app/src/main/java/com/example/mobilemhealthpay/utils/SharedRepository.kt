package com.example.mobilemhealthpay.utils

import com.example.mobilemhealthpay.data.entity.TransactionInfoTable
import com.example.mobilemhealthpay.domain.usecases.TransactionUseCase
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

    private val _transactionInfo = MutableStateFlow<TransactionInfoTable?>(null)
    val transactionInfo: StateFlow<TransactionInfoTable?> = _transactionInfo.asStateFlow()

    // Flag to block new enqueuing when balance is insufficient
    private val _isFundsInsufficient = MutableStateFlow(false)
    val isFundsInsufficient: StateFlow<Boolean> = _isFundsInsufficient.asStateFlow()

    private val _currentBalance = MutableStateFlow<String?>(null)
    val currentBalance: StateFlow<String?> = _currentBalance.asStateFlow()

    // ----- Queue & Processor State -----

    private var transactionQueue = Channel<TransactionInfoTable>(Channel.UNLIMITED)
    private var currentDeferred: CompletableDeferred<UssdResult>? = null
    private val enqueuedIds = Collections.synchronizedSet(mutableSetOf<String>())
    private val isProcessing = AtomicBoolean(false)
    private val isPaused = AtomicBoolean(false)

    sealed class UssdResult {
        object Success : UssdResult()
        data class Failure(val reason: String) : UssdResult()
        object InsufficientFunds : UssdResult()
    }

    // ----- Public API -----

    suspend fun enqueueTransactions(transactions: List<TransactionInfoTable>) {
        transactions.forEach { transaction ->
            if (!_isFundsInsufficient.value && !enqueuedIds.contains(transaction.transactionId)) {
                enqueuedIds.add(transaction.transactionId)
                transactionQueue.send(transaction)
            }
        }
    }

    fun signalOperationComplete(result: UssdResult) {
        currentDeferred?.complete(result)
    }

    suspend fun resumeAfterRecharge(pendingTransactions: List<TransactionInfoTable>) {
        _isFundsInsufficient.value = false
        enqueuedIds.clear()
        transactionQueue = Channel(Channel.UNLIMITED)
        enqueueTransactions(pendingTransactions)
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
        launchUssd: (String, Int) -> Unit
    ) {
        if (!isProcessing.compareAndSet(false, true)) return

        try {
            for (transaction in transactionQueue) {
                // Check pause state
                while (isPaused.get()) {
                    delay(1000)
                }

                _transactionInfo.value = transaction
                launchUssd(transaction.numero, transaction.montant)

                currentDeferred = CompletableDeferred()
                val result = withTimeoutOrNull(TransactionConfig.USSD_TIMEOUT_MS) {
                    currentDeferred!!.await()
                }

                when (result) {
                    null -> {
                        handleFailure(transaction, FailureReason.USSD_TIMEOUT)
                    }
                    is UssdResult.InsufficientFunds -> {
                        handleFailure(transaction, FailureReason.INSUFFICIENT_FUNDS)
                        signalInsufficientFunds()
                        transactionQueue.cancel()
                        transactionQueue = Channel(Channel.UNLIMITED)
                        return // Stop processing
                    }
                    is UssdResult.Failure -> {
                        handleFailure(transaction, result.reason)
                    }
                    is UssdResult.Success -> {
                        enqueuedIds.remove(transaction.transactionId)
                        // Success -> mark as 1 in DB
                        val updated = transaction.copy(status = 1, comment = "Success")
                        registerTransaction(updated)
                        // validate(updated, "Success") // Commenté à la demande de l'utilisateur
                    }
                }
                delay(TransactionConfig.BETWEEN_TX_DELAY_MS)
            }
        } finally {
            isProcessing.set(false)
        }
    }

    // ----- Private Helpers -----

    private suspend fun handleFailure(
        transaction: TransactionInfoTable,
        reason: String
    ) {
        val newRetryCount = transaction.retryCount + 1
        // User requirements: 3rd fail (retryCount=3) -> mark as failed
        val permanentlyFailed = newRetryCount >= TransactionConfig.MAX_RETRY_COUNT

        val updatedTransaction = transaction.copy(
            status        = if (permanentlyFailed) 2 else 0,
            retryCount    = newRetryCount,
            lastAttemptAt = System.currentTimeMillis(),
            comment       = reason
        )

        registerTransaction(updatedTransaction)

        if (permanentlyFailed) {
            enqueuedIds.remove(transaction.transactionId)
            // validate(updatedTransaction, reason) // Commenté à la demande de l'utilisateur
        } else {
            // User requirements: 1st fail (30s), 2nd fail (60s)
            val backoffDelay = TransactionConfig.BASE_RETRY_DELAY_MS * newRetryCount
            delay(backoffDelay)
            // Re-enqueue for next attempt
            transactionQueue.send(updatedTransaction)
        }
    }

    /* 
    // Méthode de validation API (Commentée à la demande de l'utilisateur)
    private suspend fun validate(transaction: TransactionInfoTable, message: String) {
        try {
            val hash = encryptWithHmacSha256(
                "${transaction.montant}${transaction.operateur}##${transaction.transactionId}",
                "--${transaction.transactionId}--"
            )
            transactionUseCase.validateTransaction(
                transaction.transactionId,
                message,
                hash,
                Global.token
            ).collect { }
        } catch (e: Exception) {
            // Timber.e(e, "Validation failed")
        }
    }

    private fun encryptWithHmacSha256(data: String, secret: String): String {
        val secretKeySpec = javax.crypto.spec.SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        val mac = javax.crypto.Mac.getInstance("HmacSHA256")
        mac.init(secretKeySpec)
        return java.util.Base64.getEncoder().encodeToString(mac.doFinal(data.toByteArray()))
    }
    */

    suspend fun registerTransaction(transaction: TransactionInfoTable) {
        transactionUseCase.registerTransaction(transaction)
    }

    fun updateBalance(balance: String) {
        _currentBalance.value = balance
    }

    fun clearTransactionInfo() {
        _transactionInfo.value = null
    }
}

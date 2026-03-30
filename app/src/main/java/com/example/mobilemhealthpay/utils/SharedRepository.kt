package com.example.mobilemhealthpay.utils

import com.example.mobilemhealthpay.data.Dao.TransactionInfoDao
import com.example.mobilemhealthpay.data.entity.TransactionInfoEntity
import com.example.mobilemhealthpay.data.entity.TransactionInfoTable
import com.example.mobilemhealthpay.domain.usecases.TransactionUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedRepository @Inject constructor(
    private val transactionUseCase: TransactionUseCase
) {

    private  val _transactionInfo= MutableStateFlow<TransactionInfoTable?>(null)
    val transactionInfo: StateFlow<TransactionInfoTable?> = _transactionInfo.asStateFlow()
    val operationState=MutableStateFlow(true)

    // Flag to block new enqueuing when balance is insufficient
    private val _isFundsInsufficient = MutableStateFlow(false)
    val isFundsInsufficient: StateFlow<Boolean> = _isFundsInsufficient.asStateFlow()

    // ----- Queue & Deferred -----

    // Unlimited channel holds all pending transactions in order
    private var transactionQueue = Channel<TransactionInfoTable>(Channel.UNLIMITED)

    // Suspension bridge: processor waits here until USSDService signals done
    private var currentDeferred: CompletableDeferred<UssdResult>? = null

    // ----- Result Sealed Class -----

    sealed class UssdResult {
        object Success : UssdResult()
        data class Failure(val reason: String) : UssdResult()
        object InsufficientFunds : UssdResult()
    }

    // Called by Fragment or PollingService to add transactions into the queue
    suspend fun enqueueTransactions(transactions: List<TransactionInfoTable>) {
        transactions.forEach { transaction ->
            // Never enqueue if funds are insufficient
            if (!_isFundsInsufficient.value) {
                transactionQueue.send(transaction)
            }
        }
    }

    // Called by USSDService after handling each USSD session
    fun signalOperationComplete(result: UssdResult) {
        currentDeferred?.complete(result)
    }

    // Called by Fragment's "Relancer" button after user recharges
    suspend fun resumeAfterRecharge(pendingTransactions: List<TransactionInfoTable>) {
        _isFundsInsufficient.value = false
        // Recreate the channel since the old one was cancelled
        transactionQueue = Channel(Channel.UNLIMITED)
        enqueueTransactions(pendingTransactions)
    }

    fun signalInsufficientFunds() {
        _isFundsInsufficient.value = true
    }

    // ----- Core Sequential Processor -----

    suspend fun startProcessing(
        onLaunchUssd: (String, Int) -> Unit,
        onValidate: (String, String, Int,String) -> Unit,
        onTransactionFailed: (TransactionInfoTable) -> Unit,
        onInsufficientFunds: (TransactionInfoTable) -> Unit
    ) {
        for (transaction in transactionQueue) {

            // 1. Expose current transaction so USSDService can read numero & montant
            _transactionInfo.value = transaction

            // 2. Fire USSD intent via Fragment
            onLaunchUssd(transaction.numero, transaction.montant)

            // 3. Suspend — wait for USSDService to signal, with a timeout
            currentDeferred = CompletableDeferred()
            val result = withTimeoutOrNull(TransactionConfig.USSD_TIMEOUT_MS) {
                currentDeferred!!.await()
            }

            // 4. Handle result
            when {
                result == null -> {
                    // Timeout — no USSD dialog appeared
                    handleFailure(transaction, FailureReason.USSD_TIMEOUT, onTransactionFailed)
                }

                result is UssdResult.InsufficientFunds -> {
                    // Mark current transaction as failed
                    handleFailure(transaction, FailureReason.INSUFFICIENT_FUNDS, onTransactionFailed)
                    // Block future enqueuing
                    signalInsufficientFunds()
                    // Cancel the queue — remaining transactions stay as status=0 in DB
                    transactionQueue.cancel()
                    // Notify Fragment to show alert
                    onInsufficientFunds(transaction)
                    return  // Exit processor entirely
                }

                result is UssdResult.Failure -> {
                    handleFailure(transaction, result.reason, onTransactionFailed)
                }

                result is UssdResult.Success -> {
                    // Confirm transaction on backend
                    onValidate(transaction.transactionId,transaction.comment ?: "",transaction.montant,transaction.operateur)
                }
            }

            // 5. Brief pause before next transaction
            delay(TransactionConfig.BETWEEN_TX_DELAY_MS)
        }
    }

    // ----- Failure Handler -----

    private suspend fun handleFailure(
        transaction: TransactionInfoTable,
        reason: String,
        onTransactionFailed: (TransactionInfoTable) -> Unit
    ) {
        val newRetryCount = transaction.retryCount + 1
        val permanentlyFailed = newRetryCount > TransactionConfig.MAX_RETRY_COUNT

        val updatedTransaction = transaction.copy(
            status        = if (permanentlyFailed) 2 else 0,
            retryCount    = newRetryCount,
            lastAttemptAt = System.currentTimeMillis(),
            comment       = reason  // comment holds the failure reason
        )

        if (permanentlyFailed) {
            // Mark as permanently failed → moves to "Echouées" tab
            registerTransaction(updatedTransaction)
            onTransactionFailed(updatedTransaction)
        } else {
            // Exponential backoff then re-enqueue
            val backoffDelay = TransactionConfig.BASE_RETRY_DELAY_MS * newRetryCount
            delay(backoffDelay)
            registerTransaction(updatedTransaction)
            transactionQueue.send(updatedTransaction)
        }
    }




//    fun updateTransactionInfo(transactionInfoEntity: TransactionInfoEntity){
//        _transactionInfo.value=transactionInfoEntity
//    }

    // ----- DB Delegate -----

    suspend fun registerTransaction(transactionInfoTable: TransactionInfoTable) {
        transactionUseCase.registerTransaction(transactionInfoTable)
    }


    fun clearTransactionInfo(){
        _transactionInfo.value=null
    }

    fun updateOperationState(state:Boolean){
        operationState.value=state
    }

}

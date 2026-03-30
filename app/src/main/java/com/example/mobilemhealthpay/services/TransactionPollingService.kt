package com.example.mobilemhealthpay.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.mobilemhealthpay.Resource
import com.example.mobilemhealthpay.data.AppDataBase
import com.example.mobilemhealthpay.data.Dao.TransactionInfoDao
import com.example.mobilemhealthpay.data.entity.TransactionInfoTable
import com.example.mobilemhealthpay.data.entity.TransactionResponseEntity
import com.example.mobilemhealthpay.domain.usecases.TransactionUseCase
import com.example.mobilemhealthpay.utils.Global
import com.example.mobilemhealthpay.utils.SharedRepository
import com.example.mobilemhealthpay.utils.TransactionConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TransactionPollingService : Service() {

    @Inject lateinit var repository: SharedRepository
    @Inject lateinit var transactionDao: TransactionInfoDao
    @Inject lateinit var transactionUseCase: TransactionUseCase

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            while (true) {
                fetchAndEnqueueNewTransactions()
                retryFailedTransactions()
                delay(TransactionConfig.POLL_INTERVAL_MS)
            }
        }
        return START_STICKY
    }

    private suspend fun fetchAndEnqueueNewTransactions() {
        try {
            transactionUseCase
                .getTransactionInformation(Global.NUMBER_OF_REQUEST, Global.token)
                .collect { resource ->
                    when (resource) {
                        is Resource.Progress    -> { /* ignore */ }
                        is Resource.HandleToken -> { /* ignore */ }

                        is Resource.Success -> {
                            resource.data.data.forEach { transactionInfo ->
                                val table = TransactionInfoTable(
                                    transactionId = transactionInfo.transaction_id,
                                    user_id       = transactionInfo.user_id,
                                    montant       = transactionInfo.montant,
                                    numero        = transactionInfo.numero,
                                    operateur     = transactionInfo.operateur,
                                    comment       = transactionInfo.comment,
                                    date_creation = transactionInfo.date_creation,
                                    status        = 0
                                )
                                repository.registerTransaction(table)
                                if (!repository.isFundsInsufficient.value) {
                                    repository.enqueueTransactions(listOf(table))
                                }
                            }
                        }

                        is Resource.Failure -> {
                            Log.e("TAG", "Fetch failed: ${resource.throwable.message}")
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("TAG", "Polling error: ${e.message}")
        }
    }

    private suspend fun retryFailedTransactions() {
        try {
            // One-shot query — returns immediately, no collect needed
            val failedTransactions = transactionDao.getTransactionEchoueFromService()
            val now = System.currentTimeMillis()

            failedTransactions.forEach { transaction ->
                val timeSinceLastAttempt = now - transaction.lastAttemptAt
                val canRetry = transaction.retryCount < TransactionConfig.MAX_AUTO_RETRY_COUNT

                if (timeSinceLastAttempt >= TransactionConfig.AUTO_RETRY_AFTER_MS && canRetry) {
                    val resetTransaction = transaction.copy(
                        status        = 0,
                        retryCount    = 0,
                        comment       = null,
                        lastAttemptAt = 0L
                    )
                    repository.registerTransaction(resetTransaction)
                    if (!repository.isFundsInsufficient.value) {
                        repository.enqueueTransactions(listOf(resetTransaction))
                    }
                } else if (!canRetry) {
                    Log.e("TAG", "Permanently failed: ${transaction.transactionId}")
                }
            }
        } catch (e: Exception) {
            Log.e("TAG", "Retry error: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
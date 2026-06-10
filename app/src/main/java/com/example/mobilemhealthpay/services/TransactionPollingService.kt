package com.example.mobilemhealthpay.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.IBinder
import timber.log.Timber
import androidx.core.content.ContextCompat
import com.example.mobilemhealthpay.Resource
import com.example.mobilemhealthpay.data.Dao.TransactionInfoDao
import com.example.mobilemhealthpay.data.entity.TransactionInfoTable
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
        // 1. Start sequential processing job
        serviceScope.launch {
            repository.startProcessing { numero, montant ->
                launchUssdCodeBackground(numero, montant)
            }
        }

        // 2. Start periodic fetch job (every 5 minutes)
        serviceScope.launch {
            while (true) {
                fetchAndEnqueueNewTransactions()
                // Periodic maintenance: retry long-failed ones if configured
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
                    if (resource is Resource.Success) {
                        resource.data.data.forEach { tx ->
                            val table = TransactionInfoTable(
                                transactionId = tx.transaction_id,
                                user_id       = tx.user_id,
                                montant       = tx.montant,
                                numero        = tx.numero,
                                operateur     = tx.operateur,
                                comment       = tx.comment,
                                date_creation = tx.date_creation,
                                status        = 0
                            )
                            repository.registerTransaction(table)
                            if (!repository.isFundsInsufficient.value) {
                                repository.enqueueTransactions(listOf(table))
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Timber.e(e, "Fetch error")
        }
    }

    private suspend fun retryFailedTransactions() {
        try {
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
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Retry error")
        }
    }

    private fun launchUssdCodeBackground(numero: String, montant: Int) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) return

        try {
            val ussdCode = "*144*2*$numero*$montant#"
            val encodedUssd = ussdCode.replace("#", Uri.encode("#"))
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$encodedUssd"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "USSD Error")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

package com.example.mobilemhealthpay.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.IBinder
import java.util.Calendar
import timber.log.Timber
import androidx.core.content.ContextCompat
import com.example.mobilemhealthpay.Resource
import com.example.mobilemhealthpay.data.AppDataBase
import com.example.mobilemhealthpay.data.entity.RefundInfoTable
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
    @Inject lateinit var appDataBase: AppDataBase
    @Inject lateinit var transactionUseCase: TransactionUseCase

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. Execution management (08:00 - 15:00)
        serviceScope.launch {
            while (true) {
                val executionActive = isExecutionPeriod()
                repository.setPaused(!executionActive)
                
                if (executionActive) {
                    repository.startProcessing { transaction ->
                        launchUssdCodeBackground(transaction)
                    }
                }
                delay(60000) // Check every minute
            }
        }

        // 2. Collection management (00:00 - 07:30, every hour)
        serviceScope.launch {
            while (true) {
                if (isCollectionPeriod()) {
                    fetchAndEnqueueNewTransactions()
                    delay(3600000) // Wait 1 hour for next fetch
                } else {
                    delay(600000) // Check every 10 mins outside window
                }
            }
        }
        
        return START_STICKY
    }

    private fun isCollectionPeriod(): Boolean {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        
        // 00:00 to 07:30
        return if (hour < 7) {
            true
        } else if (hour == 7) {
            minute <= 30
        } else {
            false
        }
    }

    private fun isExecutionPeriod(): Boolean {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        
        // 08:00 to 15:00 (Stops at 15h00)
        return hour in 8..14
    }

    private suspend fun fetchAndEnqueueNewTransactions() {
        // Fetch Refunds
        fetchTransactions { transactionUseCase.getRefunds(Global.secret_key, Global.TO_REFUND, Global.PAGE_SIZE) }
        
        // Fetch Virements
        fetchTransactions { transactionUseCase.getVirements(Global.secret_key, "pending", Global.PAGE_SIZE) }
    }

    private suspend fun fetchTransactions(call: () -> kotlinx.coroutines.flow.Flow<Resource<*>>) {
        try {
            call().collect { resource ->
                if (resource is Resource.Success) {
                    val data = resource.data
                    if (data is com.example.mobilemhealthpay.data.entity.RefundResponseEntity && data.status == 1) {
                        data.data.forEach { rf ->
                            val table = rf.toTable()
                            repository.registerRefund(table)
                            if (!repository.isFundsInsufficient.value) {
                                repository.enqueueRefunds(listOf(table))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Fetch error")
        }
    }

    private suspend fun retryFailedRefunds() {
        // Implementation for retrying failed refunds from DB if needed
        // This can be added if there's a specific requirement for auto-retry of failures
    }

    private fun launchUssdCodeBackground(transaction: RefundInfoTable) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) return

        try {
            val ussdCode = "*144*2*${transaction.phoneNumber}*${transaction.amount}#"
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

package com.example.mobilemhealthpay.services

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
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
    @Inject lateinit var webSocketManager: WebSocketManager
    @Inject lateinit var apiService: com.example.mobilemhealthpay.data.remote.Api_Service.PaiementApiService

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastFetchDayOfYear = -1

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. Socket.IO setup for real-time updates (Documentation 13.3)
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
        webSocketManager.connect(deviceId) {
            serviceScope.launch {
                Timber.d("Real-time trigger request_device_info received")
                sendDeviceStatus()
            }
        }

        // 2. Periodic status update via HTTP POST (Documentation 13.5)
        serviceScope.launch {
            while (true) {
                sendDeviceStatus()
                delay(3600000) // Every 1 hour
            }
        }

        // 3. Execution management (08:00 - 15:00)
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

        // 4. Collection management (Triggers at 07:30, once per day)
        serviceScope.launch {
            while (true) {
                val now = Calendar.getInstance()
                val currentDay = now.get(Calendar.DAY_OF_YEAR)
                
                if (isCollectionTime() && lastFetchDayOfYear != currentDay) {
                    fetchAndEnqueueNewTransactions()
                    lastFetchDayOfYear = currentDay
                    Timber.d("Daily collection fetch completed for day: $lastFetchDayOfYear")
                }
                
                delay(60000) // Check every minute
            }
        }
        
        return START_STICKY
    }

    private fun sendDeviceStatus() {
        serviceScope.launch {
            try {
                val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
                val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
                val solde = repository.currentBalance.value ?: null
                
                // ISO 8601 format per documentation 13.5
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                val currentDate = sdf.format(Date())
                
                val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
                    registerReceiver(null, ifilter)
                }
                val batteryPct: Float? = batteryStatus?.let { intent ->
                    val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level != -1 && scale != -1) level * 100 / scale.toFloat() else null
                }
                val battery = "${batteryPct?.toInt() ?: 0}%"
                
                val status = if (isExecutionPeriod()) 1 else 0

                val deviceStatus = WebSocketManager.DeviceStatus(
                    device_id = deviceId,
                    device_name = deviceName,
                    solde = solde,
                    date = currentDate,
                    battery = battery,
                    status = status
                )

                // Post info via HTTP as required by documentation 13.5
                apiService.postDeviceInfo(Global.secret_key, deviceStatus)
                Timber.d("Device status posted successfully via HTTP")
            } catch (e: Exception) {
                Timber.e(e, "Error sending device status via POST")
            }
        }
    }

    private fun isCollectionTime(): Boolean {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        
        // Trigger as soon as it's 07:30 or later in the 7th hour
        return hour == 7 && minute >= 30
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
        webSocketManager.disconnect()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

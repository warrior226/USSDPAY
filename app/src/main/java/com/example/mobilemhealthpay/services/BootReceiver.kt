package com.example.mobilemhealthpay.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, TransactionPollingService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
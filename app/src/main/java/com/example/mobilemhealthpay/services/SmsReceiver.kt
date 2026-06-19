package com.example.mobilemhealthpay.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.mobilemhealthpay.utils.SharedRepository
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: SharedRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isEmpty()) return

            val sender = messages[0].displayOriginatingAddress
            Timber.d("SMS Received from: $sender")

            // Only process if the sender is "OrangeMoney"
            if (sender?.contains("OrangeMoney", ignoreCase = true) == true) {
                val fullMessage = messages.joinToString("") { it.displayMessageBody ?: "" }
                Timber.d("SMS Content: $fullMessage")

                if (fullMessage.contains("solde", ignoreCase = true)) {
                    val regex = """(\d+[\d,.]*\s*F?CFA)""".toRegex(RegexOption.IGNORE_CASE)
                    regex.find(fullMessage)?.let { 
                        val balance = it.value.trim()
                        Timber.d("Extracted Balance from OrangeMoney SMS: $balance")
                        repository.updateBalance(balance)
                    }
                }
            }
        }
    }
}

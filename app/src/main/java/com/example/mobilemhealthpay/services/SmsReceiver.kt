package com.example.mobilemhealthpay.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
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

            val fullMessage = messages.joinToString("") { it.displayMessageBody ?: "" }
            Timber.d("SMS Content: $fullMessage")

            val isMobileMoneySms = sender?.contains("Orange", ignoreCase = true) == true ||
                    sender?.contains("OM", ignoreCase = true) == true ||
                    sender?.contains("144") == true ||
                    fullMessage.contains("solde", ignoreCase = true) ||
                    fullMessage.contains("recharge", ignoreCase = true) ||
                    fullMessage.contains("depot", ignoreCase = true) ||
                    fullMessage.contains("credite", ignoreCase = true) ||
                    fullMessage.contains("recu", ignoreCase = true)

            if (isMobileMoneySms) {
                // Check if message contains explicit balance
                val regex = """(?:solde(?:\s+actuel|\s+disponible)?\s*:?\s*|est de\s*)?(\d+[\d\s.,]*\s*F?CFA)""".toRegex(RegexOption.IGNORE_CASE)
                val match = regex.find(fullMessage)
                if (match != null) {
                    val balance = match.groupValues.getOrNull(1)?.trim() ?: match.value.trim()
                    Timber.d("Extracted Balance from SMS: $balance")
                    repository.updateBalance(balance)
                }

                // If message indicates a deposit, top-up, or recharge, trigger automatic USSD balance query to refresh balance
                if (fullMessage.contains("recharge", ignoreCase = true) ||
                    fullMessage.contains("depot", ignoreCase = true) ||
                    fullMessage.contains("credite", ignoreCase = true) ||
                    fullMessage.contains("recu", ignoreCase = true) ||
                    fullMessage.contains("solde", ignoreCase = true)
                ) {
                    triggerUssdBalanceCheck(context)
                }
            }
        }
    }

    private fun triggerUssdBalanceCheck(context: Context) {
        try {
            val ussdCode = "*144*9*1#"
            val encodedUssd = ussdCode.replace("#", Uri.encode("#"))
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$encodedUssd")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Timber.d("Triggered auto USSD balance check after money deposit/recharge")
        } catch (e: Exception) {
            Timber.e(e, "Failed to trigger auto USSD balance check: ${e.message}")
        }
    }
}

package com.example.mobilemhealthpay.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Constant {

    const val DATABASE_NAME = "mHealthPayDb"
    const val NUMBER = "3304"
    const val token="Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6ImJub3VyZGluZUB0aWNhbmFseXNlLm9yZyIsIm5vbSI6IkJBTUJBUkEiLCJwcmVub20iOiJNYXJ0aWFsIiwidGVsZXBob25lIjoiNjA3MTQ2MDUiLCJvcmdhbmlzYXRpb25faWQiOjEsImV4cCI6MzY0MzEwOTMxM30.ZlhfLMw2c6yhJqrdBzA4tQg6sXUSIIFsrk982xGTwPc"

    //const val NUMBER = "1367"
    const val REQUEST_PERMISSION_CODE = 200
    const val SHARED_PREF_NAME = "mHealth_com"

    val MONTHCODE = mapOf(
        "01" to "1","02" to "2","03" to "3","04" to "4","05" to "5","06" to "6","07" to "7","08" to "8","09" to "9",
        "10" to "A","11" to "B","12" to "C"
    )

    public fun convertDateIntoString(date:Long):String{
        val date= Date(date)
        val formater= SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val formatedDate=formater.format(date)
        return formatedDate
    }


}

object FailureReason {
    const val USSD_TIMEOUT        = "USSD_TIMEOUT"
    const val USSD_REJECTED       = "USSD_REJECTED"
    const val WRONG_RESPONSE      = "WRONG_RESPONSE"
    const val NETWORK_ERROR       = "NETWORK_ERROR"
    const val MAX_RETRIES_REACHED = "MAX_RETRIES_REACHED"
    const val INSUFFICIENT_FUNDS  = "INSUFFICIENT_FUNDS"
}

object TransactionConfig {
    const val MAX_RETRY_COUNT      = 3
    const val MAX_AUTO_RETRY_COUNT = 3
    const val BASE_RETRY_DELAY_MS  = 30_000L   // 30 seconds
    const val USSD_TIMEOUT_MS      = 30_000L   // 30 seconds to wait for USSD dialog
    const val BETWEEN_TX_DELAY_MS  = 2_000L    // 2 seconds between transactions
    const val POLL_INTERVAL_MS     = 5 * 60 * 1000L  // 5 minutes
    const val AUTO_RETRY_AFTER_MS  = 15 * 60 * 1000L // retry failed ones after 15 minutes
}
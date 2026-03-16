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
package com.example.mobilemhealthpay.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


class Global {

    companion object {
//        private lateinit var circular: AlertDialog
//        const val CRYPTKEY = "0303030303030303"
        const val MESS_ENCRYPTIONSEC = "%4fis7OqF8Ee9hI9Xc#eOHInV@1f@4V0"
//        var aesEncryption = AESEncryption(MESS_ENCRYPTIONSEC)

        const val NOTIFICATION_CHANNEL_ID = "lagfo_retrait_channel"
        const val NOTIFICATION_CHANNEL_NAME = "lagfo_retrait"
        const val NOTIFICATION_ID = 1
        const val REQUEST_PERMISSION_CODE = 200
        const val Bearer="Bearer "
        const val PAGE_SIZE = 100
        const val number1 = "+22676600150"
        const val secret_key="Q6rMfr7cRNhVaUY88xoEq7gIP_BgLC2iOgoTGcgdBe4"
        const val NUMBER_OF_REQUEST=5
        const val token ="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2xhZ2ZvLmNvbSIsImlhdCI6MTcwNzM5OTgzMywibW9kIjoiYWNjZXNzVG9rZW4iLCJ0eXAiOiJBUFBfUkVUUkFJVCIsImV4cCI6MTgwNzQwMDEzM30.sxG4ivauDjOclwRu3zhGovUMfNhGDjGY__VMutGVjJY"
        /**
         * Fallback password. 
         * For production, use SecurityManager to save the real password securely.
         */
        const val TRANSACTION_PASSWORD="2011"
        const val SIM_SLOT=0
        const val TO_REFUND = "to_refund"
        fun checkPermission(context: Context, permissions: ArrayList<String>): Boolean {
            for (p in permissions) {
                if (ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED) return false
            }
            return true
        }

        val newPermissions = arrayListOf(
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CALL_PHONE
        )

    }


}
package com.example.mobilemhealthpay.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityManager @Inject constructor(@ApplicationContext context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_TRANSACTION_PASSWORD = "transaction_password"
    }

    fun saveTransactionPassword(password: String) {
        sharedPreferences.edit().putString(KEY_TRANSACTION_PASSWORD, password).apply()
    }

    fun getTransactionPassword(): String {
        return sharedPreferences.getString(KEY_TRANSACTION_PASSWORD, Global.TRANSACTION_PASSWORD) ?: Global.TRANSACTION_PASSWORD
    }
}

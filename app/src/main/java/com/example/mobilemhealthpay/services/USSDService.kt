package com.example.mobilemhealthpay.services

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.mobilemhealthpay.utils.FailureReason
import com.example.mobilemhealthpay.utils.SecurityManager
import com.example.mobilemhealthpay.utils.SharedRepository
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class USSDService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    
    @Inject
    lateinit var repository: SharedRepository

    @Inject
    lateinit var securityManager: SecurityManager

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            if (it.className == "android.app.AlertDialog" || it.packageName == "com.android.phone") {
                Timber.d("onAccessibilityEvent: USSD dialog detected!")
                handleUSSDResponse(it)
            }
        }
    }

    private fun handleUSSDResponse(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow ?: return
        val ussdText = extractUSSDText(rootNode)
        Timber.d("USSD Content: $ussdText")

        val response = determineResponse(ussdText)

        if (response != null) {
            // If there's a next step (menu option or password)
            handler.postDelayed({
                sendUSSDResponse(rootNode, response)
            }, 2000) 
        } else {
            // End of flow or terminal error
            dismissDialog(rootNode)
        }
    }

    private fun determineResponse(ussdText: String): String? {
        // 1. Check for insufficient funds
        if (ussdText.contains("insuffisant", ignoreCase = true)) {
            repository.updateBalance("Solde insuffisant.Veuillez recharger votre compte.")
            repository.signalOperationComplete(SharedRepository.UssdResult.InsufficientFunds)
            return null
        }

        // 2. Check for general failure
        if (ussdText.contains("Echec", ignoreCase = true) || ussdText.contains("Erreur", ignoreCase = true)) {
            repository.signalOperationComplete(SharedRepository.UssdResult.Failure(FailureReason.USSD_REJECTED))
            return null
        }

        // 3. Check for success
        if (ussdText.contains("TRANS ID", ignoreCase = true) || ussdText.contains("transfere", ignoreCase = true)) {
            repository.signalOperationComplete(SharedRepository.UssdResult.Success)
            return null
        }

        // 4. Extract balance if present
        if (ussdText.contains("solde", ignoreCase = true)) {
            // Updated regex to capture decimals and FCFA
            val regex = """(\d+[\d,.]*\s*F?CFA)""".toRegex(RegexOption.IGNORE_CASE)
            regex.find(ussdText)?.let { 
                val balance = it.value.trim()
                repository.updateBalance(balance)
            }
        }

        // 5. Navigation logic (Examples - to be adjusted to your specific provider menu)
        return when {
            ussdText.contains("Saisir le code PIN", ignoreCase = true) -> securityManager.getTransactionPassword()
            // Add other menu steps here (e.g., "1. Confirmer" -> return "1")
            else -> null 
        }
    }

    private fun extractUSSDText(node: AccessibilityNodeInfo): String {
        val sb = StringBuilder()
        fun traverse(n: AccessibilityNodeInfo) {
            n.text?.let { sb.append(it).append("\n") }
            for (i in 0 until n.childCount) {
                n.getChild(i)?.let { traverse(it) }
            }
        }
        traverse(node)
        return sb.toString()
    }

    private fun sendUSSDResponse(node: AccessibilityNodeInfo, response: String) {
        findEditText(node)?.let { editText ->
            val arguments = Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, response)
            editText.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            
            handler.postDelayed({
                findAndClickButton(node, "Send", "OK", "Envoyer", "Confirmer")
            }, 5000)
        }
    }

    private fun findEditText(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.className == "android.widget.EditText") return node
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let {
                val result = findEditText(it)
                if (result != null) return result
            }
        }
        return null
    }

    private fun findAndClickButton(node: AccessibilityNodeInfo, vararg texts: String) {
        fun search(n: AccessibilityNodeInfo): Boolean {
            if (n.className == "android.widget.Button") {
                val btnText = n.text?.toString()?.lowercase() ?: ""
                if (texts.any { btnText.contains(it.lowercase()) }) {
                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return true
                }
            }
            for (i in 0 until n.childCount) {
                n.getChild(i)?.let { if (search(it)) return true }
            }
            return false
        }
        search(node)
    }

    private fun dismissDialog(node: AccessibilityNodeInfo) {
        findAndClickButton(node, "Cancel", "Close", "Annuler", "Fermer", "OK")
        // Signal success only if we didn't signal failure/insufficient funds earlier
        repository.signalOperationComplete(SharedRepository.UssdResult.Success)
    }

    override fun onInterrupt() {}
}

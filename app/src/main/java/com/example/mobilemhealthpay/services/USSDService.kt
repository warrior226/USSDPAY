package com.example.mobilemhealthpay.services

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.mobilemhealthpay.data.entity.TransactionInfoTable
import com.example.mobilemhealthpay.utils.Global
import com.example.mobilemhealthpay.utils.SharedRepository
import com.example.mobilemhealthpay.viewmodel.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class USSDService : AccessibilityService() {

    private var ussdRunning = false
    private val handler = Handler(Looper.getMainLooper())
    @Inject
    lateinit var repository: SharedRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    override fun onCreate() {
        super.onCreate()
        Log.d("TAG", "onCreate: USSDService created")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("TAG", "onServiceConnected: Service is now connected and active!")
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d("TAG", "onAccessibilityEvent: Event received!")

        event?.let {
            Log.d("TAG", "onAccessibilityEvent: EventType = ${it.eventType}")
            Log.d("TAG", "onAccessibilityEvent: ClassName = ${it.className}")
            Log.d("TAG", "onAccessibilityEvent: PackageName = ${it.packageName}")
            Log.d("TAG", "onAccessibilityEvent: Text = ${it.text}")

            //Mise à jour du status et validation de la transaction
            if(it.text.contains("")){
                serviceScope.launch {
                    repository.transactionInfo.collect { transaction ->
                        if(transaction!=null){
                            transaction.copy(comment = it.text.toString(), status = 1)
                            val transactionTable = TransactionInfoTable(
                                id = null, // Let Room auto-generate
                                user_id = transaction.user_id,
                                montant = transaction.montant,
                                transactionId = transaction.transaction_id,
                                numero = transaction.numero,
                                operateur = transaction.operateur,
                                comment = transaction.comment, // Update with USSD response
                                date_creation = transaction.date_creation,
                                status = 1 // Success status
                            )
                            //Save transaction
                           repository.registerTransaction(transactionTable)
                            //L'operation s'est bien passée, la prochaine transaction peut etre lancée
                            repository.updateOperationState(true)
                        }

                    }

                }

            }


            if (it.className == "android.app.AlertDialog" ||
                it.packageName == "com.android.phone") {

                Log.d("TAG", "onAccessibilityEvent: USSD dialog detected!")
                handleUSSDResponse(it)
            } else {
                Log.d("TAG", "onAccessibilityEvent: Not a USSD dialog")
            }
        } ?: run {
            Log.d("TAG", "onAccessibilityEvent: Event is null")
        }
    }
    private fun handleUSSDResponse(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow ?: return

        // Extract USSD text
        val ussdText = extractUSSDText(rootNode)
        Log.d("TAG", "handleUSSDResponse: $ussdText ")
        // Determine next action based on USSD text
        val response = determineResponse(ussdText)

        if (response != null) {
            // Send response with delay
            handler.postDelayed({
                sendUSSDResponse(rootNode, response)
            }, 500) // Adjust delay as needed
        } else {
            // End of flow - dismiss dialog
            dismissDialog(rootNode)
        }
    }

    private fun extractUSSDText(node: AccessibilityNodeInfo): String {
        val textBuilder = StringBuilder()

        fun traverse(n: AccessibilityNodeInfo) {
            if (n.text != null) {
                textBuilder.append(n.text).append("\n")
            }
            for (i in 0 until n.childCount) {
                n.getChild(i)?.let { traverse(it) }
            }
        }

        traverse(node)
        return textBuilder.toString()
    }

    private fun sendUSSDResponse(node: AccessibilityNodeInfo, response: String) {
        // Find EditText
        val editText = findEditText(node)

        editText?.let {
            val arguments = Bundle()
            arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                response
            )
            it.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)

            // Find and click Send/OK button
            handler.postDelayed({
                findAndClickButton(node, "Send", "OK", "Envoyer")
            }, 300)

        }
    }

    private fun findEditText(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.className == "android.widget.EditText") {
            return node
        }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let {
                val result = findEditText(it)
                if (result != null) return result
            }
        }
        return null
    }

    private fun findAndClickButton(node: AccessibilityNodeInfo, vararg buttonTexts: String) {
        fun search(n: AccessibilityNodeInfo): Boolean {
            if (n.className == "android.widget.Button") {
                val text = n.text?.toString()?.lowercase()
                if (buttonTexts.any { text?.contains(it.lowercase()) == true }) {
                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return true
                }
            }

            for (i in 0 until n.childCount) {
                n.getChild(i)?.let {
                    if (search(it)) {
                        return true
                    }
                }
            }
            return false
        }

        search(node)
    }

    private fun dismissDialog(node: AccessibilityNodeInfo) {
        // Find Cancel/Close button
        findAndClickButton(node, "Cancel", "Close", "Annuler", "Fermer","OK")
        ussdRunning = false
    }

    private fun determineResponse(ussdText: String): String? {
        // Implement your logic here based on the USSD menu
        // Return the option number/text to send, or null if done

        return when {
            ussdText.contains("2.Transfert d'argent") -> "2"
            ussdText.contains("1.National") -> "1"
            ussdText.contains("Saisir le numero beneficiaire") -> "1"
            ussdText.contains("Saisir le montant du transfert") -> "50"
            ussdText.contains("") -> Global.TRANSACTION_PASSWORD
            else -> null // End flow
        }
    }

    override fun onInterrupt() {}
}
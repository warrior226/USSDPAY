package com.example.mobilemhealthpay

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobilemhealthpay.databinding.ActivityMainBinding
import com.example.mobilemhealthpay.presentation.viewmodel.PaiementViewModel
import com.example.mobilemhealthpay.presentation.viewmodel.UiState
import com.example.mobilemhealthpay.services.TransactionPollingService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: PaiementViewModel by viewModels()

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
    }

    // Required permissions
    private val requiredPermissions = arrayOf(
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_PHONE_STATE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        startPollingService()
        // Setup connectivity observer
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isConnected.collect { isConnected ->
                    if (isConnected) {
                        //Log.d("TAG", "onCreate: you are connected")
                    } else {
                       // Log.d("TAG", "onCreate: you are not connected")
                    }
                }
            }
        }

        // Observe ViewModel
        // oberveViewModel()

        // Check permissions first, then launch USSD
        checkAndRequestPermissions()
    }

    private fun oberveViewModel() {
        viewModel.paiementsState.observe(this) { state ->
            if (state.isNotEmpty()) {
               // Log.d("TAG", "oberveViewModel: paiement are not empty")
            }
        }
    }

    private fun showLoader(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun checkAndRequestPermissions() {
        // Find which permissions are not granted
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            // Some permissions are missing, request them
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            // All permissions granted
            onPermissionsGranted()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

                if (allGranted) {
                    Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
                    onPermissionsGranted()
                } else {
                    // Some permissions denied
                    Toast.makeText(
                        this,
                        "CALL_PHONE permission is required to use USSD codes",
                        Toast.LENGTH_LONG
                    ).show()

                    // Show dialog to guide user to settings
                    showPermissionDeniedDialog()
                }
            }
        }
    }

    private fun onPermissionsGranted() {
        // All permissions granted, proceed with app functionality

        // Check accessibility service
        checkAccessibilityPermission()

        // Check overlay permission if needed
        checkOverlayPermission()

        // Fetch paiements
      //  viewModel.fetchPaiements()

        // Launch USSD code
        //launchUssdCode()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app needs CALL_PHONE permission to dial USSD codes. Please grant it in Settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                finish() // Close app if permission is critical
            }
            .setCancelable(false)
            .show()
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                AlertDialog.Builder(this)
                    .setTitle("Overlay Permission")
                    .setMessage("Enable overlay permission for better USSD automation experience.")
                    .setPositiveButton("Enable") { _, _ ->
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        startActivity(intent)
                    }
                    .setNegativeButton("Skip") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    private fun checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            AlertDialog.Builder(this)
                .setTitle("Accessibility Service")
                .setMessage("To automate USSD responses, please enable 'USSDService' in Accessibility Settings.")
                .setPositiveButton("Open Settings") { _, _ ->
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                    Toast.makeText(
                        this,
                        "Find and enable 'USSDService' or 'MobileMhealthPay'",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .setNegativeButton("Later") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val serviceName = "$packageName/.services.USSDService"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(serviceName) == true
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun launchUssdCode() {
        // Check permission one more time before dialing
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Call permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val ussdCode = "*400#"
            val encodedUssd = ussdCode.replace("#", Uri.encode("#"))
            val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$encodedUssd"))
            startActivity(callIntent)

        //    Log.d("TAG", "launchUssdCode: USSD code dialed successfully")

        } catch (e: SecurityException) {
          //  Log.e("TAG", "launchUssdCode: Security Exception", e)
            Toast.makeText(
                this,
                "Permission error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
           // Log.e("TAG", "launchUssdCode: Error", e)
            Toast.makeText(
                this,
                "Error dialing USSD: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Optional: Add a manual trigger button
    fun onDialUSSDClick(view: View) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED) {
            launchUssdCode()
        } else {
            Toast.makeText(this, "Please grant CALL_PHONE permission first", Toast.LENGTH_SHORT).show()
            checkAndRequestPermissions()
        }
    }

    private fun startPollingService() {
        val intent = Intent(this, TransactionPollingService::class.java)
        startForegroundService(intent)  // Android 8+
    }
}



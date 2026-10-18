package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.ui.RemoteScreen
import com.example.ui.RemoteViewModel
import com.example.ui.theme.SmartIRRemoteTheme

class MainActivity : ComponentActivity() {

    private val viewModel: RemoteViewModel by viewModels()

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        // If any Bluetooth permission denied, show guidance dialog
        // (fixes audit finding #15: was silent "unavailable" before)
        if (grants.values.any { !it }) {
            viewModel.onBluetoothPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestBluetoothPermissionsIfNeeded()
        enableEdgeToEdge()
        setContent {
            SmartIRRemoteTheme {
                RemoteScreen(viewModel = viewModel)
            }
        }
    }

    private fun requestBluetoothPermissionsIfNeeded() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        val missing = permissions.filter {
            checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            bluetoothPermissionLauncher.launch(missing.toTypedArray())
        }
    }
}

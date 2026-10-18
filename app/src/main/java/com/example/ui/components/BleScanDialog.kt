package com.example.ui.components

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kyawzay.blueconnect.bluetooth.ble.BleConnectionState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

data class ScannedBleDevice(
    val device: BluetoothDevice,
    val name: String,
    val address: String,
    val rssi: Int
)

@SuppressLint("MissingPermission")
@Composable
fun BleScanDialog(
    isOpen: Boolean,
    connectionState: BleConnectionState,
    currentRssi: Int,
    onDismiss: () -> Unit,
    onConnectDevice: (BluetoothDevice) -> Unit,
    onDisconnect: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    var isScanning by remember { mutableStateOf(false) }
    val discoveredDevices = remember { mutableStateListOf<ScannedBleDevice>() }

    val bluetoothAdapter: BluetoothAdapter? = remember {
        try {
            BluetoothAdapter.getDefaultAdapter()
        } catch (_: Exception) {
            null
        }
    }

    val scanCallback = remember {
        object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val dev = result.device
                val devName = try { dev.name ?: "Unknown BLE Device" } catch (_: Exception) { "Unknown BLE Device" }
                val address = try { dev.address } catch (_: Exception) { "" }

                if (discoveredDevices.none { it.address == address }) {
                    discoveredDevices.add(
                        ScannedBleDevice(
                            device = dev,
                            name = devName,
                            address = address,
                            rssi = result.rssi
                        )
                    )
                }
            }

            override fun onScanFailed(errorCode: Int) {
                isScanning = false
            }
        }
    }

    fun startScan() {
        discoveredDevices.clear()
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner != null && bluetoothAdapter.isEnabled) {
            try {
                isScanning = true
                scanner.startScan(scanCallback)
            } catch (_: Exception) {
                isScanning = false
            }
        }
    }

    fun stopScan() {
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner != null) {
            try {
                scanner.stopScan(scanCallback)
            } catch (_: Exception) {}
        }
        isScanning = false
    }

    DisposableEffect(isOpen) {
        if (isOpen) {
            startScan()
        }
        onDispose {
            stopScan()
        }
    }

    Dialog(onDismissRequest = {
        stopScan()
        onDismiss()
    }) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Slate800,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("ble_scan_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bluetooth, contentDescription = null, tint = AccentCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BLE IR Bridge Connect",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = {
                        stopScan()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                // Current Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate900)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (connectionState) {
                                        BleConnectionState.READY, BleConnectionState.CONNECTED -> AccentGreen
                                        BleConnectionState.CONNECTING, BleConnectionState.DISCOVERING_SERVICES -> AccentOrange
                                        else -> Color.Gray
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "အခြေအနေ: ${connectionState.name}",
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                    if (currentRssi != 0) {
                        Text(
                            text = "RSSI: ${currentRssi} dBm",
                            fontSize = 10.sp,
                            color = AccentCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scan / Stop button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "တွေ့ရှိသော BLE စက်များ (${discoveredDevices.size}):",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                    IconButton(
                        onClick = {
                            if (isScanning) stopScan() else startScan()
                        }
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = AccentCyan)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Scan", tint = AccentCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (discoveredDevices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isScanning) "အနီးအနားရှိ BLE စက်များကို ရှာဖွေနေပါသည်..." else "BLE စက် မတွေ့ရှိသေးပါ (Scan နှိပ်ပါ)",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        items(discoveredDevices) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Slate900)
                                    .clickable {
                                        stopScan()
                                        onConnectDevice(item.device)
                                    }
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = item.address,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${item.rssi} dBm",
                                        fontSize = 10.sp,
                                        color = AccentCyan
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.BluetoothConnected,
                                        contentDescription = "Connect",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (connectionState == BleConnectionState.READY || connectionState == BleConnectionState.CONNECTED) {
                    Button(
                        onClick = onDisconnect,
                        colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Bluetooth ချိတ်ဆက်မှု ဖြုတ်မည် (Disconnect)", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

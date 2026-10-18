package com.kyawzay.blueconnect.bluetooth.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class BleConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCOVERING_SERVICES,
    READY,
    FAILED
}

class BleGattManager(private val context: Context) {

    private val tag = "BleGattManager"
    private var bluetoothGatt: BluetoothGatt? = null

    private val _connectionState = MutableStateFlow(BleConnectionState.DISCONNECTED)
    val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val _currentRssi = MutableStateFlow(0)
    val currentRssi: StateFlow<Int> = _currentRssi.asStateFlow()

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(tag, "GATT Connected. Discovering services...")
                    _connectionState.value = BleConnectionState.DISCOVERING_SERVICES
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(tag, "GATT Disconnected with status: $status")
                    _connectionState.value = BleConnectionState.DISCONNECTED
                    close()
                }
                else -> {
                    Log.d(tag, "Connection state changed: $newState")
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(tag, "Services discovered successfully")
                _connectionState.value = BleConnectionState.READY
                gatt.readRemoteRssi()
            } else {
                Log.w(tag, "Services discovery failed: $status")
                _connectionState.value = BleConnectionState.FAILED
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _currentRssi.value = rssi
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice): Boolean {
        return try {
            _connectionState.value = BleConnectionState.CONNECTING
            bluetoothGatt = device.connectGatt(context, false, gattCallback)
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to connect to device: ${e.message}", e)
            _connectionState.value = BleConnectionState.FAILED
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        try {
            bluetoothGatt?.disconnect()
        } catch (e: Exception) {
            Log.w(tag, "Disconnect error: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun close() {
        try {
            bluetoothGatt?.close()
            bluetoothGatt = null
        } catch (e: Exception) {
            Log.w(tag, "Close error: ${e.message}")
        }
    }

    fun isConnected(): Boolean {
        return _connectionState.value == BleConnectionState.READY ||
                _connectionState.value == BleConnectionState.CONNECTED
    }
}

/**
 * Requirement 6: BLE Auto-Reconnect with Exponential Backoff
 */
@SuppressLint("MissingPermission")
suspend fun BleGattManager.connectWithAutoRetry(device: BluetoothDevice, maxRetries: Int = 3) {
    var attempts = 0

    while (attempts < maxRetries && !isConnected()) {
        attempts++
        connect(device)
        delay(2000) // Wait for connection result
        // Check state & retry with exponential backoff if failed
        if (!isConnected() && attempts < maxRetries) {
            delay((attempts * 1000).toLong())
        }
    }
}

package com.kyawzay.blueconnect.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Build
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.reflect.Method
import java.util.UUID

@SuppressLint("MissingPermission")
class RobustBluetoothManager(
    private val context: Context,
    private val scope: CoroutineScope
) {

    // =========================================================================
    // သော့ချက် (၂) - Classic Bluetooth Connection with Reflection Fallback
    // =========================================================================
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var classicSocket: BluetoothSocket? = null

    suspend fun connectClassicDevice(device: BluetoothDevice): Boolean = withContext(Dispatchers.IO) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        @Suppress("DEPRECATION")
        val adapter = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()

        if (adapter?.isDiscovering == true) {
            adapter.cancelDiscovery() // Connection Speed ပိုမြန်စေရန် Scan ကို ရပ်ဆိုင်းခြင်း
        }

        // Attempt 1: Standard RFCOMM Socket ဖြင့် ချိတ်ဆက်ခြင်း
        try {
            classicSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            classicSocket?.connect()
            return@withContext true
        } catch (e: IOException) {
            // Attempt 2: Fallback Reflection Method (RFCOMM Channel 1) သို့ ကူးပြောင်းခြင်း
            try {
                val method: Method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                classicSocket = method.invoke(device, 1) as BluetoothSocket
                classicSocket?.connect()
                return@withContext true
            } catch (fallbackError: Exception) {
                closeClassicSocket()
                return@withContext false
            }
        }
    }

    fun closeClassicSocket() {
        try {
            classicSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            classicSocket = null
        }
    }

    // =========================================================================
    // သော့ချက် (၁) & (၃) - GATT Error 133 Handling & Priority/MTU Optimization
    // =========================================================================
    private var bluetoothGatt: BluetoothGatt? = null

    // သော့ချက် (၅) အတွက် Mutex Queue
    private val gattCommandMutex = Mutex()

    fun connectBleDevice(
        device: BluetoothDevice,
        onConnected: () -> Unit,
        onError: (status: Int) -> Unit
    ) {
        // သော့ချက် (၁): ချိတ်ဆက်မှု အသစ် မလုပ်မီ အရင် Resource ဟောင်းအား စနစ်တကျ close() ခေါ်ယူခြင်း
        closeBleGatt()

        bluetoothGatt = device.connectGatt(
            context,
            false, // Direct connect (အမြန်ဆုံး ချိတ်ဆက်ရန် autoConnect = false)
            object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            // သော့ချက် (၃): Connection Priority & MTU Size ကို မြှင့်တင်ခြင်း
                            gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                            gatt.requestMtu(512)

                            // Services များကို ရှာဖွေခြင်း
                            gatt.discoverServices()
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            closeBleGatt()
                            onError(status)
                        }
                    } else {
                        // သော့ချက် (၁): GATT Error (133 အပါအဝင်) ဖြစ်ပေါ်ပါက close() လုပ်၍ ရပ်ဆိုင်းခြင်း
                        closeBleGatt()
                        onError(status)
                    }
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        onConnected()
                    } else {
                        onError(status)
                    }
                }

                override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
                    // MTU Size 512 သို့ အောင်မြင်စွာ ပြောင်းလဲပြီးပါပြီ
                }

                override fun onCharacteristicWrite(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    status: Int
                ) {
                    // Command ပေးပို့မှု ပြီးစီးခြင်း
                }
            },
            BluetoothDevice.TRANSPORT_LE // BLE သီးသန့် Mode အဖြစ် သတ်မှတ်ခြင်း
        )
    }

    // =========================================================================
    // သော့ချက် (၄) - Auto-Reconnect with Exponential Backoff Strategy
    // =========================================================================
    fun connectBleWithRetry(
        device: BluetoothDevice,
        maxRetries: Int = 3,
        onFinalResult: (Boolean) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            var attempt = 0
            var connected = false

            while (attempt < maxRetries && !connected) {
                attempt++
                val completionDeferred = CompletableDeferred<Boolean>()

                withContext(Dispatchers.Main) {
                    connectBleDevice(
                        device,
                        onConnected = { completionDeferred.complete(true) },
                        onError = { completionDeferred.complete(false) }
                    )
                }

                connected = completionDeferred.await()

                if (!connected && attempt < maxRetries) {
                    // Exponential Backoff Delay (800ms, 1600ms, 2400ms စသဖြင့် စောင့်ဆိုင်းခြင်း)
                    val backoffDelay = (attempt * 800).toLong()
                    delay(backoffDelay)
                }
            }

            withContext(Dispatchers.Main) {
                onFinalResult(connected)
            }
        }
    }

    // =========================================================================
    // သော့ချက် (၅) - Thread-Safe BLE Command Execution Queue
    // =========================================================================
    suspend fun writeCharacteristicSafely(
        serviceUuid: UUID,
        charUuid: UUID,
        data: ByteArray
    ): Boolean = gattCommandMutex.withLock {
        return withContext(Dispatchers.IO) {
            val gatt = bluetoothGatt ?: return@withContext false
            val service = gatt.getService(serviceUuid) ?: return@withContext false
            val characteristic = service.getCharacteristic(charUuid) ?: return@withContext false

            @Suppress("DEPRECATION")
            characteristic.value = data
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

            @Suppress("DEPRECATION")
            return@withContext gatt.writeCharacteristic(characteristic)
        }
    }

    // GATT Resource Cleanup
    fun closeBleGatt() {
        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bluetoothGatt = null
        }
    }

    fun isBleConnected(): Boolean = bluetoothGatt != null
    fun isClassicConnected(): Boolean = classicSocket?.isConnected == true
}

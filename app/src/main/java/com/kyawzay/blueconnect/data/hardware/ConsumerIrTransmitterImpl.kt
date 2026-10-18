package com.kyawzay.blueconnect.data.hardware

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Build
import android.util.Log
import com.kyawzay.blueconnect.bluetooth.ir.NecProtocolEncoder
import com.kyawzay.blueconnect.bluetooth.ir.ProntoHexConverter

class ConsumerIrTransmitterImpl(private val context: Context) {

    private val tag = "ConsumerIrTransmitter"

    private val irManager: ConsumerIrManager? = try {
        context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
    } catch (e: Exception) {
        Log.w(tag, "ConsumerIrManager not available: ${e.message}")
        null
    }

    fun isIrEmitterSupported(): Boolean {
        return irManager?.hasIrEmitter() == true
    }

    fun isFrequencySupported(frequency: Int = 38000): Boolean {
        val manager = irManager ?: return false
        if (!manager.hasIrEmitter()) return false
        return try {
            val ranges = manager.carrierFrequencies ?: return true
            ranges.any { frequency in it.minFrequency..it.maxFrequency }
        } catch (e: Exception) {
            true // default to true if query fails
        }
    }

    fun isXiaomiOrRedmiDevice(): Boolean {
        val m = Build.MANUFACTURER
        val b = Build.BRAND
        val model = Build.MODEL
        return m.contains("Xiaomi", ignoreCase = true) ||
                b.contains("Redmi", ignoreCase = true) ||
                model.contains("Redmi", ignoreCase = true)
    }

    fun transmitNecHex(hexCode: String): Boolean {
        val manager = irManager ?: run {
            Log.i(tag, "Simulation: transmitNecHex($hexCode)")
            return false
        }
        if (!manager.hasIrEmitter()) {
            Log.i(tag, "Simulation (No hardware emitter): transmitNecHex($hexCode)")
            return false
        }

        return try {
            val pattern = NecProtocolEncoder.encodeHexToNecPattern(hexCode)
            val frequency = NecProtocolEncoder.getCarrierFrequency()
            manager.transmit(frequency, pattern)
            Log.i(tag, "NEC 38kHz transmitted successfully: $hexCode (pulses: ${pattern.size})")
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to transmit NEC hex: ${e.message}", e)
            false
        }
    }

    fun transmitProntoHex(prontoHex: String): Boolean {
        val manager = irManager ?: run {
            Log.i(tag, "Simulation: transmitProntoHex($prontoHex)")
            return false
        }
        if (!manager.hasIrEmitter()) {
            Log.i(tag, "Simulation (No hardware emitter): transmitProntoHex")
            return false
        }

        return ProntoHexConverter.transmitProntoHex(manager, prontoHex)
    }

    fun transmitRawPattern(frequency: Int, pattern: IntArray): Boolean {
        val manager = irManager ?: return false
        if (!manager.hasIrEmitter()) return false
        return try {
            manager.transmit(frequency, pattern)
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to transmit raw pattern: ${e.message}", e)
            false
        }
    }
}

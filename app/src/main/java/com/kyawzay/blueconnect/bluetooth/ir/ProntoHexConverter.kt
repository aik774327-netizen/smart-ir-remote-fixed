package com.kyawzay.blueconnect.bluetooth.ir

import android.hardware.ConsumerIrManager
import android.util.Log

object ProntoHexConverter {

    private const val TAG = "ProntoHexConverter"
    private const val DEFAULT_FREQUENCY = 38_000
    private const val PRONTO_RAW = 0x0000

    /** Transmits a raw Pronto 0000 sequence through Android's IR manager. */
    fun transmitProntoHex(irManager: ConsumerIrManager, prontoHex: String): Boolean {
        return try {
            val parts = prontoHex.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
            if (parts.size < 4) return invalid("Pronto Hex requires at least four words")

            val preamble = parts[0].parseWord()
            if (preamble != PRONTO_RAW) {
                return invalid("Only raw Pronto format 0000 is supported; got ${parts[0]}")
            }

            val frequencyCode = parts[1].parseWord()
            val oncePairs = parts[2].parseWord()
            val repeatPairs = parts[3].parseWord()
            val requiredWords = 4L + 2L * (oncePairs + repeatPairs)
            if (requiredWords > parts.size) {
                return invalid("Pronto sequence is truncated: expected $requiredWords words, got ${parts.size}")
            }

            val frequency = if (frequencyCode == 0) {
                DEFAULT_FREQUENCY
            } else {
                (1_000_000.0 / (frequencyCode * 0.241246)).toInt()
            }
            require(frequency in 20_000..100_000) { "Unsupported carrier frequency: $frequency Hz" }

            val pulseCount = (oncePairs + repeatPairs) * 2
            val pattern = IntArray(pulseCount)
            val microsecondsPerCycle = 1_000_000.0 / frequency
            for (i in 0 until pulseCount) {
                val cycles = parts[4 + i].parseWord()
                pattern[i] = (cycles * microsecondsPerCycle).toInt().coerceAtLeast(1)
            }

            irManager.transmit(frequency, pattern)
            Log.d(TAG, "Transmitted Pronto: $frequency Hz, ${pattern.size} pulses")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse/transmit Pronto Hex: ${e.message}", e)
            false
        }
    }

    private fun String.parseWord(): Int = toInt(16).also { require(it >= 0) }

    private fun invalid(message: String): Boolean {
        Log.e(TAG, message)
        return false
    }
}

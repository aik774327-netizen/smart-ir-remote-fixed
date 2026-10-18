package com.example.data.preset

import com.example.domain.model.IrCommandType
import com.example.domain.model.IrSignalPattern
import com.kyawzay.blueconnect.bluetooth.ir.NecProtocolEncoder as BlueConnectEncoder

/**
 * NEC Infrared Protocol Timing Encoder
 * Standard 38kHz Carrier Frequency
 *
 * UNIFIED: Delegates raw pattern generation to
 * com.kyawzay.blueconnect.bluetooth.ir.NecProtocolEncoder to eliminate
 * logic duplication. This wrapper adds the IrSignalPattern domain object
 * with isAiSynthesized tracking.
 */
object NecProtocolEncoder {

    const val FREQUENCY_NEC = 38000

    fun encodeNec32(address: Int, command: Int, commandType: IrCommandType, hexCode: String = ""): IrSignalPattern {
        val addr = address and 0xFF
        val addrInv = (addr.inv()) and 0xFF
        val cmd = command and 0xFF
        val cmdInv = (cmd.inv()) and 0xFF

        // 32 bits: Address (8), Address Inverse (8), Command (8), Command Inverse (8)
        val data32 = (addr) or (addrInv shl 8) or (cmd shl 16) or (cmdInv shl 24)

        return encode32BitWord(data32, commandType, hexCode)
    }

    fun encode32BitWord(word: Int, commandType: IrCommandType, hexCode: String = "", isAiSynthesized: Boolean = false): IrSignalPattern {
        // Delegate to unified blueconnect encoder (single source of truth for NEC timing)
        val hexStr = Integer.toHexString(word)
        val pattern = BlueConnectEncoder.encodeHexToNecPattern(hexStr)

        val formattedHex = if (hexCode.isNotBlank()) hexCode else "0x" + hexStr.uppercase()

        return IrSignalPattern(
            commandType = commandType,
            frequencyHz = FREQUENCY_NEC,
            patternMicroseconds = pattern,
            hexCode = formattedHex,
            protocolName = "NEC",
            isAiSynthesized = isAiSynthesized
        )
    }

    /**
     * Parses raw timing string or Pronto hex or returns default NEC pattern
     * WARNING: Codes from this function are AI-synthesized or user-provided (unverified).
     */
    fun parseCustomHex(hexString: String, commandType: IrCommandType): IrSignalPattern {
        val cleanHex = hexString.trim().removePrefix("0x").removePrefix("0X")
        val parsedWord = cleanHex.toLongOrNull(16)?.toInt() ?: 0x00FFE01F
        return encode32BitWord(parsedWord, commandType, "0x$cleanHex", isAiSynthesized = true)
    }
}

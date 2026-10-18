package com.kyawzay.blueconnect.bluetooth.ir

object NecProtocolEncoder {

    private const val CARRIER_FREQUENCY = 38_000
    private const val HEADER_MARK = 9_000
    private const val HEADER_SPACE = 4_500
    private const val BIT_MARK = 560
    private const val ONE_SPACE = 1_690
    private const val ZERO_SPACE = 560
    private const val STOP_BIT = 560

    /**
     * Encodes a 32-bit NEC word. NEC transmits the complete word LSB-first.
     * The caller must provide a protocol-correct 32-bit word; manufacturer
     * variants (Samsung, LG, extended NEC, etc.) should be verified separately.
     */
    fun encodeHexToNecPattern(hexString: String): IntArray {
        val cleanHex = hexString
            .trim()
            .removePrefix("0x")
            .removePrefix("0X")
            .replace(" ", "")
        require(cleanHex.isNotEmpty() && cleanHex.length <= 8) {
            "NEC hex must contain 1 to 8 hexadecimal digits"
        }
        val value = cleanHex.toLongOrNull(16)
            ?: throw IllegalArgumentException("Invalid NEC hex: $hexString")

        val pattern = IntArray(67)
        var index = 0
        pattern[index++] = HEADER_MARK
        pattern[index++] = HEADER_SPACE

        for (bitIndex in 0 until 32) {
            val bit = (value ushr bitIndex) and 1L
            pattern[index++] = BIT_MARK
            pattern[index++] = if (bit == 1L) ONE_SPACE else ZERO_SPACE
        }
        pattern[index] = STOP_BIT
        return pattern
    }

    fun getCarrierFrequency(): Int = CARRIER_FREQUENCY
}

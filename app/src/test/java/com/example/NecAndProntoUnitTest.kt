package com.example

import com.kyawzay.blueconnect.bluetooth.ir.NecProtocolEncoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NecAndProntoUnitTest {

    @Test
    fun testNecEncoderCarrierFrequency() {
        val freq = NecProtocolEncoder.getCarrierFrequency()
        assertEquals(38000, freq)
    }

    @Test
    fun testNecEncoderPulsesCount() {
        val pattern = NecProtocolEncoder.encodeHexToNecPattern("0x00FF00FF")
        assertNotNull(pattern)
        // 1 leader mark (9000) + 1 leader space (4500) + 32 bits * 2 pulses (64) + 1 stop bit (560) = 67 pulses
        assertEquals(67, pattern.size)
        assertEquals(9000, pattern[0])
        assertEquals(4500, pattern[1])
        assertEquals(560, pattern[pattern.size - 1])
    }

    @Test
    fun testNecEncoderCleanHexWithoutPrefix() {
        val pattern = NecProtocolEncoder.encodeHexToNecPattern("00FE01FE")
        assertEquals(67, pattern.size)
    }
}

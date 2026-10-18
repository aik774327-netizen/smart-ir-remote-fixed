package com.example

import com.example.data.preset.NecProtocolEncoder
import com.example.domain.model.IrCommandType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NecProtocolEncoderTest {

    @Test
    fun testNecEncodingPulseLength() {
        val signal = NecProtocolEncoder.encodeNec32(0x00, 0xFF, IrCommandType.POWER)
        assertNotNull(signal)
        assertEquals(38000, signal.frequencyHz)
        // NEC 32-bit: 2 pulses for leader + 32*2 pulses for data + 1 pulse for stop = 67 pulses
        assertEquals(67, signal.patternMicroseconds.size)
        // Leader mark is 9000us, leader space is 4500us
        assertEquals(9000, signal.patternMicroseconds[0])
        assertEquals(4500, signal.patternMicroseconds[1])
    }

    @Test
    fun testParseCustomHex() {
        val signal = NecProtocolEncoder.parseCustomHex("0x00FF00FF", IrCommandType.POWER)
        assertEquals(67, signal.patternMicroseconds.size)
        assertEquals("0x00FF00FF", signal.hexCode)
    }
}

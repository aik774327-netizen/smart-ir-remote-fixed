package com.example

import com.kyawzay.blueconnect.bluetooth.RobustBluetoothManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class RobustBluetoothManagerTest {

    @Test
    fun testInitializationAndResourceCleanup() {
        val context = RuntimeEnvironment.getApplication()
        val manager = RobustBluetoothManager(context, CoroutineScope(Dispatchers.Main))

        assertNotNull(manager)
        assertFalse(manager.isBleConnected())
        assertFalse(manager.isClassicConnected())

        manager.closeBleGatt()
        manager.closeClassicSocket()

        assertFalse(manager.isBleConnected())
        assertFalse(manager.isClassicConnected())
    }
}

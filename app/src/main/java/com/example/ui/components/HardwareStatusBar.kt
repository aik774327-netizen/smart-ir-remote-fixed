package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.HardwareState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800

@Composable
fun HardwareStatusBar(
    hardwareState: HardwareState,
    isHapticEnabled: Boolean,
    isSoundEnabled: Boolean,
    lastFiredCommand: String?,
    lastFiredHex: String?,
    onToggleHaptic: () -> Unit,
    onToggleSound: () -> Unit,
    onOpenAi: () -> Unit,
    onOpenHex: () -> Unit,
    onOpenBle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hardware_status_bar"),
        color = Slate800,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Hardware Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (hardwareState.hasIrEmitter) AccentGreen else AccentOrange)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (hardwareState.hasIrEmitter) "38kHz IR Blaster (Ready)" else "IR Blaster (Simulation)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = if (hardwareState.isRedmiOrXiaomi) "Redmi Note 12 Pro (HyperOS)" else "Standard Android IR",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentCyan.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Action buttons: AI, Manual Hex, Haptic, Sound
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenBle,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Slate700)
                            .testTag("open_ble_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bluetooth,
                            contentDescription = "BLE Connection",
                            tint = AccentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onOpenAi,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Slate700)
                            .testTag("open_ai_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini AI Assistant",
                            tint = AccentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onOpenHex,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Slate700)
                            .testTag("open_hex_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Manual Hex",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onToggleHaptic,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isHapticEnabled) AccentCyan.copy(alpha = 0.2f) else Slate700)
                            .testTag("toggle_haptic_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Haptic Toggle",
                            tint = if (isHapticEnabled) AccentCyan else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onToggleSound,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSoundEnabled) AccentCyan.copy(alpha = 0.2f) else Slate700)
                            .testTag("toggle_sound_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Sound Toggle",
                            tint = if (isSoundEnabled) AccentCyan else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Live Transmitter Monitor
            if (lastFiredCommand != null || lastFiredHex != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "နောက်ဆုံးလွှင့်ထုတ်: ${lastFiredCommand ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                    Text(
                        text = lastFiredHex ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

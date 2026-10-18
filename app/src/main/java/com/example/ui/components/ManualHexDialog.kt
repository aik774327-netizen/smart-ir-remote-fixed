package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.preset.NecProtocolEncoder
import com.example.domain.model.IrCommandType
import com.example.domain.model.IrSignalPattern
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

enum class HexInputMode {
    NEC_32BIT,
    PRONTO_HEX
}

@Composable
fun ManualHexDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onFireSignal: (IrSignalPattern) -> Unit,
    onFireProntoHex: (String) -> Unit
) {
    if (!isOpen) return

    var inputMode by remember { mutableStateOf(HexInputMode.NEC_32BIT) }
    var necHexInput by remember { mutableStateOf("0x00FF00FF") }
    var prontoHexInput by remember {
        mutableStateOf("0000 006D 0022 0002 0157 00AC 0015 0016 0015 0041 0015 0016 0015 0016 0015 0016")
    }
    var hexError by remember { mutableStateOf<String?>(null) }

    /**
     * Validates NEC 32-bit hex format: 0x followed by 1-8 hex digits,
     * or bare 1-8 hex digits.
     */
    fun isValidNecHex(input: String): Boolean {
        val clean = input.trim().removePrefix("0x").removePrefix("0X")
        if (clean.isEmpty() || clean.length > 8) return false
        return clean.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
    }

    /**
     * Validates Pronto hex format: at least 4 space-separated 4-digit hex words,
     * first word must be 0000.
     */
    fun isValidProntoHex(input: String): Boolean {
        val parts = input.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (parts.size < 4) return false
        if (!parts[0].equals("0000", ignoreCase = true)) return false
        return parts.all { it.length == 4 && it.all { c -> c in '0'..'9' || c in 'a'..'f' || c in 'A'..'F' } }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Slate800,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("manual_hex_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = AccentCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Custom IR Hex Tester",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Text(
                    text = "32-bit NEC Hex သို့မဟုတ် Pronto Raw Hex ကုဒ်များကို 38kHz IR လှိုင်းဖြင့် တိုက်ရိုက် လွှင့်ထုတ် စမ်းသပ်နိုင်ပါသည်",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // WARNING: Manually entered codes are unverified
                Text(
                    text = "⚠️ ကိုယ်တိုင်ရိုက်ထည့်သော code များသည် အတည်မပြုရသေးပါ — device တုံ့ပြန်မှု မရှိပါက code မှားနေနိုင်ပါသည်",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mode Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = inputMode == HexInputMode.NEC_32BIT,
                        onClick = { inputMode = HexInputMode.NEC_32BIT },
                        label = { Text("NEC 32-bit Hex") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentCyan.copy(alpha = 0.25f),
                            selectedLabelColor = AccentCyan,
                            containerColor = Slate900,
                            labelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = inputMode == HexInputMode.PRONTO_HEX,
                        onClick = { inputMode = HexInputMode.PRONTO_HEX },
                        label = { Text("Pronto Hex") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentCyan.copy(alpha = 0.25f),
                            selectedLabelColor = AccentCyan,
                            containerColor = Slate900,
                            labelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (inputMode == HexInputMode.NEC_32BIT) {
                    OutlinedTextField(
                        value = necHexInput,
                        onValueChange = {
                            necHexInput = it
                            hexError = null
                        },
                        label = { Text("NEC 32-bit Hex Code") },
                        placeholder = { Text("0x00FF00FF သို့မဟုတ် 0xE0E040BF") },
                        singleLine = true,
                        isError = hexError != null,
                        supportingText = hexError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_custom_hex")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (!isValidNecHex(necHexInput)) {
                                hexError = "Hex format မမှန်ပါ — 0x + hex digit 1-8 လုံး (ဥပမာ: 0x00FF00FF)"
                                return@Button
                            }
                            hexError = null
                            val signal = NecProtocolEncoder.parseCustomHex(necHexInput, IrCommandType.POWER)
                            onFireSignal(signal)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_fire_custom_hex"),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                    ) {
                        Icon(Icons.Default.Sensors, contentDescription = null, tint = Slate900)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("38kHz NEC ဖြင့် လွှင့်ထုတ်မည်", color = Slate900, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedTextField(
                        value = prontoHexInput,
                        onValueChange = {
                            prontoHexInput = it
                            hexError = null
                        },
                        label = { Text("Pronto Raw Hex String") },
                        placeholder = { Text("0000 006D 0022 0002 ...") },
                        maxLines = 4,
                        isError = hexError != null,
                        supportingText = hexError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_pronto_hex")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (!isValidProntoHex(prontoHexInput)) {
                                hexError = "Pronto format မမှန်ပါ — '0000' နဲ့ စပြီး 4-digit hex word အနည်းဆုံး 4 လုံး ရှိရမယ်"
                                return@Button
                            }
                            hexError = null
                            onFireProntoHex(prontoHexInput)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_fire_pronto_hex"),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                    ) {
                        Icon(Icons.Default.Sensors, contentDescription = null, tint = Slate900)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pronto Hex ဖြင့် လွှင့်ထုတ်မည်", color = Slate900, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

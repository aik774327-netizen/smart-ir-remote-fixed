package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentRed
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun AiAssistantDialog(
    isOpen: Boolean,
    isLoading: Boolean,
    statusMessage: String?,
    maskedApiKey: String,
    onDismiss: () -> Unit,
    onRequestSynthesis: (brand: String, appliance: String, action: String, hint: String?) -> Unit,
    onTestAndSaveKey: (String) -> Unit
) {
    if (!isOpen) return

    var brand by remember { mutableStateOf("") }
    var appliance by remember { mutableStateOf("") }
    var action by remember { mutableStateOf("") }
    var hint by remember { mutableStateOf("") }
    var apiKeyInput by remember { mutableStateOf("") }
    var showKeyEditor by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Slate800,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("ai_assistant_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini AI IR Synthesizer",
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
                    text = "စက်ပစ္စည်း အမှတ်တံဆိပ်နှင့် ခလုတ်ကို ရိုက်ထည့်၍ IR လှိုင်းကုဒ်ကို Gemini AI ဖြင့် ရှာဖွေနိုင်ပါသည်",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // WARNING: AI-synthesized codes are unverified
                Text(
                    text = "⚠️ AI က ထုတ်ပေးသော code များသည် အတည်မပြုရသေးပါ — မှားနိုင်သည်။ Preset database ထဲက verified code ကို ဦးစားပေးသုံးပါ",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand (ဥပမာ- Xiaomi, Hatari, Gree)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_ai_brand")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = appliance,
                    onValueChange = { appliance = it },
                    label = { Text("Appliance (ဥပမာ- Standing Fan, Smart TV)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_ai_appliance")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = action,
                    onValueChange = { action = it },
                    label = { Text("Button Action (ဥပမာ- POWER, SPEED, TEMP_UP)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_ai_action")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = hint,
                    onValueChange = { hint = it },
                    label = { Text("Additional Hint (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onRequestSynthesis(brand, appliance, action, hint) },
                    enabled = !isLoading && brand.isNotBlank() && action.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_synthesize_ai"),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Slate900)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI တွက်ချက်နေပါသည်...", color = Slate900, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Slate900)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("IR လှိုင်း ရှာဖွေပြီး လွှင့်ထုတ်မည်", color = Slate900, fontWeight = FontWeight.Bold)
                    }
                }

                if (!statusMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (statusMessage.startsWith("✅")) AccentGreen else AccentOrange,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Slate700)
                Spacer(modifier = Modifier.height(12.dp))

                // API Key Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Gemini API Key: $maskedApiKey",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "သတိပြုရန်: မြန်မာနိုင်ငံအတွက် VPN ဖွင့်ရန် လိုအပ်ပါသည်",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentOrange,
                            fontSize = 10.sp
                        )
                    }
                    TextButton(onClick = { showKeyEditor = !showKeyEditor }) {
                        Text(if (showKeyEditor) "ပိတ်မည်" else "Key ပြင်မည်", fontSize = 11.sp, color = AccentCyan)
                    }
                }

                if (showKeyEditor) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("API Key အသစ် ရိုက်ထည့်ပါ (AQ. or AIza...)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onTestAndSaveKey(apiKeyInput) },
                        enabled = !isLoading && apiKeyInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                    ) {
                        Text("သိမ်းဆည်းပြီး စစ်ဆေးမည်", color = Color.White)
                    }
                }
            }
        }
    }
}

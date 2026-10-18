package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

/**
 * Guidance dialog shown when Bluetooth permissions are denied.
 * Directs user to app Settings to re-enable permissions.
 * Fixes audit finding #15.
 */
@Composable
fun PermissionGuideDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Slate800,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("permission_guide_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BluetoothDisabled, contentDescription = null, tint = AccentOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bluetooth Permission လိုအပ်ပါသည်",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "BLE စက်များ ရှာဖွေရန် Bluetooth permission လိုအပ်ပါသည်။ အောက်ပါ အဆင့်များအတိုင်း ဖွင့်ပေးပါ:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                val steps = listOf(
                    "1. အောက်က \"Setting ဖွင့်မည်\" ကို နှိပ်ပါ",
                    "2. Permissions ကို နှိပ်ပါ",
                    "3. Bluetooth / Nearby devices ကို Allow လုပ်ပါ"
                )
                steps.forEach { step ->
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_open_app_settings"),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Slate900)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Setting ဖွင့်မည်", color = Slate900, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("နောက်မှ", color = Color.LightGray)
                }
            }
        }
    }
}

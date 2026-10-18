package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ModeFanOff
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ApplianceType
import com.example.domain.model.IrCommandType
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRed
import com.example.ui.theme.ButtonBorder
import com.example.ui.theme.ButtonDark
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun RemoteControlPad(
    applianceType: ApplianceType,
    onFireCommand: (IrCommandType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("remote_control_pad"),
        color = Slate900,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (applianceType) {
                ApplianceType.FAN -> FanRemoteLayout(onFireCommand)
                ApplianceType.TV, ApplianceType.TV_BOX -> TvRemoteLayout(onFireCommand)
                ApplianceType.AIR_CONDITIONER -> AcRemoteLayout(onFireCommand)
                else -> GenericRemoteLayout(onFireCommand)
            }
        }
    }
}

@Composable
fun FanRemoteLayout(onFire: (IrCommandType) -> Unit) {
    var currentSpeed by remember { mutableIntStateOf(1) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Power Button
        PowerRoundButton(onClick = { onFire(IrCommandType.POWER) })

        Spacer(modifier = Modifier.height(24.dp))

        // Speed Display & Selector
        Text(
            text = "လေအလျင် အဆင့်: $currentSpeed",
            style = MaterialTheme.typography.titleMedium,
            color = AccentCyan,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (speed in 1..4) {
                val isSelected = speed == currentSpeed
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) AccentCyan else ButtonDark)
                        .border(1.dp, if (isSelected) AccentCyan else ButtonBorder, CircleShape)
                        .clickable {
                            currentSpeed = speed
                            onFire(IrCommandType.FAN_SPEED)
                        }
                        .testTag("fan_speed_$speed"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$speed",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Slate900 else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Swing, Timer, Mode
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RemotePadButton(
                icon = Icons.Default.RotateRight,
                label = "လည်ပတ်\n(Swing)",
                onClick = { onFire(IrCommandType.SWING) },
                testTag = "btn_fan_swing"
            )
            RemotePadButton(
                icon = Icons.Default.Timer,
                label = "အချိန်ကိုက်\n(Timer)",
                onClick = { onFire(IrCommandType.TIMER) },
                testTag = "btn_fan_timer"
            )
            RemotePadButton(
                icon = Icons.Default.ModeNight,
                label = "စနစ်\n(Mode)",
                onClick = { onFire(IrCommandType.MODE) },
                testTag = "btn_fan_mode"
            )
        }
    }
}

@Composable
fun TvRemoteLayout(onFire: (IrCommandType) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Power and Mute
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PowerRoundButton(onClick = { onFire(IrCommandType.POWER) })
            RemotePadButton(
                icon = Icons.Default.VolumeMute,
                label = "အသံပိတ်",
                onClick = { onFire(IrCommandType.MUTE) },
                tint = AccentOrange,
                testTag = "btn_tv_mute"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5-way D-Pad Navigation
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .background(Slate800)
                .border(2.dp, ButtonBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Up
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onFire(IrCommandType.UP) }
                    .testTag("btn_tv_up"),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = Color.White)
            }

            // Down
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onFire(IrCommandType.DOWN) }
                    .testTag("btn_tv_down"),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = Color.White)
            }

            // Left
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 10.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onFire(IrCommandType.LEFT) }
                    .testTag("btn_tv_left"),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left", tint = Color.White)
            }

            // Right
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onFire(IrCommandType.RIGHT) }
                    .testTag("btn_tv_right"),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right", tint = Color.White)
            }

            // Center OK
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AccentCyan)
                    .clickable { onFire(IrCommandType.OK) }
                    .testTag("btn_tv_ok"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "OK",
                    color = Slate900,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Home and Back
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RemotePadButton(
                icon = Icons.Default.ArrowBack,
                label = "နောက်သို့",
                onClick = { onFire(IrCommandType.BACK) },
                testTag = "btn_tv_back"
            )
            RemotePadButton(
                icon = Icons.Default.Home,
                label = "ပင်မ",
                onClick = { onFire(IrCommandType.HOME) },
                testTag = "btn_tv_home"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Volume and Channel Rockers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Volume Rocker
            Column(
                modifier = Modifier
                    .width(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ButtonDark)
                    .border(1.dp, ButtonBorder, RoundedCornerShape(20.dp)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable { onFire(IrCommandType.VOL_UP) }
                        .testTag("btn_vol_up"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Vol +", tint = Color.White)
                }
                Text("VOL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable { onFire(IrCommandType.VOL_DOWN) }
                        .testTag("btn_vol_down"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Vol -", tint = Color.White)
                }
            }

            // Channel Rocker
            Column(
                modifier = Modifier
                    .width(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ButtonDark)
                    .border(1.dp, ButtonBorder, RoundedCornerShape(20.dp)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable { onFire(IrCommandType.CH_UP) }
                        .testTag("btn_ch_up"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "CH +", tint = Color.White)
                }
                Text("CH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentPurple)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable { onFire(IrCommandType.CH_DOWN) }
                        .testTag("btn_ch_down"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "CH -", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun AcRemoteLayout(onFire: (IrCommandType) -> Unit) {
    var temp by remember { mutableIntStateOf(24) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PowerRoundButton(onClick = { onFire(IrCommandType.POWER) })

        Spacer(modifier = Modifier.height(16.dp))

        // Digital Temperature Display Box
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .border(1.dp, AccentCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${temp}°C",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan
                )
                Text(
                    text = "COOL MODE • AUTO SPEED • 38kHz",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Temp Up & Temp Down
        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(ButtonDark)
                    .border(1.dp, AccentCyan, CircleShape)
                    .clickable {
                        if (temp < 32) temp++
                        onFire(IrCommandType.TEMP_UP)
                    }
                    .testTag("btn_ac_temp_up"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = AccentCyan)
                    Text("တိုး", fontSize = 10.sp, color = Color.White)
                }
            }

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(ButtonDark)
                    .border(1.dp, AccentCyan, CircleShape)
                    .clickable {
                        if (temp > 16) temp--
                        onFire(IrCommandType.TEMP_DOWN)
                    }
                    .testTag("btn_ac_temp_down"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = AccentCyan)
                    Text("လျှော့", fontSize = 10.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AC Controls: Mode, Fan Speed, Swing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RemotePadButton(
                icon = Icons.Default.Sync,
                label = "စနစ်\n(Mode)",
                onClick = { onFire(IrCommandType.MODE) },
                testTag = "btn_ac_mode"
            )
            RemotePadButton(
                icon = Icons.Default.ModeFanOff,
                label = "လေအား\n(Speed)",
                onClick = { onFire(IrCommandType.FAN_SPEED) },
                testTag = "btn_ac_speed"
            )
            RemotePadButton(
                icon = Icons.Default.RotateRight,
                label = "လည်ပတ်\n(Swing)",
                onClick = { onFire(IrCommandType.SWING) },
                testTag = "btn_ac_swing"
            )
        }
    }
}

@Composable
fun GenericRemoteLayout(onFire: (IrCommandType) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PowerRoundButton(onClick = { onFire(IrCommandType.POWER) })
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RemotePadButton(
                icon = Icons.Default.VolumeUp,
                label = "အသံတိုး",
                onClick = { onFire(IrCommandType.VOL_UP) },
                testTag = "btn_gen_vol_up"
            )
            RemotePadButton(
                icon = Icons.Default.VolumeMute,
                label = "အသံပိတ်",
                onClick = { onFire(IrCommandType.MUTE) },
                testTag = "btn_gen_mute"
            )
        }
    }
}

@Composable
fun PowerRoundButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(AccentRed)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White),
                onClick = onClick
            )
            .testTag("btn_power"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PowerSettingsNew,
            contentDescription = "Power",
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun RemotePadButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = AccentCyan,
    testTag: String = ""
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(ButtonDark)
                .border(1.dp, ButtonBorder, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = AccentCyan),
                    onClick = onClick
                )
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.LightGray,
            lineHeight = 12.sp
        )
    }
}

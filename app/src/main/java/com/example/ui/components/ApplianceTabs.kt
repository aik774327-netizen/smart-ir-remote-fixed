package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ModeFanOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ApplianceProfile
import com.example.domain.model.ApplianceType
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun ApplianceTabs(
    selectedType: ApplianceType,
    availableProfiles: List<ApplianceProfile>,
    currentProfile: ApplianceProfile?,
    onSelectType: (ApplianceType) -> Unit,
    onSelectProfile: (ApplianceProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Top Appliance Categories Tab
        ScrollableTabRow(
            selectedTabIndex = ApplianceType.values().indexOf(selectedType),
            containerColor = Slate900,
            contentColor = AccentCyan,
            edgePadding = 8.dp,
            divider = {}
        ) {
            ApplianceType.values().forEach { type ->
                val isSelected = type == selectedType
                Tab(
                    selected = isSelected,
                    onClick = { onSelectType(type) },
                    modifier = Modifier.testTag("tab_${type.name.lowercase()}"),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = getApplianceIcon(type),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) AccentCyan else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = type.displayNameMy,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AccentCyan else Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Brand Presets Chips
        if (availableProfiles.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableProfiles) { profile ->
                    val isSelected = profile.id == currentProfile?.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectProfile(profile) },
                        label = {
                            Column {
                                Text(
                                    text = profile.brand,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = profile.model,
                                    fontSize = 9.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentCyan.copy(alpha = 0.25f),
                            selectedLabelColor = AccentCyan,
                            containerColor = Slate800,
                            labelColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("preset_${profile.id}")
                    )
                }
            }
        }
    }
}

fun getApplianceIcon(type: ApplianceType): ImageVector {
    return when (type) {
        ApplianceType.FAN -> Icons.Default.ModeFanOff
        ApplianceType.TV -> Icons.Default.Tv
        ApplianceType.AIR_CONDITIONER -> Icons.Default.Air
        ApplianceType.PROJECTOR -> Icons.Default.Videocam
        ApplianceType.TV_BOX -> Icons.Default.Tv
        ApplianceType.AUDIO -> Icons.Default.MusicNote
        ApplianceType.CUSTOM -> Icons.Default.SettingsRemote
    }
}

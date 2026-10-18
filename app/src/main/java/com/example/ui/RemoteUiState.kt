package com.example.ui

import com.example.domain.model.ApplianceProfile
import com.example.domain.model.ApplianceType
import com.example.domain.model.HardwareState

data class RemoteUiState(
    val selectedApplianceType: ApplianceType = ApplianceType.FAN,
    val availableProfiles: List<ApplianceProfile> = emptyList(),
    val currentProfile: ApplianceProfile? = null,
    val hardwareState: HardwareState = HardwareState(),
    val isHapticEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isAiDialogOpen: Boolean = false,
    val isManualHexDialogOpen: Boolean = false,
    val isBleDialogOpen: Boolean = false,
    val bleConnectionState: com.kyawzay.blueconnect.bluetooth.ble.BleConnectionState = com.kyawzay.blueconnect.bluetooth.ble.BleConnectionState.DISCONNECTED,
    val bleRssi: Int = 0,
    val isAiLoading: Boolean = false,
    val aiStatusMessage: String? = null,
    val isApiKeyValid: Boolean? = null,
    val currentApiKeyMasked: String = "",
    val lastFiredCommandName: String? = null,
    val lastFiredHex: String? = null,
    /**
     * True when Bluetooth permissions were denied — shows guidance dialog
     * directing user to Settings → Apps → Permissions.
     * Fixes audit finding #15.
     */
    val showPermissionGuide: Boolean = false
)

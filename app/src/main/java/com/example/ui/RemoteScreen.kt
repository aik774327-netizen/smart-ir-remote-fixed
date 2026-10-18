package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.components.AiAssistantDialog
import com.example.ui.components.ApplianceTabs
import com.example.ui.components.HardwareStatusBar
import com.example.ui.components.ManualHexDialog
import com.example.ui.components.RemoteControlPad
import com.example.ui.theme.Slate900

@Composable
fun RemoteScreen(
    viewModel: RemoteViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is com.kyawzay.blueconnect.viewmodel.UiEvent.ShowToast -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is com.kyawzay.blueconnect.viewmodel.UiEvent.NavigateTo -> {
                    // Handle navigation if needed
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Slate900
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate900),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Hardware Status & Quick Actions Bar
                HardwareStatusBar(
                    hardwareState = uiState.hardwareState,
                    isHapticEnabled = uiState.isHapticEnabled,
                    isSoundEnabled = uiState.isSoundEnabled,
                    lastFiredCommand = uiState.lastFiredCommandName,
                    lastFiredHex = uiState.lastFiredHex,
                    onToggleHaptic = { viewModel.toggleHaptic() },
                    onToggleSound = { viewModel.toggleSound() },
                    onOpenAi = { viewModel.openAiDialog(true) },
                    onOpenHex = { viewModel.openManualHexDialog(true) },
                    onOpenBle = { viewModel.openBleDialog(true) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Appliance Category & Brand Selector
                ApplianceTabs(
                    selectedType = uiState.selectedApplianceType,
                    availableProfiles = uiState.availableProfiles,
                    currentProfile = uiState.currentProfile,
                    onSelectType = { viewModel.selectApplianceType(it) },
                    onSelectProfile = { viewModel.selectProfile(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Ergonomic Remote Control Pad
                RemoteControlPad(
                    applianceType = uiState.selectedApplianceType,
                    onFireCommand = { viewModel.fireCommand(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Dialogs
        AiAssistantDialog(
            isOpen = uiState.isAiDialogOpen,
            isLoading = uiState.isAiLoading,
            statusMessage = uiState.aiStatusMessage,
            maskedApiKey = uiState.currentApiKeyMasked,
            onDismiss = { viewModel.openAiDialog(false) },
            onRequestSynthesis = { brand, appliance, action, hint ->
                viewModel.requestAiSynthesis(brand, appliance, action, hint)
            },
            onTestAndSaveKey = { viewModel.testAndSaveApiKey(it) }
        )

        ManualHexDialog(
            isOpen = uiState.isManualHexDialogOpen,
            onDismiss = { viewModel.openManualHexDialog(false) },
            onFireSignal = { viewModel.fireDirectSignal(it) },
            onFireProntoHex = { viewModel.fireProntoHex(it) }
        )

        com.example.ui.components.BleScanDialog(
            isOpen = uiState.isBleDialogOpen,
            connectionState = uiState.bleConnectionState,
            currentRssi = uiState.bleRssi,
            onDismiss = { viewModel.openBleDialog(false) },
            onConnectDevice = { viewModel.connectBleDevice(it) },
            onDisconnect = { viewModel.disconnectBleDevice() }
        )

        // Permission guidance dialog (audit finding #15)
        com.example.ui.components.PermissionGuideDialog(
            isOpen = uiState.showPermissionGuide,
            onDismiss = { viewModel.dismissPermissionGuide() }
        )
    }
}

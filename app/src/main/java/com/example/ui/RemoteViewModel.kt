package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClientProvider
import com.example.domain.model.ApplianceProfile
import com.example.domain.model.ApplianceType
import com.example.domain.model.HardwareState
import com.example.domain.model.IrCommandType
import com.example.domain.model.IrSignalPattern
import com.kyawzay.blueconnect.bluetooth.ble.BleConnectionState
import com.kyawzay.blueconnect.bluetooth.ble.BleGattManager
import com.kyawzay.blueconnect.bluetooth.ble.connectWithAutoRetry
import com.kyawzay.blueconnect.bluetooth.ir.NecProtocolEncoder
import com.kyawzay.blueconnect.bluetooth.ir.ProntoHexConverter
import com.kyawzay.blueconnect.data.hardware.ConsumerIrTransmitterImpl
import com.kyawzay.blueconnect.data.local.ApplianceDatabase
import com.kyawzay.blueconnect.data.local.ApplianceIrCodeEntity
import com.kyawzay.blueconnect.data.remote.GeminiRemoteApiService
import com.kyawzay.blueconnect.domain.usecase.SynthesizeAiSignalUseCase
import com.kyawzay.blueconnect.ui.feedback.FeedbackHelper
import com.kyawzay.blueconnect.viewmodel.UiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RemoteViewModel(application: Application) : AndroidViewModel(application) {

    // 1. Hardware IR Transmitter & Feedback
    private val irTransmitter = ConsumerIrTransmitterImpl(application)
    private val feedbackHelper = FeedbackHelper(application)

    // 2. Room Database
    private val database = ApplianceDatabase.getInstance(application)
    private val dao = database.applianceIrDao()

    // 3. BLE GATT Manager & Robust Bluetooth Manager
    val bleGattManager = BleGattManager(application)
    val robustBtManager = com.kyawzay.blueconnect.bluetooth.RobustBluetoothManager(application, viewModelScope)

    // 4. UI Event Channel for One-time Events (Toast, Navigation)
    private val _eventChannel = Channel<UiEvent>(Channel.BUFFERED)
    val eventFlow = _eventChannel.receiveAsFlow()

    // 5. UI State
    private val _uiState = MutableStateFlow(RemoteUiState())
    val uiState: StateFlow<RemoteUiState> = _uiState.asStateFlow()

    init {
        // Initialize Gemini API Key store from SharedPreferences
        GeminiClientProvider.init(application)
        // Load persisted haptic/sound settings
        loadFeedbackSettings()
        // Initialize Hardware state
        checkHardwareState()
        // Load Initial presets
        loadProfilesForType(ApplianceType.FAN)
        updateMaskedApiKey()

        // Observe Room Database for auto-seeded or custom presets
        viewModelScope.launch {
            dao.getAllCodes().collect { list ->
                if (list.isNotEmpty()) {
                    mergeRoomPresetsIntoUi(list)
                }
            }
        }

        // Observe BLE state
        viewModelScope.launch {
            bleGattManager.connectionState.collect { state ->
                _uiState.update { it.copy(bleConnectionState = state) }
            }
        }
        viewModelScope.launch {
            bleGattManager.currentRssi.collect { rssi ->
                _uiState.update { it.copy(bleRssi = rssi) }
            }
        }
    }

    fun openBleDialog(open: Boolean) {
        feedbackHelper.triggerClickFeedback(_uiState.value.isHapticEnabled, false)
        _uiState.update { it.copy(isBleDialogOpen = open) }
    }

    fun connectBleDevice(device: android.bluetooth.BluetoothDevice) {
        viewModelScope.launch {
            val devName = try { device.name ?: device.address } catch (_: Exception) { device.address }
            triggerUiMessage("ချိတ်ဆက်နေပါသည်: $devName...")

            if (device.type == android.bluetooth.BluetoothDevice.DEVICE_TYPE_CLASSIC) {
                // Classic SPP with Reflection Fallback
                val success = robustBtManager.connectClassicDevice(device)
                if (success) {
                    _uiState.update { it.copy(bleConnectionState = BleConnectionState.READY) }
                    triggerUiMessage("Classic Bluetooth (SPP) ချိတ်ဆက်မှု အောင်မြင်ပါသည်")
                } else {
                    _uiState.update { it.copy(bleConnectionState = BleConnectionState.FAILED) }
                    triggerUiMessage("Classic Bluetooth ချိတ်ဆက်မှု မအောင်မြင်ပါ")
                }
            } else {
                // BLE with GATT Error 133 Prevention, MTU 512 & Exponential Backoff
                robustBtManager.connectBleWithRetry(device, maxRetries = 3) { success ->
                    if (success) {
                        _uiState.update { it.copy(bleConnectionState = BleConnectionState.READY) }
                        triggerUiMessage("BLE (GATT MTU 512) ချိတ်ဆက်မှု အောင်မြင်ပါသည်")
                    } else {
                        _uiState.update { it.copy(bleConnectionState = BleConnectionState.FAILED) }
                        triggerUiMessage("BLE ချိတ်ဆက်မှု မအောင်မြင်ပါ (Auto-retry ကုန်ဆုံးသည်)")
                    }
                }
            }
        }
    }

    fun disconnectBleDevice() {
        robustBtManager.closeBleGatt()
        robustBtManager.closeClassicSocket()
        bleGattManager.disconnect()
        _uiState.update { it.copy(bleConnectionState = BleConnectionState.DISCONNECTED) }
        triggerUiMessage("Bluetooth ချိတ်ဆက်မှု ဖြုတ်လိုက်ပါပြီ")
    }

    fun triggerUiMessage(msg: String) {
        viewModelScope.launch {
            _eventChannel.send(UiEvent.ShowToast(msg))
        }
    }

    fun triggerNavigation(route: String) {
        viewModelScope.launch {
            _eventChannel.send(UiEvent.NavigateTo(route))
        }
    }

    private fun checkHardwareState() {
        val hasEmitter = irTransmitter.isIrEmitterSupported()
        val isXiaomi = irTransmitter.isXiaomiOrRedmiDevice()
        val isFreqSupported = irTransmitter.isFrequencySupported(38000)

        _uiState.update {
            it.copy(
                hardwareState = HardwareState(
                    hasIrEmitter = hasEmitter,
                    carrierFrequencies = if (hasEmitter) listOf(Pair(36000, 40000)) else emptyList(),
                    isRedmiOrXiaomi = isXiaomi
                )
            )
        }
    }

    private fun updateMaskedApiKey() {
        val key = GeminiClientProvider.getApiKey()
        val masked = if (key.length >= 10) {
            "${key.take(4)}••••••••${key.takeLast(4)}"
        } else if (key.isNotBlank()) {
            "••••••••"
        } else {
            "Not Configured"
        }
        _uiState.update { it.copy(currentApiKeyMasked = masked) }
    }

    fun selectApplianceType(type: ApplianceType) {
        feedbackHelper.triggerClickFeedback(_uiState.value.isHapticEnabled, false)
        _uiState.update { it.copy(selectedApplianceType = type) }
        loadProfilesForType(type)
    }

    private fun loadProfilesForType(type: ApplianceType) {
        viewModelScope.launch {
            val presets = com.example.data.preset.AppliancePresetDatabase.getAllPresets().filter { it.type == type }
            _uiState.update {
                it.copy(
                    availableProfiles = presets,
                    currentProfile = presets.firstOrNull()
                )
            }
        }
    }

    private fun mergeRoomPresetsIntoUi(entities: List<ApplianceIrCodeEntity>) {
        val groupedByBrand = entities.groupBy { it.brandName to it.modelName }
        val customProfiles = groupedByBrand.map { (key, codes) ->
            val brand = key.first
            val model = key.second
            val category = codes.firstOrNull()?.category ?: "Custom"
            val appType = when {
                category.contains("Fan", ignoreCase = true) -> ApplianceType.FAN
                category.contains("TV", ignoreCase = true) -> ApplianceType.TV
                category.contains("Air", ignoreCase = true) || category.contains("AC", ignoreCase = true) -> ApplianceType.AIR_CONDITIONER
                else -> ApplianceType.CUSTOM
            }

            val commandsMap = mutableMapOf<IrCommandType, IrSignalPattern>()
            codes.forEach { codeEntity ->
                val cmdType = IrCommandType.values().firstOrNull { it.name.equals(codeEntity.buttonName, ignoreCase = true) }
                    ?: IrCommandType.POWER

                val hex = codeEntity.hexCode ?: "0x00FF00FF"
                val pattern = NecProtocolEncoder.encodeHexToNecPattern(hex)

                commandsMap[cmdType] = IrSignalPattern(
                    commandType = cmdType,
                    frequencyHz = codeEntity.frequency,
                    patternMicroseconds = pattern,
                    hexCode = hex,
                    protocolName = "NEC"
                )
            }

            ApplianceProfile(
                id = "room_${brand}_$model",
                brand = brand,
                model = model,
                type = appType,
                commands = commandsMap,
                isCustom = true
            )
        }

        _uiState.update { state ->
            val combined = (state.availableProfiles + customProfiles.filter { it.type == state.selectedApplianceType }).distinctBy { it.id }
            state.copy(
                availableProfiles = combined,
                currentProfile = state.currentProfile ?: combined.firstOrNull()
            )
        }
    }

    fun selectProfile(profile: ApplianceProfile) {
        feedbackHelper.triggerClickFeedback(_uiState.value.isHapticEnabled, false)
        _uiState.update { it.copy(currentProfile = profile) }
    }

    fun fireCommand(commandType: IrCommandType) {
        feedbackHelper.triggerClickFeedback(_uiState.value.isHapticEnabled, _uiState.value.isSoundEnabled)

        val profile = _uiState.value.currentProfile
        val signal = profile?.commands?.get(commandType)
            ?: run {
                val defaultHex = "0x00FF00FF"
                IrSignalPattern(
                    commandType = commandType,
                    frequencyHz = 38000,
                    patternMicroseconds = NecProtocolEncoder.encodeHexToNecPattern(defaultHex),
                    hexCode = defaultHex,
                    protocolName = "NEC"
                )
            }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    lastFiredCommandName = commandType.labelMy,
                    lastFiredHex = signal.hexCode
                )
            }

            val success = irTransmitter.transmitNecHex(signal.hexCode)
            if (success) {
                triggerUiMessage("လွှင့်ထုတ်ပြီး: ${commandType.labelMy} (${signal.hexCode})")
            }
        }
    }

    fun fireDirectSignal(signal: IrSignalPattern) {
        feedbackHelper.triggerClickFeedback(_uiState.value.isHapticEnabled, _uiState.value.isSoundEnabled)

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    lastFiredCommandName = signal.commandType.labelMy,
                    lastFiredHex = signal.hexCode
                )
            }
            irTransmitter.transmitNecHex(signal.hexCode)
            triggerUiMessage("Custom Hex လွှင့်ထုတ်ပြီး: ${signal.hexCode}")
        }
    }

    fun fireProntoHex(prontoHex: String) {
        feedbackHelper.triggerClickFeedback(_uiState.value.isHapticEnabled, _uiState.value.isSoundEnabled)
        viewModelScope.launch {
            val success = irTransmitter.transmitProntoHex(prontoHex)
            if (success) {
                triggerUiMessage("Pronto Hex အောင်မြင်စွာ လွှင့်ထုတ်ပြီးပါပြီ")
            } else {
                triggerUiMessage("Pronto Hex လွှင့်ထုတ်မှု မအောင်မြင်ပါ")
            }
        }
    }

    fun toggleHaptic() {
        _uiState.update { it.copy(isHapticEnabled = !it.isHapticEnabled) }
        saveFeedbackSettings()
        triggerUiMessage(if (!_uiState.value.isHapticEnabled) "တုန်ခါမှု (Haptic) ပိတ်လိုက်ပါပြီ" else "တုန်ခါမှု (Haptic) ဖွင့်လိုက်ပါပြီ")
    }

    fun toggleSound() {
        _uiState.update { it.copy(isSoundEnabled = !it.isSoundEnabled) }
        saveFeedbackSettings()
        triggerUiMessage(if (!_uiState.value.isSoundEnabled) "အသံ (Beep) ပိတ်လိုက်ပါပြီ" else "အသံ (Beep) ဖွင့်လိုက်ပါပြီ")
    }

    /**
     * Persist haptic/sound settings to SharedPreferences.
     * Fixes audit finding #14: settings were lost on app restart.
     */
    private fun getFeedbackPrefs(): android.content.SharedPreferences {
        return getApplication<android.app.Application>()
            .getSharedPreferences("smart_remote_prefs", android.content.Context.MODE_PRIVATE)
    }

    private fun loadFeedbackSettings() {
        val prefs = getFeedbackPrefs()
        val haptic = prefs.getBoolean("haptic_enabled", true)
        val sound = prefs.getBoolean("sound_enabled", true)
        _uiState.update { it.copy(isHapticEnabled = haptic, isSoundEnabled = sound) }
    }

    private fun saveFeedbackSettings() {
        getFeedbackPrefs().edit()
            .putBoolean("haptic_enabled", _uiState.value.isHapticEnabled)
            .putBoolean("sound_enabled", _uiState.value.isSoundEnabled)
            .apply()
    }

    /**
     * Called when Bluetooth permissions are denied.
     * Shows guidance dialog instead of silent "unavailable".
     * Fixes audit finding #15.
     */
    fun onBluetoothPermissionDenied() {
        _uiState.update { it.copy(showPermissionGuide = true) }
    }

    fun dismissPermissionGuide() {
        _uiState.update { it.copy(showPermissionGuide = false) }
    }

    fun openAiDialog(open: Boolean) {
        feedbackHelper.triggerClickFeedback(_uiState.value.isHapticEnabled, false)
        _uiState.update { it.copy(isAiDialogOpen = open) }
    }

    fun openManualHexDialog(open: Boolean) {
        feedbackHelper.triggerClickFeedback(_uiState.value.isHapticEnabled, false)
        _uiState.update { it.copy(isManualHexDialogOpen = open) }
    }

    fun requestAiSynthesis(brand: String, appliance: String, action: String, hint: String?) {
        _uiState.update { it.copy(isAiLoading = true, aiStatusMessage = "Gemini AI မှ ကုဒ်ရှာဖွေနေပါသည်...") }
        viewModelScope.launch {
            val key = GeminiClientProvider.getApiKey()
            val apiService = GeminiRemoteApiService(key)
            val useCase = SynthesizeAiSignalUseCase(apiService)

            val result = useCase(brand, appliance, action)
            result.fold(
                onSuccess = { hexCode ->
                    _uiState.update {
                        it.copy(
                            isAiLoading = false,
                            aiStatusMessage = "✅ ရှာဖွေတွေ့ရှိသည်: $hexCode (NEC 38kHz)",
                            lastFiredHex = hexCode,
                            lastFiredCommandName = action
                        )
                    }
                    irTransmitter.transmitNecHex(hexCode)
                    triggerUiMessage("AI တွေ့ရှိချက် လွှင့်ထုတ်ပြီး: $hexCode")

                    // Also save into Room Database for offline reuse
                    dao.insertIrCode(
                        ApplianceIrCodeEntity(
                            brandName = brand,
                            category = appliance,
                            modelName = "AI_Synthesized",
                            buttonName = action,
                            hexCode = hexCode,
                            frequency = 38000
                        )
                    )
                },
                onFailure = { error ->
                    val errorMsg = error.message ?: "Unknown error"
                    _uiState.update {
                        it.copy(
                            isAiLoading = false,
                            aiStatusMessage = "❌ အမှား: $errorMsg"
                        )
                    }
                    triggerUiMessage("❌ $errorMsg")
                }
            )
        }
    }

    fun testAndSaveApiKey(newKey: String) {
        val sanitized = newKey.trim().removeSurrounding("\"").removeSurrounding("'").filterNot { it.isWhitespace() }
        _uiState.update {
            it.copy(
                isAiLoading = true,
                aiStatusMessage = "API Key အခြေအနေ စစ်ဆေးနေပါသည်...",
                isApiKeyValid = null
            )
        }
        viewModelScope.launch {
            val result = GeminiClientProvider.validateApiKey(sanitized)
            result.fold(
                onSuccess = { msg ->
                    GeminiClientProvider.setRuntimeApiKey(sanitized)
                    updateMaskedApiKey()
                    _uiState.update {
                        it.copy(
                            isAiLoading = false,
                            aiStatusMessage = "✅ $msg",
                            isApiKeyValid = true
                        )
                    }
                    triggerUiMessage("API Key အောင်မြင်စွာ သိမ်းဆည်းပြီးပါပြီ")
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isAiLoading = false,
                            aiStatusMessage = "❌ ${error.message}",
                            isApiKeyValid = false
                        )
                    }
                    triggerUiMessage("API Key မှားယွင်းနေပါသည်")
                }
            )
        }
    }
}

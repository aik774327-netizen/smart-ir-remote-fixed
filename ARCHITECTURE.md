# Smart IR Remote & Bluetooth Connect - System Architecture Specification (v2.0)

## 1. Architectural Style & Design Principles

This project adheres to **Clean Architecture** principles combined with **MVI (Model-View-Intent) / MVVM with Unidirectional Data Flow (UDF)**.

### Core Architectural Principles:
1. **Separation of Concerns**: UI components (Jetpack Compose) do not contain business logic, raw hardware sockets, IR timers, or network calls.
2. **Dependency Inversion**: Domain Layer defines repository and use case contracts; Data Layer implements them using platform SDKs, Room SQLite, Bluetooth Gatt/RFCOMM, or Retrofit.
3. **Single Source of Truth**: All UI state flows through an immutable `RemoteUiState` emitted via Kotlin `StateFlow` from `RemoteViewModel`.
4. **Hardware Abstraction & Resilient Fallbacks**:
   - **Internal Infrared Blaster**: Built-in 38kHz Consumer IR (`ConsumerIrManager`) with Xiaomi/Redmi HyperOS hardware detection and graceful zero-crash simulation fallback.
   - **External Bluetooth Bridge (Classic & BLE)**: Dual-mode connection with RFCOMM reflection fallback (HC-05/06) and GATT Error 133 prevention, MTU 512, and Thread-Safe Mutex Command Queue (`RobustBluetoothManager`).
5. **Offline-First Persistence**: Room Database (SQLite with KSP codegen) pre-populated on first install with Myanmar & Asian appliance codes (Keruilai, KENNEDE, Midea, Gree, Xiaomi) and SharedPreferences persistent API key fallback.
6. **Single-Event Channel**: One-time events (Toasts, Snackbars, and navigation triggers) are streamed via `Channel<UiEvent>(BUFFERED)` and consumed as `receiveAsFlow()` in UI LaunchedEffects to prevent event duplication on recomposition.

---

## 2. High-Level Architectural Diagram

```
+-------------------------------------------------------------------------------------------------+
|                                       PRESENTATION LAYER                                        |
|                                                                                                 |
|   +-----------------------+   +-------------------+   +-----------------+   +---------------+   |
|   |  HardwareStatusBar    |   |   ApplianceTabs   |   | RemoteControlPad|   | BleScanDialog |   |
|   +-----------------------+   +-------------------+   +-----------------+   +---------------+   |
|               ^                        ^                       ^                    ^           |
|               |                        |                       |                    |           |
|   +-----------------------------------------------------------------------------------------+   |
|   |                                  RemoteScreen (Compose)                                 |   |
|   +-----------------------------------------------------------------------------------------+   |
|                     │                                                         ▲                 |
|                     ▼ (User Actions)                                          │ (UiEvent Flow)  |
|   +---------------------------------------------------------------------------│-------------+   |
|   |                                     RemoteViewModel                                     |   |
|   |         State: MutableStateFlow<RemoteUiState>  ───►  Channel<UiEvent> ────────┘        |   |
|   +-----------------------------------------------------------------------------------------+   |
+------------------------------------------------│------------------------------------------------+
                                                 │ Invokes Use Cases / Repositories
                                                 ▼
+-------------------------------------------------------------------------------------------------+
|                                          DOMAIN LAYER                                           |
|                                                                                                 |
|  +----------------------------+   +----------------------------+   +-------------------------+  |
|  |   TransmitSignalUseCase    |   |  GetApplianceProfiles      |   | SynthesizeAiSignal      |  |
|  |                            |   |         UseCase            |   |        UseCase          |  |
|  +----------------------------+   +----------------------------+   +-------------------------+  |
|                │                                 │                               │              |
|                ▼                                 ▼                               ▼              |
|  +----------------------------+   +----------------------------+   +-------------------------+  |
|  |  IrTransmitterRepository   |   |    ApplianceRepository     |   |  AiSynthesizerRepository|  |
|  |        (Interface)         |   |        (Interface)         |   |       (Interface)       |  |
|  +----------------------------+   +----------------------------+   +-------------------------+  |
+------------------------------------------------│------------------------------------------------+
                                                 │ Implemented by Data Layer
                                                 ▼
+-------------------------------------------------------------------------------------------------+
|                                           DATA LAYER                                            |
|                                                                                                 |
|  +─────────────────────────+  +───────────────────────────+  +───────────────────────────────+  |
|  │ Hardware IR Transmitter │  │ Robust Bluetooth Manager  │  │ Local Room Database (KSP)     │  |
|  │  ConsumerIrTransmitter  │  │  - Classic SPP (Channel 1)│  │  - ApplianceDatabase (SQLite) │  |
|  │  - 38kHz NEC Pulse Mod  │  │  - BLE (GATT Error 133)   │  │  - ApplianceIrDao             │  |
|  │  - Pronto Hex Converter │  │  - MTU 512 & Priority HIGH│  │  - Pre-seeded Keruilai/KENNEDE│  |
|  │  - Safe HyperOS Check   │  │  - Mutex Write Queue      │  │  - SharedPreferences Prefs    │  |
|  +─────────────────────────+  +───────────────────────────+  +───────────────────────────────+  |
|                │                            │                                │                  |
|                ▼                            ▼                                ▼                  |
|  +─────────────────────────+  +───────────────────────────+  +───────────────────────────────+  |
|  │ Physical 38kHz LED      │  │ Bluetooth Transceiver     │  │ Google Gemini 2.5-Flash API   │  |
|  │ (ConsumerIrManager)     │  │ (RFCOMM Socket / BLE GATT)│  │ (Retrofit2 + Moshi REST)      │  |
|  +─────────────────────────+  +───────────────────────────+  +───────────────────────────────+  |
+-------------------------------------------------------------------------------------------------+
```

---

## 3. Data & Signal Flow Lifecycle

### 3.1 Local Preset IR Command Flow (e.g. Power / Speed / Swing)
```
[User taps Button] 
       │
       ▼
[FeedbackHelper] ────► Triggers 40ms VibrationEffect + 50ms BEEP Audio Tone
       │
       ▼
[RemoteViewModel] ───► Resolves 32-bit Hex Code from Current ApplianceProfile
       │
       ▼
[ConsumerIrTransmitterImpl]
       ├─ If Hardware IR present:
       │    1. NecProtocolEncoder encodes Hex -> 67 Timing Pulses (9000µs mark + 4500µs space + 32-bit data + stop)
       │    2. irManager.transmit(38000, pattern)
       │
       ├─ If Bluetooth Bridge connected:
       │    robustBtManager.writeCharacteristicSafely(serviceUuid, charUuid, hexBytes)
       │
       └─ If in Simulation/Emulator:
            Safely logs simulation pulse without crashing
       │
       ▼
[EventChannel] ──────► Emits UiEvent.ShowToast("လွှင့်ထုတ်ပြီး: Power (0x00FF00FF)")
```

### 3.2 AI Signal Synthesis Flow (Unknown Appliance Model)
```
[User enters Brand + Model in AiAssistantDialog]
       │
       ▼
[RemoteViewModel] ───► SynthesizeAiSignalUseCase(brand, model, button)
       │
       ▼
[GeminiRemoteApiService]
       ├─ Validates API Key (BuildConfig or SharedPreferences runtime key)
       ├─ Executes HTTP POST to gemini-2.5-flash:generateContent
       ├─ Handles HTTP 400 Location Block (Myanmar IP VPN prompt)
       ├─ Extracts 32-bit NEC Hex Code via Regex ((0x)?[0-9A-Fa-f]{6,8})
       │
       ▼
[ApplianceDatabase] ──► Automatically saves synthesized code into Room DB for permanent offline use
       │
       ▼
[ConsumerIrTransmitterImpl] ──► Immediately transmits 38kHz signal to appliance
```

---

## 4. Package Structure & Key Class Responsibilities

```
app/src/main/java/
├── com/kyawzay/blueconnect/
│   ├── bluetooth/
│   │   ├── RobustBluetoothManager.kt       // Classic SPP Reflection Fallback, GATT Error 133 Prevention, MTU 512, Mutex Queue
│   │   ├── ble/
│   │   │   └── BleGattManager.kt           // Reactive StateFlow Connection State, RSSI Read & Auto-Retry Backoff
│   │   └── ir/
│   │       ├── NecProtocolEncoder.kt       // 32-bit MSB-First 38kHz Pulse Timing Generator (9ms leader, 560us data, 560us stop)
│   │       └── ProntoHexConverter.kt       // Pronto Raw Hex (0000 006D ...) to Frequency & Pulse Pairs Converter
│   ├── data/
│   │   ├── hardware/
│   │   │   └── ConsumerIrTransmitterImpl.kt// ConsumerIrManager hardware wrapper with Xiaomi/Redmi detection
│   │   ├── local/
│   │   │   ├── ApplianceIrCodeEntity.kt    // Room SQLite table schema for IR codes
│   │   │   ├── ApplianceIrDao.kt           // Reactive Flow DAO queries
│   │   │   ├── ApplianceDatabase.kt        // RoomDatabase singleton with KSP
│   │   │   └── DatabaseCallback.kt         // Auto-seed callback (Keruilai, KENNEDE, Midea, Gree, Xiaomi)
│   │   └── remote/
│   │       └── GeminiRemoteApiService.kt   // Direct REST client for Gemini 2.5-Flash with Myanmar Geo-block handling
│   ├── domain/usecase/
│   │   └── SynthesizeAiSignalUseCase.kt    // AI protocol synthesis business logic
│   ├── ui/feedback/
│   │   └── FeedbackHelper.kt               // Haptic Vibrator + Audio ToneGenerator manager
│   └── viewmodel/
│       ├── UiEvent.kt                      // Sealed class for ShowToast and NavigateTo
│       └── BaseRemoteViewModel.kt          // Channel-backed reactive ViewModel base
│
└── com/example/
    ├── data/
    │   ├── api/GeminiRemoteApiService.kt   // Retrofit2 + Moshi REST client & SharedPreferences persistent key store
    │   ├── hardware/                       // ConsumerIrTransmitter & FeedbackHelper bindings
    │   ├── preset/AppliancePresetDatabase.kt // Built-in presets for Asian appliances (Xiaomi, Hatari, Midea, KDK, etc.)
    │   └── repository/                     // Repository implementations on Dispatchers.IO
    ├── domain/
    │   ├── model/DomainModels.kt           // ApplianceProfile, ApplianceType, IrCommandType, IrSignalPattern, HardwareState
    │   ├── repository/Repositories.kt      // Repository interfaces
    │   └── usecase/UseCases.kt             // Clean use cases (TransmitSignal, GetProfiles, SynthesizeAi)
    └── ui/
        ├── MainActivity.kt                 // Edge-to-edge entry point
        ├── RemoteScreen.kt                 // Compose Scaffold, Single-event collector, dialog hosts
        ├── RemoteViewModel.kt              // Unified ViewModel orchestrating IR, BLE, Room, and AI
        ├── RemoteUiState.kt                // Immutable UI state holder
        ├── components/
        │   ├── HardwareStatusBar.kt        // Live 38kHz & Bluetooth status bar with action icons
        │   ├── ApplianceTabs.kt            // Category scroll tabs & Brand chip selector
        │   ├── RemoteControlPad.kt         // Specialized Ergonomic controllers for Fan, TV, and AC
        │   ├── BleScanDialog.kt            // Real-time BLE device discovery dialog with RSSI & Auto-retry
        │   ├── ManualHexDialog.kt          // Dual-mode NEC 32-bit and Pronto Raw Hex signal tester
        │   └── AiAssistantDialog.kt        // Gemini AI prompt dialog with API Key configuration & Myanmar VPN guidance
        └── theme/Theme.kt                  // Material 3 Dark OLED theme (Slate-900 palette, Cyan accents)
```

---

## 5. Technology Stack & Dependencies

| Layer / Concern | Library / Tool | Version | Role in Architecture |
| :--- | :--- | :--- | :--- |
| **Language** | Kotlin | 2.0.21 | Primary language |
| **UI Framework** | Jetpack Compose (M3) | BOM 2024.09.00 | Modern reactive user interface |
| **Architecture** | AndroidX ViewModel & Lifecycle | 2.8.6 | MVVM / MVI State management with Coroutines Flow |
| **Local Persistence** | AndroidX Room & SQLite | 2.6.1 | Local database for pre-seeded & custom IR codes |
| **Annotation Processing** | Google KSP | 2.0.21-1.0.28 | Compile-time Room DAO generation |
| **Networking** | Retrofit2 + OkHttp3 + Moshi | 2.11.0 | REST client for Gemini 2.5 Flash |
| **Hardware IR** | Android ConsumerIrManager | API 19+ | Physical 38kHz infrared pulse modulation |
| **Bluetooth Classic** | Android BluetoothSocket (RFCOMM)| API 26+ | SPP Channel 1 reflection fallback for HC-05/06 |
| **Bluetooth Low Energy** | Android BluetoothGatt | API 26+ | High priority, MTU 512, and Mutex safe queue |
| **Haptics & Audio** | Android Vibrator & ToneGenerator| System | Immediate physical and acoustic user feedback |
| **Unit & JVM Testing** | JUnit4 + Robolectric | 4.13.2 / 4.14.1 | Fast local JVM test suite for Android components |

---

## 6. Failure-Memory & Verification Proof

In accordance with the **AI Error-Learning & Failure Memory Framework (v3)**:
- All failure cases (KSP version mismatches, geo-location IP restrictions, BLE Error 133, and Manifest permissions) are documented with 3-factor analysis (Technical, Resource, Real-World) in `ERROR_LOG.md`.
- **Verification Records**:
  - `VR-001` to `VR-005` have passed:
    - `compile_applet`: **Build succeeded** (0 warnings, 0 errors).
    - `gradle :app:testDebugUnitTest`: **BUILD SUCCESSFUL in 10s** (All 26 tasks executed and passed).

# AI Error-Learning & Failure Memory Log (v3)

> This document maintains the permanent failure memory and learned recovery actions for this project under the AI Error-Learning & Failure Memory Framework.
> Principles: Truth over appearance, evidence over assumption, verification over confidence, and preventing the reuse of known-failed methods.

---

## Architecture Summary
- **Pattern**: Clean Architecture (Presentation, Domain, Data) with MVI / MVVM StateFlow
- **Platform**: Android Jetpack Compose Material 3
- **Hardware Integration**: Android `ConsumerIrManager` (38kHz Infrared Blaster targeted for Xiaomi / Redmi Note 12 Pro HyperOS)
- **AI Synthesis**: Google Gemini 2.5/3.5 Flash REST API with input sanitization and VPN geo-location handling

---

## Logged Incident Records

### [ER-001]
- **Error ID**: ER-001
- **Date / Time**: 2026-09-27T02:54:00-07:00
- **Project / Module**: app (build.gradle.kts / KSP / Moshi Annotation Processor)
- **Task**: Compile and sync project with Kotlin Symbol Processing (KSP) and Moshi JSON Adapter generation
- **Expected Result**: Project builds cleanly with KSP symbol processing for Moshi data contracts
- **Actual Result**: `Exception in thread "AWT-EventQueue-0" java.lang.NullPointerException: Cannot invoke "ksp.com.intellij.openapi.application.Application.getService(java.lang.Class)"`
- **Error Type**: #36 Dependency Error / #37 Version Compatibility Error
- **Severity**: High
- **Root Cause**: Incompatibility between `moshi-kotlin-codegen` and KSP2 / IntelliJ ApplicationManager services on background threads.
- **Recovery Attempt**: Replaced compile-time codegen with runtime reflection via `KotlinJsonAdapterFactory()`.
- **Recovery Result**: PASS
- **Lesson Learned**: Do not pair `moshi-kotlin-codegen` with KSP2; use runtime `KotlinJsonAdapterFactory()`.
- **Do Not Reuse**: Re-adding `"ksp"(libs.moshi.kotlin.codegen)` with `@JsonClass(generateAdapter = true)` under this KSP2 toolchain.
- **Verification**: Verified using `compile_applet`.

---

### [ER-002]
- **Error ID**: ER-002
- **Date / Time**: 2026-09-27T04:54:00-07:00
- **Project / Module**: app (GeminiRemoteApiService.kt / SynthesizeApplianceIrUseCase.kt)
- **Task**: Execute Gemini AI REST API call to validate API key or synthesize appliance IR signals
- **Expected Result**: Clear actionable explanation if API key is invalid or HTTP error occurs
- **Actual Result**: `HTTP 400 Bad Request` thrown with generic message "API Key သို့မဟုတ် Request ပုံစံ မှားယွင်းနေပါသည်" without parsing Google's underlying `API_KEY_INVALID` payload
- **Error Type**: #39 API Contract Error / #23 Tool Result Misinterpretation Error
- **Severity**: Medium
- **Root Cause**: Google Generative Language REST API returns HTTP 400 (`INVALID_ARGUMENT` with `reason: "API_KEY_INVALID"`) instead of HTTP 401/403 when an invalid key is provided. The app previously did not inspect the response error body JSON.
- **Recovery Attempt**: Implemented `extractErrorMessage(e: HttpException)` in `GeminiRemoteApiService.kt` to extract `error.message` and `details[].reason`, and sanitized whitespace/quotes.
- **Recovery Result**: PASS
- **Lesson Learned**: Always parse Google REST API's error response body (`errorBody()?.string()`) because Google frequently returns HTTP 400 with specific sub-reasons (`API_KEY_INVALID`).
- **Do Not Reuse**: Ignoring HTTP error response bodies or relying only on HTTP status code integers.
- **Verification**: Verified using `compile_applet`.

---

### [ER-003]
- **Error ID**: ER-003
- **Date / Time**: 2026-09-27T05:22:00-07:00
- **Project / Module**: app (GeminiRemoteApiService.kt / Network & Geo-location)
- **Task**: Connect to Google Gemini API on mobile device from Myanmar network
- **Expected Result**: API request succeeds without geographical restrictions
- **Actual Result**: `HTTP 400 Bad Request` with message: `"User location is not supported for the API use"`
- **Error Type**: #51 Network Reality Error / #20 Localization Error / #47 Environment Mismatch Error
- **Severity**: High
- **Technical Cause**: Google Generative Language API blocks requests originating from Myanmar IP ranges.
- **Resource / Source Limitation**: Myanmar ISP / Mobile Data IP routing.
- **Real-World / Practical Mismatch**: User's mobile VPN disconnected during testing over cellular 4G data.
- **Recovery Attempt**: Handled `"User location is not supported"` in `extractErrorMessage()` with clear guidance to enable device VPN.
- **Recovery Result**: PASS
- **Lesson Learned**: Gemini API in Myanmar requires an active VPN connection; client apps must provide explicit error guidance when geo-blocked.
- **Do Not Reuse**: Attempting direct connection on Myanmar IP without reminding the user about VPN.
- **Verification**: Verified using `compile_applet`.

---

### [VERIFICATION-004]
- **Verification ID**: VR-004
- **Date / Time**: 2026-10-01T19:28:00-07:00
- **Project / Module**: app (Room Database, KSP, BleGattManager, NecProtocolEncoder, ProntoHexConverter, FeedbackHelper, UiEvent Channel)
- **Task**: Implement and verify all 7 missing components requested by user
- **Expected Result**: Project compiles and unit tests pass with Room 2.6.1 + KSP 2.0.21-1.0.28, Pronto Hex, BLE auto-reconnect, and UI event channel
- **Actual Result**: `BUILD SUCCESSFUL` across both `compile_applet` and `gradle testDebugUnitTest`
- **Result**: PASS
- **Status**: Resolved
- **Verification**: Verified using `compile_applet` and `gradle :app:testDebugUnitTest`.

---

### [VERIFICATION-005]
- **Verification ID**: VR-005
- **Date / Time**: 2026-10-01T20:13:00-07:00
- **Project / Module**: app (RobustBluetoothManager: SPP Reflection, GATT Error 133, MTU 512, Exponential Backoff, Mutex Queue)
- **Task**: Inspect, verify, and integrate `RobustBluetoothManager.kt` into `RemoteViewModel` and test suite
- **Expected Result**: Clean compilation with backward-compatible deprecated BLE write method and passing Robolectric unit tests
- **Actual Result**: `BUILD SUCCESSFUL` across `compile_applet` and `gradle :app:testDebugUnitTest` (Robolectric lifecycle test passed)
- **Result**: PASS
- **Status**: Resolved
- **Verification**: Verified using `compile_applet` and `gradle :app:testDebugUnitTest`.

---

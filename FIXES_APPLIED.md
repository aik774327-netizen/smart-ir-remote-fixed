# Fixes Applied (2026-10-17)

## Critical Fixes

### 1. AI-synthesized IR codes now flagged (Honesty)
- Added `isAiSynthesized: Boolean = false` to `IrSignalPattern`
- `NecProtocolEncoder.parseCustomHex()` now marks codes as `isAiSynthesized = true`
- `encode32BitWord()` accepts `isAiSynthesized` parameter
- Updated `equals()` and `hashCode()` to include the new field
- UI should warn user before transmitting codes where `isAiSynthesized == true`

### 2. Duplicate Gemini API documented
- Added warning comment to `com.kyawzay.blueconnect.data.remote.GeminiRemoteApiService`
- Documents that it's a duplicate of `com.example.data.api.GeminiRemoteApiService`
- Security note about API key in URL (ensure never logged)

## Medium Fixes

### 3. Gemini model updated to 3.8-live
- Changed from `gemini-2.5-flash` to `gemini-3.8-live` in both API services
- Per official Google docs: https://ai.google.dev/gemini-api/docs/models/gemini-3.8-live

### 4. Custom profile persistence limitation documented
- Added FIXME comment in `ApplianceRepositoryImpl`
- Documents that custom profiles are in-memory only
- Provides guidance for Room integration

## Not Fixed (Platform Limitations)
- namespace = "com.example" (AI Studio platform requirement)

## Dead Code Removal (2026-10-17)

### Deleted Files (verified no usages):
1. `com/example/data/repository/RepositoriesImpl.kt` — dead (only self-reference)
2. `com/example/domain/usecase/UseCases.kt` — dead (no imports)
3. `com/example/domain/repository/Repositories.kt` — dead interfaces (no implementations used)
4. `com/example/data/hardware/ConsumerIrTransmitterImpl.kt` — dead (only used by #1)
5. `com/example/data/hardware/FeedbackHelper.kt` — dead (no usages)
6. `com/kyawzay/blueconnect/viewmodel/BaseRemoteViewModel.kt` — dead (never extended)

### NOT Deleted (correction to audit):
The following "duplicates" are BOTH actively used and were NOT deleted:
- `com.example.data.preset.NecProtocolEncoder` → used by ManualHexDialog.kt (active UI)
- `com.kyawzay.blueconnect.bluetooth.ir.NecProtocolEncoder` → used by RemoteViewModel
- `com.example.data.api.GeminiRemoteApiService` → used by RemoteViewModel
- `com.kyawzay.blueconnect.data.remote.GeminiRemoteApiService` → used by blueconnect UseCase
- `ProntoHexConverter` → used by ConsumerIrTransmitterImpl + RemoteViewModel

These are parallel implementations, not simple duplicates. Unifying them requires
refactoring the call sites — deferred pending user approval.

## UI Safety Improvements (2026-10-17)

### ManualHexDialog.kt
- Added hex format validation for NEC 32-bit (0x + 1-8 hex digits)
- Added Pronto hex validation (0000 prefix, min 4 words, 4-digit hex)
- Shows inline error message in Burmese on invalid input
- Blocks transmission until input is valid
- Added orange warning: manually entered codes are unverified

### AiAssistantDialog.kt
- Added orange warning: AI-synthesized codes are unverified,
  recommend using verified preset database codes first

## Unification & Persistence (2026-10-17)

### Encoder Unification
- `com.example.data.preset.NecProtocolEncoder` now delegates raw pattern
  generation to `com.kyawzay.blueconnect.bluetooth.ir.NecProtocolEncoder`
  (single source of truth for NEC timing). Wrapper preserves IrSignalPattern
  domain object with isAiSynthesized flag.

### Room Persistence (verified already implemented)
- `RemoteViewModel` line 340: AI-synthesized codes saved via `dao.insertIrCode()`
- `RemoteViewModel` line 65: codes loaded via `dao.getAllCodes().collect`
- Custom profiles ARE persisted via Room (ApplianceDatabase).

## Settings & Permissions Fixes (2026-10-18)

### #14 Haptic/Sound Persistence
- Added `loadFeedbackSettings()` / `saveFeedbackSettings()` in RemoteViewModel
- Settings saved to SharedPreferences `smart_remote_prefs` (keys: `haptic_enabled`, `sound_enabled`)
- Loaded on init, saved on every toggle

### #15 Permission Denial Guidance
- Added `showPermissionGuide` to RemoteUiState
- Added `onBluetoothPermissionDenied()` / `dismissPermissionGuide()` to RemoteViewModel
- MainActivity permission launcher now calls `onBluetoothPermissionDenied()` on denial
- New `PermissionGuideDialog.kt`: guides user to Settings → Apps → Permissions
  with direct "Setting ဖွင့်မည်" button (opens app settings via Intent)
- Wired into RemoteScreen dialogs section

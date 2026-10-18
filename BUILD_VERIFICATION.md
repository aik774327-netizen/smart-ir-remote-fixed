# Build Verification

## Applied fixes

- Removed the hard-coded Gemini API key from `app/build.gradle.kts`; the key must come from a local `.env` file or `GEMINI_API_KEY` environment variable.
- Added `.gitignore` entries for local secrets and generated Android/Gradle files.
- Corrected the NEC encoder to transmit the 32-bit word LSB-first.
- Added validation for invalid NEC hex input.
- Restricted Pronto transmission to raw `0000` format and added sequence-length/frequency validation.
- Removed recursive Room database initialization from the seed callback; seed rows are inserted using the callback's SQLite database handle.
- Added Android 12+ Bluetooth runtime permission requests, with location permission handling for older Android versions.
- Added Gradle 8.9 wrapper files for reproducible builds.

## Verification

Command:

```text
./gradlew clean testDebugUnitTest assembleDebug --no-daemon
```

Result: **BUILD SUCCESSFUL**

The build completed with deprecation warnings from existing Android/Compose APIs and a non-fatal native-library strip warning. No compile or unit-test failure remained.

## Security note

The previously distributed API key must be revoked/rotated in Google AI Studio or Google Cloud. This source package intentionally contains no usable fallback key.

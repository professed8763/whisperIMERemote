# WhisperIME Remote - Development Log

## Project Overview

This is a fork of [woheller69/whisperIME](https://github.com/woheller69/whisperIME) that adds **remote Whisper API support**. Instead of requiring a 435 MB on-device TFLite model, the app can send audio to cloud-based Whisper-compatible APIs (Groq, OpenAI, or custom endpoints) for transcription.

**Branch:** `claude/remote-whisper-ime-oDV8V` (based on `master`)

## What Was Built

### Core Feature: Remote API Engine

A new `WhisperEngineRemote` class that implements the existing `WhisperEngine` interface, making it a drop-in replacement for the local TFLite engine. The remote engine:

- Records audio as PCM16, converts to WAV using `WavUtil`
- Sends multipart HTTP requests to Whisper-compatible `/v1/audio/transcriptions` and `/v1/audio/translations` endpoints
- Parses JSON responses using `ApiResponseParser`
- Supports language selection and translate-to-English mode

### New Files Created

| File | Purpose |
|------|---------|
| `WhisperEngineRemote.java` | Remote API engine (OkHttp-based) |
| `ApiEndpointBuilder.java` | URL builder for Groq/OpenAI/custom endpoints |
| `ApiResponseParser.java` | JSON response parser for API results |
| `WavUtil.java` | PCM16 to WAV conversion utility |
| `ApiSettingsActivity.java` | Settings screen for API configuration |
| `activity_api_settings.xml` | Layout for API settings screen |

### Modified Files

| File | Changes |
|------|---------|
| `Whisper.java` | Added remote mode constructor, `isRemoteMode()` check, `initRemote()` method |
| `WhisperInputMethodService.java` | Added remote mode path in `onStartInputView()`, `initModelRemote()` |
| `WhisperRecognitionService.java` | Added remote mode support for system-wide voice input |
| `WhisperRecognizeActivity.java` | Added remote mode support for intent-based recognition |
| `MainActivity.java` | UI changes: hide model spinner in remote mode, show API settings button |
| `DownloadActivity.kt` | Skip model download when remote mode is configured, added "Configure Remote API" button |
| `AndroidManifest.xml` | Registered `ApiSettingsActivity` |
| `strings.xml` | Added remote API-related strings |
| `build.gradle` | Added OkHttp + JSON dependencies, signed release config |

### Provider Presets

| Provider | Base URL | Default Model |
|----------|----------|---------------|
| Groq | `https://api.groq.com/openai` | `whisper-large-v3-turbo` |
| OpenAI | `https://api.openai.com` | `whisper-1` |
| Custom | User-defined | User-defined |

## CI/CD Pipeline

### Build Workflow (`.github/workflows/build.yml`)
- Triggers on push to the feature branch
- Sets up JDK 17, Android SDK
- Runs unit tests (`./gradlew testDebugUnitTest`)
- Runs lint (`./gradlew lintDebug`)
- Builds debug APK (`./gradlew assembleDebug`)
- Supports Groq API integration tests when `GROQ_API_KEY` secret is set

### Release Workflow (`.github/workflows/release.yml`)
- Triggers on push to `master` branch
- Builds signed release APK using keystore from GitHub secrets
- Creates a GitHub Release with the signed APK attached
- Required secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`

## Tests

### Unit Tests
| Test File | Coverage |
|-----------|----------|
| `ApiEndpointBuilderTest.java` | URL building, provider presets, validation |
| `ApiResponseParserTest.java` | JSON parsing, error handling, edge cases |
| `WavUtilTest.java` | WAV header generation, PCM conversion |
| `InputLangTest.java` | Language code/token mapping |
| `WhisperResultTest.java` | Result object behavior |
| `WhisperInputMethodServiceTest.java` | IME service basic tests (Robolectric) |

### Integration Tests
| Test File | Notes |
|-----------|-------|
| `GroqApiIntegrationTest.java` | Real API calls to Groq; skipped when `GROQ_API_KEY` env var not set. Note: Groq doesn't support `/translations` endpoint. |

## Architecture Decisions

1. **Interface-based engine swap**: `WhisperEngineRemote` implements the same `WhisperEngine` interface as `WhisperEngineJava`, so the `Whisper` class just picks which engine to instantiate based on a boolean flag.

2. **SharedPreferences for config**: API settings (key, endpoint, model, provider) stored in default SharedPreferences, consistent with the existing app pattern.

3. **Remote mode check**: `Whisper.isRemoteMode(context)` checks both `useRemoteApi` boolean AND that `apiKey` is non-empty, preventing accidental remote mode with no credentials.

4. **Download skip**: When remote mode is active, `DownloadActivity.onResume()` skips straight to `MainActivity`, so users don't need to download the 435 MB model at all.

5. **Pure Java utilities**: `ApiEndpointBuilder`, `ApiResponseParser`, and `WavUtil` have no Android dependencies, making them fully unit-testable without Robolectric.

## How to Set Up Remote Mode

1. Install the APK
2. On the download screen, tap **"Configure Remote API"**
3. Toggle **"Use Remote API"** on
4. Select a provider (Groq recommended - free tier available)
5. Enter your API key
6. Tap **Save Settings**
7. Go back - the app will skip model download and go straight to the main screen

## Signing Configuration

Release builds use a keystore stored as a base64-encoded GitHub secret. The `build.gradle` signing config reads from environment variables:
- `KEYSTORE_FILE` - path to decoded keystore file
- `KEYSTORE_PASSWORD` - keystore password
- `KEY_ALIAS` - key alias name
- `KEY_PASSWORD` - key password

## Commit History (oldest first)

1. `c8ff428` - Add remote Whisper API support (Groq, OpenAI, custom endpoints)
2. `15aea70` - Add build artifacts and local.properties to .gitignore
3. `283e302` - Improve CI: install SDK components, add stacktrace, upload test results
4. `0acc8cc` through `51e93c8` - CI debugging iterations (error capture, test output)
5. `d6e2362` - Add Robolectric tests, unit tests, CI lint step
6. `77c12f7` - Fix compilation error
7. `18694a6` - Fix test failures
8. `4a35d8a` - Add Groq API integration tests
9. `00d8596` - Fix translation test (Groq limitation)
10. `49ea8c9` - Add signed release build workflow
11. `1807262` - Auto-release signed APK on merge
12. `acbd38a` - Fix release workflow trigger branch

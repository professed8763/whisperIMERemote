# CLAUDE.md - Project Context for Claude Code

## Project
WhisperIME Remote - Android IME (Input Method Editor) that uses remote Whisper APIs (Groq, OpenAI, custom) for speech-to-text instead of on-device models. Fork of [woheller69/whisperIME](https://github.com/woheller69/whisperIME).

**Repo:** `professed8763/whisperIMERemote`
**Default branch:** `master`

## Build & Test Commands
```bash
./gradlew testDebugUnitTest      # Run all unit tests
./gradlew lintDebug              # Run lint
./gradlew assembleDebug          # Build debug APK
```

- Tests require `includeAndroidResources = true` (already configured)
- Robolectric tests use SDK 35 / Java 11
- Integration tests in `GroqApiIntegrationTest` are skipped unless `GROQ_API_KEY` env var is set

## Architecture
- **Namespace:** `com.whispertflite` (package name: `org.woheller69.whisperremote`)
- **Language:** Java primary, some Kotlin (DownloadActivity)
- **Min SDK:** 28, **Target SDK:** 35
- **Key pattern:** `WhisperEngine` interface with two implementations:
  - `WhisperEngineJava` - local TFLite inference
  - `WhisperEngineRemote` - remote API calls via OkHttp
- **Config storage:** SharedPreferences (keys: `useRemoteApi`, `apiKey`, `apiBaseUrl`, `apiModel`, `apiProvider`, `customDictionary`)
- **Remote mode check:** `Whisper.isRemoteMode(context)` requires both `useRemoteApi=true` AND non-empty `apiKey`

## Key Source Paths
- `app/src/main/java/com/whispertflite/` - main source
  - `ApiSettingsActivity.java` - API config UI (provider, key, model, custom dictionary)
  - `engine/WhisperEngineRemote.java` - remote API engine + `buildPrompt()` for custom dictionary
  - `engine/ApiEndpointBuilder.java` - URL construction for providers
  - `engine/ApiResponseParser.java` - JSON response parsing
  - `engine/WavUtil.java` - PCM16 to WAV conversion
- `app/src/test/java/com/whispertflite/` - tests
- `app/src/main/res/layout/` - XML layouts
- `.github/workflows/` - CI (build.yml) and release (release.yml)

## Features Implemented
1. **Remote Whisper API** - Groq/OpenAI/custom endpoint support
2. **Custom dictionary** - comma-separated words sent as Whisper `prompt` parameter for better recognition of names/terms
3. **Signed release builds** - via GitHub Actions using keystore secrets

## CI/CD
- **build.yml:** Runs on push to feature branches - tests, lint, debug build
- **release.yml:** Runs on push to `master` - builds signed APK, creates GitHub Release
- Required secrets for release: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`

## Git Workflow Notes
- PRs are squash-merged into `master`
- After squash merge, feature branches must be rebased onto updated `master` (old branch history diverges)
- Always fetch and rebase from `master` before starting new work

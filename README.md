# Whisper Remote - Voice recognition based on Whisper

> **This is a fork of [woheller69/whisperIME](https://github.com/woheller69/whisperIME)** with added support for remote Whisper-compatible APIs.

## What's different from the original?

The original WhisperIME runs Whisper entirely on-device using TFLite models (~435 MB download). This fork adds an **optional remote API mode** that sends audio to cloud-based Whisper APIs instead, which means:

- **No large model download required** - skip the 435 MB download entirely when using remote mode
- **Faster and more accurate transcription** - cloud APIs use larger Whisper models (e.g. `whisper-large-v3-turbo`) that would be too large to run on most phones
- **Multiple provider support** - built-in presets for Groq (free tier available) and OpenAI, plus custom endpoint support for any Whisper-compatible API
- **Local mode still works** - the original on-device TFLite mode is fully preserved; remote API is opt-in

### Supported providers

| Provider | Model | Notes |
|----------|-------|-------|
| **Groq** | `whisper-large-v3-turbo` | Free tier available, very fast |
| **OpenAI** | `whisper-1` | Paid API |
| **Custom** | Any | Any Whisper-compatible endpoint (e.g. self-hosted) |

### Remote API setup

1. Install the app
2. Tap **"Configure Remote API"** on the download screen
3. Toggle **"Use Remote API"** on
4. Select a provider and enter your API key
5. Tap **Save Settings**

The app will skip the model download and use the remote API for all transcription (IME, standalone, and system-wide voice input).

For detailed technical documentation, see [DEVELOPMENT.md](DEVELOPMENT.md).

---

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/01.png" width="150"/> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/02.png" width="150"/>

Whisper is an input method editor (IME) that leverages voice recognition technology based on the Whisper engine. 
It offers a seamless user experience, functioning both as a standalone application and an integrated IME that can be activated, e.g. via the microphone button in HeliBoard.
As a standalone app Whisper can also translate any supported language to English.

Besides providing an IME, whisper can also be selected as system-wide voice input (RecognitionService) and it supports calls via intent (RecognizerIntent.ACTION_RECOGNIZE_SPEECH).

## Initial Setup

Upon launching Whisper for the first time, the app will download the necessary Whisper models (~435 MB) from Hugging Face. 
Please note that this is the only instance where internet permission is required. 
Once the models are downloaded, voice recognition works entirely offline, ensuring your privacy and convenience.

Please note that for use as voice input (not as IME) there is a separate settings activity which can be accessed from Android settings 
(System > Languages > Speech > Voice Input). There you can activate the app as voice input and then click the settings button.
In settings you can then select the model for voice input.

If after installation you do not find Whisper as voice input or only see a limited list (hard-coded ones like Google/Samsung)
- enable USB debugging
- type adb shell settings put secure voice_recognition_service org.woheller69.whisper/com.whispertflite.WhisperRecognitionService

## Model Selection

Whisper offers two models to choose from: a compact English-only model that prioritizes speed and a more comprehensive multi-lingual model that, while much slower, 
supports a broader range of languages. Select your preferred model within the app, and it will be applied consistently across all uses, including when used as an IME.

## Using Whisper

To get the most out of Whisper, follow these simple tips:

- Press and hold the button while speaking or use automatic mode where available
- Pause briefly before starting to speak
- Speak clearly, loudly, and at a moderate pace
- Please note that there is a limit of 30s for each recording

By following these guidelines, you'll be able to enjoy accurate and efficient voice recognition with Whisper.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="75">](https://f-droid.org/de/packages/org.woheller69.whisper/) [<img src="https://www.openapk.net/images/openapk-badge.png" height="75">]( https://www.openapk.net/whisper/org.woheller69.whisper/)


## Contribute

For translations use https://toolate.othing.xyz/projects/whisperime/

# License
This work is licensed under MIT license, © woheller69

- This app is based on the [Whisper-Android project](https://github.com/vilassn/whisper_android), published under MIT license
- It uses [OpenAI Whisper](https://github.com/openai/whisper) published under MIT license. Details on Whisper are found [here](https://arxiv.org/abs/2212.04356).
- It uses [Android VAD](https://github.com/gkonovalov/android-vad), which is published under MIT license
- It uses [Opencc4j](https://github.com/houbb/opencc4j), for Chinese conversions, published under Apache-2.0 license
- At first start it downloads the Whisper TFLite models from [Hugging Face](https://huggingface.co/DocWolle/whisper_tflite_models), which is published under MIT license

# OTHER APPS

| **RadarWeather** | **Gas Prices** | **Smart Eggtimer** |
|:---:|:---:|:--:|
| [<img src="https://github.com/woheller69/weather/blob/main/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.weather/) | [<img src="https://github.com/woheller69/spritpreise/blob/main/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.spritpreise/) | [<img src="https://github.com/woheller69/eggtimer/blob/main/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.eggtimer/) |
| **Bubble** | **hEARtest** | **GPS Cockpit** |
| [<img src="https://github.com/woheller69/Level/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.level/) | [<img src="https://github.com/woheller69/audiometry/blob/new/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.audiometry/) | [<img src="https://github.com/woheller69/gpscockpit/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.gpscockpit/) |
| **Audio Analyzer** | **LavSeeker** | **TimeLapseCam** |
| [<img src="https://github.com/woheller69/audio-analyzer-for-android/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.audio_analyzer_for_android/) |[<img src="https://github.com/woheller69/lavatories/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.lavatories/) | [<img src="https://github.com/woheller69/TimeLapseCamera/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.TimeLapseCam/) |
| **Arity** | **Cirrus** | **solXpect** |
| [<img src="https://github.com/woheller69/arity/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.arity/) | [<img src="https://github.com/woheller69/omweather/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.omweather/) | [<img src="https://github.com/woheller69/solXpect/blob/main/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.solxpect/) |
| **gptAssist** | **dumpSeeker** | **huggingAssist** |
| [<img src="https://github.com/woheller69/gptassist/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.gptassist/) | [<img src="https://github.com/woheller69/dumpseeker/blob/main/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.dumpseeker/) | [<img src="https://github.com/woheller69/huggingassist/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.hugassist/) |
| **FREE Browser** | **whoBIRD** | **PeakOrama** |
| [<img src="https://github.com/woheller69/browser/blob/newmaster/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.browser/) | [<img src="https://github.com/woheller69/whoBIRD/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.whobird/) | [<img src="https://github.com/woheller69/PeakOrama/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.PeakOrama/) |
| **Whisper** | **Seamless** | **SherpaTTS** |
| [<img src="https://github.com/woheller69/whisperIME/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.whisper/) | [<img src="https://github.com/woheller69/seamless/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.seemless/) | [<img src="https://github.com/woheller69/ttsengine/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.ttsengine/) |

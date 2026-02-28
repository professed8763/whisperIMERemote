package com.whispertflite.engine;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.whispertflite.asr.RecordBuffer;
import com.whispertflite.asr.Whisper;
import com.whispertflite.asr.WhisperResult;
import com.whispertflite.utils.ApiEndpointBuilder;
import com.whispertflite.utils.ApiResponseParser;
import com.whispertflite.utils.InputLang;
import com.whispertflite.utils.WavUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Remote Whisper engine that sends audio to a Whisper-compatible API endpoint.
 * Implements the same WhisperEngine interface as WhisperEngineJava, so it can
 * be used as a drop-in replacement.
 */
public class WhisperEngineRemote implements WhisperEngine {
    private static final String TAG = "WhisperEngineRemote";

    private final Context mContext;
    private boolean mIsInitialized = false;
    private OkHttpClient mClient;

    private String mApiKey;
    private String mEndpoint;
    private String mModel;

    public WhisperEngineRemote(Context context) {
        mContext = context;
    }

    @Override
    public boolean isInitialized() {
        return mIsInitialized;
    }

    @Override
    public void initialize(String modelPath, String vocabPath, boolean multilingual) throws IOException {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        mApiKey = sp.getString("apiKey", "");
        mEndpoint = sp.getString("apiEndpoint", ApiEndpointBuilder.OPENAI_BASE_URL);
        mModel = sp.getString("apiModel", ApiEndpointBuilder.OPENAI_DEFAULT_MODEL);

        String validationError = ApiEndpointBuilder.validateSettings(mApiKey, mEndpoint, mModel);
        if (validationError != null) {
            throw new IOException("Invalid API settings: " + validationError);
        }

        mClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        mIsInitialized = true;
        Log.d(TAG, "Remote engine initialized. Endpoint: " + mEndpoint + ", Model: " + mModel);
    }

    @Override
    public void deinitialize() {
        if (mClient != null) {
            mClient.dispatcher().cancelAll();
            mClient = null;
        }
        mIsInitialized = false;
    }

    @Override
    public WhisperResult processRecordBuffer(Whisper.Action action, int langToken) {
        try {
            // Get raw PCM16 audio from the record buffer
            byte[] pcmData = RecordBuffer.getOutputBuffer();
            if (pcmData == null || pcmData.length == 0) {
                Log.e(TAG, "No audio data in record buffer");
                return new WhisperResult("", "", action);
            }

            // Convert PCM to WAV
            byte[] wavData = WavUtil.pcmToWav16kMono(pcmData);
            Log.d(TAG, "WAV data size: " + wavData.length + " bytes (" + pcmData.length + " PCM bytes)");

            // Determine the API endpoint based on action
            String url;
            if (action == Whisper.Action.TRANSLATE) {
                url = ApiEndpointBuilder.buildTranslationUrl(mEndpoint);
            } else {
                url = ApiEndpointBuilder.buildTranscriptionUrl(mEndpoint);
            }

            // Build multipart request body
            MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "audio.wav",
                            RequestBody.create(wavData, MediaType.parse("audio/wav")))
                    .addFormDataPart("model", mModel)
                    .addFormDataPart("response_format", "json");

            // Add language parameter if specified (not "auto")
            if (langToken != -1) {
                String langCode = InputLang.getLanguageCodeById(InputLang.getLangList(), langToken);
                if (langCode != null && !langCode.isEmpty()) {
                    bodyBuilder.addFormDataPart("language", langCode);
                    Log.d(TAG, "Language: " + langCode);
                }
            }

            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + mApiKey)
                    .post(bodyBuilder.build())
                    .build();

            Log.d(TAG, "Sending request to: " + url);

            // Execute synchronously (we're already on a background thread)
            try (Response response = mClient.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Response code: " + response.code() + ", body length: " + responseBody.length());

                if (!response.isSuccessful()) {
                    ApiResponseParser.ApiResult errorResult = ApiResponseParser.parse(responseBody);
                    String errorMsg = errorResult.isSuccess() ?
                            "HTTP " + response.code() :
                            errorResult.getError();
                    Log.e(TAG, "API error: " + errorMsg);
                    return new WhisperResult("[Error: " + errorMsg + "]", "", action);
                }

                ApiResponseParser.ApiResult result = ApiResponseParser.parse(responseBody);
                if (result.isSuccess()) {
                    // Determine language: the API might not return it, so use what we sent
                    String language = "";
                    if (langToken != -1) {
                        language = InputLang.getLanguageCodeById(InputLang.getLangList(), langToken);
                    }
                    Log.d(TAG, "Transcription result: " + result.getText());
                    return new WhisperResult(result.getText(), language, action);
                } else {
                    Log.e(TAG, "Parse error: " + result.getError());
                    return new WhisperResult("[Error: " + result.getError() + "]", "", action);
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "Network error during transcription", e);
            return new WhisperResult("[Error: " + e.getMessage() + "]", "", action);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during transcription", e);
            return new WhisperResult("[Error: " + e.getMessage() + "]", "", action);
        }
    }
}

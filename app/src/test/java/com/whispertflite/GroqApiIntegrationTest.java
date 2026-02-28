package com.whispertflite;

import static org.junit.Assert.*;

import com.whispertflite.utils.ApiEndpointBuilder;
import com.whispertflite.utils.ApiResponseParser;
import com.whispertflite.utils.WavUtil;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Integration tests that call the real Groq Whisper API.
 * Skipped automatically when GROQ_API_KEY env var is not set.
 *
 * These tests exercise the same code path as WhisperEngineRemote:
 *   PCM audio → WavUtil → OkHttp multipart request → Groq API → ApiResponseParser
 */
public class GroqApiIntegrationTest {

    private String apiKey;
    private OkHttpClient client;

    @Before
    public void setUp() {
        apiKey = System.getenv("GROQ_API_KEY");
        Assume.assumeTrue("Skipping: GROQ_API_KEY not set", apiKey != null && !apiKey.isEmpty());

        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Generate silent PCM audio (all zeros) to use as test input.
     * The API should accept it and return an empty or near-empty transcription.
     */
    private byte[] generateSilentPcm(float durationSeconds) {
        int sampleRate = 16000;
        int bytesPerSample = 2; // 16-bit
        int numSamples = (int) (sampleRate * durationSeconds);
        return new byte[numSamples * bytesPerSample]; // all zeros = silence
    }

    // --- Transcription endpoint tests ---

    @Test
    public void transcribeSilenceReturnsValidResponse() throws IOException {
        byte[] pcmData = generateSilentPcm(1.0f);
        byte[] wavData = WavUtil.pcmToWav16kMono(pcmData);

        String url = ApiEndpointBuilder.buildTranscriptionUrl(ApiEndpointBuilder.GROQ_BASE_URL);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + apiKey)
                .post(new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "audio.wav",
                                RequestBody.create(wavData, MediaType.parse("audio/wav")))
                        .addFormDataPart("model", ApiEndpointBuilder.GROQ_DEFAULT_MODEL)
                        .addFormDataPart("response_format", "json")
                        .build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertTrue("HTTP response should be successful, got: " + response.code(),
                    response.isSuccessful());

            String body = response.body() != null ? response.body().string() : "";
            assertFalse("Response body should not be empty", body.isEmpty());

            ApiResponseParser.ApiResult result = ApiResponseParser.parse(body);
            assertTrue("Response should parse successfully: " + result.getError(),
                    result.isSuccess());
            assertNotNull("Transcription text should not be null", result.getText());
        }
    }

    @Test
    public void transcribeWithLanguageParameter() throws IOException {
        byte[] pcmData = generateSilentPcm(1.0f);
        byte[] wavData = WavUtil.pcmToWav16kMono(pcmData);

        String url = ApiEndpointBuilder.buildTranscriptionUrl(ApiEndpointBuilder.GROQ_BASE_URL);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + apiKey)
                .post(new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "audio.wav",
                                RequestBody.create(wavData, MediaType.parse("audio/wav")))
                        .addFormDataPart("model", ApiEndpointBuilder.GROQ_DEFAULT_MODEL)
                        .addFormDataPart("response_format", "json")
                        .addFormDataPart("language", "en")
                        .build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertTrue("HTTP response should be successful with language param",
                    response.isSuccessful());

            String body = response.body().string();
            ApiResponseParser.ApiResult result = ApiResponseParser.parse(body);
            assertTrue("Response should parse successfully with language param",
                    result.isSuccess());
        }
    }

    // --- Translation endpoint tests ---

    @Test
    public void translationEndpointReturnsValidResponse() throws IOException {
        byte[] pcmData = generateSilentPcm(1.0f);
        byte[] wavData = WavUtil.pcmToWav16kMono(pcmData);

        String url = ApiEndpointBuilder.buildTranslationUrl(ApiEndpointBuilder.GROQ_BASE_URL);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + apiKey)
                .post(new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "audio.wav",
                                RequestBody.create(wavData, MediaType.parse("audio/wav")))
                        .addFormDataPart("model", ApiEndpointBuilder.GROQ_DEFAULT_MODEL)
                        .addFormDataPart("response_format", "json")
                        .build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertTrue("Translation endpoint should return successful response",
                    response.isSuccessful());

            String body = response.body().string();
            ApiResponseParser.ApiResult result = ApiResponseParser.parse(body);
            assertTrue("Translation response should parse successfully",
                    result.isSuccess());
        }
    }

    // --- Error handling tests ---

    @Test
    public void invalidApiKeyReturnsError() throws IOException {
        byte[] pcmData = generateSilentPcm(1.0f);
        byte[] wavData = WavUtil.pcmToWav16kMono(pcmData);

        String url = ApiEndpointBuilder.buildTranscriptionUrl(ApiEndpointBuilder.GROQ_BASE_URL);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer invalid_key_12345")
                .post(new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "audio.wav",
                                RequestBody.create(wavData, MediaType.parse("audio/wav")))
                        .addFormDataPart("model", ApiEndpointBuilder.GROQ_DEFAULT_MODEL)
                        .addFormDataPart("response_format", "json")
                        .build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertFalse("Invalid API key should return unsuccessful response",
                    response.isSuccessful());
            assertEquals("Should return 401 Unauthorized", 401, response.code());

            String body = response.body().string();
            ApiResponseParser.ApiResult result = ApiResponseParser.parse(body);
            assertFalse("Error response should not parse as success",
                    result.isSuccess());
            assertNotNull("Error message should not be null", result.getError());
        }
    }

    @Test
    public void invalidModelReturnsError() throws IOException {
        byte[] pcmData = generateSilentPcm(1.0f);
        byte[] wavData = WavUtil.pcmToWav16kMono(pcmData);

        String url = ApiEndpointBuilder.buildTranscriptionUrl(ApiEndpointBuilder.GROQ_BASE_URL);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + apiKey)
                .post(new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "audio.wav",
                                RequestBody.create(wavData, MediaType.parse("audio/wav")))
                        .addFormDataPart("model", "nonexistent-model-xyz")
                        .addFormDataPart("response_format", "json")
                        .build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertFalse("Invalid model should return unsuccessful response",
                    response.isSuccessful());

            String body = response.body().string();
            ApiResponseParser.ApiResult result = ApiResponseParser.parse(body);
            assertFalse("Error response should not parse as success",
                    result.isSuccess());
        }
    }

    // --- WAV encoding verification ---

    @Test
    public void wavUtilProducesValidWavAcceptedByApi() throws IOException {
        // Generate 0.5 seconds of PCM audio with a simple tone pattern
        int sampleRate = 16000;
        int numSamples = sampleRate / 2; // 0.5 seconds
        byte[] pcmData = new byte[numSamples * 2];
        // Create a simple 440Hz tone
        for (int i = 0; i < numSamples; i++) {
            short sample = (short) (Short.MAX_VALUE * 0.5 * Math.sin(2 * Math.PI * 440 * i / sampleRate));
            pcmData[i * 2] = (byte) (sample & 0xFF);
            pcmData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        byte[] wavData = WavUtil.pcmToWav16kMono(pcmData);

        // Verify WAV structure
        assertEquals("WAV should start with RIFF", 'R', (char) wavData[0]);
        assertEquals("WAV should have WAVE marker", 'W', (char) wavData[8]);
        assertEquals("WAV header should be 44 bytes + PCM data",
                44 + pcmData.length, wavData.length);

        // Send to Groq API — this verifies our WAV encoding is accepted
        String url = ApiEndpointBuilder.buildTranscriptionUrl(ApiEndpointBuilder.GROQ_BASE_URL);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + apiKey)
                .post(new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "audio.wav",
                                RequestBody.create(wavData, MediaType.parse("audio/wav")))
                        .addFormDataPart("model", ApiEndpointBuilder.GROQ_DEFAULT_MODEL)
                        .addFormDataPart("response_format", "json")
                        .build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertTrue("API should accept our WAV encoding, got: " + response.code(),
                    response.isSuccessful());
        }
    }

    // --- URL building verification ---

    @Test
    public void endpointUrlsAreCorrect() {
        assertEquals("Transcription URL should be correct",
                "https://api.groq.com/openai/v1/audio/transcriptions",
                ApiEndpointBuilder.buildTranscriptionUrl(ApiEndpointBuilder.GROQ_BASE_URL));

        assertEquals("Translation URL should be correct",
                "https://api.groq.com/openai/v1/audio/translations",
                ApiEndpointBuilder.buildTranslationUrl(ApiEndpointBuilder.GROQ_BASE_URL));
    }
}

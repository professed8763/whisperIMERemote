package com.whispertflite;

import com.whispertflite.utils.ApiEndpointBuilder;

import org.junit.Test;

import static org.junit.Assert.*;

public class ApiEndpointBuilderTest {

    @Test
    public void testGroqTranscriptionUrl() {
        String url = ApiEndpointBuilder.buildTranscriptionUrl(ApiEndpointBuilder.GROQ_BASE_URL);
        assertEquals("https://api.groq.com/openai/v1/audio/transcriptions", url);
    }

    @Test
    public void testOpenaiTranscriptionUrl() {
        String url = ApiEndpointBuilder.buildTranscriptionUrl(ApiEndpointBuilder.OPENAI_BASE_URL);
        assertEquals("https://api.openai.com/v1/audio/transcriptions", url);
    }

    @Test
    public void testGroqTranslationUrl() {
        String url = ApiEndpointBuilder.buildTranslationUrl(ApiEndpointBuilder.GROQ_BASE_URL);
        assertEquals("https://api.groq.com/openai/v1/audio/translations", url);
    }

    @Test
    public void testOpenaiTranslationUrl() {
        String url = ApiEndpointBuilder.buildTranslationUrl(ApiEndpointBuilder.OPENAI_BASE_URL);
        assertEquals("https://api.openai.com/v1/audio/translations", url);
    }

    @Test
    public void testCustomEndpointWithTrailingSlash() {
        String url = ApiEndpointBuilder.buildTranscriptionUrl("https://my-server.com/");
        assertEquals("https://my-server.com/v1/audio/transcriptions", url);
    }

    @Test
    public void testCustomEndpointMultipleTrailingSlashes() {
        String url = ApiEndpointBuilder.buildTranscriptionUrl("https://my-server.com///");
        assertEquals("https://my-server.com/v1/audio/transcriptions", url);
    }

    @Test
    public void testCustomEndpointNoTrailingSlash() {
        String url = ApiEndpointBuilder.buildTranscriptionUrl("https://my-server.com");
        assertEquals("https://my-server.com/v1/audio/transcriptions", url);
    }

    @Test
    public void testNullEndpoint() {
        String url = ApiEndpointBuilder.buildTranscriptionUrl(null);
        assertEquals("/v1/audio/transcriptions", url);
    }

    @Test
    public void testEmptyEndpoint() {
        String url = ApiEndpointBuilder.buildTranscriptionUrl("");
        assertEquals("/v1/audio/transcriptions", url);
    }

    @Test
    public void testGetBaseUrlGroq() {
        assertEquals(ApiEndpointBuilder.GROQ_BASE_URL, ApiEndpointBuilder.getBaseUrl("groq"));
    }

    @Test
    public void testGetBaseUrlOpenai() {
        assertEquals(ApiEndpointBuilder.OPENAI_BASE_URL, ApiEndpointBuilder.getBaseUrl("openai"));
    }

    @Test
    public void testGetBaseUrlCustom() {
        assertEquals("", ApiEndpointBuilder.getBaseUrl("custom"));
    }

    @Test
    public void testGetDefaultModelGroq() {
        assertEquals("whisper-large-v3-turbo", ApiEndpointBuilder.getDefaultModel("groq"));
    }

    @Test
    public void testGetDefaultModelOpenai() {
        assertEquals("whisper-1", ApiEndpointBuilder.getDefaultModel("openai"));
    }

    @Test
    public void testValidateSettingsValid() {
        assertNull(ApiEndpointBuilder.validateSettings("sk-test", "https://api.groq.com/openai", "whisper-large-v3-turbo"));
    }

    @Test
    public void testValidateSettingsMissingApiKey() {
        String error = ApiEndpointBuilder.validateSettings("", "https://api.groq.com/openai", "whisper-large-v3-turbo");
        assertNotNull(error);
        assertTrue(error.contains("API key"));
    }

    @Test
    public void testValidateSettingsNullApiKey() {
        String error = ApiEndpointBuilder.validateSettings(null, "https://api.groq.com/openai", "whisper-large-v3-turbo");
        assertNotNull(error);
    }

    @Test
    public void testValidateSettingsMissingEndpoint() {
        String error = ApiEndpointBuilder.validateSettings("sk-test", "", "whisper-large-v3-turbo");
        assertNotNull(error);
        assertTrue(error.contains("endpoint"));
    }

    @Test
    public void testValidateSettingsMissingModel() {
        String error = ApiEndpointBuilder.validateSettings("sk-test", "https://api.groq.com/openai", "");
        assertNotNull(error);
        assertTrue(error.contains("Model"));
    }

    @Test
    public void testValidateSettingsBadProtocol() {
        String error = ApiEndpointBuilder.validateSettings("sk-test", "ftp://api.groq.com", "model");
        assertNotNull(error);
        assertTrue(error.contains("http"));
    }

    @Test
    public void testValidateSettingsHttpOk() {
        assertNull(ApiEndpointBuilder.validateSettings("sk-test", "http://localhost:8080", "model"));
    }
}

package com.whispertflite.utils;

/**
 * Builds Whisper-compatible API endpoint URLs from provider presets or custom configuration.
 * Pure Java, no Android dependencies — fully unit-testable.
 */
public class ApiEndpointBuilder {

    public static final String PROVIDER_GROQ = "groq";
    public static final String PROVIDER_OPENAI = "openai";
    public static final String PROVIDER_CUSTOM = "custom";

    // Default endpoints (base URLs, without /v1/audio/...)
    public static final String GROQ_BASE_URL = "https://api.groq.com/openai";
    public static final String OPENAI_BASE_URL = "https://api.openai.com";

    // Default model names
    public static final String GROQ_DEFAULT_MODEL = "whisper-large-v3-turbo";
    public static final String OPENAI_DEFAULT_MODEL = "whisper-1";

    /**
     * Returns the base URL for a given provider.
     */
    public static String getBaseUrl(String provider) {
        if (provider == null) return OPENAI_BASE_URL;
        switch (provider) {
            case PROVIDER_GROQ:
                return GROQ_BASE_URL;
            case PROVIDER_OPENAI:
                return OPENAI_BASE_URL;
            default:
                return "";
        }
    }

    /**
     * Returns the default model name for a given provider.
     */
    public static String getDefaultModel(String provider) {
        if (provider == null) return OPENAI_DEFAULT_MODEL;
        switch (provider) {
            case PROVIDER_GROQ:
                return GROQ_DEFAULT_MODEL;
            case PROVIDER_OPENAI:
                return OPENAI_DEFAULT_MODEL;
            default:
                return "";
        }
    }

    /**
     * Builds the full transcription endpoint URL.
     *
     * @param baseUrl The base URL (e.g. "https://api.groq.com/openai")
     * @return The full URL (e.g. "https://api.groq.com/openai/v1/audio/transcriptions")
     */
    public static String buildTranscriptionUrl(String baseUrl) {
        return buildUrl(baseUrl, "/v1/audio/transcriptions");
    }

    /**
     * Builds the full translation endpoint URL.
     *
     * @param baseUrl The base URL (e.g. "https://api.openai.com")
     * @return The full URL (e.g. "https://api.openai.com/v1/audio/translations")
     */
    public static String buildTranslationUrl(String baseUrl) {
        return buildUrl(baseUrl, "/v1/audio/translations");
    }

    /**
     * Combines a base URL and path, handling trailing/leading slashes.
     */
    static String buildUrl(String baseUrl, String path) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return path;
        }
        String base = baseUrl.trim();
        // Remove trailing slash from base
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        // Ensure path starts with /
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return base + path;
    }

    /**
     * Validates that the required API settings are present.
     *
     * @return null if valid, or an error message string if invalid
     */
    public static String validateSettings(String apiKey, String endpoint, String model) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "API key is required";
        }
        if (endpoint == null || endpoint.trim().isEmpty()) {
            return "API endpoint URL is required";
        }
        if (model == null || model.trim().isEmpty()) {
            return "Model name is required";
        }
        if (!endpoint.trim().startsWith("http://") && !endpoint.trim().startsWith("https://")) {
            return "Endpoint URL must start with http:// or https://";
        }
        return null; // Valid
    }
}

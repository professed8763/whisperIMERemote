package com.whispertflite.utils;

/**
 * Parses Whisper-compatible API JSON responses.
 * Pure Java, no Android dependencies — fully unit-testable.
 *
 * Handles two response formats:
 * - Success: {"text": "transcription result"}
 * - Error:   {"error": {"message": "Invalid API key", "type": "...", "code": "..."}}
 *
 * Uses simple string parsing to avoid adding a JSON library dependency.
 */
public class ApiResponseParser {

    public static class ApiResult {
        private final String text;
        private final String error;
        private final boolean success;

        private ApiResult(String text, String error, boolean success) {
            this.text = text;
            this.error = error;
            this.success = success;
        }

        public static ApiResult success(String text) {
            return new ApiResult(text, null, true);
        }

        public static ApiResult failure(String error) {
            return new ApiResult(null, error, false);
        }

        public String getText() { return text; }
        public String getError() { return error; }
        public boolean isSuccess() { return success; }
    }

    /**
     * Parses a Whisper API JSON response body.
     *
     * @param responseBody Raw JSON string from the API
     * @return ApiResult with either the transcription text or an error message
     */
    public static ApiResult parse(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return ApiResult.failure("Empty response from server");
        }

        String trimmed = responseBody.trim();

        // Check for error response
        if (trimmed.contains("\"error\"")) {
            String errorMsg = extractJsonString(trimmed, "message");
            if (errorMsg == null) {
                // Try to extract the error field directly if it's a string
                errorMsg = extractJsonString(trimmed, "error");
            }
            if (errorMsg == null) {
                errorMsg = "Unknown API error: " + trimmed;
            }
            return ApiResult.failure(errorMsg);
        }

        // Parse success response — extract "text" field
        String text = extractJsonString(trimmed, "text");
        if (text != null) {
            return ApiResult.success(text);
        }

        return ApiResult.failure("Could not parse response: " + trimmed);
    }

    /**
     * Extracts a string value for a given key from a JSON object.
     * Simple parser that handles escaped quotes.
     *
     * @param json JSON string
     * @param key  The key to look for
     * @return The string value, or null if not found
     */
    static String extractJsonString(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;

        // Find the colon after the key
        int colonIndex = json.indexOf(':', keyIndex + searchKey.length());
        if (colonIndex == -1) return null;

        // Find the opening quote of the value
        int openQuote = json.indexOf('"', colonIndex + 1);
        if (openQuote == -1) return null;

        // Find the closing quote, handling escaped quotes
        StringBuilder value = new StringBuilder();
        for (int i = openQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                if (next == '"') {
                    value.append('"');
                    i++;
                } else if (next == '\\') {
                    value.append('\\');
                    i++;
                } else if (next == 'n') {
                    value.append('\n');
                    i++;
                } else if (next == 't') {
                    value.append('\t');
                    i++;
                } else {
                    value.append(c);
                }
            } else if (c == '"') {
                return value.toString();
            } else {
                value.append(c);
            }
        }

        return null; // Unterminated string
    }
}

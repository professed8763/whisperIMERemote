package com.whispertflite;

import com.whispertflite.utils.ApiResponseParser;
import com.whispertflite.utils.ApiResponseParser.ApiResult;

import org.junit.Test;

import static org.junit.Assert.*;

public class ApiResponseParserTest {

    @Test
    public void testParseSuccessResponse() {
        String json = "{\"text\": \"hello world\"}";
        ApiResult result = ApiResponseParser.parse(json);
        assertTrue(result.isSuccess());
        assertEquals("hello world", result.getText());
        assertNull(result.getError());
    }

    @Test
    public void testParseEmptyTextResponse() {
        String json = "{\"text\": \"\"}";
        ApiResult result = ApiResponseParser.parse(json);
        assertTrue(result.isSuccess());
        assertEquals("", result.getText());
    }

    @Test
    public void testParseTextWithSpaces() {
        String json = "{\"text\": \"  The quick brown fox jumps over the lazy dog.  \"}";
        ApiResult result = ApiResponseParser.parse(json);
        assertTrue(result.isSuccess());
        assertEquals("  The quick brown fox jumps over the lazy dog.  ", result.getText());
    }

    @Test
    public void testParseErrorResponse() {
        String json = "{\"error\": {\"message\": \"Invalid API key\", \"type\": \"invalid_request_error\"}}";
        ApiResult result = ApiResponseParser.parse(json);
        assertFalse(result.isSuccess());
        assertEquals("Invalid API key", result.getError());
        assertNull(result.getText());
    }

    @Test
    public void testParseErrorStringResponse() {
        String json = "{\"error\": \"Something went wrong\"}";
        ApiResult result = ApiResponseParser.parse(json);
        assertFalse(result.isSuccess());
        assertEquals("Something went wrong", result.getError());
    }

    @Test
    public void testParseNullInput() {
        ApiResult result = ApiResponseParser.parse(null);
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    public void testParseEmptyInput() {
        ApiResult result = ApiResponseParser.parse("");
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    public void testParseWhitespaceOnly() {
        ApiResult result = ApiResponseParser.parse("   ");
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    public void testParseMalformedJson() {
        String json = "this is not json";
        ApiResult result = ApiResponseParser.parse(json);
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    public void testParseTextWithEscapedQuotes() {
        String json = "{\"text\": \"She said \\\"hello\\\" to him\"}";
        ApiResult result = ApiResponseParser.parse(json);
        assertTrue(result.isSuccess());
        assertEquals("She said \"hello\" to him", result.getText());
    }

    @Test
    public void testParseTextWithNewlines() {
        String json = "{\"text\": \"line one\\nline two\"}";
        ApiResult result = ApiResponseParser.parse(json);
        assertTrue(result.isSuccess());
        assertEquals("line one\nline two", result.getText());
    }

    @Test
    public void testParseTextWithBackslash() {
        String json = "{\"text\": \"path\\\\to\\\\file\"}";
        ApiResult result = ApiResponseParser.parse(json);
        assertTrue(result.isSuccess());
        assertEquals("path\\to\\file", result.getText());
    }

    @Test
    public void testParseResponseWithExtraFields() {
        String json = "{\"text\": \"transcribed text\", \"language\": \"en\", \"duration\": 3.5}";
        ApiResult result = ApiResponseParser.parse(json);
        assertTrue(result.isSuccess());
        assertEquals("transcribed text", result.getText());
    }

    @Test
    public void testParseMissingTextField() {
        // JSON with no "text" key and no "error" key should fail gracefully
        String json = "{\"language\": \"en\", \"duration\": 3.5}";
        ApiResult result = ApiResponseParser.parse(json);
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    public void testParseGroqErrorFormat() {
        String json = "{\"error\":{\"message\":\"Invalid API Key\",\"type\":\"invalid_api_key\",\"code\":\"invalid_api_key\"}}";
        ApiResult result = ApiResponseParser.parse(json);
        assertFalse(result.isSuccess());
        assertEquals("Invalid API Key", result.getError());
    }
}

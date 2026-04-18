package com.whispertflite.engine;

import org.junit.Test;

import static org.junit.Assert.*;

public class CustomDictionaryPromptTest {

    @Test
    public void testBuildPromptWithWords() {
        String result = WhisperEngineRemote.buildPrompt("Anthropic, GPT-4, Kotlin");
        assertEquals("Anthropic, GPT-4, Kotlin", result);
    }

    @Test
    public void testBuildPromptTrimsWhitespace() {
        String result = WhisperEngineRemote.buildPrompt("  Anthropic ,  GPT-4 , Kotlin  ");
        assertEquals("Anthropic, GPT-4, Kotlin", result);
    }

    @Test
    public void testBuildPromptRemovesEmptyEntries() {
        String result = WhisperEngineRemote.buildPrompt("Anthropic,,, Kotlin,");
        assertEquals("Anthropic, Kotlin", result);
    }

    @Test
    public void testBuildPromptNullReturnsNull() {
        assertNull(WhisperEngineRemote.buildPrompt(null));
    }

    @Test
    public void testBuildPromptEmptyReturnsNull() {
        assertNull(WhisperEngineRemote.buildPrompt(""));
    }

    @Test
    public void testBuildPromptWhitespaceOnlyReturnsNull() {
        assertNull(WhisperEngineRemote.buildPrompt("   "));
    }

    @Test
    public void testBuildPromptCommasOnlyReturnsNull() {
        assertNull(WhisperEngineRemote.buildPrompt(",,,"));
    }

    @Test
    public void testBuildPromptSingleWord() {
        String result = WhisperEngineRemote.buildPrompt("Anthropic");
        assertEquals("Anthropic", result);
    }

    @Test
    public void testBuildPromptHasNoEnglishProseWrapper() {
        // Regression test: the prompt must not contain natural-language
        // instructions that would bias Whisper toward English output.
        String result = WhisperEngineRemote.buildPrompt("Anthropic, Kotlin");
        assertFalse("Prompt must not contain the word 'Dictionary'",
                result.toLowerCase().contains("dictionary"));
        assertFalse("Prompt must not contain the word 'spellings'",
                result.toLowerCase().contains("spellings"));
    }
}

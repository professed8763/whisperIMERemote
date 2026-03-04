package com.whispertflite;

import com.whispertflite.engine.WhisperEngineRemote;

import org.junit.Test;

import static org.junit.Assert.*;

public class CustomDictionaryPromptTest {

    private static final String PREFIX =
            "Custom Dictionary (use these exact spellings when they appear in the text): ";

    @Test
    public void testBuildPromptWithWords() {
        String result = WhisperEngineRemote.buildPrompt("Anthropic, GPT-4, Kotlin");
        assertEquals(PREFIX + "Anthropic, GPT-4, Kotlin", result);
    }

    @Test
    public void testBuildPromptTrimsWhitespace() {
        String result = WhisperEngineRemote.buildPrompt("  Anthropic ,  GPT-4 , Kotlin  ");
        assertEquals(PREFIX + "Anthropic, GPT-4, Kotlin", result);
    }

    @Test
    public void testBuildPromptRemovesEmptyEntries() {
        String result = WhisperEngineRemote.buildPrompt("Anthropic,,, Kotlin,");
        assertEquals(PREFIX + "Anthropic, Kotlin", result);
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
        assertEquals(PREFIX + "Anthropic", result);
    }
}

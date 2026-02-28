package com.whispertflite;

import static org.junit.Assert.*;

import com.whispertflite.asr.Whisper;
import com.whispertflite.asr.WhisperResult;

import org.junit.Test;

public class WhisperResultTest {

    @Test
    public void constructorSetsFieldsCorrectly() {
        WhisperResult result = new WhisperResult("hello world", "en", Whisper.Action.TRANSCRIBE);
        assertEquals("hello world", result.getResult());
        assertEquals("en", result.getLanguage());
        assertEquals(Whisper.Action.TRANSCRIBE, result.getTask());
    }

    @Test
    public void translateAction() {
        WhisperResult result = new WhisperResult("translated text", "de", Whisper.Action.TRANSLATE);
        assertEquals("translated text", result.getResult());
        assertEquals("de", result.getLanguage());
        assertEquals(Whisper.Action.TRANSLATE, result.getTask());
    }

    @Test
    public void emptyResult() {
        WhisperResult result = new WhisperResult("", "", Whisper.Action.TRANSCRIBE);
        assertEquals("", result.getResult());
        assertEquals("", result.getLanguage());
    }

    @Test
    public void errorResult() {
        WhisperResult result = new WhisperResult("[Error: Network timeout]", "", Whisper.Action.TRANSCRIBE);
        assertEquals("[Error: Network timeout]", result.getResult());
    }

    @Test
    public void chineseLanguageResult() {
        WhisperResult result = new WhisperResult("你好世界", "zh", Whisper.Action.TRANSCRIBE);
        assertEquals("你好世界", result.getResult());
        assertEquals("zh", result.getLanguage());
    }

    @Test
    public void resultWithWhitespace() {
        WhisperResult result = new WhisperResult("  hello world  ", "en", Whisper.Action.TRANSCRIBE);
        assertEquals("  hello world  ", result.getResult());
    }
}

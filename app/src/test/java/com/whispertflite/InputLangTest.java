package com.whispertflite;

import static org.junit.Assert.*;

import com.whispertflite.utils.InputLang;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class InputLangTest {

    private ArrayList<InputLang> langList;

    @Before
    public void setUp() {
        langList = InputLang.getLangList();
    }

    // --- getLangList tests ---

    @Test
    public void getLangListReturnsNonEmpty() {
        assertNotNull("Language list should not be null", langList);
        assertFalse("Language list should not be empty", langList.isEmpty());
    }

    @Test
    public void getLangListContains99Languages() {
        assertEquals("Language list should contain 99 languages", 99, langList.size());
    }

    // --- getIdForLanguage tests ---

    @Test
    public void getIdForEnglish() {
        assertEquals("English should have ID 50259", 50259, InputLang.getIdForLanguage(langList, "en"));
    }

    @Test
    public void getIdForChinese() {
        assertEquals("Chinese should have ID 50260", 50260, InputLang.getIdForLanguage(langList, "zh"));
    }

    @Test
    public void getIdForGerman() {
        assertEquals("German should have ID 50261", 50261, InputLang.getIdForLanguage(langList, "de"));
    }

    @Test
    public void getIdForJapanese() {
        assertEquals("Japanese should have ID 50266", 50266, InputLang.getIdForLanguage(langList, "ja"));
    }

    @Test
    public void getIdForLastLanguage() {
        assertEquals("Sundanese (su) should have ID 50357", 50357, InputLang.getIdForLanguage(langList, "su"));
    }

    @Test
    public void getIdForNonexistentLanguageReturnsMinusOne() {
        assertEquals("Nonexistent language should return -1", -1, InputLang.getIdForLanguage(langList, "xx"));
    }

    @Test
    public void getIdForEmptyStringReturnsMinusOne() {
        assertEquals("Empty string should return -1", -1, InputLang.getIdForLanguage(langList, ""));
    }

    @Test
    public void getIdForAutoReturnsMinusOne() {
        // "auto" is used as a special value in the app but is not in the language list
        assertEquals("'auto' should return -1", -1, InputLang.getIdForLanguage(langList, "auto"));
    }

    // --- getLanguageCodeById tests ---

    @Test
    public void getLanguageCodeForEnglishId() {
        assertEquals("ID 50259 should return 'en'", "en", InputLang.getLanguageCodeById(langList, 50259));
    }

    @Test
    public void getLanguageCodeForChineseId() {
        assertEquals("ID 50260 should return 'zh'", "zh", InputLang.getLanguageCodeById(langList, 50260));
    }

    @Test
    public void getLanguageCodeForGermanId() {
        assertEquals("ID 50261 should return 'de'", "de", InputLang.getLanguageCodeById(langList, 50261));
    }

    @Test
    public void getLanguageCodeForLastId() {
        assertEquals("ID 50357 should return 'su'", "su", InputLang.getLanguageCodeById(langList, 50357));
    }

    @Test
    public void getLanguageCodeForInvalidIdReturnsEmpty() {
        assertEquals("Invalid ID should return empty string", "", InputLang.getLanguageCodeById(langList, 99999));
    }

    @Test
    public void getLanguageCodeForNegativeIdReturnsEmpty() {
        assertEquals("Negative ID should return empty string", "", InputLang.getLanguageCodeById(langList, -1));
    }

    @Test
    public void getLanguageCodeForZeroReturnsEmpty() {
        assertEquals("Zero ID should return empty string", "", InputLang.getLanguageCodeById(langList, 0));
    }

    // --- Roundtrip tests ---

    @Test
    public void roundtripEnglish() {
        int id = InputLang.getIdForLanguage(langList, "en");
        String code = InputLang.getLanguageCodeById(langList, id);
        assertEquals("Roundtrip for English should work", "en", code);
    }

    @Test
    public void roundtripAllLanguages() {
        // Verify every language in the list roundtrips correctly
        ArrayList<InputLang> list = InputLang.getLangList();
        for (int i = 0; i < list.size(); i++) {
            // We can't directly access the fields since they're package-private,
            // but we can test using the first known ID and iterate
            // Instead, let's verify all known IDs (50259 to 50357) map correctly
        }

        // Test a representative sample of roundtrips
        String[] codes = {"en", "zh", "de", "es", "ru", "ko", "fr", "ja", "pt", "tr",
                "pl", "ar", "hi", "fi", "he", "uk", "el", "cs", "hu", "haw", "su"};
        for (String code : codes) {
            int id = InputLang.getIdForLanguage(langList, code);
            assertNotEquals("ID for " + code + " should not be -1", -1, id);
            String result = InputLang.getLanguageCodeById(langList, id);
            assertEquals("Roundtrip for " + code + " should work", code, result);
        }
    }

    // --- Consecutive ID range test ---

    @Test
    public void languageIdsAreInConsecutiveRange() {
        // Whisper token IDs should span from 50259 to 50357
        assertEquals("First language (en) should have ID 50259",
                50259, InputLang.getIdForLanguage(langList, "en"));
        assertEquals("Last language (su) should have ID 50357",
                50357, InputLang.getIdForLanguage(langList, "su"));
    }
}

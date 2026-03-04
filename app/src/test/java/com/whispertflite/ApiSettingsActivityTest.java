package com.whispertflite;

import static org.junit.Assert.*;

import android.widget.EditText;

import androidx.preference.PreferenceManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

@RunWith(RobolectricTestRunner.class)
public class ApiSettingsActivityTest {

    @Test
    public void activityCanBeLaunched() {
        ActivityController<ApiSettingsActivity> controller =
                Robolectric.buildActivity(ApiSettingsActivity.class);
        ApiSettingsActivity activity = controller.create().start().resume().get();
        assertNotNull("Activity should be created", activity);
    }

    @Test
    public void customDictionaryEditTextExists() {
        ActivityController<ApiSettingsActivity> controller =
                Robolectric.buildActivity(ApiSettingsActivity.class);
        ApiSettingsActivity activity = controller.create().start().resume().get();

        EditText editDict = activity.findViewById(R.id.editCustomDictionary);
        assertNotNull("Custom dictionary EditText should exist", editDict);
    }

    @Test
    public void loadSettingsRestoresSavedDictionary() {
        ActivityController<ApiSettingsActivity> controller =
                Robolectric.buildActivity(ApiSettingsActivity.class);
        ApiSettingsActivity activity = controller.create().start().resume().get();

        // Save a dictionary value to SharedPreferences
        PreferenceManager.getDefaultSharedPreferences(activity)
                .edit()
                .putString("customDictionary", "Anthropic, GPT-4")
                .commit();

        // Recreate the activity to trigger loadSettings()
        ActivityController<ApiSettingsActivity> controller2 =
                Robolectric.buildActivity(ApiSettingsActivity.class);
        ApiSettingsActivity activity2 = controller2.create().start().resume().get();

        EditText editDict = activity2.findViewById(R.id.editCustomDictionary);
        assertEquals("Dictionary should be loaded from SharedPreferences",
                "Anthropic, GPT-4", editDict.getText().toString());
    }

    @Test
    public void emptyDictionaryFieldIsHandledGracefully() {
        ActivityController<ApiSettingsActivity> controller =
                Robolectric.buildActivity(ApiSettingsActivity.class);
        ApiSettingsActivity activity = controller.create().start().resume().get();

        EditText editDict = activity.findViewById(R.id.editCustomDictionary);
        assertEquals("Empty dictionary should default to empty string",
                "", editDict.getText().toString());
    }

    @Test
    public void activityFinishesAfterSuccessfulSave() {
        ActivityController<ApiSettingsActivity> controller =
                Robolectric.buildActivity(ApiSettingsActivity.class);
        ApiSettingsActivity activity = controller.create().start().resume().get();

        // Fill in required fields so validation passes
        EditText editApiKey = activity.findViewById(R.id.editApiKey);
        EditText editEndpoint = activity.findViewById(R.id.editEndpoint);
        EditText editModel = activity.findViewById(R.id.editModel);
        editApiKey.setText("test-key");
        editEndpoint.setText("https://api.example.com");
        editModel.setText("whisper-1");

        // Click save
        activity.findViewById(R.id.btnSave).performClick();

        assertTrue("Activity should finish after successful save", activity.isFinishing());
    }

    @Test
    public void savePersistsDictionaryToSharedPreferences() {
        ActivityController<ApiSettingsActivity> controller =
                Robolectric.buildActivity(ApiSettingsActivity.class);
        ApiSettingsActivity activity = controller.create().start().resume().get();

        // Fill in required fields
        EditText editApiKey = activity.findViewById(R.id.editApiKey);
        EditText editEndpoint = activity.findViewById(R.id.editEndpoint);
        EditText editModel = activity.findViewById(R.id.editModel);
        EditText editDict = activity.findViewById(R.id.editCustomDictionary);
        editApiKey.setText("test-key");
        editEndpoint.setText("https://api.example.com");
        editModel.setText("whisper-1");
        editDict.setText("Anthropic, Kotlin, GPT-4");

        // Click save
        activity.findViewById(R.id.btnSave).performClick();

        // Verify dictionary was persisted
        String savedDict = PreferenceManager.getDefaultSharedPreferences(activity)
                .getString("customDictionary", "");
        assertEquals("Dictionary should be saved to SharedPreferences",
                "Anthropic, Kotlin, GPT-4", savedDict);
    }
}
